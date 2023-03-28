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

package com.instructure.pandautils.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SectionEntity(
    @PrimaryKey
    val id: Long,
    var name: String = "",
    val courseId: Long = 0,
    val startAt: String? = null,
    val endAt: String? = null,
    val totalStudents: Int = 0,
    val restrictEnrollmentsToSectionDates: Boolean = false
)