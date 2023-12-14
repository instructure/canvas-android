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
package com.instructure.pandarecycler.util

import androidx.recyclerview.widget.SortedList
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class GroupSortedList<GROUP, ITEM>(
    private val groupKlazz: Class<GROUP>,
    private val itemKlazz: Class<ITEM>,
    private val visualArrayCallback: VisualArrayCallback,

    /** Handles changes to the groups, unique group ids, and group types */
    private var groupComparatorCallback: GroupComparatorCallback<GROUP>,

    /** Handles changes to items in each group, unique item ids, and item types */
    private var itemComparatorCallback: ItemComparatorCallback<GROUP, ITEM>
) {
    /** Maps the items in each group to the id of the group */
    private var items = HashMap<Long, SortedList<ITEM>>()

    var disallowCollapse = false

    /** Groups will be expanded by default when true */
    var isExpandedByDefault = false

    var isDisplayEmptyCell = false

    /** The child item appear above the groups when true, below the group if false */
    var isChildrenAboveGroup = false

    /** The expanded state of a group is stored by its ID, so it doesn't matter if it moves positions */
    val isChildrenAboveGroupMap = HashMap<Long, Boolean?>()

    /** Manages the SortedList that contains the groups. */
    private val groupCallback: SortedList.Callback<GROUP> = object : SortedList.Callback<GROUP>() {
        override fun compare(o1: GROUP, o2: GROUP): Int {
            return groupComparatorCallback.compare(o1, o2)
        }

        override fun onInserted(storedGroupPosition: Int, count: Int) {
            visualArrayCallback.onInserted(getGroupVisualPositionFromStoredPosition(storedGroupPosition), count)
        }

        override fun onRemoved(storedGroupPosition: Int, count: Int) {
            visualArrayCallback.onRemoved(getGroupVisualPositionFromStoredPosition(storedGroupPosition), count)
        }

        override fun onMoved(storedGroupFromPosition: Int, storedGroupToPosition: Int) {
            // Handles when groups change positions
            // Collapse both groups, move them, then expand again if expanded.
            val groupNumberFromPosition = getStoredGroupPosition(storedGroupFromPosition)
            val fromGroup = getGroup(groupNumberFromPosition)
            val isFromGroupExpanded = isGroupExpanded(fromGroup)
            collapseGroup(fromGroup)
            val groupNumberToPosition = getStoredGroupPosition(storedGroupToPosition)
            val toGroup = getGroup(groupNumberToPosition)
            val isToGroupExpanded = isGroupExpanded(toGroup)
            collapseGroup(toGroup)
            visualArrayCallback.onMoved(
                getGroupVisualPositionFromStoredPosition(storedGroupFromPosition),
                getGroupVisualPositionFromStoredPosition(storedGroupToPosition)
            )
            if (isFromGroupExpanded) {
                expandGroup(fromGroup)
            }
            if (isToGroupExpanded) {
                expandGroup(toGroup)
            }
        }

        override fun onChanged(storedGroupPosition: Int, count: Int) {
            visualArrayCallback.onChanged(getGroupVisualPositionFromStoredPosition(storedGroupPosition), count)
        }

        override fun areContentsTheSame(oldItem: GROUP, newItem: GROUP): Boolean {
            return groupComparatorCallback.areContentsTheSame(oldItem, newItem)
        }

        override fun areItemsTheSame(item1: GROUP, item2: GROUP): Boolean {
            return groupComparatorCallback.areItemsTheSame(item1, item2)
        }
    }

    /** Manages the Group objects */
    private var groupObjects: SortedList<GROUP> = SortedList(groupKlazz, groupCallback)

    // region Interfaces
    interface GroupComparatorCallback<GRP> {
        fun compare(o1: GRP, o2: GRP): Int
        fun areContentsTheSame(oldGroup: GRP, newGroup: GRP): Boolean // Visual Contents
        fun areItemsTheSame(group1: GRP, group2: GRP): Boolean // Actual Groups (normally compare the ids)
        fun getUniqueGroupId(group: GRP): Long // Must be unique among the groups
        fun getGroupType(group: GRP): Int
    }

    interface ItemComparatorCallback<GRP, ITM> {
        fun compare(group: GRP, o1: ITM, o2: ITM): Int
        fun areContentsTheSame(oldItem: ITM, newItem: ITM): Boolean // Visual Contents
        fun areItemsTheSame(item1: ITM, item2: ITM): Boolean // Actual Items (normally compare the ids)
        fun getUniqueItemId(item: ITM): Long // Only has to be unique within the group
        fun getChildType(group: GRP, item: ITM): Int
    }

    abstract class VisualArrayCallback {
        abstract fun onInserted(position: Int, count: Int)
        abstract fun onRemoved(position: Int, count: Int)
        abstract fun onMoved(fromPosition: Int, toPosition: Int)
        abstract fun onChanged(position: Int, count: Int)
    }
    // endregion

    // region Adapter methods
    /** Determines whether a visualPosition is a group position */
    fun isVisualGroupPosition(visualPosition: Int): Boolean = visualPosition == getGroupVisualPosition(visualPosition)

    fun isVisualEmptyItemPosition(visualPosition: Int): Boolean {
        // TODO test this
        val group = getGroup(visualPosition)
        return getStoredChildrenCount(group) == 0 && isGroupExpanded(group) && !isVisualGroupPosition(visualPosition) && isDisplayEmptyCell
    }

    /** The visual size of the list */
    fun size(): Int {
        var totalChildren = 0
        for (i in 0 until storedGroupCount) {
            totalChildren += calculatedChildrenCount(i)
        }
        return totalChildren + storedGroupCount
    }

    /** Returns the number of groups in the list */
    val groupCount: Int get() = groupObjects.size()

    /** Returns number of items in a group */
    fun getGroupItemCount(group: GROUP): Int = getGroupItems(group).size()

    /** Passes a GROUP or ITEM to callbacks to determine type */
    fun getItemViewType(position: Int): Int {
        val groupStoredPosition = getStoredGroupPosition(position)
        val groupIndex = getGroupVisualPositionFromStoredPosition(groupStoredPosition)
        val groupObject = groupObjects[groupStoredPosition]
        return if (position == groupIndex) {
            groupComparatorCallback.getGroupType(groupObject)
        } else {
            val item: ITEM = if (isChildrenAboveGroup) {
                val groupItemPosition = abs(groupIndex - calculatedChildrenCount(groupStoredPosition) - position)
                items[getGroupId(groupObject)]!![groupItemPosition]
            } else {
                items[getGroupId(groupObject)]!![position - groupIndex - 1] // -1 for group header
            }
            itemComparatorCallback.getChildType(groupObject, item)
        }
    }
    // endregion

    // region expand/collapse helpers
    /** True if group is expanded; false otherwise */
    fun isGroupExpanded(group: GROUP): Boolean = isGroupExpanded(getGroupId(group))

    private fun isGroupExpanded(groupId: Long): Boolean {
        return isChildrenAboveGroupMap.getOrPut(groupId) { isExpandedByDefault }!!

    }

    private fun setExpanded(groupId: Long, isExpanded: Boolean) {
        isChildrenAboveGroupMap[groupId] = isExpanded
    }

    /**
     * Marks the groups matching the provided group IDs as expanded or collapsed.
     * Note that this only updates the underlying map and does notify any callbacks of the change.
     * @param groupIds IDs of the groups to mark as expanded or collapsed.
     * @param isExpanded Whether the groups should be marked as expanded (true) or collapsed (false)
     */
    fun markExpanded(groupIds: Set<Long>, isExpanded: Boolean) {
        if (disallowCollapse) return
        for (groupId in groupIds) {
            isChildrenAboveGroupMap[groupId] = isExpanded
        }
    }

    /**
     * Clears the underlying map that tracks which groups are expanded and collapsed.
     * Note that this only updates the underlying map and does notify any callbacks of the change.
     */
    fun clearExpanded() {
        isChildrenAboveGroupMap.clear()
    }

    /**
     * Expands the group
     * @param isNotifyGroupChange when true calls notify changed on the group's view holder
     */
    @JvmOverloads
    fun expandGroup(group: GROUP, isNotifyGroupChange: Boolean = false) {
        if (!isGroupExpanded(group)) {
            // add 1 to offset from where the header is located
            val groupPosition = storedGroupPosition(getGroupId(group))
            if (groupPosition == GROUP_NOT_FOUND) {
                return
            }
            setExpanded(getGroupId(group), true)
            var visualGroupPosition = getGroupVisualPositionFromStoredPosition(groupPosition)
            if (isNotifyGroupChange) {
                visualArrayCallback.onChanged(visualGroupPosition, 1)
            }
            if (calculatedChildrenCount(groupPosition) > 0) {
                if (isChildrenAboveGroup) {
                    visualGroupPosition -= calculatedChildrenCount(groupPosition)
                } else {
                    visualGroupPosition += 1
                }
                visualArrayCallback.onInserted(visualGroupPosition, calculatedChildrenCount(groupPosition))
            }
        }
    }

    /** Collapses all groups */
    fun collapseAll() {
        for (i in 0 until groupObjects.size()) collapseGroup(groupObjects[i], true)
    }

    /** Expands all groups */
    fun expandAll() {
        for (i in 0 until groupObjects.size()) expandGroup(groupObjects[i], true)
    }

    /** @param isNotifyGroupChange when true calls notify changed on the group's view holder */
    @JvmOverloads
    fun collapseGroup(group: GROUP, isNotifyGroupChange: Boolean = false) {
        if (isGroupExpanded(group) && !disallowCollapse) {
            // add 1 to offset from where the header is located
            val groupPosition = storedGroupPosition(getGroupId(group))
            if (groupPosition == GROUP_NOT_FOUND) {
                return
            }
            var visualGroupPosition = getGroupVisualPositionFromStoredPosition(groupPosition)
            if (isNotifyGroupChange) {
                visualArrayCallback.onChanged(visualGroupPosition, 1)
            }
            if (calculatedChildrenCount(groupPosition) > 0) {
                if (isChildrenAboveGroup) {
                    visualGroupPosition -= calculatedChildrenCount(groupPosition)
                } else {
                    visualGroupPosition += 1
                }
                visualArrayCallback.onRemoved(visualGroupPosition, calculatedChildrenCount(groupPosition))
            }
            // make sure setExpanded occurs after calculatedChildrenCount, so the proper amount is notified to be removed
            setExpanded(getGroupId(group), false)
        }
    }

    /** Expands if collapsed, collapses if expanded. */
    fun expandCollapseGroup(group: GROUP) {
        if (isGroupExpanded(group)) collapseGroup(group) else expandGroup(group)
    }

    /**
     * Expands if collapsed, collapses if expanded.
     * @param isNotifyGroupChange when true calls notify changed on the group's view holder
     */
    fun expandCollapseGroup(group: GROUP, isNotifyGroupChange: Boolean) {
        if (isGroupExpanded(group)) {
            collapseGroup(group, isNotifyGroupChange)
        } else {
            expandGroup(group, isNotifyGroupChange)
        }
    }
    // endregion

    // region Pseudo Array helpers
    private val storedGroupCount: Int get() = groupObjects.size()

    private fun getStoredChildrenCount(storedGroupPosition: Int): Int {
        val moduleItems = items[getGroupObjectId(storedGroupPosition)]
        return moduleItems?.size() ?: 0
    }

    private fun getStoredChildrenCount(group: GROUP): Int {
        val moduleItems = items[getGroupId(group)]
        return moduleItems?.size() ?: 0
    }

    /** Provides the index of where the group is located in the visual array */
    fun getGroupVisualPosition(visualPosition: Int): Int {
        val groupStoredPosition = getStoredGroupPosition(visualPosition)
        return getGroupVisualPositionFromStoredPosition(groupStoredPosition)
    }

    /**
     * Translates the stored position of a group to the Visual Position
     *
     * A way to think about finding the visual position is counting the group header, then the children in each group.
     * i.e. Given Group0 = {child0, child1}; Group1 = {child0, child1, child2} (GCCGCCC).
     *
     * The storedPosition of Group1 is 1.
     *
     * Running through the loop would be as follows (where the condition is i < storedPosition):
     * groupVisualPosition = 3; i = 0; storedPosition = 1 [0 < 1 is true]
     * groupVisualPosition = 3; i = 1; storedPosition = 1 [1 < 1 is false]. Visual group position is found.
     *
     * It is reversed when children are above (isChildrenAboveGroup). Given CCGCCCG
     *
     * The storedPosition of Group1 is 1.
     *
     * Running through the loop would be as follows (where the condition is i <= storedPosition):
     * groupVisualPosition = 3; i = 0; storedPosition = 1 [0 <= 1 is true]
     * groupVisualPosition = 7; i = 1; storedPosition = 1 [1 <= 1 is true]
     * groupVisualPosition = 7; i = 2; storedPosition = 1 [2 <= 1 is false]. Visual group position is found.
     */
    private fun getGroupVisualPositionFromStoredPosition(storedPosition: Int): Int {
        var groupVisualPosition = 0
        if (isChildrenAboveGroup) {
            // Notice: This for loop has a '<=' which is different from the for loop in the else condition
            for (i in 0..storedPosition) {
                if (i == storedPosition) { // Edge case, group is first in array
                    groupVisualPosition += calculatedChildrenCount(i)
                    break
                }
                groupVisualPosition += 1 // group header
                groupVisualPosition += calculatedChildrenCount(i)
            }
        } else {
            for (i in 0 until storedPosition) {
                groupVisualPosition += 1 // group header
                groupVisualPosition += calculatedChildrenCount(i)
            }
        }
        return groupVisualPosition
    }

    private fun getItemStoredPosition(visualPosition: Int): Int {
        val groupVisualPosition = getGroupVisualPosition(visualPosition)
        val itemPosition: Int
        itemPosition = if (isChildrenAboveGroup) {
            val groupStoredPosition = getStoredGroupPosition(visualPosition)
            // When the children are above the group, calculate the position relative to the groupVisualPosition
            abs(groupVisualPosition - calculatedChildrenCount(groupStoredPosition) - visualPosition)
        } else {
            visualPosition - groupVisualPosition - 1 // -1 for group header
        }
        return itemPosition
    }

    /**
     * When a group is expanded it returns the child count. If the Group has no children AND IsDisplayEmptyCell is true, it'll return 1 when the group is expanded
     * Otherwise when the group is collapsed it'll return 0.
     *
     * Helps in maintaining the visual array representation for the recycler adapter
     */
    private fun calculatedChildrenCount(storedGroupPosition: Int): Int {
        return if (storedGroupPosition < groupObjects.size() && isGroupExpanded(groupObjects[storedGroupPosition])) {
            val storedChildrenCount = getStoredChildrenCount(storedGroupPosition)
            if (isDisplayEmptyCell) {
                if (storedChildrenCount == 0) 1 else storedChildrenCount // Return 1 so that the empty item will be displayed. Used for if a group is empty
            } else {
                storedChildrenCount
            }
        } else {
            0
        }
    }

    /**
     * Searches for the group.
     *
     * i.e. Given a data set that had 2 groups first group has 2 children, the second group has 3 children (GCCGCCC).
     * The second group in the list has a visual position (or index) of 3.
     * As soon as the search position is greater than the visual position, or 3 in this case, the stored position is known.
     *
     * Running through the loop would be as follows (where the condition is visualPosition < searchPosition):
     * searchPosition = 3; i = 0; visualPosition = 3 [3 < 3 is false]
     * searchPosition = 7; i = 1; visualPosition = 3 [3 < 7 is true]. Stored group position is found
     */
    private fun getStoredGroupPosition(visualPosition: Int): Int {
        var searchPosition = 0
        for (i in 0 until storedGroupCount) {
            searchPosition += 1 // group header
            searchPosition += calculatedChildrenCount(i)
            if (visualPosition < searchPosition) {
                return i
            }
        }
        return 0 // 0 by default
    }
    // endregion

    // region model helpers
    // NOTE: Items are stored by their group ids NOT the stored group position
    private fun getGroupId(group: GROUP): Long = groupComparatorCallback.getUniqueGroupId(group)

    fun getItemId(item: ITEM): Long = itemComparatorCallback.getUniqueItemId(item)

    /**
     * Add or updates the group. If updated onChange is called in the visualArrayCallback, if added onInserted is called.
     */
    fun addOrUpdateGroup(group: GROUP): Int {
        var position = storedGroupPosition(getGroupId(group))
        if (position != GROUP_NOT_FOUND) { // TODO this can be better (if same group object, sorted list assumes object has changed)
            groupObjects.updateItemAt(position, group)
        } else {
            position = groupObjects.add(group)
        }
        return position
    }

    /** Removes a group */
    fun removeGroup(group: GROUP): Boolean {
        val items = items[getGroupId(group)]
        removeItems(items)
        return groupObjects.remove(group)
    }

    /** Get the group based on the visual position */
    fun getGroup(visualPosition: Int): GROUP {
        val groupNumber = getStoredGroupPosition(visualPosition)
        return groupObjects[groupNumber]
    }

    /**
     * @param groupId the group ID to look for
     * @return the visible position in the adapter, or -1 if not found
     */
    fun getGroupPosition(groupId: Long): Int {
        var expandedItems = 0
        for (i in 0 until groupObjects.size()) {
            expandedItems += if (getGroupId(groupObjects[i]) == groupId) {
                return i + expandedItems
            } else {
                calculatedChildrenCount(i)
            }
        }
        return -1
    }

    /**
     * Gets the group based on id.
     *
     * Returns [.GROUP_NOT_FOUND] if group doesn't exist
     */
    fun getGroup(id: Long): GROUP? {
        val storedGroupPosition = storedGroupPosition(id)
        return if (storedGroupPosition == GROUP_NOT_FOUND) {
            null
        } else getGroupFromStoredPosition(storedGroupPosition)
    }

    private fun getGroupFromStoredPosition(storedGroupPosition: Int): GROUP? {
        return if (groupObjects.size() > storedGroupPosition) groupObjects[storedGroupPosition] else null
    }

    /**
     * Gets all groups as a list
     * @return returns all groups as a list of GROUPs
     */
    val groups: ArrayList<GROUP> get() = ArrayList(groupObjects.toList())

    /** Add or updates the groups */
    fun addOrUpdateAllGroups(groups: List<GROUP>) {
        // TODO batched updates
        for (groupObject in groups) addOrUpdateGroup(groupObject)
    }

    /** Add all the groups */
    fun addOrUpdateAllGroups(groups: Array<GROUP>) {
        addOrUpdateAllGroups(groups.toList())
    }

    private fun getGroupObjectId(groupNumber: Int): Long = getGroupId(groupObjects[groupNumber])

    /** Remove all items */
    fun clearAll() {
        groupObjects = SortedList(groupKlazz, groupCallback)
        items = HashMap()
    }

    private fun removeItems(items: SortedList<ITEM>?) {
        items ?: return
        while (items.size() > 0) {
            items.remove(items[items.size() - 1])
        }
    }

    /** Iterates through all the groups to find the group by its id */
    private fun storedGroupPosition(id: Long): Int {
        // TODO Optimize this (it is really slow)
        for (i in 0 until groupObjects.size()) {
            val group = groupObjects[i]
            if (getGroupId(group) == id) {
                return i
            }
        }
        return GROUP_NOT_FOUND
    }

    // ITEMs
    /**
     * Remove the item
     * @param removeGroupIfEmpty Removes the group if no items remain in the group upon successful deletion.
     */
    @JvmOverloads
    fun removeItem(item: ITEM, removeGroupIfEmpty: Boolean = true): Boolean {
        val itemPosition = storedItemPosition(getItemId(item)) ?: return false
        val group = getGroup(itemPosition.groupId)
        val groupItems = getGroupItems(group!!)
        val isRemoved = groupItems.remove(item)

        // If the item was the last in the group, remove the group too.
        if (removeGroupIfEmpty && groupItems.size() == 0) {
            groupObjects.remove(group)
        }
        return isRemoved
    }

    fun changeItemPosition(group: GROUP, item: ITEM, newPosition: Int) {
        val groupItems = getGroupItems(group).toList().toMutableList()
        groupItems.remove(item)
        groupItems.add(newPosition, item)

        addOrUpdateAllItems(group, groupItems)
    }

    /**
     * Add the item to the group.
     *
     * If only the group id is known, call [.getGroup] to get the group object.
     * @return index of item in group
     */
    fun addOrUpdateItem(group: GROUP, item: ITEM): Int {
        if (getGroup(getGroupId(group)) !== group) { // assume if same object nothing changed
            addOrUpdateGroup(group)
        }
        val itemPosition = storedItemPosition(getItemId(item)) // determine if the item exists
        val groupItems = getGroupItems(group)

        // handles empty cell
        if (groupItems.size() == 0 && isGroupExpanded(group) && isDisplayEmptyCell) {
            val storedGroupPosition = groupObjects.indexOf(group)
            if (storedGroupPosition != -1) {
                if (isChildrenAboveGroup) {
                    // The Empty cell will be above the group, so -1
                    visualArrayCallback.onRemoved(
                        getGroupVisualPositionFromStoredPosition(storedGroupPosition) - 1,
                        1
                    ) // remove the empty cell assuming an item is added
                } else {
                    // The Empty cell will be below the group, so +1
                    visualArrayCallback.onRemoved(
                        getGroupVisualPositionFromStoredPosition(storedGroupPosition) + 1,
                        1
                    ) // remove the empty cell assuming an item is added
                }
            }
        }

        // Add or update the item
        if (itemPosition != null) {
            if (itemPosition.groupId == getGroupId(group)) {
                groupItems.updateItemAt(itemPosition.itemPosition, item)
            } else { // handle the case where the item has changed groups
                val oldGroupItems = getGroupItems(getGroup(itemPosition.groupId)!!)
                oldGroupItems.removeItemAt(itemPosition.itemPosition)
                return groupItems.add(item)
            }
        } else { // if its not there, just add it
            return groupItems.add(item)
        }
        return itemPosition.itemPosition
    }

    /** Adds or updates all the items */
    fun addOrUpdateAllItems(group: GROUP, items: List<ITEM>) {
        if (items.isEmpty()) return
        if (getGroup(getGroupId(group)) !== group) { // assume if same object nothing changed
            addOrUpdateGroup(group)
        }
        val groupItems = getGroupItems(group)
        groupItems.beginBatchedUpdates()

        // handles empty cell
        if (groupItems.size() == 0 && isGroupExpanded(group) && isDisplayEmptyCell) {
            val storedGroupPosition = groupObjects.indexOf(group)
            if (storedGroupPosition != -1) {
                if (isChildrenAboveGroup) {
                    // The Empty cell will be above the group, so -1
                    visualArrayCallback.onRemoved(
                        getGroupVisualPositionFromStoredPosition(storedGroupPosition) - 1,
                        1
                    ) // remove the empty cell assuming an item is added
                } else {
                    // The Empty cell will be below the group, so +1
                    visualArrayCallback.onRemoved(
                        getGroupVisualPositionFromStoredPosition(storedGroupPosition) + 1,
                        1
                    ) // remove the empty cell assuming an item is added
                }
            }
        }
        for (item in items) {
            val itemPosition = storedItemPosition(getItemId(item))
            // Add or update the item
            if (itemPosition != null) {
                if (itemPosition.groupId == getGroupId(group)) {
                    groupItems.updateItemAt(itemPosition.itemPosition, item)
                } else { // handle the case where the item has changed groups
                    val oldGroupItems = getGroupItems(getGroup(itemPosition.groupId)!!)
                    oldGroupItems.removeItemAt(itemPosition.itemPosition)
                    groupItems.add(item)
                }
            } else { // if its not there, just add it
                groupItems.add(item)
            }
        }
        groupItems.endBatchedUpdates()
    }

    fun addOrUpdateAllItems(group: GROUP, items: Array<ITEM>) {
        addOrUpdateAllItems(group, items.toList())
    }

    /**
     * returns where the item is in the pseudo array
     * @return -1 if not found, visual position otherwise
     */
    fun getItemVisualPosition(itemId: Long): Int {
        if (itemId == -1L) {
            return -1
        }
        val itemPosition = storedItemPosition(itemId) ?: return -1 // Locate the item
        val groupId = itemPosition.groupId
        val storedGroupPosition = storedGroupPosition(groupId)
        if (storedGroupPosition == GROUP_NOT_FOUND) {
            return -1
        }
        return if (isChildrenAboveGroup) {
            getGroupVisualPositionFromStoredPosition(storedGroupPosition) - calculatedChildrenCount(
                storedGroupPosition
            ) + itemPosition.itemPosition
        } else {
            getGroupVisualPositionFromStoredPosition(storedGroupPosition) + itemPosition.itemPosition + 1
        }
    }

    /** Get an item based on its visualPosition */
    fun getItem(visualPosition: Int): ITEM? {
        // TODO test this
        val group = getGroup(visualPosition)
        val itemPosition = getItemStoredPosition(visualPosition)
        val list = getGroupItems(group)
        return if (isValidItemIndex(itemPosition, list)) list[itemPosition] else null
    }

    /**
     * Get an item based on its stored position (the position relative to the list of items only)
     *
     * i.e. Group1 = { item0, item1, item2 }; Group2= { item3, item4, item5 };
     * getItem(Group2, 1) == item4
     */
    fun getItem(group: GROUP, itemStoredPosition: Int): ITEM? {
        val list = getGroupItems(group)
        return if (isValidItemIndex(itemStoredPosition, list)) list[itemStoredPosition] else null
    }

    /**
     * Returns all items for a specified group as a List<ITEM>
     * @param group A group of items
     * @return returns a list of ITEMs for a GROUP
     */
    fun getItems(group: GROUP): ArrayList<ITEM> {
        if (group == null) return ArrayList()
        val list = getGroupItems(group)
        val items = ArrayList<ITEM>(list.size())
        for (i in 0 until list.size()) {
            items.add(list[i])
        }
        return items
    }

    /**
     * If the index is -1 or size() (itemPosition == 5, when size() == 5)
     * Then the position is most likely a header, in that case we shouldn't crash, but just return null
     */
    private fun isValidItemIndex(itemStoredPosition: Int, list: SortedList<ITEM>): Boolean {
        return list.size() > 0 && itemStoredPosition >= 0 && itemStoredPosition < list.size()
    }

    /**
     * Get item index in relation to its group
     * @return -1 if not found
     */
    fun storedIndexOfItem(group: GROUP, item: ITEM): Int {
        val list = getGroupItems(group)
        return if (list.size() > 0) list.indexOf(item) else -1
    }

    /** Search for the item in all of the groups */
    private fun storedItemPosition(itemId: Long): ItemPosition? {
        // TODO Optimize this (it is really slow)
        for ((key, itemList) in items) {
            for (i in 0 until itemList.size()) {
                val item = itemList[i]
                if (getItemId(item) == itemId) {
                    return ItemPosition(key, i)
                }
            }
        }
        return null
    }

    /** Get the items in a group */
    private fun getGroupItems(group: GROUP): SortedList<ITEM> {
        val groupObjectId = getGroupId(group)
        var groupItems = items[groupObjectId]
        if (groupItems == null) {
            groupItems = SortedList(itemKlazz, createCallback(groupObjectId))
            items[groupObjectId] = groupItems
        }
        return groupItems
    }

    /** Manages the SortedLists that contains the items in each group. */
    private fun createCallback(groupObjectId: Long): ExpandableCallback<ITEM> {
        return object : ExpandableCallback<ITEM>(groupObjectId) {
            /** Translate the storedGroupPosition and item position to the visual index. */
            private fun getVisualIndex(storedGroupPosition: Int, position: Int): Int {
                var visualIndex = getGroupVisualPositionFromStoredPosition(storedGroupPosition)
                if (isChildrenAboveGroup) {
                    visualIndex = visualIndex - calculatedChildrenCount(storedGroupPosition) + position
                } else {
                    visualIndex += 1 + position
                }
                return visualIndex
            }

            override fun compare(o1: ITEM, o2: ITEM): Int {
                return itemComparatorCallback.compare(getGroup(groupObjectId)!!, o1, o2)
            }

            override fun onInserted(position: Int, count: Int) {
                val groupId = this.groupObjectId
                if (isGroupExpanded(groupId)) {
                    val storedGroupPosition = storedGroupPosition(groupId)
                    if (storedGroupPosition == GROUP_NOT_FOUND) {
                        return
                    }
                    visualArrayCallback.onInserted(getVisualIndex(storedGroupPosition, position), count)
                }
            }

            override fun onRemoved(position: Int, count: Int) {
                val groupId = this.groupObjectId
                if (isGroupExpanded(groupId)) {
                    val storedGroupPosition = storedGroupPosition(groupId)
                    if (storedGroupPosition == GROUP_NOT_FOUND) {
                        return
                    }
                    visualArrayCallback.onRemoved(getVisualIndex(storedGroupPosition, position), count)
                }
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                val groupId = this.groupObjectId
                if (isGroupExpanded(groupId)) {
                    val storedGroupPosition = storedGroupPosition(groupId)
                    if (storedGroupPosition == GROUP_NOT_FOUND) {
                        return
                    }
                    visualArrayCallback.onMoved(
                        getVisualIndex(storedGroupPosition, fromPosition),
                        getVisualIndex(storedGroupPosition, toPosition)
                    )
                }
            }

            override fun onChanged(position: Int, count: Int) {
                val groupId = this.groupObjectId
                if (isGroupExpanded(groupId)) {
                    val storedGroupPosition = storedGroupPosition(groupId)
                    if (storedGroupPosition == GROUP_NOT_FOUND) {
                        return
                    }
                    visualArrayCallback.onChanged(getVisualIndex(storedGroupPosition, position), count)
                }
            }

            override fun areContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean {
                return itemComparatorCallback.areContentsTheSame(oldItem, newItem)
            }

            override fun areItemsTheSame(item1: ITEM, item2: ITEM): Boolean {
                return itemComparatorCallback.areItemsTheSame(item1, item2)
            }
        }
    }

    abstract class ExpandableCallback<T>(var groupObjectId: Long) :
        SortedList.Callback<T>()

    private class ItemPosition(var groupId: Long, var itemPosition: Int)

    @Suppress("unused")
    fun setGroupComparatorCallback(groupComparatorCallback: GroupComparatorCallback<GROUP>) {
        this.groupComparatorCallback = groupComparatorCallback
    }

    @Suppress("unused")
    fun setItemComparatorCallback(itemComparatorCallback: ItemComparatorCallback<GROUP, ITEM>) {
        this.itemComparatorCallback = itemComparatorCallback
    }

    companion object {
        private const val GROUP_NOT_FOUND = -2
    }
}
