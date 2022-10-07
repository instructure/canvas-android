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

class UpdatableSortedList<MODEL>(
    klass: Class<MODEL>,
    callback: Callback<MODEL>,
    private val onGetItemId: (item: MODEL) -> Long,
    defaultSize: Int = 10
) : SortedList<MODEL>(klass, callback, defaultSize) {

    private fun getItemId(item: MODEL): Long = onGetItemId(item)

    fun addOrUpdate(item: MODEL): Int {
        var position = indexOfItemById(getItemId(item))
        position = if (position != NOT_IN_LIST) {
            updateItemAt(position, item)
            return ITEM_UPDATED
        } else {
            this.add(item)
        }
        return position
    }

    fun indexOfItemById(id: Long): Int {
        for (i in 0 until size()) {
            val item1 = this[i]
            if (getItemId(item1) == id) {
                return i
            }
        }
        return NOT_IN_LIST
    }

    fun addOrUpdate(items: List<MODEL>) {
        beginBatchedUpdates()
        for (item in items) addOrUpdate(item)
        endBatchedUpdates()
    }

    fun removeDistinctItems(items: List<MODEL>) {
        val ids = items.map { getItemId(it) }.toSet()
        beginBatchedUpdates()
        for (i in size() - 1 downTo 0) {
            val item = get(i)
            if (!ids.contains(getItemId(item))) {
                removeItemAt(i)
            }
        }
        endBatchedUpdates()
    }

    companion object {
        var NOT_IN_LIST = -2
        var ITEM_UPDATED = -3
    }
}
