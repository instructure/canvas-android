/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.ExternalToolAttributes
import com.instructure.horizon.database.dao.HorizonAssignmentDetailsDao
import com.instructure.horizon.database.entity.HorizonAssignmentDetailsEntity
import javax.inject.Inject

class AssignmentDetailsLocalDataSource @Inject constructor(
    private val assignmentDetailsDao: HorizonAssignmentDetailsDao,
) {

    suspend fun getAssignment(assignmentId: Long): Assignment? {
        return assignmentDetailsDao.getAssignment(assignmentId)?.toAssignment()
    }

    suspend fun saveAssignment(assignment: Assignment, courseId: Long, parsedDescription: String?) {
        assignmentDetailsDao.saveAssignment(
            HorizonAssignmentDetailsEntity(
                assignmentId = assignment.id,
                courseId = courseId,
                name = assignment.name,
                description = parsedDescription,
                pointsPossible = assignment.pointsPossible,
                allowedAttempts = assignment.allowedAttempts,
                dueAt = assignment.dueAt,
                submissionTypes = assignment.submissionTypesRaw.joinToString(","),
                gradingType = assignment.gradingType,
                lockedForUser = assignment.lockedForUser,
                lockExplanation = assignment.lockExplanation,
                quizId = assignment.quizId,
                url = assignment.url,
                ltiToolUrl = assignment.externalToolAttributes?.url,
            )
        )
    }

    private fun HorizonAssignmentDetailsEntity.toAssignment(): Assignment {
        return Assignment(
            id = assignmentId,
            name = name,
            description = description,
            pointsPossible = pointsPossible,
            allowedAttempts = allowedAttempts,
            dueAt = dueAt,
            submissionTypesRaw = submissionTypes.split(",").filter { it.isNotEmpty() },
            gradingType = gradingType,
            lockedForUser = lockedForUser,
            lockExplanation = lockExplanation,
            quizId = quizId,
            url = url,
            externalToolAttributes = ltiToolUrl?.let { ExternalToolAttributes(url = it) },
        )
    }
}
