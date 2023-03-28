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
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val name: String = "",
    val shortName: String? = null,
    val loginId: String? = null,
    var avatarUrl: String? = null,
    val primaryEmail: String? = null,
    val email: String? = null,
    val sortableName: String? = null,
    val bio: String? = null,
    val enrollmentIndex: Int = 0,
    val lastLogin: String? = null,
    val locale: String? = null,
    val effective_locale: String? = null,
    val pronouns: String? = null,
    val k5User: Boolean = false,
    val rootAccount: String? = null,
    val isFakeStudent: Boolean = false,
    val calendarId: Long? = null,
    val sectionId: Long? = null,
)