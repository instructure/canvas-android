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
 *
 */

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import com.instructure.canvasapi2.CustomGradeStatusesQuery

@Entity(
    primaryKeys = ["id", "courseId"],
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CustomGradeStatusEntity(
    val id: String,
    val name: String,
    val courseId: Long
) {
    constructor(customGradeStatus: CustomGradeStatusesQuery.Node, courseId: Long) : this(
        id = customGradeStatus._id,
        name = customGradeStatus.name,
        courseId = courseId
    )

    fun toApiModel(): CustomGradeStatusesQuery.Node {
        return CustomGradeStatusesQuery.Node(
            _id = id,
            name = name
        )
    }
}
