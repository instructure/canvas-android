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
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.isInvited
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApis
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.student.holders.EditFavoritesCourseHeaderViewHolder
import com.instructure.student.holders.EditFavoritesCourseViewHolder
import com.instructure.student.holders.EditFavoritesGroupHeaderViewHolder
import com.instructure.student.holders.EditFavoritesGroupViewHolder
import com.instructure.student.interfaces.AdapterToFragmentCallback

class EditFavoritesRecyclerAdapter(
        context: Activity,
        private val mAdapterToFragmentCallback: AdapterToFragmentCallback<CanvasComparable<*>>
) : ExpandableRecyclerAdapter<EditFavoritesRecyclerAdapter.ItemType, CanvasComparable<*>, RecyclerView.ViewHolder>(
        context,
        ItemType::class.java,
        CanvasComparable::class.java
) {

    enum class ItemType {
        COURSE_HEADER,
        COURSE,
        GROUP_HEADER,
        GROUP
    }

    private var mApiCalls: WeaveJob? = null

    init {
        isExpandedByDefault = true
        loadData()
    }

    override fun createItemCallback(): GroupSortedList.ItemComparatorCallback<ItemType, CanvasComparable<*>> {
        return object : GroupSortedList.ItemComparatorCallback<ItemType, CanvasComparable<*>> {
            override fun compare(group: ItemType?, o1: CanvasComparable<*>?, o2: CanvasComparable<*>?) = when {
                o1 is Course && o2 is Course -> o1.compareTo(o2)
                o1 is Group && o2 is Group -> o1.compareTo(o2)
                else -> -1
            }

            override fun areContentsTheSame(oldItem: CanvasComparable<*>?, newItem: CanvasComparable<*>?) = false

            override fun areItemsTheSame(item1: CanvasComparable<*>?, item2: CanvasComparable<*>?) = when {
                item1 is Course && item2 is Course -> item1.contextId.hashCode() == item2.contextId.hashCode()
                item1 is Group && item2 is Group -> item1.contextId.hashCode() == item2.contextId.hashCode()
                else -> false
            }

            override fun getUniqueItemId(item: CanvasComparable<*>?) = when (item) {
                is Course -> item.contextId.hashCode().toLong()
                is Group -> item.contextId.hashCode().toLong()
                else -> -1L
            }

            override fun getChildType(group: ItemType?, item: CanvasComparable<*>?) = when (item) {
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

    override fun itemLayoutResId(viewType: Int): Int = when(ItemType.values()[viewType]) {
        ItemType.COURSE_HEADER -> EditFavoritesCourseHeaderViewHolder.HOLDER_RES_ID
        ItemType.COURSE -> EditFavoritesCourseViewHolder.HOLDER_RES_ID
        ItemType.GROUP_HEADER -> EditFavoritesGroupHeaderViewHolder.HOLDER_RES_ID
        ItemType.GROUP -> EditFavoritesGroupViewHolder.HOLDER_RES_ID
    }

    override fun createViewHolder(v: View, viewType: Int) = when(ItemType.values()[viewType]) {
        ItemType.COURSE_HEADER -> EditFavoritesCourseHeaderViewHolder(v)
        ItemType.COURSE -> EditFavoritesCourseViewHolder(v)
        ItemType.GROUP_HEADER -> EditFavoritesGroupHeaderViewHolder(v)
        ItemType.GROUP -> EditFavoritesGroupViewHolder(v)
    }

    override fun onBindChildHolder(holder: RecyclerView.ViewHolder, header: ItemType, item: CanvasComparable<*>) {
        when {
            holder is EditFavoritesCourseViewHolder && item is Course -> holder.bind(context,item, mAdapterToFragmentCallback)
            holder is EditFavoritesGroupViewHolder && item is Group -> holder.bind(context, item, mAdapterToFragmentCallback)
        }
    }

    override fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, header: ItemType, isExpanded: Boolean) = Unit

    override fun loadData() {
        mApiCalls?.cancel()
        mApiCalls = tryWeave {
            val (rawCourses, rawGroups) = awaitApis<List<Course>,List<Group>>(
                    { CourseManager.getCourses(true, it) },
                    { GroupManager.getAllGroups(it,true)})
            val validCourses = rawCourses.filter { !it.accessRestrictedByDate && !it.isInvited() }
            addOrUpdateAllItems(ItemType.COURSE_HEADER,validCourses)
            val courseMap = rawCourses.associateBy { it.id }
            val groups = rawGroups.filter { group ->
                val groupCourse = courseMap[group.courseId] ?: return@filter true // Account groups don't have a course
                with(groupCourse) { isValidTerm() && !accessRestrictedByDate && endDate?.before(java.util.Date()) != true }
            }
            addOrUpdateAllItems(ItemType.GROUP_HEADER,groups)
            notifyDataSetChanged()
            isAllPagesLoaded = true
            if (itemCount == 0) adapterToRecyclerViewCallback.setIsEmpty(true)
            mAdapterToFragmentCallback.onRefreshFinished()
        } catch {
            onNoNetwork()
        }
    }

    override fun refresh() {
        mApiCalls?.cancel()
        super.refresh()
    }

    override fun cancel() {
        mApiCalls?.cancel()
    }
}
