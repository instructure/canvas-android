/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.room.appdatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.pandautils.features.todolist.filter.DateRangeSelection

@Entity(tableName = "todo_filter")
data class ToDoFilterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userDomain: String,
    val userId: Long,
    val personalTodos: Boolean = false,
    val calendarEvents: Boolean = false,
    val showCompleted: Boolean = false,
    val favoriteCourses: Boolean = false,
    val pastDateRange: DateRangeSelection = DateRangeSelection.FOUR_WEEKS,
    val futureDateRange: DateRangeSelection = DateRangeSelection.THIS_WEEK
)