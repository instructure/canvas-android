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
import com.instructure.canvasapi2.managers.AccountNotificationManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.isInvited
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.student.holders.*
import com.instructure.student.interfaces.CourseAdapterToFragmentCallback
import java.util.*


class DashboardRecyclerAdapter(
        context: Activity,
        private val mAdapterToFragmentCallback: CourseAdapterToFragmentCallback
) : ExpandableRecyclerAdapter<DashboardRecyclerAdapter.ItemType, CanvasComparable<*>, RecyclerView.ViewHolder>(
        context,
        ItemType::class.java,
        CanvasComparable::class.java
) {

    enum class ItemType {
        INVITATION_HEADER,
        INVITATION,
        ANNOUNCEMENT_HEADER,
        ANNOUNCEMENT,
        COURSE_HEADER,
        COURSE,
        GROUP_HEADER,
        GROUP
    }

    private var mApiCalls: WeaveJob? = null
    private var mRawCourseMap = mapOf<Long, Course>()
    private var mCourseMap = mapOf<Long, Course>()

    init {
        isExpandedByDefault = true
        loadData()
    }

    override fun createViewHolder(v: View, viewType: Int) = when (ItemType.values()[viewType]) {
        ItemType.INVITATION_HEADER -> BlankViewHolder(v)
        ItemType.INVITATION -> CourseInvitationViewHolder(v)
        ItemType.ANNOUNCEMENT_HEADER -> BlankViewHolder(v)
        ItemType.ANNOUNCEMENT -> AnnouncementViewHolder(v)
        ItemType.COURSE_HEADER -> CourseHeaderViewHolder(v)
        ItemType.COURSE -> CourseViewHolder(v)
        ItemType.GROUP_HEADER -> GroupHeaderViewHolder(v)
        ItemType.GROUP -> GroupViewHolder(v)
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, header: ItemType, item: CanvasComparable<*>) {
        when {
            holder is CourseInvitationViewHolder && item is Enrollment -> holder.bind(item, mRawCourseMap[item.courseId]!!, mAdapterToFragmentCallback)
            holder is AnnouncementViewHolder && item is AccountNotification -> holder.bind(item, mAdapterToFragmentCallback)
            holder is CourseViewHolder && item is Course -> holder.bind(item, mAdapterToFragmentCallback)
            holder is GroupViewHolder && item is Group -> holder.bind(item, mCourseMap, mAdapterToFragmentCallback)
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, header: ItemType, isExpanded: Boolean) {
        (holder as? CourseHeaderViewHolder)?.bind(mAdapterToFragmentCallback)
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<ItemType, CanvasComparable<*>> {
        return object : GroupSortedList.ItemComparatorCallback<ItemType, CanvasComparable<*>> {
            override fun compare(group: ItemType?, o1: CanvasComparable<*>?, o2: CanvasComparable<*>?) = when {
                o1 is AccountNotification && o2 is AccountNotification -> o1.compareTo(o2)
                o1 is Course && o2 is Course -> o1.compareTo(o2)
                o1 is Group && o2 is Group -> o1.compareTo(o2)
                else -> -1
            }

            override fun areContentsTheSame(oldItem: CanvasComparable<*>?, newItem: CanvasComparable<*>?) = false

            override fun areItemsTheSame(item1: CanvasComparable<*>?, item2: CanvasComparable<*>?) = when {
                item1 is AccountNotification && item2 is AccountNotification -> item1.id == item2.id
                item1 is Course && item2 is Course -> item1.contextId.hashCode() == item2.contextId.hashCode()
                item1 is Group && item2 is Group -> item1.contextId.hashCode() == item2.contextId.hashCode()
                else -> false
            }

            override fun getUniqueItemId(item: CanvasComparable<*>?) = when (item) {
                is AccountNotification -> item.id
                is Enrollment -> item.id
                is Course -> item.contextId.hashCode().toLong()
                is Group -> item.contextId.hashCode().toLong()
                else -> -1L
            }

            override fun getChildType(group: ItemType?, item: CanvasComparable<*>?) = when (item) {
                is AccountNotification -> ItemType.ANNOUNCEMENT.ordinal
                is Enrollment -> ItemType.INVITATION.ordinal
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
            if (isRefresh) ColorApiHelper.awaitSync()
            val (rawCourses, groups, announcements) = awaitApis<List<Course>, List<Group>, List<AccountNotification>>(
                    { CourseManager.getCourses(isRefresh, it) },
                    { GroupManager.getAllGroups(it, isRefresh) },
                    { AccountNotificationManager.getAllAccountNotifications(it, true)}
            )

            mRawCourseMap = rawCourses.associateBy { it.id }

            // Get enrollment invites
            val invites = awaitApi<List<Enrollment>> {
                EnrollmentManager.getSelfEnrollments(null, listOf(EnrollmentAPI.STATE_INVITED), isRefresh, it)
            }

            val favoriteCourses = rawCourses.filter { it.isFavorite && !it.accessRestrictedByDate && !it.isInvited() }

            // Add courses
            addOrUpdateAllItems(ItemType.COURSE_HEADER, favoriteCourses)

            // Add groups
            mCourseMap = rawCourses.associateBy { it.id }
            val rawGroups = groups.filter { group ->
                val groupCourse = mCourseMap[group.courseId] ?: return@filter true // Account groups don't have a course
                with(groupCourse) { isValidTerm() && !accessRestrictedByDate && endDate?.before(Date()) != true }
            }
            val favoriteGroups = rawGroups.filter {
                (it.isFavorite)
            }
            if (favoriteGroups.isEmpty()) {
                addOrUpdateAllItems(ItemType.GROUP_HEADER,rawGroups)
            } else {
                addOrUpdateAllItems(ItemType.GROUP_HEADER,favoriteGroups)
            }

            // Add announcements
            addOrUpdateAllItems(ItemType.ANNOUNCEMENT_HEADER, announcements)

            // Add course invites
            val validInvites = invites.filter {
                mRawCourseMap[it.courseId]?.let { course ->
                    course.isValidTerm() && !course.accessRestrictedByDate && !course.restrictEnrollmentsToCourseDate} ?: false
            }
            
            addOrUpdateAllItems(ItemType.INVITATION_HEADER, validInvites)

            notifyDataSetChanged()
            isAllPagesLoaded = true
            if (itemCount == 0) adapterToRecyclerViewCallback.setIsEmpty(true)
            mAdapterToFragmentCallback.onRefreshFinished()
        } catch {
            adapterToRecyclerViewCallback.setDisplayNoConnection(true)
            mAdapterToFragmentCallback.onRefreshFinished()
        }
    }

    override fun itemLayoutResId(viewType: Int) = when (ItemType.values()[viewType]) {
        ItemType.INVITATION_HEADER -> BlankViewHolder.HOLDER_RES_ID
        ItemType.INVITATION -> CourseInvitationViewHolder.HOLDER_RES_ID
        ItemType.ANNOUNCEMENT_HEADER -> BlankViewHolder.HOLDER_RES_ID
        ItemType.ANNOUNCEMENT -> AnnouncementViewHolder.HOLDER_RES_ID
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
