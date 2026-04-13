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
 * Stores page content for offline access.
 * [body] contains the parsed HTML with local file references replacing remote URLs.
 * [pageUrl] is the slug used by the Pages API (e.g. "introduction-to-kotlin").
 */
@Entity(
    tableName = "horizon_pages",
    indices = [Index("courseId"), Index("pageUrl")]
)
data class HorizonPageEntity(
    @PrimaryKey val pageId: Long,
    val courseId: Long,
    val pageUrl: String,
    val title: String?,
    val body: String?,
)
