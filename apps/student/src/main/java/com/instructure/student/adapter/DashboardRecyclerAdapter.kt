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

import android.app.Activity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.holders.*
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback
import com.instructure.student.util.StudentPrefs
import org.threeten.bp.OffsetDateTime
import java.util.*

class DashboardRecyclerAdapter(
        context: Activity,
        private val mAdapterToFragmentCallback: CourseAdapterToFragmentCallback
) : ExpandableRecyclerAdapter<DashboardRecyclerAdapter.ItemType, Any, RecyclerView.ViewHolder>(
        context,
        ItemType::class.java,
        Any::class.java
) {

    enum class ItemType {
        COURSE_HEADER,
        COURSE,
        GROUP_HEADER,
        GROUP
    }

    private var mApiCalls: WeaveJob? = null
    private var mCourseMap = mapOf<Long, Course>()

    init {
        isExpandedByDefault = true
        loadData()
    }

    override fun createViewHolder(v: View, viewType: Int) = when (ItemType.values()[viewType]) {
        ItemType.COURSE_HEADER -> CourseHeaderViewHolder(v)
        ItemType.COURSE -> CourseViewHolder(v)
        ItemType.GROUP_HEADER -> GroupHeaderViewHolder(v)
        ItemType.GROUP -> GroupViewHolder(v)
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, header: ItemType, item: Any) {
        when {
            holder is CourseViewHolder && item is Course -> holder.bind(item, mAdapterToFragmentCallback)
            holder is GroupViewHolder && item is Group -> holder.bind(item, mCourseMap, mAdapterToFragmentCallback)
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, header: ItemType, isExpanded: Boolean) {
        (holder as? CourseHeaderViewHolder)?.bind(mAdapterToFragmentCallback)
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<ItemType, Any> {
        return object : GroupSortedList.ItemComparatorCallback<ItemType, Any> {
            override fun compare(group: ItemType, o1: Any, o2: Any) = when {
                o1 is Course && o2 is Course -> -1 // Don't sort courses, the api returns in the users order
                o1 is Group && o2 is Group -> o1.compareTo(o2)
                else -> -1
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any) = false

            override fun areItemsTheSame(item1: Any, item2: Any) = when {
                item1 is Course && item2 is Course -> item1.contextId.hashCode() == item2.contextId.hashCode()
                item1 is Group && item2 is Group -> item1.contextId.hashCode() == item2.contextId.hashCode()
                else -> false
            }

            override fun getUniqueItemId(item: Any) = when (item) {
                is Course -> item.contextId.hashCode().toLong()
                is Group -> item.contextId.hashCode().toLong()
                else -> -1L
            }

            override fun getChildType(group: ItemType, item: Any) = when (item) {
                is Course -> ItemType.COURSE.ordinal
                is Group -> ItemType.GROUP.ordinal
                else -> -1
            }
        }
    }

    override fun createGroupCallback(): GroupSortedList.GroupComparatorCallback<ItemType> {
        return object : GroupSortedList.GroupComparatorCallback<ItemType> {
            override fun compare(o1: ItemType, o2: ItemType) = o1.ordinal.compareTo(o2.ordinal)
            override fun areContentsTheSame(oldGroup: ItemType, newGroup: ItemType) = oldGroup == newGroup
            override fun areItemsTheSame(group1: ItemType, group2: ItemType) = group1 == group2
            override fun getUniqueGroupId(group: ItemType) = group.ordinal.toLong()
            override fun getGroupType(group: ItemType) = group.ordinal
        }
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun loadData() {
        mApiCalls?.cancel()
        mApiCalls = tryWeave {
            if (isRefresh) {
                ColorApiHelper.awaitSync()
                FlutterComm.sendUpdatedTheme()
            }
            val (rawCourses, groups) = awaitApis<List<Course>, List<Group>>(
                    { CourseManager.getCourses(isRefresh, it) },
                    { GroupManager.getAllGroups(it, isRefresh) }
            )
            val dashboardCards = awaitApi<List<DashboardCard>> { CourseManager.getDashboardCourses(isRefresh, it) }

            mCourseMap = rawCourses.associateBy { it.id }
            val groupMap = groups.associateBy { it.id }

            // Map not null is needed because the dashboard api can return unpublished courses
            val visibleCourses = dashboardCards.mapNotNull { mCourseMap[it.id] }
                    .filter { it.isCurrentEnrolment() || it.isFutureEnrolment() }

            // Filter groups
            val allActiveGroups = groups.filter { group -> group.isActive(mCourseMap[group.courseId])}

            val isAnyFavoritePresent = visibleCourses.any { it.isFavorite } || allActiveGroups.any { it.isFavorite }
            val visibleGroups = if (isAnyFavoritePresent) allActiveGroups.filter { it.isFavorite } else allActiveGroups

            // Add courses
            addOrUpdateAllItems(ItemType.COURSE_HEADER, visibleCourses)

            // Add groups
            addOrUpdateAllItems(ItemType.GROUP_HEADER, visibleGroups)

            notifyDataSetChanged()
            isAllPagesLoaded = true
            if (itemCount == 0) adapterToRecyclerViewCallback.setIsEmpty(true)
            mAdapterToFragmentCallback.onRefreshFinished()
        } catch {
            adapterToRecyclerViewCallback.setDisplayNoConnection(true)
            mAdapterToFragmentCallback.onRefreshFinished()
        }
    }

    private fun hasValidCourseForEnrollment(enrollment: Enrollment): Boolean {
        return mCourseMap[enrollment.courseId]?.let { course ->
            course.isValidTerm() && !course.accessRestrictedByDate && isEnrollmentBeforeEndDateOrNotRestricted(course)
        } ?: false
    }

    private fun isEnrollmentBeforeEndDateOrNotRestricted(course: Course): Boolean {
        val isBeforeEndDate = course.endAt?.let {
            val now = OffsetDateTime.now()
            val endDate = OffsetDateTime.parse(it).withOffsetSameInstant(OffsetDateTime.now().offset)
            now.isBefore(endDate)
        } ?: true // Case when the course has no end date

        return !course.restrictEnrollmentsToCourseDate || isBeforeEndDate
    }

    override fun itemLayoutResId(viewType: Int) = when (ItemType.values()[viewType]) {
        ItemType.COURSE_HEADER -> CourseHeaderViewHolder.HOLDER_RES_ID
        ItemType.COURSE -> CourseViewHolder.HOLDER_RES_ID
        ItemType.GROUP_HEADER -> GroupHeaderViewHolder.HOLDER_RES_ID
        ItemType.GROUP -> GroupViewHolder.HOLDER_RES_ID
    }

    override fun contextReady() = Unit

    override fun setupCallbacks() = Unit

    override fun cancel() {
        mApiCalls?.cancel()
    }

    override fun refresh() {
        mApiCalls?.cancel()
        super.refresh()
    }
}
