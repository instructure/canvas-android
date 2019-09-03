/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.postpolicies

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.Submission

sealed class PostGradeEvent {
    object GradesPosted : PostGradeEvent()
    object PostGradesClicked : PostGradeEvent()
    object SpecificSectionsToggled : PostGradeEvent()
    data class GradedOnlySelected(val gradedOnly: Boolean) : PostGradeEvent()
    data class SectionToggled(val sectionId: Long) : PostGradeEvent()
    data class DataLoaded(val sections: List<Section>, val submissions: List<Submission>) : PostGradeEvent()
}

sealed class PostGradeEffect {
    data class LoadData(val assignment: Assignment) : PostGradeEffect()
    data class HideGrades(val assignmentId: Long, val sectionIds: List<String>) : PostGradeEffect()
    data class PostGrades(val assignmentId: Long, val sectionIds: List<String>, val gradedOnly: Boolean) : PostGradeEffect()
    data class ShowGradesPosted(val isHidingGrades: Boolean) : PostGradeEffect()
}

data class PostGradeModel(
    val assignment: Assignment,
    val isHidingGrades: Boolean,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val postGradedOnly: Boolean = false,
    val specificSectionsVisible: Boolean = false,
    val sections: List<PostSection> = emptyList(),
    val submissions: List<Submission> = emptyList()
)

data class PostSection(
    val section: Section,
    val selected: Boolean = false,
    val courseColor: Int = 0
)