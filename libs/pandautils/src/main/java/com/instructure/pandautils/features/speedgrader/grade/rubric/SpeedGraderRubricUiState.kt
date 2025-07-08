/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.speedgrader.grade.rubric

data class SpeedGraderRubricUiState(
    val loading: Boolean = false,
    val criterions: List<RubricCriterion> = emptyList(),
)

data class RubricCriterion(
    val id: String,
    val description: String? = null,
    val longDescription: String? = null,
    val points: Double? = null,
    val ratings: List<RubricRating> = emptyList(),
)

data class RubricRating(
    val id: String,
    val description: String,
    val longDescription: String? = null,
    val points: Double? = null,
    val rubricId: String? = null
)