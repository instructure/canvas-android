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
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Grades

@Entity(
    foreignKeys = [ForeignKey(
        entity = EnrollmentEntity::class,
        parentColumns = ["id"],
        childColumns = ["enrollmentId"],
        onDelete = ForeignKey.CASCADE
    )]
)
class GradesEntity(
    @PrimaryKey
    val id: Long,
    val enrollmentId: Long,
    val htmlUrl: String?,
    val currentScore: Double?,
    val finalScore: Double?,
    val currentGrade: String?,
    val finalGrade: String?
) {
    constructor(grades: Grades, enrollmentId: Long) : this(
        grades.id,
        enrollmentId,
        grades.htmlUrl,
        grades.currentScore,
        grades.finalScore,
        grades.currentGrade,
        grades.finalGrade
    )
}