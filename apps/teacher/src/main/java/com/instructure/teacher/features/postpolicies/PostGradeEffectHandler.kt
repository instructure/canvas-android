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

import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.PostPolicyManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.teacher.features.postpolicies.ui.PostGradeView
import com.instructure.teacher.mobius.common.ui.EffectHandler
import kotlinx.coroutines.launch

class PostGradeEffectHandler : EffectHandler<PostGradeView, PostGradeEvent, PostGradeEffect>() {
    override fun accept(effect: PostGradeEffect) {
        launch {
            when (effect) {
                is PostGradeEffect.LoadData -> loadData(effect.assignment)
                is PostGradeEffect.HideGrades -> hideGrades(effect.assignmentId, effect.sectionIds)
                is PostGradeEffect.PostGrades -> postGrades(effect.assignmentId, effect.sectionIds, effect.gradedOnly)
            }.exhaustive
        }
    }

    private suspend fun loadData(assignment: Assignment) {
        val sections = SectionManager.getAllSectionsForCourseAsync(assignment.courseId, false)
        val submissions = AssignmentManager.getAllSubmissionsForAssignmentAsync(assignment.courseId, assignment.id, false)

        consumer.accept(PostGradeEvent.DataLoaded(sections.await().dataOrNull ?: emptyList(), submissions.await().dataOrNull ?: emptyList()))
    }

    private suspend fun hideGrades(assignmentId: Long, sections: List<String>) {
        if (sections.isEmpty()) {
            PostPolicyManager.hideGradesAsync(assignmentId)
        } else {
            PostPolicyManager.hideGradesForSectionsAsync(assignmentId, sections)
        }
    }

    private suspend fun postGrades(assignmentId: Long, sections: List<String>, gradedOnly: Boolean) {
        if (sections.isEmpty()) {
            PostPolicyManager.postGradesAsync(assignmentId, gradedOnly)
        } else {
            PostPolicyManager.postGradesForSectionsAsync(assignmentId, gradedOnly, sections)
        }
    }
}
