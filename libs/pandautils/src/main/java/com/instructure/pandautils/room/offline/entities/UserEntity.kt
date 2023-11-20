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
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User

@Entity
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val shortName: String?,
    val loginId: String?,
    val avatarUrl: String?,
    val primaryEmail: String?,
    val email: String?,
    val sortableName: String?,
    val bio: String?,
    val enrollmentIndex: Int,
    val lastLogin: String?,
    val locale: String?,
    val effective_locale: String?,
    val pronouns: String?,
    val k5User: Boolean,
    val rootAccount: String?,
    val isFakeStudent: Boolean
) {
    constructor(user: User) : this(
        user.id,
        user.name,
        user.shortName,
        user.loginId,
        user.avatarUrl,
        user.primaryEmail,
        user.email,
        user.sortableName,
        user.bio,
        user.enrollmentIndex,
        user.lastLogin,
        user.locale,
        user.effective_locale,
        user.pronouns,
        user.k5User,
        user.rootAccount,
        user.isFakeStudent
    )

    fun toApiModel(enrollments: List<Enrollment> = emptyList()) = User(
        id = id,
        name = name,
        shortName = shortName,
        loginId = loginId,
        avatarUrl = avatarUrl,
        primaryEmail = primaryEmail,
        email = email,
        sortableName = sortableName,
        bio = bio,
        enrollments = enrollments,
        enrollmentIndex = enrollmentIndex,
        lastLogin = lastLogin,
        locale = locale,
        effective_locale = effective_locale,
        pronouns = pronouns,
        k5User = k5User,
        rootAccount = rootAccount,
        isFakeStudent = isFakeStudent,
        calendar = null
    )
}