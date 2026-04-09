/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Lightweight list-metadata entity. Stores the ordering and bucketing information for items
 * shown on the My Content screen. Domain data (course/program fields) lives in the unified
 * [HorizonCourseEntity] and [HorizonProgramEntity] tables, keyed by [id].
 */
@Entity(
    tableName = "horizon_learn_items",
    indices = [Index("queryKey")]
)
data class HorizonLearnItemEntity(
    @PrimaryKey val id: String,
    val queryKey: String,
    val itemType: String,
    val position: Int,
)
