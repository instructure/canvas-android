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

package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.*

class AssignmentFacade(
    private val assignmentGroupDao: AssignmentGroupDao,
    private val assignmentDao: AssignmentDao,
    private val plannerOverrideDao: PlannerOverrideDao,
    private val rubricSettingsDao: RubricSettingsDao,
    private val submissionFacade: SubmissionFacade,
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
    private val assignmentScoreStatisticsDao: AssignmentScoreStatisticsDao,
    private val rubricCriterionDao: RubricCriterionDao,
    private val lockInfoFacade: LockInfoFacade,
    private val rubricCriterionRatingDao: RubricCriterionRatingDao,
    private val assignmentRubricCriterionDao: AssignmentRubricCriterionDao
) {

    suspend fun insertAssignmentGroups(assignmentGroups: List<AssignmentGroup>) {
        assignmentGroups.forEach { assignmentGroup ->
            assignmentGroupDao.insert(AssignmentGroupEntity(assignmentGroup))

            assignmentGroup.assignments.forEach { assignment ->
                val rubricSettingsId = assignment.rubricSettings?.let {
                    rubricSettingsDao.insert(RubricSettingsEntity(it))
                }

                val submissionId = assignment.submission?.let { submission ->
                    submissionFacade.insertSubmission(submission)
                    submission.id
                }

                val plannerOverrideId = insertPlannerOverride(assignment.plannerOverride)

                val discussionTopicHeaderId = assignment.discussionTopicHeader?.let {
                    discussionTopicHeaderFacade.insertDiscussion(it, assignment.courseId)
                }

                val assignmentEntity = AssignmentEntity(
                    assignment = assignment,
                    rubricSettingsId = rubricSettingsId,
                    submissionId = submissionId,
                    discussionTopicHeaderId = discussionTopicHeaderId,
                    plannerOverrideId = plannerOverrideId,
                )

                assignmentDao.insert(assignmentEntity)

                assignment.scoreStatistics?.let {
                    assignmentScoreStatisticsDao.insert(AssignmentScoreStatisticsEntity(it, assignment.id))
                }

                assignment.rubric?.forEach { rubricCriterion ->
                    rubricCriterionDao.insert(RubricCriterionEntity(rubricCriterion))
                    rubricCriterionRatingDao.insertAll(rubricCriterion.ratings.map {
                        RubricCriterionRatingEntity(it, rubricCriterion.id.orEmpty())
                    })
                    assignmentRubricCriterionDao.insert(
                        AssignmentRubricCriterionEntity(assignment.id, rubricCriterion.id.orEmpty())
                    )
                }

                assignment.lockInfo?.let {
                    lockInfoFacade.insertLockInfo(it, assignment.id)
                }
            }
        }
    }

    private suspend fun insertPlannerOverride(plannerOverride: PlannerOverride?): Long? {
        return plannerOverride?.let {
            plannerOverrideDao.insert(PlannerOverrideEntity(it))
        }
    }

    suspend fun getAssignmentById(id: Long): Assignment? {
        return assignmentDao.findById(id)?.let { createFullApiModelFromEntity(it) }
    }

    suspend fun getAssignmentGroupsWithAssignments(
        courseId: Long
    ): List<AssignmentGroup> {
        val assignments = assignmentDao.findByCourseId(courseId).map { createFullApiModelFromEntity(it) }
        return assignments.groupBy { it.assignmentGroupId }.map { assignmentGroupDao.findById(it.key).toApiModel(it.value) }
    }

    suspend fun getAssignmentGroupsWithAssignmentsForGradingPeriod(
        courseId: Long,
        gradingPeriodId: Long
    ): List<AssignmentGroup> {
        return getAssignmentGroupsWithAssignments(courseId).map { group ->
            group.copy(assignments = group.assignments.filter { it.submission?.gradingPeriodId == gradingPeriodId })
        }
    }

    private suspend fun createFullApiModelFromEntity(assignmentEntity: AssignmentEntity): Assignment {
        val rubricSettingEntity = assignmentEntity.rubricSettingsId?.let { rubricSettingsDao.findById(it) }
        val submission = assignmentEntity.submissionId?.let { submissionFacade.getSubmissionById(it) }
        val discussionTopicHeader = assignmentEntity.discussionTopicHeaderId?.let { discussionTopicHeaderFacade.getDiscussionTopicHeaderById(it) }
        val lockInfo = lockInfoFacade.getLockInfoByAssignmentId(assignmentEntity.id)
        val scoreStatisticsEntity = assignmentScoreStatisticsDao.findByAssignmentId(assignmentEntity.id)
        val plannerOverrideEntity = assignmentEntity.plannerOverrideId?.let { plannerOverrideDao.findById(it) }
        val rubricCriterionEntities = assignmentRubricCriterionDao.findByAssignmentId(assignmentEntity.id).mapNotNull {
            rubricCriterionDao.findById(it.rubricId)
        }

        return assignmentEntity.toApiModel(
            rubric = rubricCriterionEntities.map { rubricCriterionEntity ->
                val rubricCriterionRatings = rubricCriterionRatingDao.findByRubricCriterionId(rubricCriterionEntity.id).map { it.toApiModel() }
                rubricCriterionEntity.toApiModel(rubricCriterionRatings)
            },
            rubricSettings = rubricSettingEntity?.toApiModel(),
            submission = submission,
            lockInfo = lockInfo,
            discussionTopicHeader = discussionTopicHeader,
            scoreStatistics = scoreStatisticsEntity?.toApiModel(),
            plannerOverride = plannerOverrideEntity?.toApiModel()
        ).apply {
            /*
             * the assignment model has a submission that contains the assignment, but the inner assignment model cannot
             * contain the submission because it causes a circular reference and leads to a stackoverflow exception
             */
            this.submission = submission?.copy(assignment = this.copy(submission = null))
            this.discussionTopicHeader = discussionTopicHeader?.copy(assignment = this.copy(submission = null))
        }
    }
}