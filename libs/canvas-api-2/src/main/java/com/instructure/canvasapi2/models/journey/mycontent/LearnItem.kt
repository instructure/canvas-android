/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.canvasapi2.models.journey.mycontent

import java.util.Date

sealed class LearnItem {
    abstract val id: String
    abstract val name: String
    abstract val position: Int
    abstract val enrolledAt: Date?
    abstract val completionPercentage: Double?
}

data class ProgramEnrollmentItem(
    override val id: String,
    override val name: String,
    override val position: Int,
    override val enrolledAt: Date?,
    override val completionPercentage: Double?,
    val startDate: Date?,
    val endDate: Date?,
    val status: String,
    val description: String?,
    val variant: String,
    val estimatedDurationMinutes: Int?,
    val courseCount: Int,
) : LearnItem()

data class CourseEnrollmentItem(
    override val id: String,
    override val name: String,
    override val position: Int,
    override val enrolledAt: Date?,
    override val completionPercentage: Double?,
    val startAt: Date?,
    val endAt: Date?,
    val requirementCount: Int?,
    val requirementCompletedCount: Int?,
    val completedAt: Date?,
    val grade: Double?,
    val imageUrl: String?,
    val workflowState: String,
    val lastActivityAt: Date?,
) : LearnItem()