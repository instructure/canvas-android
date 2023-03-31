/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.discussions

import com.instructure.canvasapi2.models.DiscussionEntry
import io.paperdb.Paper

class DiscussionCaching(discussionTopicHeaderId: Long) {

    private val book = Paper.book(discussionTopicHeaderId.toString())

    fun saveEntry(entry: DiscussionEntry?) {
        if(entry == null) return
        book.write(entry.id.toString(), entry)
    }

    fun loadEntries(): MutableList<DiscussionEntry> {
        val keys = book.allKeys
        val entries: ArrayList<DiscussionEntry> = ArrayList(keys.size)
        if(keys.isNotEmpty()) keys.forEach { key ->
            val entry = book.read(key) as DiscussionEntry?
            entry?.let {
                it.id = key.toLong()
                entries.add(it)
            }
        }
        return entries.toMutableList()
    }

    fun removeEntry(entry: DiscussionEntry?) {
        if(entry == null) return
        removeEntry(entry.id)
    }

    fun removeEntry(entryId: Long) {
        book.delete(entryId.toString())
    }

    fun exists(entry: DiscussionEntry): Boolean = book.contains(entry.id.toString())

    fun isEmpty(): Boolean = book.allKeys.isEmpty()
}
