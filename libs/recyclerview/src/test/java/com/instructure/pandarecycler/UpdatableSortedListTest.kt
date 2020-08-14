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

import androidx.recyclerview.widget.SortedList
import com.instructure.pandarecycler.util.UpdatableSortedList
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class UpdatableSortedListTest : Assert() {
    private lateinit var sortedList: UpdatableSortedList<Item>
    var additions: MutableList<Pair<Int, Int>> = ArrayList()
    var removals: MutableList<Pair<Int, Int>> = ArrayList()
    var moves: MutableList<Pair<Int, Int>> = ArrayList()
    var updates: MutableList<Pair<Int, Int>> = ArrayList()

    @Before
    fun setUp() {
        val callback: SortedList.Callback<Item> = object : SortedList.Callback<Item>() {
            override fun compare(o1: Item, o2: Item): Int = o1.cmpField.compareTo(o2.cmpField)

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

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.cmpField == newItem.cmpField && oldItem.data == newItem.data
            }

            override fun areItemsTheSame(item1: Item, item2: Item): Boolean = item1.id == item2.id
        }
        sortedList = UpdatableSortedList(
            Item::class.java,
            callback,
            UpdatableSortedList.ItemCallback<Item> { item -> item.id.toLong() }
        )
    }

    // region Original SortedList tests
    // these should still work
    @Test
    fun testAdd() {
        val item = Item()
        assertEquals(insert(item).toLong(), 0)
        assertEquals(size().toLong(), 1)
        assertTrue(additions.contains(Pair(0, 1)))
        val item2 = Item()
        item2.cmpField = item.cmpField + 1
        assertEquals(insert(item2).toLong(), 1)
        assertEquals(size().toLong(), 2)
        assertTrue(additions.contains(Pair(1, 1)))
        val item3 = Item()
        item3.cmpField = item.cmpField - 1
        additions.clear()
        assertEquals(insert(item3).toLong(), 0)
        assertEquals(size().toLong(), 3)
        assertTrue(additions.contains(Pair(0, 1)))
    }

    @Test
    fun testRemove() {
        val item = Item()
        assertFalse(remove(item))
        assertEquals(0, removals.size.toLong())
        insert(item)
        assertTrue(remove(item))
        assertEquals(1, removals.size.toLong())
        assertTrue(removals.contains(Pair(0, 1)))
        assertEquals(0, size().toLong())
        assertFalse(remove(item))
        assertEquals(1, removals.size.toLong())
    }

    @Test
    fun testRemove2() {
        val item = Item()
        val item2 = Item(item.cmpField)
        insert(item)
        assertFalse(remove(item2))
        assertEquals(0, removals.size.toLong())
    }

    // endregion
    // region Update
    @Test
    fun testUpdate_ShouldChangePosition() {
        val item = Item(3)
        val item2 = Item(6)
        assertEquals(0, addOrUpdate(item).toLong())
        assertEquals(1, addOrUpdate(item2).toLong())
        assertEquals(2, size().toLong())
        item.cmpField = 10
        additions.clear()
        assertEquals(UpdatableSortedList.ITEM_UPDATED.toLong(), addOrUpdate(item).toLong())
        assertEquals(UpdatableSortedList.ITEM_UPDATED.toLong(), addOrUpdate(item2).toLong())
        val expectedMoves: MutableList<Pair<Int, Int>> = ArrayList()
        expectedMoves.add(Pair(0, 1))
        assertEquals(0, removals.size.toLong())
        assertEquals(0, additions.size.toLong())
        assertEquals(expectedMoves, moves)
        val expectedUpdates: MutableList<Pair<Int, Int>> = ArrayList()
        expectedUpdates.add(Pair(0, 1))
        expectedUpdates.add(Pair(0, 1)) // sorted list assumes if its the same item it changed
        assertEquals(expectedUpdates, updates)
    }

    @Test
    fun testUpdate_ForCacheDifferentContents() {
        for (i in 0..5) {
            val item = Item(i, i)
            item.data = i
            addOrUpdate(item)
        }
        assertEquals(6, additions.size.toLong())
        additions.clear()
        assertEquals(6, size().toLong())
        for (i in 0..5) {
            val item = Item(i, i)
            item.data = i + 10
            addOrUpdate(item)
        }
        assertEquals(0, additions.size.toLong())
        assertEquals(6, updates.size.toLong())
        assertEquals(0, removals.size.toLong())
        assertEquals(0, moves.size.toLong())
        assertEquals(6, size().toLong())
    }

    @Test
    fun testUpdate_ForCacheSameContents() {
        for (i in 0..5) {
            val item = Item(i, i)
            item.data = i
            addOrUpdate(item)
        }
        assertEquals(6, additions.size.toLong())
        additions.clear()
        assertEquals(6, size().toLong())
        for (i in 0..5) {
            val item = Item(i, i)
            item.data = i
            addOrUpdate(item)
        }
        assertEquals(0, additions.size.toLong())
        assertEquals(0, updates.size.toLong())
        assertEquals(0, removals.size.toLong())
        assertEquals(0, moves.size.toLong())
        assertEquals(6, size().toLong())
    }

    // endregion
    private fun size(): Int = sortedList.size()

    private fun insert(item: Item): Int = sortedList.add(item)

    private fun addOrUpdate(item: Item): Int = sortedList.addOrUpdate(item)

    private fun remove(item: Item): Boolean = sortedList.remove(item)
}
