/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Tab

@Entity
data class CourseSyncSettingsEntity(
    @PrimaryKey
    val courseId: Long,
    val courseName: String,
    val fullContentSync: Boolean,
    val tabs: Map<String, Boolean> = TABS.associateWith { false },
    val fullFileSync: Boolean = false
) {

    fun isTabSelected(tabId: String): Boolean {
        val isSelected = if (tabId == Tab.FILES_ID) {
            fullFileSync
        } else {
            tabs[tabId] ?: false
        }
        return fullContentSync || isSelected
    }

    fun areAnyTabsSelected(tabIds: Set<String>): Boolean {
        return tabIds.any { isTabSelected(it) }
    }

    val allTabsEnabled: Boolean
        get() = tabs.values.all { it } && fullFileSync

    val anySyncEnabled: Boolean
        get() = fullContentSync || fullFileSync || tabs.values.any { it }

    companion object {
        val TABS = setOf(
            Tab.ASSIGNMENTS_ID,
            Tab.PAGES_ID,
            Tab.GRADES_ID,
            Tab.SYLLABUS_ID,
            Tab.ANNOUNCEMENTS_ID,
            Tab.DISCUSSIONS_ID,
            Tab.CONFERENCES_ID,
            Tab.PEOPLE_ID,
            Tab.MODULES_ID,
            Tab.QUIZZES_ID
        )
    }
}