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
import com.instructure.canvasapi2.models.Tab

@Entity(
    primaryKeys = ["id", "courseId"],
    foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TabEntity(
    val id: String,
    val label: String?,
    val type: String,
    val htmlUrl: String?,
    val externalUrl: String?,
    val visibility: String,
    val isHidden: Boolean,
    val position: Int,
    val ltiUrl: String,
    val courseId: Long
) {
    constructor(tab: Tab, courseId: Long) : this(
        tab.tabId,
        tab.label,
        tab.type,
        tab.htmlUrl,
        tab.externalUrl,
        tab.visibility,
        tab.isHidden,
        tab.position,
        tab.ltiUrl,
        courseId
    )

    fun toApiModel() = Tab(
        tabId = id,
        label = label,
        type = type,
        htmlUrl = htmlUrl,
        externalUrl = externalUrl,
        visibility = visibility,
        isHidden = isHidden,
        position = position,
        ltiUrl = ltiUrl
    )
}