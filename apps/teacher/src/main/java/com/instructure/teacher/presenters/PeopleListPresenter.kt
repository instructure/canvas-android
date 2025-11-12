/*
 * Copyright (C) 2017 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.presenters

import android.os.Handler
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.RecipientManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.NaturalOrderComparator
import com.instructure.teacher.viewinterface.PeopleListView
import com.instructure.pandautils.blueprint.SyncPresenter
import retrofit2.Response
import java.util.*


class PeopleListPresenter(private val mCanvasContext: CanvasContext?) : SyncPresenter<User, PeopleListView>(User::class.java) {

    // Make api call to get group members
    // Add it to the list so we can search for sections and remember which contexts we have selected if the user re-opens the dialog
    // we've made api calls to get the groups, now filter the rest
    var canvasContextList = ArrayList<CanvasContext>()
        set(canvasContextList) {
            this.canvasContextList.clear()
            mGroupUserCallback.reset()
            clearData()

            for (canvasContext in canvasContextList) {
                if (CanvasContext.Type.isGroup(canvasContext)) {
                    getGroupUsers(canvasContext)
                }
                this.canvasContextList.add(canvasContext)

            }
            filterCanvasContexts()
        }
    private val mUserList = ArrayList<User>()
    private var shouldApplyFilterAfterLoad = false
    private var mRun: RecipientRunnable? = null
    // If we try to automate this class the handler might create some issues. Cross that bridge when we come to it
    private val mHandler = Handler()

    private val mGroupUserCallback = object : StatusCallback<List<User>>() {
        override fun onResponse(response: Response<List<User>>, linkHeaders: LinkHeaders, type: ApiType) {
            // Group user api doesn't return the enrollment, so we need to add the user from the mUserList
            for (user in response.body()!!) {
                val currentUser = mUserList[mUserList.indexOf(user)]
                data.addOrUpdate(currentUser)
            }
            // All the users in this group should already be in the user list, so we don't need to add them again
        }

        override fun onFinished(type: ApiType) {
            viewCallback?.checkIfEmpty()
            viewCallback?.onRefreshFinished()
        }
    }

    private val mUserListCallback = object : StatusCallback<List<User>>() {
        override fun onResponse(response: Response<List<User>>, linkHeaders: LinkHeaders, type: ApiType) {
            data.addOrUpdate(response.body()!!)
            mUserList.addAll(response.body()!!)

            if (shouldApplyFilterAfterLoad && canvasContextList.isNotEmpty()) {
                shouldApplyFilterAfterLoad = false
                filterCanvasContexts()
            }

            viewCallback?.checkIfEmpty()
            viewCallback?.onRefreshFinished()
        }
    }

    private val mRecipientCallback = object : StatusCallback<List<Recipient>>() {
        override fun onResponse(response: Response<List<Recipient>>, linkHeaders: LinkHeaders, type: ApiType) {
            clearData()
            data.beginBatchedUpdates()
            for (recipient in response.body()!!) {
                // Convert recipient to user
                val user = convertRecipientToUser(recipient)

                data.add(user)
            }
            data.endBatchedUpdates()
        }

        override fun onFinished(type: ApiType) {
            if (viewCallback != null) {
                viewCallback!!.onRefreshFinished()
                viewCallback!!.checkIfEmpty()
            }
        }
    }

    /**
     * Convert the list of CanvasContexts to a list of just ids so the dialog can know which CanvasContexts
     * have been selected
     *
     * @return
     */
    val canvasContextListIds: ArrayList<Long>
        get() {
            val contextIds = ArrayList<Long>()
            for (canvasContext in canvasContextList) {
                contextIds.add(canvasContext.id)
            }
            return contextIds
        }

    override fun loadData(forceNetwork: Boolean) {
        if (forceNetwork) {
            mUserList.clear()
        }
        onRefreshStarted()
        UserManager.getAllEnrollmentsPeopleList(mCanvasContext!!, mUserListCallback, forceNetwork)
    }

    override fun refresh(forceNetwork: Boolean) {
        if (canvasContextList.isEmpty()) {
            onRefreshStarted()
            mUserListCallback.reset()
            mUserList.clear()
            clearData()
            loadData(forceNetwork)
        } else {
            viewCallback?.checkIfEmpty()
            viewCallback?.onRefreshFinished()
        }
    }

    private fun getGroupUsers(group: CanvasContext) {
        UserManager.getAllPeopleList(group, mGroupUserCallback, true)
    }

    /**
     * Calls our API to query for possible recipients, with the mCurrentConstraint as the search parameter.
     * This process will "kill" any pending runnables. With a delay of 500ms.
     */
    private fun fetchAdditionalRecipients(constraint: String) {
        if (mRun != null) {
            mRun?.kill()
            mHandler.removeCallbacks(mRun!!)

        }
        mRun = RecipientRunnable(constraint)
        mHandler.post(mRun!!)
    }

    fun searchPeopleList(searchTerm: String) {

        mRecipientCallback.reset()
        mUserList.clear()
        clearData()
        fetchAdditionalRecipients(searchTerm)
    }

    private fun convertRecipientToUser(recipient: Recipient): User {
        // Get enrollments
        val enrollments = ArrayList<Enrollment>()
        if (recipient.commonCourses != null) {
            val commonCoursesEnrollments = recipient.commonCourses!![mCanvasContext!!.id.toString()]
            if (commonCoursesEnrollments != null) {
                for (enrollment in commonCoursesEnrollments) {

                    val newEnrollment = Enrollment(
                            type = Enrollment.EnrollmentType.values().firstOrNull { it.apiRoleString == enrollment }
                    )
                    enrollments.add(newEnrollment)
                }
            }
        }

        return User(
                avatarUrl = recipient.avatarURL,
                id = recipient.idAsLong,
                name = recipient.fullName ?: recipient.name ?: "",
                pronouns = recipient.pronouns,
                sortableName = recipient.name,
                enrollments = enrollments
        )
    }


    override fun compare(item1: User, item2: User): Int {
        return NaturalOrderComparator.compare(item1.sortableName?.lowercase(Locale.getDefault()).orEmpty(), item2.sortableName?.lowercase(Locale.getDefault()).orEmpty())
    }


    override fun areItemsTheSame(user1: User, user2: User): Boolean {
        return user1.id == user2.id
    }

    fun clearCanvasContextList() {
        canvasContextList.clear()
        refresh(false)
    }

    fun restoreCanvasContextList(contexts: ArrayList<CanvasContext>) {
        canvasContextList.clear()
        canvasContextList.addAll(contexts)
        shouldApplyFilterAfterLoad = true
    }

    private fun filterCanvasContexts() {
        clearData()

        // filter the list based on the user's enrollments
        if (!canvasContextList.isEmpty()) {
            // get a list of ids to make it easier to check section enrollments
            val contextIds = ArrayList<Long>()
            for (canvasContext in canvasContextList) {
                if (CanvasContext.Type.isSection(canvasContext)) {
                    contextIds.add(canvasContext.id)
                }
            }

            for (user in mUserList) {
                for ((_, _, _, _, courseSectionId) in user.enrollments) {
                    if (contextIds.contains(courseSectionId)) {
                        data.addOrUpdate(user)
                    }
                }
            }
        } else {
            refresh(false)
        }
    }

    private inner class RecipientRunnable(val constraint: String) : Runnable {
        private var isKilled = false

        override fun run() {
            if (!isKilled && mCanvasContext != null) {
                onRefreshStarted()
                RecipientManager.searchAllRecipientsNoSyntheticContexts(true, constraint, mCanvasContext.contextId, mRecipientCallback)
            } else {
                if (viewCallback != null) {
                    viewCallback!!.onRefreshFinished()
                    viewCallback!!.checkIfEmpty()
                }
            }
        }

        fun kill() {
            isKilled = true
        }
    }
}
