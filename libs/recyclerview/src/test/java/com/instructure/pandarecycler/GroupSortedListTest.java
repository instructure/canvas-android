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

package com.instructure.pandarecycler;

import com.instructure.pandarecycler.util.GroupSortedList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class GroupSortedListTest extends Assert {

    private static final int TYPE_HEADER = 100;
    private static final int TYPE_ITEM = 101;

    private GroupSortedList<Item, Item> mGroupList;
    private GroupSortedList.VisualArrayCallback mVisualArrayCallback;
    private GroupSortedList.GroupComparatorCallback<Item> mGroupCallback;
    private GroupSortedList.ItemComparatorCallback<Item, Item> mItemCallback;

    List<Pair> mAdditions = new ArrayList<Pair>();
    List<Pair> mRemovals = new ArrayList<Pair>();
    List<Pair> mMoves = new ArrayList<Pair>();
    List<Pair> mUpdates = new ArrayList<Pair>();

    @Before
    public void setUp() throws Exception {
        mVisualArrayCallback = new GroupSortedList.VisualArrayCallback() {
            @Override
            public void onInserted(int position, int count) {
                mAdditions.add(new Pair(position, count));
            }

            @Override
            public void onRemoved(int position, int count) {
                mRemovals.add(new Pair(position, count));
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                mMoves.add(new Pair(fromPosition, toPosition));
            }

            @Override
            public void onChanged(int position, int count) {
                mUpdates.add(new Pair(position, count));
            }
        };

        mGroupCallback = new GroupSortedList.GroupComparatorCallback<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.cmpField < o2.cmpField ? -1 : (o1.cmpField == o2.cmpField ? 0 : 1);
            }

            @Override
            public boolean areContentsTheSame(Item oldGroup, Item newGroup) {
                return oldGroup.cmpField == newGroup.cmpField && oldGroup.data == newGroup.data;
            }

            @Override
            public boolean areItemsTheSame(Item group1, Item group2) {
                return group1.id == group2.id;
            }

            @Override
            public int getGroupType(Item group) {
                return TYPE_HEADER;
            }

            @Override
            public long getUniqueGroupId(Item group) {
                return group.getId();
            }
        };

        mItemCallback = new GroupSortedList.ItemComparatorCallback<Item, Item>() {
            @Override
            public int compare(Item group, Item o1, Item o2) {
                return o1.cmpField < o2.cmpField ? -1 : (o1.cmpField == o2.cmpField ? 0 : 1);
            }

            @Override
            public boolean areContentsTheSame(Item oldItem, Item newItem) {
                return oldItem.cmpField == newItem.cmpField && oldItem.data == newItem.data;
            }

            @Override
            public boolean areItemsTheSame(Item item1, Item item2) {
                return item1.id == item2.id;
            }

            @Override
            public int getChildType(Item group, Item item) {
                return TYPE_ITEM;
            }

            @Override
            public long getUniqueItemId(Item item) {
                return item.getId();
            }
        };

        mGroupList = new GroupSortedList(Item.class, Item.class, mVisualArrayCallback, mGroupCallback, mItemCallback);
        mGroupList.setExpandedByDefault(true);
    }

    public void testEmpty() {
        assertEquals("empty", mGroupList.size(), 0);
    }


    // region add
    @Test
    public void testGroupAdd() {
        Item item = new Item();
        assertEquals(addOrUpdateGroup(item), 0);
        assertEquals(size(), 1);
        assertTrue(mAdditions.contains(new Pair(0, 1)));
        Item item2 = new Item();
        item2.cmpField = item.cmpField + 1;
        assertEquals(addOrUpdateGroup(item2), 1);
        assertEquals(size(), 2);
        assertTrue(mAdditions.contains(new Pair(1, 1)));
        Item item3 = new Item();
        item3.cmpField = item.cmpField - 1;
        mAdditions.clear();
        assertEquals(addOrUpdateGroup(item3), 0);
        assertEquals(size(), 3);
        assertTrue(mAdditions.contains(new Pair(0, 1)));
    }

    @Test
    public void testItemAdd_ToGroup1() {
        Item group = new Item();
        Item item = new Item();
        List<Pair> expected = new ArrayList<>();
        expected.add(new Pair(0, 1));
        expected.add(new Pair(1, 1));
        assertEquals(addOrUpdateItem(group, item), 0);
        assertEquals(size(), 2);
        assertEquals(expected, mAdditions);


        Item item2 = new Item();
        item2.cmpField = item.cmpField + 1;
        expected.add(new Pair(2, 1));
        assertEquals(addOrUpdateItem(group, item2), 1);
        assertEquals(size(), 3);
        assertEquals(expected, mAdditions);

        Item item3 = new Item();
        item3.cmpField = item.cmpField - 1;
        expected.clear();
        expected.add(new Pair(1, 1));
        mAdditions.clear();
        assertEquals(addOrUpdateItem(group, item3), 0);
        assertEquals(size(), 4);
        assertEquals(expected, mAdditions);
    }

    @Test
    public void testItemAdd_endGroup() {
        Item group = new Item(3);
        Item item = new Item();
        List<Pair> expected = new ArrayList<>();
        expected.add(new Pair(0, 1));
        expected.add(new Pair(1, 1));
        assertEquals(addOrUpdateItem(group, item), 0);
        assertEquals(size(), 2);
        assertEquals(expected, mAdditions);


        Item item2 = new Item();
        item2.cmpField = item.cmpField + 1;
        expected.add(new Pair(2, 1));
        assertEquals(addOrUpdateItem(group, item2), 1);
        assertEquals(size(), 3);
        assertEquals(expected, mAdditions);

        Item item3 = new Item();
        item3.cmpField = item.cmpField - 1;
        expected.clear();
        expected.add(new Pair(1, 1));
        mAdditions.clear();
        assertEquals(addOrUpdateItem(group, item3), 0);
        assertEquals(size(), 4);
        assertEquals(expected, mAdditions);
        mAdditions.clear();

        // group 2
        Item group1 = new Item(5);
        item = new Item();
        expected = new ArrayList<>();
        expected.add(new Pair(4, 1));
        expected.add(new Pair(5, 1));
        assertEquals(addOrUpdateItem(group1, item), 0);
        assertEquals(size(), 6);
        assertEquals(expected, mAdditions);


        item2 = new Item();
        item2.cmpField = item.cmpField + 1;
        expected.add(new Pair(6, 1));
        assertEquals(addOrUpdateItem(group1, item2), 1);
        assertEquals(size(), 7);
        assertEquals(expected, mAdditions);

        item3 = new Item();
        item3.cmpField = item.cmpField - 1;
        expected.clear();
        expected.add(new Pair(5, 1));
        mAdditions.clear();
        assertEquals(addOrUpdateItem(group1, item3), 0);
        assertEquals(size(), 8);
        assertEquals(expected, mAdditions);
    }

    @Test
    public void testAddAllItems() {
        Item group = new Item(5);
        addOrUpdateAllItems(group, createList(0));
        assertEquals(0, size());
        addOrUpdateAllItems(group, createList(5));
        assertEquals(6, size());
    }


    // endregion

    // region getters
    @Test
    public void testGetItem_visualPos() {
        Item group1 = new Item(1000, 1);
        Item group2 = new Item(1001, 2);

        addOrUpdateAllItems(group1, createList(5, 0));
        addOrUpdateAllItems(group2, createList(5, 5));
        assertEquals(12, size());

        // Group 1
        assertEquals(0, getItem(1).id);
        assertEquals(1, getItem(2).id);
        assertEquals(4, getItem(5).id);

        // Group 2
        assertEquals(5, getItem(7).id);
        assertEquals(6, getItem(8).id);
        assertEquals(9, getItem(11).id);
    }

    @Test
    public void testGetGroup_visualPosition() {
        Item group1 = new Item(1000, 1);
        Item group2 = new Item(1001, 2);
        addOrUpdateAllItems(group1, createList(5, 0));
        addOrUpdateAllItems(group2, createList(5, 5));

        assertEquals(0, getGroupVisualPostion(0));
        assertEquals(0, getGroupVisualPostion(1));
        assertEquals(0, getGroupVisualPostion(2));
        assertEquals(0, getGroupVisualPostion(3));
        assertEquals(0, getGroupVisualPostion(4));
        assertEquals(0, getGroupVisualPostion(5));

        assertEquals(6, getGroupVisualPostion(6));
        assertEquals(6, getGroupVisualPostion(7));
        assertEquals(6, getGroupVisualPostion(8));
        assertEquals(6, getGroupVisualPostion(9));
        assertEquals(6, getGroupVisualPostion(10));
        assertEquals(6, getGroupVisualPostion(11));
    }

    public void testGetItem_visualPosException() {
        Item group1 = new Item(1000, 1);
        Item group2 = new Item(1001, 2);

        addOrUpdateAllItems(group1, createList(200, 0));
        addOrUpdateAllItems(group2, createList(200, 200));
        assertEquals(402, size());
        assertNull(getItem(201));
    }


    @Test
    public void testGetItem_visualPosBigNumbers() {
        Item group1 = new Item(1000, 1);
        Item group2 = new Item(1001, 2);

        addOrUpdateAllItems(group1, createList(200, 0));
        addOrUpdateAllItems(group2, createList(200, 200));
        assertEquals(402, size());

        // Group 1
        assertEquals(0, getItem(1).id);
        assertEquals(1, getItem(2).id);
        assertEquals(199, getItem(200).id);

        // Group 2
        assertEquals(200, getItem(202).id);
        assertEquals(201, getItem(203).id);
        assertEquals(399, getItem(401).id);
    }

    @Test
    public void testGetItem_groupStoredPos() {
        Item group1 = new Item(1000, 1);
        Item group2 = new Item(1001, 2);

        addOrUpdateAllItems(group1, createList(200, 0));
        addOrUpdateAllItems(group2, createList(200, 200));
        assertEquals(402, size());

        // Group 1
        assertEquals(0, getItem(group1, 0).id);
        assertEquals(1, getItem(group1, 1).id);
        assertEquals(199, getItem(group1, 199).id);

        // Group 2
        assertEquals(200, getItem(group2, 0).id);
        assertEquals(201, getItem(group2, 1).id);
        assertEquals(399, getItem(group2, 199).id);
    }

    @Test
    public void testStoredIndexOfItem() {
        Item group1 = new Item(1000, 1);
        Item group2 = new Item(1001, 2);

        List<Item> items1 = createList(200, 0);
        List<Item> items2 = createList(200, 200);
        addOrUpdateAllItems(group1, items1);
        addOrUpdateAllItems(group2, items2);
        assertEquals(402, size());

        // Group 1
        assertEquals(0, storedIndexOfItem(group1, items1.get(0)));
        assertEquals(1, storedIndexOfItem(group1, items1.get(1)));
        assertEquals(199, storedIndexOfItem(group1, items1.get(199)));

        // Group 2
        assertEquals(0, storedIndexOfItem(group2, items2.get(0)));
        assertEquals(1, storedIndexOfItem(group2, items2.get(1)));
        assertEquals(199, storedIndexOfItem(group2, items2.get(199)));
    }

    @Test
    public void testIsVisualGroupPosition() {
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        Item group3 = new Item(3);
        Item group4 = new Item(4);

        addOrUpdateAllItems(group1, createList(5));
        addOrUpdateAllItems(group2, createList(5));
        addOrUpdateAllItems(group3, createList(5));
        addOrUpdateAllItems(group4, createList(5));
        assertEquals(24, size());

        assertTrue(isVisualGroupPosition(0));
        assertFalse(isVisualGroupPosition(1));

        assertTrue(isVisualGroupPosition(6));
        assertFalse(isVisualGroupPosition(7));
        assertFalse(isVisualGroupPosition(10));

        assertTrue(isVisualGroupPosition(12));
        assertFalse(isVisualGroupPosition(13));

        assertTrue(isVisualGroupPosition(18));
        assertFalse(isVisualGroupPosition(23));
    }

    @Test
    public void testIsVisualEmptyPosition() {
        setDisplayEmtpyCell(true);
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        Item group3 = new Item(3);
        Item group4 = new Item(4);

        addOrUpdateAllItems(group1, createList(5));
        addOrUpdateAllItems(group4, createList(5));
        addOrUpdateGroup(group2);
        addOrUpdateGroup(group3);

        assertEquals(16, size());

        // group 1
        assertFalse(isVisualEmptyPosition(0));
        assertFalse(isVisualEmptyPosition(1));
        assertFalse(isVisualEmptyPosition(5));

        // group 2
        assertFalse(isVisualEmptyPosition(6));
        assertTrue(isVisualEmptyPosition(7));

        // group 3
        assertFalse(isVisualEmptyPosition(8));
        assertTrue(isVisualEmptyPosition(9));

        // group 4
        assertFalse(isVisualEmptyPosition(10));
        assertFalse(isVisualEmptyPosition(11));
        assertFalse(isVisualEmptyPosition(15));
    }

    @Test
    public void testGetGroupCount() {
        addOrUpdateGroup(new Item());
        addOrUpdateGroup(new Item());
        addOrUpdateGroup(new Item());

        assertEquals(3, getGroupCount());
    }

    @Test
    public void testGetItemCount() {
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));
        assertEquals(200,getGroupItemCount(group1));
        assertEquals(200, getGroupItemCount(group2));
    }

    @Test
    public void testGetItemViewType() {
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));

        assertEquals(402, size());

        assertEquals(TYPE_HEADER, getItemViewType(0));
        assertEquals(TYPE_HEADER, getItemViewType(201));

        assertEquals(TYPE_ITEM, getItemViewType(1));
        assertEquals(TYPE_ITEM, getItemViewType(200));

        assertEquals(TYPE_ITEM, getItemViewType(202));
        assertEquals(TYPE_ITEM, getItemViewType(401));
    }

    @Test
    public void testGetItemVisualPosition() {
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        List<Item> list1 = createList(200, 0);
        List<Item> list2 = createList(200, 200);
        addOrUpdateAllItems(group1, list1);
        addOrUpdateAllItems(group2, list2);

        assertEquals(1, getItemVisualPosition(list1.get(0).id));
        assertEquals(200, getItemVisualPosition(list1.get(199).id));
        assertEquals(202, getItemVisualPosition(list2.get(0).id));
        assertEquals(401, getItemVisualPosition(list2.get(199).id));

        assertEquals(-1, getItemVisualPosition(400));
    }


    // endregion

    // region Getters Children above Groups
    @Test
    public void testGetItemVisualPosition_childrenAboveGroups() {
        setChildrenAboveGroup(true);
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        List<Item> list1 = createList(200, 0);
        List<Item> list2 = createList(200, 200);
        addOrUpdateAllItems(group1, list1);
        addOrUpdateAllItems(group2, list2);

        assertEquals(0, getItemVisualPosition(list1.get(0).id));
        assertEquals(199, getItemVisualPosition(list1.get(199).id));
        assertEquals(201, getItemVisualPosition(list2.get(0).id));
        assertEquals(400, getItemVisualPosition(list2.get(199).id));

        assertEquals(-1, getItemVisualPosition(400));
    }

    @Test
    public void testGetItem_childrenAboveGroups_visualPos() {
        setChildrenAboveGroup(true);

        Item group1 = new Item(1000, 1);
        Item group2 = new Item(1001, 2);

        addOrUpdateAllItems(group1, createList(5, 0));
        addOrUpdateAllItems(group2, createList(5, 5));
        assertEquals(12, size());

        // Group 1
        assertEquals(0, getItem(0).id);
        assertEquals(1, getItem(1).id);
        assertEquals(4, getItem(4).id);
        assertNull(getItem(5));

        // Group 2
        assertEquals(5, getItem(6).id);
        assertEquals(6, getItem(7).id);
        assertNull(getItem(11));
    }

    @Test
    public void testIsVisualGroupPosition_childrenAboveGroups() {
        setChildrenAboveGroup(true);

        Item group1 = new Item(1);
        Item group2 = new Item(2);

        addOrUpdateAllItems(group1, createList(5));
        addOrUpdateAllItems(group2, createList(5));
        assertEquals(12, size());

        assertFalse(isVisualGroupPosition(0));
        assertFalse(isVisualGroupPosition(4));
        assertTrue(isVisualGroupPosition(5));

        assertFalse(isVisualGroupPosition(6));
        assertFalse(isVisualGroupPosition(10));
        assertTrue(isVisualGroupPosition(11));
    }

    @Test
    public void testIsVisualEmptyPosition_childrenAboveGroups() {
        setChildrenAboveGroup(true);
        setDisplayEmtpyCell(true);
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        Item group3 = new Item(3);
        Item group4 = new Item(4);

        addOrUpdateAllItems(group1, createList(5));
        addOrUpdateAllItems(group4, createList(5));
        addOrUpdateGroup(group2);
        addOrUpdateGroup(group3);

        assertEquals(16, size());

        // group 1
        assertFalse(isVisualEmptyPosition(0));
        assertFalse(isVisualEmptyPosition(1));
        assertFalse(isVisualEmptyPosition(5));

        // group 2
        assertTrue(isVisualEmptyPosition(6));
        assertFalse(isVisualEmptyPosition(7));

        // group 3
        assertTrue(isVisualEmptyPosition(8));
        assertFalse(isVisualEmptyPosition(9));

        // group 4
        assertFalse(isVisualEmptyPosition(10));
        assertFalse(isVisualEmptyPosition(11));
        assertFalse(isVisualEmptyPosition(15));
    }

    @Test
    public void testGetItemViewType_childrenAboveGroups() {
        setChildrenAboveGroup(true);
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));

        assertEquals(402, size());

        assertEquals(TYPE_ITEM, getItemViewType(0));
        assertEquals(TYPE_ITEM, getItemViewType(199));
        assertEquals(TYPE_ITEM, getItemViewType(201));
        assertEquals(TYPE_ITEM, getItemViewType(400));

        assertEquals(TYPE_HEADER, getItemViewType(200));
        assertEquals(TYPE_HEADER, getItemViewType(401));
    }

    @Test
    public void testExpandGroup_notifyChildrenAboveGroups() {
        setExpandedByDefault(false);
        setChildrenAboveGroup(true);

        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));
        mAdditions.clear();
        assertEquals(2, size());
        List<Pair> expected = new ArrayList<>();
        expected.add(new Pair(0, 200));
        expected.add(new Pair(201, 200));

        List<Pair> expectedUpdates = new ArrayList<>();
        expectedUpdates.add(new Pair(200, 1));
        expectedUpdates.add(new Pair(401, 1));

        expandGroup(group1, true);
        expandGroup(group2, true);
        assertEquals(402, size());
        assertEquals(expected, mAdditions);
        assertEquals(expectedUpdates, mUpdates);
    }

    @Test
    public void testCollapseGroup_notifyChildrenAboveGroups() {
        setChildrenAboveGroup(true);
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));
        mAdditions.clear();
        assertEquals(402, size());
        List<Pair> expected = new ArrayList<>();
        expected.add(new Pair(0, 200));
        expected.add(new Pair(1, 200));

        List<Pair> expectedUpdates = new ArrayList<>();
        expectedUpdates.add(new Pair(200, 1));
        expectedUpdates.add(new Pair(201, 1));

        collapseGroup(group1, true);
        collapseGroup(group2, true);
        assertEquals(2, size());
        assertEquals(expected, mRemovals);
        assertEquals(expectedUpdates, mUpdates);
    }

    // endregion

    // region Expandable/Collapse
    @Test
    public void testIsGroupExpanded() {
        setExpandedByDefault(false);
        Item group1 = new Item(1);
        addOrUpdateAllItems(group1, createList(200));

        assertFalse(isGroupExpanded(group1));
        expandGroup(group1, false);
        assertTrue(isGroupExpanded(group1));
    }

    @Test
    public void testExpandGroup() {
        setExpandedByDefault(false);

        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));
        mAdditions.clear();
        assertEquals(2, size());
        List<Pair> expected = new ArrayList<>();
        expected.add(new Pair(1, 200));
        expected.add(new Pair(202, 200));


        expandGroup(group1, false);
        expandGroup(group2, false);
        assertEquals(402, size());
        assertEquals(expected, mAdditions);
        assertEquals(0, mUpdates.size());
    }

    @Test
    public void testExpandGroup_notify() {
        setExpandedByDefault(false);

        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));
        mAdditions.clear();
        assertEquals(2, size());
        List<Pair> expected = new ArrayList<>();
        expected.add(new Pair(1, 200));
        expected.add(new Pair(202, 200));

        List<Pair> expectedUpdates = new ArrayList<>();
        expectedUpdates.add(new Pair(0, 1));
        expectedUpdates.add(new Pair(201, 1));

        expandGroup(group1, true);
        expandGroup(group2, true);
        assertEquals(402, size());
        assertEquals(expected, mAdditions);
        assertEquals(expectedUpdates, mUpdates);
    }

    @Test
    public void testExpandAll() {
        setExpandedByDefault(false);
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));
        assertEquals(2, size());
        expandAll();
        assertEquals(402, size());
    }

    @Test
    public void testCollapseAll() {
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));
        assertEquals(402, size());
        collapseAll();
        assertEquals(2, size());
    }

    @Test
    public void testCollapseGroup() {
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));
        mAdditions.clear();
        assertEquals(402, size());
        List<Pair> expected = new ArrayList<>();
        expected.add(new Pair(202, 200));
        expected.add(new Pair(1, 200));

        collapseGroup(group2, false); // group 2 is first
        collapseGroup(group1, false);

        assertEquals(2, size());
        assertEquals(expected, mRemovals);
        assertEquals(0, mUpdates.size());

    }

    @Test
    public void testCollapseGroup_notify() {
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));
        mAdditions.clear();
        assertEquals(402, size());
        List<Pair> expected = new ArrayList<>();
        expected.add(new Pair(1, 200));
        expected.add(new Pair(2, 200));

        List<Pair> expectedUpdates = new ArrayList<>();
        expectedUpdates.add(new Pair(0, 1));
        expectedUpdates.add(new Pair(1, 1));

        collapseGroup(group1, true);
        collapseGroup(group2, true);
        assertEquals(2, size());
        assertEquals(expected, mRemovals);
        assertEquals(expectedUpdates, mUpdates);
    }

    @Test
    public void testExpandCollapseGroup() {
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        addOrUpdateAllItems(group1, createList(200));
        addOrUpdateAllItems(group2, createList(200));
        assertEquals(402, size());

        // Collapse groups
        expandCollapseGroup(group1);
        assertFalse(isGroupExpanded(group1));
        assertEquals(202, size());
        expandCollapseGroup(group2);
        assertFalse(isGroupExpanded(group2));
        assertEquals(2, size());

        // Expand again
        expandCollapseGroup(group1);
        assertTrue(isGroupExpanded(group1));
        assertEquals(202, size());
        expandCollapseGroup(group2);
        assertTrue(isGroupExpanded(group2));
        assertEquals(402, size());


    }

    // endregion

    // region remove

    @Test
    public void testRemoveGroup_noItems() {
        Item item = new Item();
        assertFalse(removeGroup(item));
        assertEquals(0, mRemovals.size());
        addOrUpdateGroup(item);
        assertTrue(removeGroup(item));
        assertEquals(1, mRemovals.size());
        assertTrue(mRemovals.contains(new Pair(0, 1)));
        assertEquals(0, size());
        assertFalse(removeGroup(item));
        assertEquals(1, mRemovals.size());
    }

    @Test
    public void testRemoveGroup_oneItem() {
        Item group = new Item();
        List<Pair> expectedRemoves = new ArrayList<>();
        expectedRemoves.add(new Pair(1, 1));
        expectedRemoves.add(new Pair(0, 1));

        addOrUpdateItem(group, new Item());
        assertTrue(removeGroup(group));
        assertEquals(2, mRemovals.size());
        assertEquals(expectedRemoves, mRemovals);
        assertTrue(mRemovals.contains(new Pair(0, 1)));
        assertEquals(0, size());
        assertFalse(removeGroup(group));
        assertEquals(2, mRemovals.size());
    }

    @Test
    public void testRemoveGroup_manyItems() {
        Item group = new Item();
        List<Pair> expectedRemoves = new ArrayList<>();
        expectedRemoves.add(new Pair(5, 1));
        expectedRemoves.add(new Pair(4, 1));
        expectedRemoves.add(new Pair(3, 1));
        expectedRemoves.add(new Pair(2, 1));
        expectedRemoves.add(new Pair(1, 1));
        expectedRemoves.add(new Pair(0, 1));

        addOrUpdateItem(group, new Item());
        addOrUpdateItem(group, new Item());
        addOrUpdateItem(group, new Item());
        addOrUpdateItem(group, new Item());
        addOrUpdateItem(group, new Item());
        assertTrue(removeGroup(group));
        assertEquals(6, mRemovals.size());
        assertEquals(expectedRemoves, mRemovals);
        assertEquals(0, size());
        assertFalse(removeGroup(group));
        assertEquals(6, mRemovals.size());
    }

    @Test
    public void testRemoveGroup_manyItems_secondGroup() {
        Item group = new Item(3);
        Item group2 = new Item(5);
        List<Pair> expectedRemoves = new ArrayList<>();
        expectedRemoves.add(new Pair(11, 1));
        expectedRemoves.add(new Pair(10, 1));
        expectedRemoves.add(new Pair(9, 1));
        expectedRemoves.add(new Pair(8, 1));
        expectedRemoves.add(new Pair(7, 1));
        expectedRemoves.add(new Pair(6, 1));
        for (int i = 0; i < 5; i++) {
            addOrUpdateItem(group, new Item());
            addOrUpdateItem(group2, new Item());
        }
        assertTrue(removeGroup(group2));
        assertEquals(6, mRemovals.size());
        assertEquals(expectedRemoves, mRemovals);
        assertEquals(6, size());
        assertFalse(removeGroup(group2));
        assertEquals(6, mRemovals.size());
    }

    @Test
    public void testRemoveItem() {
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        List<Item> items1 = createList(200);
        List<Item> items2 = createList(200);
        addOrUpdateAllItems(group1, items1);
        addOrUpdateAllItems(group2, items2);
        assertEquals(402, size());

        assertTrue(removeItem(items1.get(0)));
        assertTrue(removeItem(items1.get(199)));

        assertTrue(removeItem(items2.get(0)));
        assertTrue(removeItem(items2.get(199)));
        List<Pair> expectedRemoves = new ArrayList<>();
        expectedRemoves.add(new Pair(1, 1));
        expectedRemoves.add(new Pair(199, 1));
        expectedRemoves.add(new Pair(200, 1));
        expectedRemoves.add(new Pair(398, 1));

        assertEquals(expectedRemoves, mRemovals);
    }

    @Test
    public void testRemoveItem_lastItemInGroup() {
        Item group1 = new Item(1);
        Item group2 = new Item(2);
        Item removeItem = new Item(3);
        addOrUpdateItem(group1, removeItem);
        addOrUpdateAllItems(group2, createList(200));

        assertEquals(203, size());

        List<Pair> expectedRemoves = new ArrayList<>();
        expectedRemoves.add(new Pair(1, 1));
        expectedRemoves.add(new Pair(0, 1));

        assertTrue(removeItem(removeItem));
        assertFalse(removeItem(removeItem));

        assertEquals(expectedRemoves, mRemovals);
    }


        // endregion

    // region updates
    @Test
    public void testGroupAdd_shouldUpdatePosition() {
        Item group = new Item(3);
        Item group1 = new Item(5);
        Item group2 = new Item(7);
        List<Pair> expectedAdditions = new ArrayList<>();
        expectedAdditions.add(new Pair(0, 1));
        expectedAdditions.add(new Pair(1, 1));
        expectedAdditions.add(new Pair(2, 1));

        assertEquals(addOrUpdateGroup(group), 0);
        assertEquals(addOrUpdateGroup(group1), 1);
        assertEquals(addOrUpdateGroup(group2), 2);
        assertEquals(size(), 3);
        assertEquals(expectedAdditions, mAdditions);

        // test updating position
        List<Pair> expectedUpdates = new ArrayList<>();
        expectedUpdates.add(new Pair(0, 1));

        List<Pair> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Pair(0, 2));
        mAdditions.clear();
        group.cmpField = 10;
        assertEquals(0, addOrUpdateGroup(group));
        assertEquals(size(), 3);
        assertEquals(mAdditions.size(), 0);
        assertEquals(expectedUpdates, mUpdates);
        assertEquals(expectedMoves, mMoves);
    }

    @Test
    public void testGroupAdd_withItemsShouldUpdatePosition() {
        Item group = new Item(3);
        Item group1 = new Item(5);
        Item group2 = new Item(7);
        List<Pair> expectedAdditions = new ArrayList<>();
        expectedAdditions.add(new Pair(0, 1));
        expectedAdditions.add(new Pair(1, 1));
        expectedAdditions.add(new Pair(2, 1));

        assertEquals(addOrUpdateGroup(group), 0);
        assertEquals(addOrUpdateGroup(group1), 1);
        assertEquals(addOrUpdateGroup(group2), 2);
        assertEquals(size(), 3);
        assertEquals(expectedAdditions, mAdditions);

        // test updating position with items
        List<Pair> expectedUpdates = new ArrayList<>();
        expectedUpdates.add(new Pair(0, 1));
        for (int i = 0; i < 2; i++) {
            addOrUpdateItem(group, new Item());
            addOrUpdateItem(group1, new Item());
            addOrUpdateItem(group2, new Item());
        }
        assertEquals(9, size());

        List<Pair> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Pair(0, 2));
        mAdditions.clear();
        group.cmpField = 10;
        assertEquals(0, addOrUpdateGroup(group));
        assertEquals(2,mAdditions.size());
        // TODO check additions array is right
        // TODO assertEquals(expectedUpdates, mUpdates);
        assertEquals(expectedMoves, mMoves);

    }

    // endregion

    // region Duplicates

    @Test
    public void testAddDuplicateGroup() {
        Item item = new Item();
        Item item2 = new Item(item.id, item.cmpField);
        item2.data = item.data;
        addOrUpdateGroup(item);
        assertEquals(0, addOrUpdateGroup(item2));
        assertEquals(1, size());
        assertEquals(1, mAdditions.size());
        assertEquals(0, mUpdates.size());
    }

    @Test
    public void testAddDuplicateItem() {
        Item group = new Item();
        Item item = new Item();
        Item item2 = new Item(item.id, item.cmpField);
        item2.data = item.data;

        List<Pair> expected = new ArrayList<>();
        expected.add(new Pair(0, 1));
        expected.add(new Pair(1, 1));
        List<Pair> expectedUpdates = new ArrayList<>();
        expectedUpdates.add(new Pair(1, 1));

        addOrUpdateItem(group, item);
        assertEquals(expected, mAdditions);
        Item group1 = new Item(group.id, group.cmpField); // Assumes contents changed if exact same object is passed
        group1.data = group.data;
        assertEquals(0, addOrUpdateItem(group1, item2));
        assertEquals(2, size());
        assertEquals(2, mAdditions.size());
        // assertEquals(expectedUpdates, mUpdates); FIXME only when a different group object is passed
    }

    // endregion

    // region Helpers
    private List<Item> createList(int count) {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            items.add(new Item(i));
        }
        return items;
    }

    private List<Item> createList(int count, int idStartPosition) {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            items.add(new Item(idStartPosition + i, i));
        }
        return items;
    }

    // endregion

    // region Candy Wrappers
    private void expandCollapseGroup(Item group) {
        mGroupList.expandCollapseGroup(group);
    }

    private boolean isGroupExpanded(Item group) {
        return mGroupList.isGroupExpanded(group);
    }

    private void setExpandedByDefault(boolean isExpandedByDefault) {
        mGroupList.setExpandedByDefault(isExpandedByDefault);
    }

    private void expandAll() {
        mGroupList.expandAll();
    }

    private void collapseAll() {
        mGroupList.collapseAll();
    }

    private void expandGroup(Item group, boolean isNotify) {
        mGroupList.expandGroup(group, isNotify);
    }

    private void collapseGroup(Item group, boolean isNotify) {
        mGroupList.collapseGroup(group, isNotify);
    }

    private void setDisplayEmtpyCell(boolean isDisplayEmtpyCell) {
        mGroupList.setDisplayEmptyCell(isDisplayEmtpyCell);
    }

    private void setChildrenAboveGroup(boolean isChildrenAboveGroup) {
        mGroupList.setChildrenAboveGroup(isChildrenAboveGroup);
    }

    private int getItemViewType(int position) {
        return mGroupList.getItemViewType(position);
    }

    private int getGroupCount() {
        return mGroupList.getGroupCount();
    }

    private int getItemVisualPosition(long itemId) {
        return mGroupList.getItemVisualPosition(itemId);
    }

    private int getGroupItemCount(Item group) {
        return mGroupList.getGroupItemCount(group);
    }

    private int getGroupVisualPostion(int visualPosition) {
        return mGroupList.getGroupVisualPosition(visualPosition);
    }

    private int storedIndexOfItem(Item group, Item item) {
        return mGroupList.storedIndexOfItem(group, item);
    }

    private Item getItem(int visualPosition) {
        return mGroupList.getItem(visualPosition);
    }

    private Item getItem(Item group, int itemStoredPosition) {
        return mGroupList.getItem(group, itemStoredPosition);
    }

    private boolean isVisualGroupPosition(int visualPosition) {
        return mGroupList.isVisualGroupPosition(visualPosition);
    }

    private boolean isVisualEmptyPosition(int visualPosition) {
        return mGroupList.isVisualEmptyItemPosition(visualPosition);
    }

    private void addOrUpdateAllItems(Item group, List<Item> items) {
        mGroupList.addOrUpdateAllItems(group, items);
    }

    private int size() {
        return mGroupList.size();
    }

    private int addOrUpdateGroup(Item item) {
        return mGroupList.addOrUpdateGroup(item);
    }

    private int addOrUpdateItem(Item group, Item item) {
        return mGroupList.addOrUpdateItem(group, item);
    }

    private boolean removeGroup(Item item) {
        return mGroupList.removeGroup(item);
    }

    private boolean removeItem(Item item) {
        return mGroupList.removeItem(item);
    }
    // endregion

}
