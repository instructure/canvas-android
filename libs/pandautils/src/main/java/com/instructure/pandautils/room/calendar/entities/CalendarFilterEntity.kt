/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.room.calendar.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// We need to use the same name as the table in the Flutter version to keep the users settings
@Entity(tableName = "calendar_filter")
data class CalendarFilterEntity(
    @ColumnInfo(name = "_id") @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "user_domain") val userDomain: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val filters: Set<String>
)