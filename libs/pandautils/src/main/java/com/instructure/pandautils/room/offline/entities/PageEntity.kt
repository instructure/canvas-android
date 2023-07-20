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
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.Page
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PageEntity(
    @PrimaryKey
    val id: Long,
    val url: String?,
    val title: String?,
    val createdAt: Date?,
    val updatedAt: Date?,
    val hideFromStudents: Boolean,
    val status: String?,
    val body: String?,
    val frontPage: Boolean,
    val published: Boolean,
    val editingRoles: String?,
    val htmlUrl: String?,
    val courseId: Long
) {

    constructor(page: Page, courseId: Long) : this(
        page.id,
        page.url,
        page.title,
        page.createdAt,
        page.updatedAt,
        page.hideFromStudents,
        page.status,
        page.body,
        page.frontPage,
        page.published,
        page.editingRoles,
        page.htmlUrl,
        courseId
    )

    fun toApiModel(lockInfo: LockInfo? = null): Page {
        return Page(
            id = id,
            url = url,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            hideFromStudents = hideFromStudents,
            status = status,
            body = body,
            frontPage = frontPage,
            lockInfo = lockInfo,
            published = published,
            editingRoles = editingRoles,
            htmlUrl = htmlUrl
        )
    }
}