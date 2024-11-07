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

import androidx.room.withTransaction
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.AssignmentScoreStatistics
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricSettings
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.AssignmentDao
import com.instructure.pandautils.room.offline.daos.AssignmentGroupDao
import com.instructure.pandautils.room.offline.daos.AssignmentRubricCriterionDao
import com.instructure.pandautils.room.offline.daos.AssignmentScoreStatisticsDao
import com.instructure.pandautils.room.offline.daos.PlannerOverrideDao
import com.instructure.pandautils.room.offline.daos.RubricCriterionDao
import com.instructure.pandautils.room.offline.daos.RubricCriterionRatingDao
import com.instructure.pandautils.room.offline.daos.RubricSettingsDao
import com.instructure.pandautils.room.offline.entities.AssignmentEntity
import com.instructure.pandautils.room.offline.entities.AssignmentGroupEntity
import com.instructure.pandautils.room.offline.entities.AssignmentRubricCriterionEntity
import com.instructure.pandautils.room.offline.entities.AssignmentScoreStatisticsEntity
import com.instructure.pandautils.room.offline.entities.PlannerOverrideEntity
import com.instructure.pandautils.room.offline.entities.RubricCriterionEntity
import com.instructure.pandautils.room.offline.entities.RubricSettingsEntity
import com.instructure.pandautils.utils.orDefault
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AssignmentFacadeTest {

    private val assignmentGroupDao: AssignmentGroupDao = mockk(relaxed = true)
    private val assignmentDao: AssignmentDao = mockk(relaxed = true)
    private val plannerOverrideDao: PlannerOverrideDao = mockk(relaxed = true)
    private val rubricSettingsDao: RubricSettingsDao = mockk(relaxed = true)
    private val submissionFacade: SubmissionFacade = mockk(relaxed = true)
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade = mockk(relaxed = true)
    private val assignmentScoreStatisticsDao: AssignmentScoreStatisticsDao = mockk(relaxed = true)
    private val rubricCriterionDao: RubricCriterionDao = mockk(relaxed = true)
    private val lockInfoFacade: LockInfoFacade = mockk(relaxed = true)
    private val rubricCriterionRatingDao: RubricCriterionRatingDao = mockk(relaxed = true)
    private val assignmentRubricCriterionDao: AssignmentRubricCriterionDao = mockk(relaxed = true)
    private val offlineDatabase: OfflineDatabase = mockk(relaxed = true)

    private val facade = AssignmentFacade(
        assignmentGroupDao,
        assignmentDao,
        plannerOverrideDao,
        rubricSettingsDao,
        submissionFacade,
        discussionTopicHeaderFacade,
        assignmentScoreStatisticsDao,
        rubricCriterionDao,
        lockInfoFacade,
        rubricCriterionRatingDao,
        assignmentRubricCriterionDao,
        offlineDatabase
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )

        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { offlineDatabase.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Calling insertAssignmentGroups should insert assignment groups and related entities`() = runTest {
        val rubricSettings = RubricSettings(id = 1L)
        val submission = Submission(id = 1L)
        val plannedOverride = PlannerOverride(id = 1L, plannableType = PlannableType.ASSIGNMENT, plannableId = 1L)
        val discussionTopicHeader = DiscussionTopicHeader(id = 1L)
        val scoreStatistics = AssignmentScoreStatistics(0.0, 0.0, 0.0)
        val rubricCriterions = listOf(RubricCriterion())
        val lockInfo = LockInfo()
        val assignments = listOf(
            Assignment(
                rubricSettings = rubricSettings,
                submission = submission,
                plannerOverride = plannedOverride,
                discussionTopicHeader = discussionTopicHeader,
                scoreStatistics = scoreStatistics,
                rubric = rubricCriterions,
                lockInfo = lockInfo,
                courseId = 1,
            )
        )
        val assignmentGroups = listOf(AssignmentGroup(assignments = assignments))

        coEvery { assignmentGroupDao.insert(any()) } just Runs
        coEvery { assignmentDao.insert(any()) } just Runs
        coEvery { plannerOverrideDao.insert(any()) } returns 1L
        coEvery { rubricSettingsDao.insert(any()) } returns 1L
        coEvery { submissionFacade.insertSubmission(any()) } just Runs
        coEvery { discussionTopicHeaderFacade.insertDiscussion(any(), any()) } returns 1L
        coEvery { assignmentScoreStatisticsDao.insert(any()) } just Runs
        coEvery { rubricCriterionDao.insert(any()) } just Runs
        coEvery { lockInfoFacade.insertLockInfoForAssignment(any(), any()) } just Runs

        facade.insertAssignmentGroups(assignmentGroups, 1L)

        assignmentGroups.forEach { group ->
            coVerify { assignmentGroupDao.insert(AssignmentGroupEntity(group, 1L)) }
            assignments.forEach { assignment ->
                coVerify { rubricSettingsDao.insert(RubricSettingsEntity(rubricSettings, assignment.id)) }
                coVerify { submissionFacade.insertSubmission(submission) }
                coVerify { plannerOverrideDao.insert(PlannerOverrideEntity(plannedOverride)) }
                coVerify { discussionTopicHeaderFacade.insertDiscussion(discussionTopicHeader, 1) }
                coVerify { assignmentScoreStatisticsDao.insert(AssignmentScoreStatisticsEntity(scoreStatistics, assignment.id)) }
                rubricCriterions.forEach {
                    coVerify { rubricCriterionDao.insert(RubricCriterionEntity(it, assignment.id)) }
                    coVerify { assignmentRubricCriterionDao.insert(AssignmentRubricCriterionEntity(assignment.id, it.id.orEmpty())) }
                }
                coVerify { lockInfoFacade.insertLockInfoForAssignment(lockInfo, assignment.id) }
                coVerify {
                    assignmentDao.insertOrUpdate(
                        AssignmentEntity(
                            assignment = assignment,
                            1L,
                            1L,
                            1L,
                            1L
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `Calling insertAssignment should insert assignment with related entities`() = runTest {
        val rubricSettings = RubricSettings(id = 1L)
        val submission = Submission(id = 1L)
        val plannedOverride = PlannerOverride(id = 1L, plannableType = PlannableType.ASSIGNMENT, plannableId = 1L)
        val discussionTopicHeader = DiscussionTopicHeader(id = 1L)
        val scoreStatistics = AssignmentScoreStatistics(0.0, 0.0, 0.0)
        val rubricCriterions = listOf(RubricCriterion())
        val lockInfo = LockInfo()
        val assignment = Assignment(
            rubricSettings = rubricSettings,
            submission = submission,
            plannerOverride = plannedOverride,
            discussionTopicHeader = discussionTopicHeader,
            scoreStatistics = scoreStatistics,
            rubric = rubricCriterions,
            lockInfo = lockInfo,
            courseId = 1,
        )

        coEvery { assignmentDao.insert(any()) } just Runs
        coEvery { plannerOverrideDao.insert(any()) } returns 1L
        coEvery { rubricSettingsDao.insert(any()) } returns 1L
        coEvery { submissionFacade.insertSubmission(any()) } just Runs
        coEvery { discussionTopicHeaderFacade.insertDiscussion(any(), any()) } returns 1L
        coEvery { assignmentScoreStatisticsDao.insert(any()) } just Runs
        coEvery { rubricCriterionDao.insert(any()) } just Runs
        coEvery { lockInfoFacade.insertLockInfoForAssignment(any(), any()) } just Runs

        facade.insertAssignment(assignment)

        coVerify { rubricSettingsDao.insert(RubricSettingsEntity(rubricSettings, assignment.id)) }
        coVerify { submissionFacade.insertSubmission(submission) }
        coVerify { plannerOverrideDao.insert(PlannerOverrideEntity(plannedOverride)) }
        coVerify { discussionTopicHeaderFacade.insertDiscussion(discussionTopicHeader, 1) }
        coVerify {
            assignmentScoreStatisticsDao.insert(
                AssignmentScoreStatisticsEntity(
                    scoreStatistics,
                    assignment.id
                )
            )
        }
        rubricCriterions.forEach {
            coVerify { rubricCriterionDao.insert(RubricCriterionEntity(it, assignment.id)) }
        }
        coVerify { lockInfoFacade.insertLockInfoForAssignment(lockInfo, assignment.id) }
        coVerify {
            assignmentDao.insertOrUpdate(
                AssignmentEntity(
                    assignment = assignment,
                    1L,
                    1L,
                    1L,
                    1L
                )
            )
        }
    }

    @Test
    fun `Calling getAssignmentById should return the assignment with the specified ID`() = runTest {
        val assignmentId = 1L
        val rubricSettings = RubricSettings(id = 1L, title = "RubricSettings")
        val submission = Submission(id = 1L, grade = "Grade")
        val discussionTopicHeader = DiscussionTopicHeader(id = 1L, title = "DiscussionTopicHeader")
        val plannedOverride = PlannerOverride(id = 1L, plannableType = PlannableType.ASSIGNMENT, plannableId = 1L)
        val scoreStatistics = AssignmentScoreStatistics(0.0, 0.0, 0.0)
        val rubricCriterions = listOf(RubricCriterion(id = "id"))
        val lockInfo = LockInfo(modulePrerequisiteNames = arrayListOf("1", "2"))
        val assignment = Assignment(
            id = assignmentId,
            rubricSettings = rubricSettings,
            submission = submission,
            discussionTopicHeader = discussionTopicHeader,
            plannerOverride = plannedOverride,
            scoreStatistics = scoreStatistics,
            rubric = rubricCriterions,
            lockInfo = lockInfo
        )
        val assignmentEntity = AssignmentEntity(
            assignment = assignment,
            rubricSettingsId = rubricSettings.id,
            submissionId = submission.id,
            discussionTopicHeaderId = discussionTopicHeader.id,
            plannerOverrideId = plannedOverride.id
        )

        coEvery { assignmentDao.findById(assignmentId) } returns assignmentEntity
        coEvery { assignmentRubricCriterionDao.findByAssignmentId(assignmentId) } returns listOf(
            AssignmentRubricCriterionEntity(assignmentId, rubricCriterions[0].id!!)
        )
        coEvery { rubricCriterionDao.findById(rubricCriterions[0].id!!) } returns RubricCriterionEntity(rubricCriterions[0], assignmentId)
        coEvery { rubricSettingsDao.findById(rubricSettings.id.orDefault()) } returns RubricSettingsEntity(rubricSettings, assignmentId)
        coEvery { submissionFacade.getSubmissionById(submission.id) } returns submission
        coEvery { discussionTopicHeaderFacade.getDiscussionTopicHeaderById(discussionTopicHeader.id) } returns discussionTopicHeader
        coEvery { lockInfoFacade.getLockInfoByAssignmentId(assignmentId) } returns lockInfo
        coEvery { assignmentScoreStatisticsDao.findByAssignmentId(assignmentId) } returns AssignmentScoreStatisticsEntity(
            scoreStatistics,
            assignmentId
        )
        coEvery { plannerOverrideDao.findById(plannedOverride.id.orDefault()) } returns PlannerOverrideEntity(plannedOverride)

        val result = facade.getAssignmentById(assignmentId)!!

        Assert.assertEquals(assignmentId, result.id)
        Assert.assertEquals(rubricSettings, result.rubricSettings)
        Assert.assertEquals(submission.id, result.submission?.id)
        Assert.assertEquals(discussionTopicHeader.id, result.discussionTopicHeader?.id)
        Assert.assertEquals(plannedOverride, result.plannerOverride)
        Assert.assertEquals(scoreStatistics, result.scoreStatistics)
        Assert.assertEquals(rubricCriterions, result.rubric)
        Assert.assertEquals(lockInfo, result.lockInfo)
        Assert.assertEquals(assignmentId, result.submission?.assignment?.id)
        Assert.assertEquals(assignmentId, result.discussionTopicHeader?.assignment?.id)
    }

    @Test
    fun `Calling getAssignmentGroupsWithAssignments should return assignment groups with assignments by the specifies CourseID`() = runTest {
        val submissions = listOf(
            Submission(id = 1L, gradingPeriodId = 1L), Submission(id = 2L)
        )

        val assignments = listOf(
            Assignment(
                id = 1L,
                courseId = 1L,
                assignmentGroupId = 1L,
                submission = submissions[0]
            ), Assignment(
                id = 2L,
                courseId = 1L,
                assignmentGroupId = 1L,
                submission = submissions[1]
            )
        )

        val assignmentEntities = assignments.map {
            AssignmentEntity(
                assignment = it,
                rubricSettingsId = null,
                submissionId = it.submission?.id,
                discussionTopicHeaderId = null,
                plannerOverrideId = null
            )
        }

        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1L,
                name = "Group 1",
                position = 0,
                groupWeight = 0.0,
                assignments = assignments,
            ), AssignmentGroup(
                id = 2L,
                name = "Group 2"
            )
        )

        val assignmentGroupEntities = assignmentGroups.map {
            AssignmentGroupEntity(it, 1L)
        }

        assignmentEntities.forEach {
            coEvery { assignmentDao.findById(it.id) } returns it
        }

        coEvery { assignmentDao.findByCourseId(1L) } returns assignmentEntities

        submissions.forEach {
            coEvery { submissionFacade.getSubmissionById(it.id) } returns it
        }

        assignmentGroupEntities.forEach {
            coEvery { assignmentGroupDao.findById(it.id) } returns it
        }

        val result = facade.getAssignmentGroupsWithAssignments(1L)

        val expected = assignmentGroups.filter { it.assignments.isNotEmpty() }

        Assert.assertEquals(expected.size, result.size)
        Assert.assertEquals(expected.first().id, result.first().id)
        Assert.assertEquals(expected.first().assignments.size, result.first().assignments.size)
        Assert.assertEquals(expected.first().assignments.first().id, result.first().assignments.first().id)
    }

    @Test
    fun `Calling getAssignmentGroupsWithAssignmentsForGradingPeriod should return assignment groups with assignments by the specifies CourseID and GradingPeriodId`() =
        runTest {
            val submissions = listOf(
                Submission(id = 1L, gradingPeriodId = 1L), Submission(id = 2L)
            )

            val assignments = listOf(
                Assignment(
                    id = 1L,
                    courseId = 1L,
                    assignmentGroupId = 1L,
                    submission = submissions[0]
                ), Assignment(
                    id = 2L,
                    courseId = 1L,
                    assignmentGroupId = 1L,
                    submission = submissions[1]
                )
            )

            val assignmentEntities = assignments.map {
                AssignmentEntity(
                    assignment = it,
                    rubricSettingsId = null,
                    submissionId = it.submission?.id,
                    discussionTopicHeaderId = null,
                    plannerOverrideId = null
                )
            }

            val assignmentGroups = listOf(
                AssignmentGroup(
                    id = 1L,
                    name = "Group 1",
                    position = 0,
                    groupWeight = 0.0,
                    assignments = assignments,
                ), AssignmentGroup(
                    id = 2L,
                    name = "Group 2"
                )
            )

            val assignmentGroupEntities = assignmentGroups.map {
                AssignmentGroupEntity(it, 1L)
            }

            assignmentEntities.forEach {
                coEvery { assignmentDao.findById(it.id) } returns it
            }

            coEvery { assignmentDao.findByCourseId(1L) } returns assignmentEntities

            submissions.forEach {
                coEvery { submissionFacade.getSubmissionById(it.id) } returns it
            }

            assignmentGroupEntities.forEach {
                coEvery { assignmentGroupDao.findById(it.id) } returns it
            }

            val result = facade.getAssignmentGroupsWithAssignmentsForGradingPeriod(1L, 1L)

            val expected = assignmentGroups.filter { it.assignments.isNotEmpty() }.map { group ->
                val filteredAssignments = group.assignments.filter { it.submission?.gradingPeriodId == 1L }
                group.copy(assignments = filteredAssignments)
            }

            Assert.assertEquals(expected.size, result.size)
            Assert.assertEquals(expected.first().id, result.first().id)
            Assert.assertEquals(expected.first().assignments.size, result.first().assignments.size)
            Assert.assertEquals(expected.first().assignments.first().id, result.first().assignments.first().id)
        }
}