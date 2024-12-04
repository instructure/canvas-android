/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */
package com.instructure.teacher.presenters

import android.app.Activity
import android.net.Uri
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.DiscussionTopicPostBody
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.teacher.events.DiscussionTopicHeaderDeletedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.interfaces.RceMediaUploadPresenter
import com.instructure.teacher.viewinterface.CreateDiscussionView
import com.instructure.pandautils.blueprint.FragmentPresenter
import kotlinx.coroutines.Job
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

class CreateDiscussionPresenter(private val canvasContext: CanvasContext, private var mAssignment: Assignment?)
    : FragmentPresenter<CreateDiscussionView>(), RceMediaUploadPresenter {

    override var rceImageUploadJob: WeaveJob? = null
    private var mCreateDiscussionCall: Job? = null
    private var mGetAssignmentCall: Job? = null
    private var mDueDateApiCalls: Job? = null

    /**
     * (Creation mode only) An attachment to be uploaded alongside the discussion. Note that this
     * can only be used when creating new discussions. Setting/changing attachments on existing
     * discussions (editing mode) is currently unsupported.
     */
    var attachment: FileSubmitObject? = null

    /** (Editing mode only) Set to *true* if the existing discussions's attachment should be removed */
    var attachmentRemoved = false

    override fun loadData(forceNetwork: Boolean) {}

    override fun refresh(forceNetwork: Boolean) {}

    fun saveDiscussion(discussionTopicHeader: DiscussionTopicHeader) {
        viewCallback?.startSavingDiscussion()
        @Suppress("EXPERIMENTAL_FEATURE_WARNING")
        mCreateDiscussionCall = weave {
            try {
                var filePart: MultipartBody.Part? = null
                attachment?.let {
                    val file = File(it.fullPath)
                    val requestBody = file.asRequestBody(it.contentType.toMediaTypeOrNull())
                    filePart = MultipartBody.Part.createFormData("attachment", file.name, requestBody)
                }
                awaitApi<DiscussionTopicHeader> { DiscussionManager.createDiscussion(canvasContext, discussionTopicHeader, filePart, it) }
                viewCallback?.discussionSavedSuccessfully(null)

            } catch (e: Throwable) {
                viewCallback?.errorSavingDiscussion()
            }
        }
    }

    fun editDiscussion(topicId: Long, discussionTopicPostBody: DiscussionTopicPostBody) {
        viewCallback?.startSavingDiscussion()
        @Suppress("EXPERIMENTAL_FEATURE_WARNING")
        mCreateDiscussionCall = weave {
            try {
                if (attachmentRemoved) discussionTopicPostBody.removeAttachment = ""
                val discussionTopic = awaitApi<DiscussionTopicHeader> { DiscussionManager.editDiscussionTopic(canvasContext, topicId, discussionTopicPostBody, it) }
                viewCallback?.discussionSavedSuccessfully(discussionTopic)

            } catch (e: Throwable) {
                viewCallback?.errorSavingDiscussion()
            }
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun getDueDateInfo(groupCategoryId: Long) {
        mDueDateApiCalls = weave {
            val groupsMapped = hashMapOf<Long, Group>()
            val sectionsMapped = hashMapOf<Long, Section>()
            val studentsMapped = hashMapOf<Long, User>()
            try {
                if (groupsMapped.isEmpty() && sectionsMapped.isEmpty() && studentsMapped.isEmpty()) {
                    val sections = awaitApi<List<Section>> { SectionManager.getAllSectionsForCourse(canvasContext.id, it, false) }
                    val groups = if (groupCategoryId > 0L) awaitApi<List<Group>> { GroupCategoriesManager.getAllGroupsForCategory(groupCategoryId, it, false) } else emptyList()
                    val students = awaitApi<List<User>> { UserManager.getAllPeopleList(canvasContext, it, false) }
                    groupsMapped += groups.associateBy { it.id }
                    sectionsMapped += sections.associateBy { it.id }
                    studentsMapped += students.associateBy { it.id }
                }
                viewCallback?.updateDueDateGroups(groupsMapped, sectionsMapped, studentsMapped)

            } catch (e: Throwable) {
            }
        }
    }

    fun getAssignment(): Assignment? = mAssignment

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    fun getFullAssignment(assignmentId: Long) {
        mGetAssignmentCall = weave {
            try {
                mAssignment = awaitApi<Assignment> { AssignmentManager.getAssignment(assignmentId, canvasContext.id, true, it) }
                viewCallback?.updatedAssignment()
            } catch (e: Throwable) {
            }
        }
    }

    fun deleteDiscussionTopicHeader(discussionTopicHeaderId: Long) {
        DiscussionManager.deleteDiscussionTopicHeader(canvasContext, discussionTopicHeaderId, object : StatusCallback<Void>() {
            override fun onResponse(response: Response<Void>, linkHeaders: LinkHeaders, type: ApiType) {
                if (response.code() in 200..299) {
                    DiscussionTopicHeaderDeletedEvent(discussionTopicHeaderId, (DiscussionDetailsWebViewFragment::class.java.toString() + ".onResume()")).post()
                    viewCallback?.discussionDeletedSuccessfully(discussionTopicHeaderId)
                }
            }
        })
    }

    override fun uploadRceImage(imageUri: Uri, activity: Activity) {
        rceImageUploadJob = MediaUploadUtils.uploadRceImageJob(imageUri, canvasContext, activity) { imageUrl -> viewCallback?.insertImageIntoRCE(imageUrl) }
    }
}
