/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.pandarecycler

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked
import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.GroupSortedList.*
import com.instructure.pandarecycler.util.Types
import java.util.*

@Suppress("unused")
abstract class BaseExpandableRecyclerAdapter<GROUP, ITEM, VIEW_HOLDER : RecyclerView.ViewHolder>(
    context: Context, groupKlazz: Class<GROUP>, itemKlazz: Class<ITEM>
) : PaginatedRecyclerAdapter<VIEW_HOLDER>(context) {
    /** Create a view holder based on the given viewType  */
    abstract override fun createViewHolder(v: View, viewType: Int): VIEW_HOLDER

    /** Bind the item in a given group  */
    abstract fun onBindChildHolder(holder: RecyclerView.ViewHolder, group: GROUP, item: ITEM)

    /** Bind the Header for a given Group  */
    abstract fun onBindHeaderHolder(holder: RecyclerView.ViewHolder, group: GROUP, isExpanded: Boolean)

    /** Empty Views are shown when IsExpandedByDefault is set to true and when there are no items in the group.  */
    open fun onBindEmptyHolder(holder: RecyclerView.ViewHolder, group: GROUP) {}

    /** Must return a non-null GroupComparatorCallback  */
    abstract fun createGroupCallback(): GroupComparatorCallback<GROUP>

    /** Must return a non-null ItemComparatorCallback  */
    abstract fun createItemCallback(): ItemComparatorCallback<GROUP, ITEM>

    // Manages all the objects in the list
    private val groupSortedList: GroupSortedList<GROUP, ITEM>

    /** A generic way to handle when a header is clicked  */
    var viewHolderHeaderClicked: ViewHolderHeaderClicked<GROUP>

    private var selectedItemId: Long = 0

    var isDisplayEmptyCell = false
        set(value) {
        groupSortedList.isDisplayEmptyCell = value
        field = value
    }

    var isChildrenAboveGroup = false
        set(value) {
        groupSortedList.isChildrenAboveGroup = value
        field = value
    }

    /** See [GroupSortedList.isExpandedByDefault]  */
    var isExpandedByDefault: Boolean
        get() = groupSortedList.isExpandedByDefault
        set(isExpandedByDefault) {
            groupSortedList.isExpandedByDefault = isExpandedByDefault
        }

    override fun setSelectedItemId(itemId: Long) {
        selectedItemId = itemId
    }

    override fun clear() {
        groupSortedList.clearAll()
        notifyDataSetChanged()
    }

    init {
        @Suppress("LeakingThis")
        groupSortedList = GroupSortedList(
            groupKlazz,
            itemKlazz,
            object : VisualArrayCallback() {
                override fun onInserted(position: Int, count: Int) {
                    notifyItemRangeInserted(position, count)
                }

                override fun onRemoved(position: Int, count: Int) {
                    notifyItemRangeRemoved(position, count)
                }

                override fun onMoved(fromPosition: Int, toPosition: Int) {
                    notifyItemMoved(fromPosition, toPosition)
                }

                override fun onChanged(position: Int, count: Int) {
                    notifyItemRangeChanged(position, count)
                }
            },
            createGroupCallback(),
            createItemCallback()
        )
        groupSortedList.isDisplayEmptyCell = isDisplayEmptyCell
        groupSortedList.isChildrenAboveGroup = isChildrenAboveGroup
        viewHolderHeaderClicked = object : ViewHolderHeaderClicked<GROUP> {
            override fun viewClicked(view: View?, header: GROUP) {
                groupSortedList.expandCollapseGroup(header)
            }
        }

        // Workaround for a11y bug that causes TalkBack to skip items after expand/collapse
        val a11yManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            a11yManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN)
        if (enabledServices != null && enabledServices.isNotEmpty()) groupSortedList.disallowCollapse = true
    }

    // region Selection
    /**
     * Sets the selected position. The position is saved based on the ID of the object, so if a group
     * is expanded or collapsed, the correct position remains selected.
     */
    override fun setSelectedPosition(position: Int) {
        if (position == -1 || groupSortedList.isVisualGroupPosition(position)) {
            return
        }
        if (selectedItemId != -1L) {
            val oldPosition = groupSortedList.getItemVisualPosition(selectedItemId)
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
        }
        val item = groupSortedList.getItem(position)
        if (item != null) {
            selectedItemId = groupSortedList.getItemId(item)
            notifyItemChanged(position)
        }
        super.setSelectedPosition(position)
    }

    /** True when an item is selected; false otherwise  */
    fun isItemSelected(item: ITEM): Boolean {
        return groupSortedList.getItemId(item) == selectedItemId
    }

    // endregion
    override fun onBindViewHolder(baseHolder: VIEW_HOLDER, position: Int) {
        val group = groupSortedList.getGroup(position)
        when {
            groupSortedList.isVisualEmptyItemPosition(position) -> onBindEmptyHolder(baseHolder, group)
            groupSortedList.isVisualGroupPosition(position) -> {
                onBindHeaderHolder(baseHolder, group, groupSortedList.isGroupExpanded(group))
            }
            isLoadingFooterPosition(position) -> super.onBindViewHolder(baseHolder, position)
            else -> onBindChildHolder(baseHolder, group, groupSortedList.getItem(position)!!)
        }
    }

    /** The total size of the adapter. This includes GROUPS and ITEMS together  */
    override fun size(): Int {
        return groupSortedList.size()
    }

    override fun getItemViewType(position: Int): Int {
        if (groupSortedList.isVisualEmptyItemPosition(position)) {
            return Types.TYPE_EMPTY_CELL
        } else if (isLoadingFooterPosition(position)) {
            return super.getItemViewType(position)
        }
        return groupSortedList.getItemViewType(position)
    }
    // region helpers
    /** See [GroupSortedList.addOrUpdateAllItems]  */
    fun addOrUpdateAllItems(group: GROUP, items: List<ITEM>) {
        groupSortedList.addOrUpdateAllItems(group, items)
    }

    fun moveItems(group: GROUP, item: ITEM, newPosition: Int) {
        groupSortedList.changeItemPosition(group, item, newPosition)
    }

    /** See [GroupSortedList.addOrUpdateAllItems]  */
    fun addOrUpdateAllItems(group: GROUP, items: Array<ITEM>) {
        groupSortedList.addOrUpdateAllItems(group, items)
    }

    /** See [GroupSortedList.addOrUpdateItem]  */
    fun addOrUpdateItem(group: GROUP, item: ITEM) {
        groupSortedList.addOrUpdateItem(group, item)
    }

    /** See [GroupSortedList.removeItem]  */
    fun removeItem(item: ITEM): Boolean {
        return groupSortedList.removeItem(item)
    }

    /** See [GroupSortedList.removeItem]  */
    fun removeItem(item: ITEM, removeGroupIfEmpty: Boolean): Boolean {
        return groupSortedList.removeItem(item, removeGroupIfEmpty)
    }

    /** See [GroupSortedList.getItem]  */
    fun getItem(group: GROUP, storedPosition: Int): ITEM? {
        return groupSortedList.getItem(group, storedPosition)
    }

    fun getItem(visualPosition: Int): ITEM? {
        return groupSortedList.getItem(visualPosition)
    }

    fun getChildItemId(position: Int): Long {
        return groupSortedList.getItemId(groupSortedList.getItem(position)!!)
    }

    override fun getItemId(position: Int): Long {
        throw UnsupportedOperationException("Method getItemId() is unimplemented in BaseExpandableRecyclerAdapter. Use getChildItemId instead.")
    }

    fun getItems(group: GROUP): ArrayList<ITEM> {
        return groupSortedList.getItems(group)
    }

    /** See [GroupSortedList.storedIndexOfItem]  */
    fun storedIndexOfItem(group: GROUP, item: ITEM): Int {
        return groupSortedList.storedIndexOfItem(group, item)
    }

    /** See [GroupSortedList.addOrUpdateAllGroups]  */
    fun addOrUpdateAllGroups(groups: Array<GROUP>?) {
        groupSortedList.addOrUpdateAllGroups(groups!!)
    }

    /** See [GroupSortedList.addOrUpdateGroup]  */
    fun addOrUpdateGroup(group: GROUP) {
        groupSortedList.addOrUpdateGroup(group)
    }

    /** See [GroupSortedList.getGroup]  */
    fun getGroup(groupId: Long): GROUP? {
        return groupSortedList.getGroup(groupId)
    }

    /** See [GroupSortedList.getGroup]  */
    private fun getGroup(position: Int): GROUP {
        return groupSortedList.getGroup(position)
    }

    /** See [GroupSortedList.groups]  */
    val groups: ArrayList<GROUP>
        get() = groupSortedList.groups

    /** See [GroupSortedList.groupCount]  */
    val groupCount: Int
        get() = groupSortedList.groupCount

    /** See [GroupSortedList.getGroupItemCount]  */
    fun getGroupItemCount(group: GROUP): Int {
        return groupSortedList.getGroupItemCount(group)
    }

    fun getGroupItemCount(position: Int): Int {
        val group = getGroup(position)
        return getGroupItemCount(group)
    }

    /** See [GroupSortedList.expandCollapseGroup]  */
    fun expandCollapseGroup(group: GROUP) {
        groupSortedList.expandCollapseGroup(group)
    }

    /** See [GroupSortedList.collapseAll]  */
    fun collapseAll() {
        groupSortedList.collapseAll()
    }

    /** See [GroupSortedList.expandAll]  */
    fun expandAll() {
        groupSortedList.expandAll()
    }

    /** See [GroupSortedList.expandGroup]  */
    fun expandGroup(group: GROUP) {
        groupSortedList.expandGroup(group)
    }

    /** See [GroupSortedList.expandGroup]  */
    fun expandGroup(group: GROUP, isNotifyGroupChange: Boolean) {
        groupSortedList.expandGroup(group, isNotifyGroupChange)
    }

    /** See [GroupSortedList.collapseGroup]  */
    fun collapseGroup(group: GROUP) {
        groupSortedList.collapseGroup(group)
    }

    /** See [GroupSortedList.collapseGroup]  */
    fun collapseGroup(group: GROUP, isNotifyGroupChange: Boolean) {
        groupSortedList.collapseGroup(group, isNotifyGroupChange)
    }

    /** See [GroupSortedList.isGroupExpanded]  */
    fun isGroupExpanded(group: GROUP): Boolean {
        return groupSortedList.isGroupExpanded(group)
    }

    /** See [GroupSortedList.getGroupVisualPosition]  */
    fun getGroupVisualPosition(position: Int): Int {
        return groupSortedList.getGroupVisualPosition(position)
    }

    fun getGroupItemPosition(itemId: Long): Int {
        return groupSortedList.getGroupPosition(itemId)
    }

    /** See [GroupSortedList.isVisualGroupPosition]  */
    fun isPositionGroupHeader(position: Int): Boolean {
        return groupSortedList.isVisualGroupPosition(position)
    }

    // endregion

    override fun cancel() {}
}
