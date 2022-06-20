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
package com.instructure.student.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Enrollment.EnrollmentType
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.NaturalOrderComparator
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitPaginated
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.Types
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.holders.PeopleHeaderViewHolder
import com.instructure.student.holders.PeopleViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback
import java.util.Locale

class PeopleListRecyclerAdapter(
        context: Context,
        private val mCanvasContext: CanvasContext,
        private val mAdapterToFragmentCallback: AdapterToFragmentCallback<User>
) : ExpandableRecyclerAdapter<EnrollmentType, User, RecyclerView.ViewHolder>(context, EnrollmentType::class.java, User::class.java) {

    private val mCourseColor = ColorKeeper.getOrGenerateColor(mCanvasContext)
    private val mEnrollmentPriority = mapOf( EnrollmentType.Teacher to 4, EnrollmentType.Ta to 3, EnrollmentType.Student to 2, EnrollmentType.Observer to 1)
    private var mApiCalls: WeaveJob? = null

    init {
        isExpandedByDefault = true
        loadData()
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun loadFirstPage() {
        mApiCalls = tryWeave {
            var canvasContext = mCanvasContext

            // If the canvasContext is a group, and has a course we want to add the Teachers and TAs from that course to the peoples list
            if (CanvasContext.Type.isGroup(mCanvasContext) && (mCanvasContext as Group).courseId > 0) {
                // We build a generic CanvasContext with type set to COURSE and give it the CourseId from the group, so that it wil use the course API not the group API
                canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, mCanvasContext.courseId, "")
            }

            // Get Teachers
            awaitPaginated<List<User>> {
                onRequestFirst { UserManager.getFirstPagePeopleList(canvasContext, UserAPI.EnrollmentType.TEACHER, isRefresh, it) }
                onRequestNext { nextUrl, callback -> UserManager.getNextPagePeopleList(isRefresh, nextUrl, callback) }
                onResponse { setNextUrl(""); populateAdapter(it) }
            }

            // Get TAs
            awaitPaginated<List<User>> {
                onRequestFirst { UserManager.getFirstPagePeopleList(canvasContext, UserAPI.EnrollmentType.TA, isRefresh, it) }
                onRequestNext { nextUrl, callback -> UserManager.getNextPagePeopleList(isRefresh, nextUrl, callback) }
                onResponse { setNextUrl(""); populateAdapter(it) }
            }

            // Get others
            awaitPaginated<List<User>> {
                onRequestFirst { UserManager.getFirstPagePeopleList(mCanvasContext, isRefresh, it) }
                onRequestNext { nextUrl, callback -> UserManager.getNextPagePeopleList(isRefresh, nextUrl, callback) }
                onResponse { setNextUrl(""); populateAdapter(it) }
            }

            setNextUrl(null)
        } catch {
            context.toast(R.string.errorOccurred)
        }
    }

    override fun loadNextPage(nextURL: String) {
        mApiCalls?.next()
    }

    override val isPaginated get() = true

    override fun resetData() {
        mApiCalls?.cancel()
        super.resetData()
    }

    override fun cancel() {
        mApiCalls?.cancel()
    }

    private fun populateAdapter(result: List<User>) {
        val (enrolled, unEnrolled) = result.partition { it.enrollments.isNotEmpty() }
        enrolled.asSequence().onEach { it.enrollments.sortedByDescending { enrollment-> mEnrollmentPriority[enrollment.type] } }
                .groupBy { it.enrollments[0].type }
                .forEach { (type, users) -> addOrUpdateAllItems(type!!, users) }
        if (CanvasContext.Type.isGroup(mCanvasContext)) addOrUpdateAllItems(EnrollmentType.NoEnrollment, unEnrolled)
        notifyDataSetChanged()
        mAdapterToFragmentCallback.onRefreshFinished()
    }

    override fun createViewHolder(v: View, viewType: Int): RecyclerView.ViewHolder =
            if (viewType == Types.TYPE_HEADER) PeopleHeaderViewHolder(v) else PeopleViewHolder(v)

    override fun itemLayoutResId(viewType: Int): Int =
            if (viewType == Types.TYPE_HEADER) PeopleHeaderViewHolder.HOLDER_RES_ID else PeopleViewHolder.HOLDER_RES_ID

    override fun contextReady() = Unit

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, peopleGroupType: EnrollmentType, user: User) {
        val groupItemCount = getGroupItemCount(peopleGroupType)
        val itemPosition = storedIndexOfItem(peopleGroupType, user)
        (holder as PeopleViewHolder).bind(user, mAdapterToFragmentCallback, mCourseColor, itemPosition == 0, itemPosition == groupItemCount - 1)
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, enrollmentType: EnrollmentType, isExpanded: Boolean) {
        (holder as PeopleHeaderViewHolder).bind(enrollmentType, getHeaderTitle(enrollmentType), isExpanded, viewHolderHeaderClicked)
    }

    override fun createGroupCallback(): GroupSortedList.GroupComparatorCallback<EnrollmentType> {
        return object : GroupSortedList.GroupComparatorCallback<EnrollmentType> {
            override fun compare(o1: EnrollmentType, o2: EnrollmentType) = getHeaderTitle(o2).compareTo(getHeaderTitle(o1))
            override fun areContentsTheSame(oldGroup: EnrollmentType, newGroup: EnrollmentType) = getHeaderTitle(oldGroup) == getHeaderTitle(newGroup)
            override fun areItemsTheSame(group1: EnrollmentType, group2: EnrollmentType) = getHeaderTitle(group1) == getHeaderTitle(group2)
            override fun getUniqueGroupId(group: EnrollmentType) = getHeaderTitle(group).hashCode().toLong()
            override fun getGroupType(group: EnrollmentType) = Types.TYPE_HEADER
        }
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<EnrollmentType, User> {
        return object : GroupSortedList.ItemComparatorCallback<EnrollmentType, User> {
            override fun compare(group: EnrollmentType, o1: User, o2: User) = NaturalOrderComparator.compare(o1.sortableName?.lowercase(Locale.getDefault()).orEmpty(), o2.sortableName?.lowercase(Locale.getDefault()).orEmpty())
            override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem.sortableName == newItem.sortableName
            override fun areItemsTheSame(item1: User, item2: User) = item1.id == item2.id
            override fun getUniqueItemId(item: User) = item.id
            override fun getChildType(group: EnrollmentType, item: User) = Types.TYPE_ITEM
        }
    }

    private fun getHeaderTitle(enrollmentType: Enrollment.EnrollmentType?): String = when (enrollmentType) {
        EnrollmentType.Student -> context.getString(R.string.students)
        EnrollmentType.Teacher, EnrollmentType.Ta -> context.getString(R.string.teachersTas)
        EnrollmentType.Observer -> context.getString(R.string.observers)
        EnrollmentType.Designer -> context.getString(R.string.enrollmentTypeDesigner)
        else -> context.getString(R.string.groupMembers) // Default
    }
}
