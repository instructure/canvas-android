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

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Grades
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.horizon.database.dao.HorizonCourseScoreDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.HorizonCourseAssignmentEntity
import com.instructure.horizon.database.entity.HorizonCourseAssignmentGroupEntity
import com.instructure.horizon.database.entity.HorizonCourseGradeEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import com.instructure.horizon.database.entity.SyncDataType
import java.util.Date
import javax.inject.Inject

class CourseScoreLocalDataSource @Inject constructor(
    private val courseScoreDao: HorizonCourseScoreDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {

    suspend fun getAssignmentGroups(courseId: Long): List<AssignmentGroup> {
        val groupEntities = courseScoreDao.getGroupsForCourse(courseId)
        return groupEntities.map { groupEntity ->
            val assignmentEntities = courseScoreDao.getAssignmentsForGroup(groupEntity.groupId)
            groupEntity.toAssignmentGroup(assignmentEntities)
        }
    }

    suspend fun getEnrollments(courseId: Long): List<Enrollment> {
        val grade = courseScoreDao.getGradeForCourse(courseId)
        return listOf(
            Enrollment(
                enrollmentState = EnrollmentAPI.STATE_ACTIVE,
                grades = grade?.let { Grades(currentScore = it.currentScore) },
            )
        )
    }

    suspend fun saveScoreData(
        courseId: Long,
        assignmentGroups: List<AssignmentGroup>,
        enrollments: List<Enrollment>,
    ) {
        val groupEntities = assignmentGroups.map { it.toGroupEntity(courseId) }
        val assignmentEntities = assignmentGroups.flatMap { group ->
            group.assignments.map { it.toAssignmentEntity(group.id, courseId) }
        }
        val activeEnrollment = enrollments.firstOrNull { it.enrollmentState == EnrollmentAPI.STATE_ACTIVE }
        val gradeEntity = activeEnrollment?.currentScore?.let {
            HorizonCourseGradeEntity(courseId = courseId, currentScore = it)
        }
        courseScoreDao.replaceForCourse(courseId, groupEntities, assignmentEntities, gradeEntity)
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = SyncDataType.COURSE_SCORES,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }

    private fun HorizonCourseAssignmentGroupEntity.toAssignmentGroup(
        assignmentEntities: List<HorizonCourseAssignmentEntity>,
    ): AssignmentGroup {
        return AssignmentGroup(
            id = groupId,
            name = name,
            groupWeight = groupWeight,
            assignments = assignmentEntities.map { it.toAssignment() },
        )
    }

    private fun HorizonCourseAssignmentEntity.toAssignment(): Assignment {
        val hasSubmissionData = submissionGrade != null || submissionWorkflowState != null
                || submissionExcused || submissionMissing || submissionLate
                || submissionPostedAtMs != null || submissionCustomGradeStatusId != null
        val submission = if (hasSubmissionData) {
            Submission(
                grade = submissionGrade,
                workflowState = submissionWorkflowState,
                excused = submissionExcused,
                missing = submissionMissing,
                late = submissionLate,
                postedAt = submissionPostedAtMs?.let { Date(it) },
                customGradeStatusId = submissionCustomGradeStatusId,
                submissionComments = List(submissionCommentsCount) { SubmissionComment() },
            )
        } else null
        return Assignment(
            id = assignmentId,
            name = name,
            pointsPossible = pointsPossible,
            dueAt = dueAt,
            submission = submission,
        )
    }

    private fun AssignmentGroup.toGroupEntity(courseId: Long): HorizonCourseAssignmentGroupEntity {
        return HorizonCourseAssignmentGroupEntity(
            groupId = id,
            courseId = courseId,
            name = name,
            groupWeight = groupWeight,
        )
    }

    private fun Assignment.toAssignmentEntity(groupId: Long, courseId: Long): HorizonCourseAssignmentEntity {
        val lastSubmission = submission?.takeIf {
            it.workflowState == "graded" || it.workflowState == "submitted"
        }
        return HorizonCourseAssignmentEntity(
            assignmentId = id,
            groupId = groupId,
            courseId = courseId,
            name = name,
            pointsPossible = pointsPossible,
            dueAt = dueAt,
            submissionGrade = submission?.grade,
            submissionWorkflowState = submission?.workflowState,
            submissionExcused = submission?.excused ?: false,
            submissionMissing = submission?.missing ?: false,
            submissionLate = submission?.late ?: false,
            submissionPostedAtMs = submission?.postedAt?.time,
            submissionCustomGradeStatusId = submission?.customGradeStatusId,
            submissionCommentsCount = lastSubmission?.submissionComments?.size ?: 0,
        )
    }
}
