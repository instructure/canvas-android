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

import androidx.recyclerview.widget.SortedList;

import com.instructure.pandarecycler.util.UpdatableSortedList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UpdatableSortedListTest extends Assert {
    private SortedList.Callback<Item> mCallback;
    private UpdatableSortedList<Item> mUpdatableSortedList;

    List<Pair> mAdditions = new ArrayList<Pair>();
    List<Pair> mRemovals = new ArrayList<Pair>();
    List<Pair> mMoves = new ArrayList<Pair>();
    List<Pair> mUpdates = new ArrayList<Pair>();

    @Before
    public void setUp() throws Exception {
        mCallback = new SortedList.Callback<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.cmpField < o2.cmpField ? -1 : (o1.cmpField == o2.cmpField ? 0 : 1);
            }

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

            @Override
            public boolean areContentsTheSame(Item oldItem, Item newItem) {
                return oldItem.cmpField == newItem.cmpField && oldItem.data == newItem.data;
            }

            @Override
            public boolean areItemsTheSame(Item item1, Item item2) {
                return item1.getId() == item2.getId();
            }
        };
        mUpdatableSortedList = new UpdatableSortedList<>(Item.class, mCallback, new UpdatableSortedList.ItemCallback<Item>() {
            @Override
            public long getId(Item item) {
                return item.getId();
            }
        });
    }

    // region Original SortedList tests

    // these should still work
    @Test
    public void testAdd() {
        Item item = new Item();
        assertEquals(insert(item), 0);
        assertEquals(size(), 1);
        assertTrue(mAdditions.contains(new Pair(0, 1)));
        Item item2 = new Item();
        item2.cmpField = item.cmpField + 1;
        assertEquals(insert(item2), 1);
        assertEquals(size(), 2);
        assertTrue(mAdditions.contains(new Pair(1, 1)));
        Item item3 = new Item();
        item3.cmpField = item.cmpField - 1;
        mAdditions.clear();
        assertEquals(insert(item3), 0);
        assertEquals(size(), 3);
        assertTrue(mAdditions.contains(new Pair(0, 1)));
    }

    @Test
    public void testRemove() {
        Item item = new Item();
        assertFalse(remove(item));
        assertEquals(0, mRemovals.size());
        insert(item);
        assertTrue(remove(item));
        assertEquals(1, mRemovals.size());
        assertTrue(mRemovals.contains(new Pair(0, 1)));
        assertEquals(0, size());
        assertFalse(remove(item));
        assertEquals(1, mRemovals.size());
    }

    @Test
    public void testRemove2() {
        Item item = new Item();
        Item item2 = new Item(item.cmpField);
        insert(item);
        assertFalse(remove(item2));
        assertEquals(0, mRemovals.size());
    }

    // endregion

    // region Update


    @Test
    public void testUpdate_ShouldChangePosition(){
        Item item = new Item(3);
        Item item2 = new Item(6);

        assertEquals(0, addOrUpdate(item));
        assertEquals(1, addOrUpdate(item2));
        assertEquals(2, size());

        item.cmpField = 10;

        mAdditions.clear();
        assertEquals(UpdatableSortedList.ITEM_UPDATED, addOrUpdate(item));
        assertEquals(UpdatableSortedList.ITEM_UPDATED, addOrUpdate(item2));

        List<Pair> expectedMoves = new ArrayList<>();
        expectedMoves.add(new Pair(0, 1));

        assertEquals(0, mRemovals.size());
        assertEquals(0, mAdditions.size());
        assertEquals(expectedMoves, mMoves);

        List<Pair> expectedUpdates = new ArrayList<>();
        expectedUpdates.add(new Pair(0,1));
        expectedUpdates.add(new Pair(0,1)); // sortedlist assumes if its the same item it changed
        assertEquals(expectedUpdates, mUpdates);
    }

    @Test
    public void testUpdate_ForCacheDifferentContents(){
        for(int i = 0; i < 6; i++){
            Item item = new Item(i , i);
            item.data = i;
            addOrUpdate(item);
        }

        assertEquals(6, mAdditions.size());
        mAdditions.clear();
        assertEquals(6, size());

        for(int i = 0; i < 6; i++){
            Item item = new Item(i , i);
            item.data = i + 10;
            addOrUpdate(item);
        }

        assertEquals(0, mAdditions.size());
        assertEquals(6, mUpdates.size());
        assertEquals(0, mRemovals.size());
        assertEquals(0, mMoves.size());
        assertEquals(6, size());
    }

    @Test
    public void testUpdate_ForCacheSameContents() {
        for(int i = 0; i < 6; i++){
            Item item = new Item(i , i);
            item.data = i;
            addOrUpdate(item);
        }

        assertEquals(6, mAdditions.size());
        mAdditions.clear();
        assertEquals(6, size());

        for(int i = 0; i < 6; i++){
            Item item = new Item(i , i);
            item.data = i;
            addOrUpdate(item);
        }

        assertEquals(0, mAdditions.size());
        assertEquals(0, mUpdates.size());
        assertEquals(0, mRemovals.size());
        assertEquals(0, mMoves.size());
        assertEquals(6, size());
    }

    // endregion




    private int size() {
        return mUpdatableSortedList.size();
    }

    private int insert(Item item) {
        return mUpdatableSortedList.add(item);
    }

    private int addOrUpdate(Item item){
        return mUpdatableSortedList.addOrUpdate(item);
    }

    private boolean remove(Item item ) {
        return mUpdatableSortedList.remove(item);
    }


}

