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
import java.util.*

@Entity
data class EnrollmentEntity(
    @PrimaryKey
    val id: Long,
    val role: String,
    val type: String,
    val courseId: Long = 0,
    val courseSectionId: Long = 0,
    val enrollmentState: String? = null,
    val userId: Long = 0,
    val computedCurrentScore: Double? = null,
    val computedFinalScore: Double? = null,
    val computedCurrentGrade: String? = null,
    val computedFinalGrade: String? = null,
    val multipleGradingPeriodsEnabled: Boolean = false,
    val totalsForAllGradingPeriodsOption: Boolean = false,
    val currentPeriodComputedCurrentScore: Double? = null,
    val currentPeriodComputedFinalScore: Double? = null,
    val currentPeriodComputedCurrentGrade: String? = null,
    val currentPeriodComputedFinalGrade: String? = null,
    val currentGradingPeriodId: Long = 0,
    val currentGradingPeriodTitle: String? = null,
    val associatedUserId: Long = 0,
    val lastActivityAt: Date? = null,
    val limitPrivilegesToCourseSection: Boolean = false,
    val observedUserId: Long? = null
)