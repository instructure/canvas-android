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
data class CourseEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    var originalName: String? = null,
    val courseCode: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    var syllabusBody: String? = null,
    val hideFinalGrades: Boolean = false,
    val isPublic: Boolean = false,
    val license: String,
    val termId: Long? = null,
    val needsGradingCount: Long = 0,
    val isApplyAssignmentGroupWeights: Boolean = false,
    val currentScore: Double? = null, // Helper variable
    val finalScore: Double? = null, // Helper variable
    val currentGrade: String? = null, // Helper variable
    val finalGrade: String? = null, // Helper variable
    var isFavorite: Boolean = false,
    val accessRestrictedByDate: Boolean = false,
    val imageUrl: String? = null,
    val bannerImageUrl: String? = null,
    val isWeightedGradingPeriods: Boolean = false,
    val hasGradingPeriods: Boolean = false,
    val homePage: String? = null,
    val restrictEnrollmentsToCourseDate: Boolean = false,
    val workflowState: String? = null,
    val homeroomCourse: Boolean = false,
    val courseColor: String? = null
)