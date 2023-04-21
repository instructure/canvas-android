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

@Entity
data class GroupEntity(
    @PrimaryKey
    val id: Long,
    val name: String?,
    val description: String?,
    val avatarUrl: String?,
    val isPublic: Boolean,
    val membersCount: Int,
    val joinLevel: String?,
    val courseId: Long,
    val accountId: Long,
    val role: String,
    val groupCategoryId: Long,
    val storageQuotaMb: Long,
    val isFavorite: Boolean,
    val concluded: Boolean,
    val canAccess: Boolean?
)