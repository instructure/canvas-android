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

import com.instructure.pandarecycler.util.GroupSortedList
import com.instructure.pandarecycler.util.GroupSortedList.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class GroupSortedListTest : Assert() {
    private lateinit var groupList: GroupSortedList<Item, Item>
    var additions: MutableList<Pair<Int, Int>> = ArrayList()
    var removals: MutableList<Pair<Int, Int>> = ArrayList()
    var moves: MutableList<Pair<Int, Int>> = ArrayList()
    var updates: MutableList<Pair<Int, Int>> = ArrayList()

    @Before
    fun setUp() {
        val vaCallback: VisualArrayCallback = object : VisualArrayCallback() {
            override fun onInserted(position: Int, count: Int) {
                additions.add(Pair(position, count))
            }

            override fun onRemoved(position: Int, count: Int) {
                removals.add(Pair(position, count))
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                moves.add(Pair(fromPosition, toPosition))
            }

            override fun onChanged(position: Int, count: Int) {
                updates.add(Pair(position, count))
            }
        }
        val groupCallback: GroupComparatorCallback<Item> =
            object : GroupComparatorCallback<Item> {
                override fun compare(o1: Item, o2: Item): Int = o1.cmpField.compareTo(o2.cmpField)
                override fun areContentsTheSame(oldGroup: Item, newGroup: Item): Boolean = oldGroup.cmpField == newGroup.cmpField && oldGroup.data == newGroup.data
                override fun areItemsTheSame(group1: Item, group2: Item): Boolean = group1.id == group2.id
                override fun getGroupType(group: Item): Int = TYPE_HEADER
                override fun getUniqueGroupId(group: Item): Long = group.id.toLong()
            }

        val itemCallback: ItemComparatorCallback<Item, Item> =
            object : ItemComparatorCallback<Item, Item> {
                override fun compare(group: Item, o1: Item, o2: Item): Int = o1.cmpField.compareTo(o2.cmpField)
                override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem.cmpField == newItem.cmpField && oldItem.data == newItem.data
                override fun areItemsTheSame(item1: Item, item2: Item): Boolean = item1.id == item2.id
                override fun getChildType(group: Item, item: Item): Int = TYPE_ITEM
                override fun getUniqueItemId(item: Item): Long = item.id.toLong()
            }
        groupList = GroupSortedList(Item::class.java, Item::class.java, vaCallback, groupCallback, itemCallback)
        groupList.isExpandedByDefault = true
    }

    @Test
    fun testEmpty() {
        assertEquals("empty", groupList.size().toLong(), 0)
    }

    // region add
    @Test
    fun testGroupAdd() {
        val item = Item()
        assertEquals(addOrUpdateGroup(item).toLong(), 0)
        assertEquals(size().toLong(), 1)
        assertTrue(additions.contains(Pair(0, 1)))
        val item2 = Item()
        item2.cmpField = item.cmpField + 1
        assertEquals(addOrUpdateGroup(item2).toLong(), 1)
        assertEquals(size().toLong(), 2)
        assertTrue(additions.contains(Pair(1, 1)))
        val item3 = Item()
        item3.cmpField = item.cmpField - 1
        additions.clear()
        assertEquals(addOrUpdateGroup(item3).toLong(), 0)
        assertEquals(size().toLong(), 3)
        assertTrue(additions.contains(Pair(0, 1)))
    }

    @Test
    fun testItemAdd_ToGroup1() {
        val group = Item()
        val item = Item()
        val expected: MutableList<Pair<Int, Int>> = ArrayList()
        expected.add(Pair(0, 1))
        expected.add(Pair(1, 1))
        assertEquals(addOrUpdateItem(group, item).toLong(), 0)
        assertEquals(size().toLong(), 2)
        assertEquals(expected, additions)
        val item2 = Item()
        item2.cmpField = item.cmpField + 1
        expected.add(Pair(2, 1))
        assertEquals(addOrUpdateItem(group, item2).toLong(), 1)
        assertEquals(size().toLong(), 3)
        assertEquals(expected, additions)
        val item3 = Item()
        item3.cmpField = item.cmpField - 1
        expected.clear()
        expected.add(Pair(1, 1))
        additions.clear()
        assertEquals(addOrUpdateItem(group, item3).toLong(), 0)
        assertEquals(size().toLong(), 4)
        assertEquals(expected, additions)
    }

    @Test
    fun testItemAdd_endGroup() {
        val group = Item(3)
        var item = Item()
        var expected: MutableList<Pair<Int, Int>> = ArrayList()
        expected.add(Pair(0, 1))
        expected.add(Pair(1, 1))
        assertEquals(addOrUpdateItem(group, item).toLong(), 0)
        assertEquals(size().toLong(), 2)
        assertEquals(expected, additions)
        var item2 = Item()
        item2.cmpField = item.cmpField + 1
        expected.add(Pair(2, 1))
        assertEquals(addOrUpdateItem(group, item2).toLong(), 1)
        assertEquals(size().toLong(), 3)
        assertEquals(expected, additions)
        var item3 = Item()
        item3.cmpField = item.cmpField - 1
        expected.clear()
        expected.add(Pair(1, 1))
        additions.clear()
        assertEquals(addOrUpdateItem(group, item3).toLong(), 0)
        assertEquals(size().toLong(), 4)
        assertEquals(expected, additions)
        additions.clear()

        // group 2
        val group1 = Item(5)
        item = Item()
        expected = ArrayList()
        expected.add(Pair(4, 1))
        expected.add(Pair(5, 1))
        assertEquals(addOrUpdateItem(group1, item).toLong(), 0)
        assertEquals(size().toLong(), 6)
        assertEquals(expected, additions)
        item2 = Item()
        item2.cmpField = item.cmpField + 1
        expected.add(Pair(6, 1))
        assertEquals(addOrUpdateItem(group1, item2).toLong(), 1)
        assertEquals(size().toLong(), 7)
        assertEquals(expected, additions)
        item3 = Item()
        item3.cmpField = item.cmpField - 1
        expected.clear()
        expected.add(Pair(5, 1))
        additions.clear()
        assertEquals(addOrUpdateItem(group1, item3).toLong(), 0)
        assertEquals(size().toLong(), 8)
        assertEquals(expected, additions)
    }

    @Test
    fun testAddAllItems() {
        val group = Item(5)
        addOrUpdateAllItems(group, createList(0))
        assertEquals(0, size().toLong())
        addOrUpdateAllItems(group, createList(5))
        assertEquals(6, size().toLong())
    }

    // endregion
    // region getters
    @Test
    fun testGetItem_visualPos() {
        val group1 = Item(1, 1000)
        val group2 = Item(2, 1001)
        addOrUpdateAllItems(group1, createList(5, 0))
        addOrUpdateAllItems(group2, createList(5, 5))
        assertEquals(12, size().toLong())

        // Group 1
        assertEquals(0, getItem(1)!!.id.toLong())
        assertEquals(1, getItem(2)!!.id.toLong())
        assertEquals(4, getItem(5)!!.id.toLong())

        // Group 2
        assertEquals(5, getItem(7)!!.id.toLong())
        assertEquals(6, getItem(8)!!.id.toLong())
        assertEquals(9, getItem(11)!!.id.toLong())
    }

    @Test
    fun testGetGroup_visualPosition() {
        val group1 = Item(1, 1000)
        val group2 = Item(2, 1001)
        addOrUpdateAllItems(group1, createList(5, 0))
        addOrUpdateAllItems(group2, createList(5, 5))
        assertEquals(0, getGroupVisualPosition(0).toLong())
        assertEquals(0, getGroupVisualPosition(1).toLong())
        assertEquals(0, getGroupVisualPosition(2).toLong())
        assertEquals(0, getGroupVisualPosition(3).toLong())
        assertEquals(0, getGroupVisualPosition(4).toLong())
        assertEquals(0, getGroupVisualPosition(5).toLong())
        assertEquals(6, getGroupVisualPosition(6).toLong())
        assertEquals(6, getGroupVisualPosition(7).toLong())
        assertEquals(6, getGroupVisualPosition(8).toLong())
        assertEquals(6, getGroupVisualPosition(9).toLong())
        assertEquals(6, getGroupVisualPosition(10).toLong())
        assertEquals(6, getGroupVisualPosition(11).toLong())
    }

    @Test
    fun testGetItem_visualPosException() {
        val group1 = Item(1, 1000)
        val group2 = Item(2, 1001)
        addOrUpdateAllItems(group1, createList(200, 0))
        addOrUpdateAllItems(group2, createList(200, 200))
        assertEquals(402, size().toLong())
        assertNull(getItem(201))
    }

    @Test
    fun testGetItem_visualPosBigNumbers() {
        val group1 = Item(1, 1000)
        val group2 = Item(2, 1001)
        addOrUpdateAllItems(group1, createList(200, 0))
        addOrUpdateAllItems(group2, createList(200, 200))
        assertEquals(402, size().toLong())

        // Group 1
        assertEquals(0, getItem(1)!!.id.toLong())
        assertEquals(1, getItem(2)!!.id.toLong())
        assertEquals(199, getItem(200)!!.id.toLong())

        // Group 2
        assertEquals(200, getItem(202)!!.id.toLong())
        assertEquals(201, getItem(203)!!.id.toLong())
        assertEquals(399, getItem(401)!!.id.toLong())
    }

    @Test
    fun testGetItem_groupStoredPos() {
        val group1 = Item(1, 1000)
        val group2 = Item(2, 1001)
        addOrUpdateAllItems(group1, createList(200, 0))
        addOrUpdateAllItems(group2, createList(200, 200))
        assertEquals(402, size().toLong())

        // Group 1
        assertEquals(0, getItem(group1, 0)!!.id.toLong())
        assertEquals(1, getItem(group1, 1)!!.id.toLong())
        assertEquals(199, getItem(group1, 199)!!.id.toLong())

        // Group 2
        assertEquals(200, getItem(group2, 0)!!.id.toLong())
        assertEquals(201, getItem(group2, 1)!!.id.toLong())
        assertEquals(399, getItem(group2, 199)!!.id.toLong())
    }

    @Test
    fun testStoredIndexOfItem() {
        val group1 = Item(1, 1000)
        val group2 = Item(2, 1001)
        val items1 = createList(200, 0)
        val items2 = createList(200, 200)
        addOrUpdateAllItems(group1, items1)
        addOrUpdateAllItems(group2, items2)
        assertEquals(402, size().toLong())

        // Group 1
        assertEquals(0, storedIndexOfItem(group1, items1[0]).toLong())
        assertEquals(1, storedIndexOfItem(group1, items1[1]).toLong())
        assertEquals(199, storedIndexOfItem(group1, items1[199]).toLong())

        // Group 2
        assertEquals(0, storedIndexOfItem(group2, items2[0]).toLong())
        assertEquals(1, storedIndexOfItem(group2, items2[1]).toLong())
        assertEquals(199, storedIndexOfItem(group2, items2[199]).toLong())
    }

    @Test
    fun testIsVisualGroupPosition() {
        val group1 = Item(1)
        val group2 = Item(2)
        val group3 = Item(3)
        val group4 = Item(4)
        addOrUpdateAllItems(group1, createList(5))
        addOrUpdateAllItems(group2, createList(5))
        addOrUpdateAllItems(group3, createList(5))
        addOrUpdateAllItems(group4, createList(5))
        assertEquals(24, size().toLong())
        assertTrue(isVisualGroupPosition(0))
        assertFalse(isVisualGroupPosition(1))
        assertTrue(isVisualGroupPosition(6))
        assertFalse(isVisualGroupPosition(7))
        assertFalse(isVisualGroupPosition(10))
        assertTrue(isVisualGroupPosition(12))
        assertFalse(isVisualGroupPosition(13))
        assertTrue(isVisualGroupPosition(18))
        assertFalse(isVisualGroupPosition(23))
    }

    @Test
    fun testIsVisualEmptyPosition() {
        setDisplayEmptyCell()
        val group1 = Item(1)
        val group2 = Item(2)
        val group3 = Item(3)
        val group4 = Item(4)
        addOrUpdateAllItems(group1, createList(5))
        addOrUpdateAllItems(group4, createList(5))
        addOrUpdateGroup(group2)
        addOrUpdateGroup(group3)
        assertEquals(16, size().toLong())

        // group 1
        assertFalse(isVisualEmptyPosition(0))
        assertFalse(isVisualEmptyPosition(1))
        assertFalse(isVisualEmptyPosition(5))

        // group 2
        assertFalse(isVisualEmptyPosition(6))
        assertTrue(isVisualEmptyPosition(7))

        // group 3
        assertFalse(isVisualEmptyPosition(8))
        assertTrue(isVisualEmptyPosition(9))

        // group 4
        assertFalse(isVisualEmptyPosition(10))
        assertFalse(isVisualEmptyPosition(11))
        assertFalse(isVisualEmptyPosition(15))
    }

    @Test
    fun testGetGroupCount() {
        addOrUpdateGroup(Item())
        addOrUpdateGroup(Item())
        addOrUpdateGroup(Item())
        assertEquals(3, groupCount.toLong())
    }

    @Test
    fun testGetItemCount() {
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        assertEquals(200, getGroupItemCount(group1).toLong())
        assertEquals(200, getGroupItemCount(group2).toLong())
    }

    @Test
    fun testGetItemViewType() {
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        assertEquals(402, size().toLong())
        assertEquals(TYPE_HEADER.toLong(), getItemViewType(0).toLong())
        assertEquals(TYPE_HEADER.toLong(), getItemViewType(201).toLong())
        assertEquals(TYPE_ITEM.toLong(), getItemViewType(1).toLong())
        assertEquals(TYPE_ITEM.toLong(), getItemViewType(200).toLong())
        assertEquals(TYPE_ITEM.toLong(), getItemViewType(202).toLong())
        assertEquals(TYPE_ITEM.toLong(), getItemViewType(401).toLong())
    }

    @Test
    fun testGetItemVisualPosition() {
        val group1 = Item(1)
        val group2 = Item(2)
        val list1 = createList(200, 0)
        val list2 = createList(200, 200)
        addOrUpdateAllItems(group1, list1)
        addOrUpdateAllItems(group2, list2)
        assertEquals(1, getItemVisualPosition(list1[0].id.toLong()).toLong())
        assertEquals(200, getItemVisualPosition(list1[199].id.toLong()).toLong())
        assertEquals(202, getItemVisualPosition(list2[0].id.toLong()).toLong())
        assertEquals(401, getItemVisualPosition(list2[199].id.toLong()).toLong())
        assertEquals(-1, getItemVisualPosition(400).toLong())
    }

    // endregion
    // region Getters Children above Groups
    @Test
    fun testGetItemVisualPosition_childrenAboveGroups() {
        setChildrenAboveGroup()
        val group1 = Item(1)
        val group2 = Item(2)
        val list1 = createList(200, 0)
        val list2 = createList(200, 200)
        addOrUpdateAllItems(group1, list1)
        addOrUpdateAllItems(group2, list2)
        assertEquals(0, getItemVisualPosition(list1[0].id.toLong()).toLong())
        assertEquals(199, getItemVisualPosition(list1[199].id.toLong()).toLong())
        assertEquals(201, getItemVisualPosition(list2[0].id.toLong()).toLong())
        assertEquals(400, getItemVisualPosition(list2[199].id.toLong()).toLong())
        assertEquals(-1, getItemVisualPosition(400).toLong())
    }

    @Test
    fun testGetItem_childrenAboveGroups_visualPos() {
        setChildrenAboveGroup()
        val group1 = Item(1, 1000)
        val group2 = Item(2, 1001)
        addOrUpdateAllItems(group1, createList(5, 0))
        addOrUpdateAllItems(group2, createList(5, 5))
        assertEquals(12, size().toLong())

        // Group 1
        assertEquals(0, getItem(0)!!.id.toLong())
        assertEquals(1, getItem(1)!!.id.toLong())
        assertEquals(4, getItem(4)!!.id.toLong())
        assertNull(getItem(5))

        // Group 2
        assertEquals(5, getItem(6)!!.id.toLong())
        assertEquals(6, getItem(7)!!.id.toLong())
        assertNull(getItem(11))
    }

    @Test
    fun testIsVisualGroupPosition_childrenAboveGroups() {
        setChildrenAboveGroup()
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(5))
        addOrUpdateAllItems(group2, createList(5))
        assertEquals(12, size().toLong())
        assertFalse(isVisualGroupPosition(0))
        assertFalse(isVisualGroupPosition(4))
        assertTrue(isVisualGroupPosition(5))
        assertFalse(isVisualGroupPosition(6))
        assertFalse(isVisualGroupPosition(10))
        assertTrue(isVisualGroupPosition(11))
    }

    @Test
    fun testIsVisualEmptyPosition_childrenAboveGroups() {
        setChildrenAboveGroup()
        setDisplayEmptyCell()
        val group1 = Item(1)
        val group2 = Item(2)
        val group3 = Item(3)
        val group4 = Item(4)
        addOrUpdateAllItems(group1, createList(5))
        addOrUpdateAllItems(group4, createList(5))
        addOrUpdateGroup(group2)
        addOrUpdateGroup(group3)
        assertEquals(16, size().toLong())

        // group 1
        assertFalse(isVisualEmptyPosition(0))
        assertFalse(isVisualEmptyPosition(1))
        assertFalse(isVisualEmptyPosition(5))

        // group 2
        assertTrue(isVisualEmptyPosition(6))
        assertFalse(isVisualEmptyPosition(7))

        // group 3
        assertTrue(isVisualEmptyPosition(8))
        assertFalse(isVisualEmptyPosition(9))

        // group 4
        assertFalse(isVisualEmptyPosition(10))
        assertFalse(isVisualEmptyPosition(11))
        assertFalse(isVisualEmptyPosition(15))
    }

    @Test
    fun testGetItemViewType_childrenAboveGroups() {
        setChildrenAboveGroup()
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        assertEquals(402, size().toLong())
        assertEquals(TYPE_ITEM.toLong(), getItemViewType(0).toLong())
        assertEquals(TYPE_ITEM.toLong(), getItemViewType(199).toLong())
        assertEquals(TYPE_ITEM.toLong(), getItemViewType(201).toLong())
        assertEquals(TYPE_ITEM.toLong(), getItemViewType(400).toLong())
        assertEquals(TYPE_HEADER.toLong(), getItemViewType(200).toLong())
        assertEquals(TYPE_HEADER.toLong(), getItemViewType(401).toLong())
    }

    @Test
    fun testExpandGroup_notifyChildrenAboveGroups() {
        setNotExpandedByDefault()
        setChildrenAboveGroup()
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        additions.clear()
        assertEquals(2, size().toLong())
        val expected: MutableList<Pair<Int, Int>> = ArrayList()
        expected.add(Pair(0, 200))
        expected.add(Pair(201, 200))
        val expectedUpdates: MutableList<Pair<Int, Int>> = ArrayList()
        expectedUpdates.add(Pair(200, 1))
        expectedUpdates.add(Pair(401, 1))
        expandGroup(group1, true)
        expandGroup(group2, true)
        assertEquals(402, size().toLong())
        assertEquals(expected, additions)
        assertEquals(expectedUpdates, updates)
    }

    @Test
    fun testCollapseGroup_notifyChildrenAboveGroups() {
        setChildrenAboveGroup()
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        additions.clear()
        assertEquals(402, size().toLong())
        val expected: MutableList<Pair<Int, Int>> = ArrayList()
        expected.add(Pair(0, 200))
        expected.add(Pair(1, 200))
        val expectedUpdates: MutableList<Pair<Int, Int>> = ArrayList()
        expectedUpdates.add(Pair(200, 1))
        expectedUpdates.add(Pair(201, 1))
        collapseGroup(group1, true)
        collapseGroup(group2, true)
        assertEquals(2, size().toLong())
        assertEquals(expected, removals)
        assertEquals(expectedUpdates, updates)
    }

    // endregion
    // region Expandable/Collapse
    @Test
    fun testIsGroupExpanded() {
        setNotExpandedByDefault()
        val group1 = Item(1)
        addOrUpdateAllItems(group1, createList(200))
        assertFalse(isGroupExpanded(group1))
        expandGroup(group1, false)
        assertTrue(isGroupExpanded(group1))
    }

    @Test
    fun testExpandGroup() {
        setNotExpandedByDefault()
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        additions.clear()
        assertEquals(2, size().toLong())
        val expected: MutableList<Pair<Int, Int>> = ArrayList()
        expected.add(Pair(1, 200))
        expected.add(Pair(202, 200))
        expandGroup(group1, false)
        expandGroup(group2, false)
        assertEquals(402, size().toLong())
        assertEquals(expected, additions)
        assertEquals(0, updates.size.toLong())
    }

    @Test
    fun testExpandGroup_notify() {
        setNotExpandedByDefault()
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        additions.clear()
        assertEquals(2, size().toLong())
        val expected: MutableList<Pair<Int, Int>> = ArrayList()
        expected.add(Pair(1, 200))
        expected.add(Pair(202, 200))
        val expectedUpdates: MutableList<Pair<Int, Int>> = ArrayList()
        expectedUpdates.add(Pair(0, 1))
        expectedUpdates.add(Pair(201, 1))
        expandGroup(group1, true)
        expandGroup(group2, true)
        assertEquals(402, size().toLong())
        assertEquals(expected, additions)
        assertEquals(expectedUpdates, updates)
    }

    @Test
    fun testExpandAll() {
        setNotExpandedByDefault()
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        assertEquals(2, size().toLong())
        expandAll()
        assertEquals(402, size().toLong())
    }

    @Test
    fun testCollapseAll() {
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        assertEquals(402, size().toLong())
        collapseAll()
        assertEquals(2, size().toLong())
    }

    @Test
    fun testCollapseGroup() {
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        additions.clear()
        assertEquals(402, size().toLong())
        val expected: MutableList<Pair<Int, Int>> = ArrayList()
        expected.add(Pair(202, 200))
        expected.add(Pair(1, 200))
        collapseGroup(group2, false) // group 2 is first
        collapseGroup(group1, false)
        assertEquals(2, size().toLong())
        assertEquals(expected, removals)
        assertEquals(0, updates.size.toLong())
    }

    @Test
    fun testCollapseGroup_notify() {
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        additions.clear()
        assertEquals(402, size().toLong())
        val expected: MutableList<Pair<Int, Int>> = ArrayList()
        expected.add(Pair(1, 200))
        expected.add(Pair(2, 200))
        val expectedUpdates: MutableList<Pair<Int, Int>> = ArrayList()
        expectedUpdates.add(Pair(0, 1))
        expectedUpdates.add(Pair(1, 1))
        collapseGroup(group1, true)
        collapseGroup(group2, true)
        assertEquals(2, size().toLong())
        assertEquals(expected, removals)
        assertEquals(expectedUpdates, updates)
    }

    @Test
    fun testExpandCollapseGroup() {
        val group1 = Item(1)
        val group2 = Item(2)
        addOrUpdateAllItems(group1, createList(200))
        addOrUpdateAllItems(group2, createList(200))
        assertEquals(402, size().toLong())

        // Collapse groups
        expandCollapseGroup(group1)
        assertFalse(isGroupExpanded(group1))
        assertEquals(202, size().toLong())
        expandCollapseGroup(group2)
        assertFalse(isGroupExpanded(group2))
        assertEquals(2, size().toLong())

        // Expand again
        expandCollapseGroup(group1)
        assertTrue(isGroupExpanded(group1))
        assertEquals(202, size().toLong())
        expandCollapseGroup(group2)
        assertTrue(isGroupExpanded(group2))
        assertEquals(402, size().toLong())
    }

    // endregion
    // region remove
    @Test
    fun testRemoveGroup_noItems() {
        val item = Item()
        assertFalse(removeGroup(item))
        assertEquals(0, removals.size.toLong())
        addOrUpdateGroup(item)
        assertTrue(removeGroup(item))
        assertEquals(1, removals.size.toLong())
        assertTrue(removals.contains(Pair(0, 1)))
        assertEquals(0, size().toLong())
        assertFalse(removeGroup(item))
        assertEquals(1, removals.size.toLong())
    }

    @Test
    fun testRemoveGroup_oneItem() {
        val group = Item()
        val expectedRemoves: MutableList<Pair<Int, Int>> = ArrayList()
        expectedRemoves.add(Pair(1, 1))
        expectedRemoves.add(Pair(0, 1))
        addOrUpdateItem(group, Item())
        assertTrue(removeGroup(group))
        assertEquals(2, removals.size.toLong())
        assertEquals(expectedRemoves, removals)
        assertTrue(removals.contains(Pair(0, 1)))
        assertEquals(0, size().toLong())
        assertFalse(removeGroup(group))
        assertEquals(2, removals.size.toLong())
    }

    @Test
    fun testRemoveGroup_manyItems() {
        val group = Item()
        val expectedRemoves: MutableList<Pair<Int, Int>> = ArrayList()
        expectedRemoves.add(Pair(5, 1))
        expectedRemoves.add(Pair(4, 1))
        expectedRemoves.add(Pair(3, 1))
        expectedRemoves.add(Pair(2, 1))
        expectedRemoves.add(Pair(1, 1))
        expectedRemoves.add(Pair(0, 1))
        addOrUpdateItem(group, Item())
        addOrUpdateItem(group, Item())
        addOrUpdateItem(group, Item())
        addOrUpdateItem(group, Item())
        addOrUpdateItem(group, Item())
        assertTrue(removeGroup(group))
        assertEquals(6, removals.size.toLong())
        assertEquals(expectedRemoves, removals)
        assertEquals(0, size().toLong())
        assertFalse(removeGroup(group))
        assertEquals(6, removals.size.toLong())
    }

    @Test
    fun testRemoveGroup_manyItems_secondGroup() {
        val group = Item(3)
        val group2 = Item(5)
        val expectedRemoves: MutableList<Pair<Int, Int>> = ArrayList()
        expectedRemoves.add(Pair(11, 1))
        expectedRemoves.add(Pair(10, 1))
        expectedRemoves.add(Pair(9, 1))
        expectedRemoves.add(Pair(8, 1))
        expectedRemoves.add(Pair(7, 1))
        expectedRemoves.add(Pair(6, 1))
        for (i in 0..4) {
            addOrUpdateItem(group, Item())
            addOrUpdateItem(group2, Item())
        }
        assertTrue(removeGroup(group2))
        assertEquals(6, removals.size.toLong())
        assertEquals(expectedRemoves, removals)
        assertEquals(6, size().toLong())
        assertFalse(removeGroup(group2))
        assertEquals(6, removals.size.toLong())
    }

    @Test
    fun testRemoveItem() {
        val group1 = Item(1)
        val group2 = Item(2)
        val items1 = createList(200)
        val items2 = createList(200)
        addOrUpdateAllItems(group1, items1)
        addOrUpdateAllItems(group2, items2)
        assertEquals(402, size().toLong())
        assertTrue(removeItem(items1[0]))
        assertTrue(removeItem(items1[199]))
        assertTrue(removeItem(items2[0]))
        assertTrue(removeItem(items2[199]))
        val expectedRemoves: MutableList<Pair<Int, Int>> = ArrayList()
        expectedRemoves.add(Pair(1, 1))
        expectedRemoves.add(Pair(199, 1))
        expectedRemoves.add(Pair(200, 1))
        expectedRemoves.add(Pair(398, 1))
        assertEquals(expectedRemoves, removals)
    }

    @Test
    fun testRemoveItem_lastItemInGroup() {
        val group1 = Item(1)
        val group2 = Item(2)
        val removeItem = Item(3)
        addOrUpdateItem(group1, removeItem)
        addOrUpdateAllItems(group2, createList(200))
        assertEquals(203, size().toLong())
        val expectedRemoves: MutableList<Pair<Int, Int>> =
            ArrayList()
        expectedRemoves.add(Pair(1, 1))
        expectedRemoves.add(Pair(0, 1))
        assertTrue(removeItem(removeItem))
        assertFalse(removeItem(removeItem))
        assertEquals(expectedRemoves, removals)
    }

    // endregion
    // region updates
    @Test
    fun testGroupAdd_shouldUpdatePosition() {
        val group = Item(3)
        val group1 = Item(5)
        val group2 = Item(7)
        val expectedAdditions: MutableList<Pair<Int, Int>> = ArrayList()
        expectedAdditions.add(Pair(0, 1))
        expectedAdditions.add(Pair(1, 1))
        expectedAdditions.add(Pair(2, 1))
        assertEquals(addOrUpdateGroup(group).toLong(), 0)
        assertEquals(addOrUpdateGroup(group1).toLong(), 1)
        assertEquals(addOrUpdateGroup(group2).toLong(), 2)
        assertEquals(size().toLong(), 3)
        assertEquals(expectedAdditions, additions)

        // test updating position
        val expectedUpdates: MutableList<Pair<Int, Int>> = ArrayList()
        expectedUpdates.add(Pair(0, 1))
        val expectedMoves: MutableList<Pair<Int, Int>> = ArrayList()
        expectedMoves.add(Pair(0, 2))
        additions.clear()
        group.cmpField = 10
        assertEquals(0, addOrUpdateGroup(group).toLong())
        assertEquals(size().toLong(), 3)
        assertEquals(additions.size.toLong(), 0)
        assertEquals(expectedUpdates, updates)
        assertEquals(expectedMoves, moves)
    }

    @Test
    fun testGroupAdd_withItemsShouldUpdatePosition() {
        val group = Item(3)
        val group1 = Item(5)
        val group2 = Item(7)
        val expectedAdditions: MutableList<Pair<Int, Int>> = ArrayList()
        expectedAdditions.add(Pair(0, 1))
        expectedAdditions.add(Pair(1, 1))
        expectedAdditions.add(Pair(2, 1))
        assertEquals(addOrUpdateGroup(group).toLong(), 0)
        assertEquals(addOrUpdateGroup(group1).toLong(), 1)
        assertEquals(addOrUpdateGroup(group2).toLong(), 2)
        assertEquals(size().toLong(), 3)
        assertEquals(expectedAdditions, additions)

        // test updating position with items
        val expectedUpdates: MutableList<Pair<Int, Int>> = ArrayList()
        expectedUpdates.add(Pair(0, 1))
        for (i in 0..1) {
            addOrUpdateItem(group, Item())
            addOrUpdateItem(group1, Item())
            addOrUpdateItem(group2, Item())
        }
        assertEquals(9, size().toLong())
        val expectedMoves: MutableList<Pair<Int, Int>> = ArrayList()
        expectedMoves.add(Pair(0, 2))
        additions.clear()
        group.cmpField = 10
        assertEquals(0, addOrUpdateGroup(group).toLong())
        assertEquals(2, additions.size.toLong())
        // TODO check additions array is right
        // TODO assertEquals(expectedUpdates, mUpdates);
        assertEquals(expectedMoves, moves)
    }

    // endregion
    // region Duplicates
    @Test
    fun testAddDuplicateGroup() {
        val item = Item()
        val item2 = Item(item.cmpField, item.id)
        item2.data = item.data
        addOrUpdateGroup(item)
        assertEquals(0, addOrUpdateGroup(item2).toLong())
        assertEquals(1, size().toLong())
        assertEquals(1, additions.size.toLong())
        assertEquals(0, updates.size.toLong())
    }

    @Test
    fun testAddDuplicateItem() {
        val group = Item()
        val item = Item()
        val item2 = Item(item.cmpField, item.id)
        item2.data = item.data
        val expected: MutableList<Pair<Int, Int>> = ArrayList()
        expected.add(Pair(0, 1))
        expected.add(Pair(1, 1))
        val expectedUpdates: MutableList<Pair<Int, Int>> = ArrayList()
        expectedUpdates.add(Pair(1, 1))
        addOrUpdateItem(group, item)
        assertEquals(expected, additions)
        val group1 = Item(group.cmpField, group.id) // Assumes contents changed if exact same object is passed
        group1.data = group.data
        assertEquals(0, addOrUpdateItem(group1, item2).toLong())
        assertEquals(2, size().toLong())
        assertEquals(2, additions.size.toLong())
        // assertEquals(expectedUpdates, mUpdates); FIXME only when a different group object is passed
    }

    // endregion
    // region Helpers
    private fun createList(count: Int): List<Item> {
        val items: MutableList<Item> = ArrayList()
        for (i in 0 until count) {
            items.add(Item(i))
        }
        return items
    }

    private fun createList(count: Int, idStartPosition: Int): List<Item> {
        val items: MutableList<Item> = ArrayList()
        for (i in 0 until count) {
            items.add(Item(i, idStartPosition + i))
        }
        return items
    }

    // endregion
    // region Candy Wrappers
    private fun expandCollapseGroup(group: Item) = groupList.expandCollapseGroup(group)

    private fun isGroupExpanded(group: Item): Boolean = groupList.isGroupExpanded(group)

    private fun setNotExpandedByDefault() {
        groupList.isExpandedByDefault = false
    }

    private fun expandAll() = groupList.expandAll()

    private fun collapseAll() = groupList.collapseAll()

    private fun expandGroup(group: Item, isNotify: Boolean) = groupList.expandGroup(group, isNotify)

    private fun collapseGroup(group: Item, isNotify: Boolean) = groupList.collapseGroup(group, isNotify)

    private fun setDisplayEmptyCell() {
        groupList.isDisplayEmptyCell = true
    }

    private fun setChildrenAboveGroup() {
        groupList.isChildrenAboveGroupMap = true
    }

    private fun getItemViewType(position: Int): Int = groupList.getItemViewType(position)

    private val groupCount: Int get() = groupList.groupCount

    private fun getItemVisualPosition(itemId: Long): Int = groupList.getItemVisualPosition(itemId)

    private fun getGroupItemCount(group: Item): Int = groupList.getGroupItemCount(group)

    private fun getGroupVisualPosition(visualPosition: Int): Int = groupList.getGroupVisualPosition(visualPosition)

    private fun storedIndexOfItem(group: Item, item: Item): Int = groupList.storedIndexOfItem(group, item)

    private fun getItem(visualPosition: Int): Item? = groupList.getItem(visualPosition)

    private fun getItem(group: Item, itemStoredPosition: Int): Item? = groupList.getItem(group, itemStoredPosition)

    private fun isVisualGroupPosition(visualPosition: Int): Boolean = groupList.isVisualGroupPosition(visualPosition)

    private fun isVisualEmptyPosition(position: Int): Boolean = groupList.isVisualEmptyItemPosition(position)

    private fun addOrUpdateAllItems(group: Item, items: List<Item>) = groupList.addOrUpdateAllItems(group, items)

    private fun size(): Int = groupList.size()

    private fun addOrUpdateGroup(item: Item): Int = groupList.addOrUpdateGroup(item)

    private fun addOrUpdateItem(group: Item, item: Item): Int = groupList.addOrUpdateItem(group, item)

    private fun removeGroup(item: Item): Boolean = groupList.removeGroup(item)

    private fun removeItem(item: Item): Boolean = groupList.removeItem(item)
    // endregion

    companion object {
        private const val TYPE_HEADER = 100
        private const val TYPE_ITEM = 101
    }
}
