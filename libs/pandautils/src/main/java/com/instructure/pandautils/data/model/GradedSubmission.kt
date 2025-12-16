/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.data.model

import java.util.Date

data class GradedSubmission(
    val submissionId: Long,
    val assignmentId: Long,
    val assignmentName: String,
    val courseId: Long,
    val courseName: String,
    val score: Double?,
    val grade: String?,
    val gradedAt: Date?,
    val excused: Boolean,
    val assignmentUrl: String?,
    val pointsPossible: Double?
)