/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.SubAssignmentSubmission
import com.instructure.canvasapi2.models.Submission
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.AssignmentEntity
import com.instructure.pandautils.room.offline.entities.AssignmentGroupEntity
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.SubAssignmentSubmissionEntity
import com.instructure.pandautils.room.offline.entities.SubmissionEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubAssignmentSubmissionDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var subAssignmentSubmissionDao: SubAssignmentSubmissionDao
    private lateinit var submissionDao: SubmissionDao
    private lateinit var assignmentDao: AssignmentDao
    private lateinit var assignmentGroupDao: AssignmentGroupDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        subAssignmentSubmissionDao = db.subAssignmentSubmissionDao()
        submissionDao = db.submissionDao()
        assignmentDao = db.assignmentDao()
        assignmentGroupDao = db.assignmentGroupDao()
        courseDao = db.courseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertAndFind() = runTest {
        setupSubmission(1L, 1L, 1L)

        val subAssignmentSubmission = SubAssignmentSubmissionEntity(
            submissionId = 1L,
            submissionAttempt = 1L,
            grade = "A",
            score = 10.0,
            late = false,
            excused = false,
            missing = false,
            latePolicyStatus = null,
            customGradeStatusId = null,
            subAssignmentTag = "reply_to_topic",
            enteredScore = 10.0,
            enteredGrade = "A",
            userId = 1L,
            isGradeMatchesCurrentSubmission = true
        )

        subAssignmentSubmissionDao.insert(subAssignmentSubmission)

        val result = subAssignmentSubmissionDao.findBySubmissionIdAndAttempt(1L, 1L)
        assertEquals(1, result.size)
        assertEquals("A", result[0].grade)
        assertEquals(10.0, result[0].score)
        assertEquals("reply_to_topic", result[0].subAssignmentTag)
    }

    @Test
    fun testInsertMultipleSubAssignmentSubmissions() = runTest {
        setupSubmission(1L, 1L, 1L)

        val subAssignment1 = SubAssignmentSubmissionEntity(
            submissionId = 1L,
            submissionAttempt = 1L,
            grade = "A",
            score = 5.0,
            late = false,
            excused = false,
            missing = false,
            latePolicyStatus = null,
            customGradeStatusId = null,
            subAssignmentTag = "reply_to_topic",
            enteredScore = 5.0,
            enteredGrade = "A",
            userId = 1L,
            isGradeMatchesCurrentSubmission = true
        )

        val subAssignment2 = SubAssignmentSubmissionEntity(
            submissionId = 1L,
            submissionAttempt = 1L,
            grade = "B",
            score = 4.0,
            late = true,
            excused = false,
            missing = false,
            latePolicyStatus = "late",
            customGradeStatusId = null,
            subAssignmentTag = "reply_to_entry",
            enteredScore = 5.0,
            enteredGrade = "B",
            userId = 1L,
            isGradeMatchesCurrentSubmission = true
        )

        subAssignmentSubmissionDao.insertAll(listOf(subAssignment1, subAssignment2))

        val result = subAssignmentSubmissionDao.findBySubmissionIdAndAttempt(1L, 1L)
        assertEquals(2, result.size)
        assertTrue(result.any { it.subAssignmentTag == "reply_to_topic" && it.score == 5.0 })
        assertTrue(result.any { it.subAssignmentTag == "reply_to_entry" && it.late })
    }

    @Test
    fun testDeleteBySubmissionIdAndAttempt() = runTest {
        setupSubmission(1L, 1L, 1L)

        val subAssignmentSubmission = SubAssignmentSubmissionEntity(
            submissionId = 1L,
            submissionAttempt = 1L,
            grade = "A",
            score = 10.0,
            late = false,
            excused = false,
            missing = false,
            latePolicyStatus = null,
            customGradeStatusId = null,
            subAssignmentTag = "reply_to_topic",
            enteredScore = 10.0,
            enteredGrade = "A",
            userId = 1L,
            isGradeMatchesCurrentSubmission = true
        )

        subAssignmentSubmissionDao.insert(subAssignmentSubmission)
        subAssignmentSubmissionDao.deleteBySubmissionIdAndAttempt(1L, 1L)

        val result = subAssignmentSubmissionDao.findBySubmissionIdAndAttempt(1L, 1L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testCascadeDelete() = runTest {
        setupSubmission(1L, 1L, 1L)

        val subAssignmentSubmission = SubAssignmentSubmissionEntity(
            submissionId = 1L,
            submissionAttempt = 1L,
            grade = "A",
            score = 10.0,
            late = false,
            excused = false,
            missing = false,
            latePolicyStatus = null,
            customGradeStatusId = null,
            subAssignmentTag = "reply_to_topic",
            enteredScore = 10.0,
            enteredGrade = "A",
            userId = 1L,
            isGradeMatchesCurrentSubmission = true
        )

        subAssignmentSubmissionDao.insert(subAssignmentSubmission)

        val submissions = submissionDao.findById(1L)
        val submissionEntity = submissions.first { it.id == 1L && it.attempt == 1L }
        submissionDao.delete(submissionEntity)

        val result = subAssignmentSubmissionDao.findBySubmissionIdAndAttempt(1L, 1L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testMultipleAttempts() = runTest {
        setupSubmission(1L, 1L, 1L)

        val submission2 = SubmissionEntity(
            Submission(id = 1L, assignmentId = 1L, attempt = 2L),
            null,
            null
        )
        submissionDao.insert(submission2)

        val subAssignment1 = SubAssignmentSubmissionEntity(
            submissionId = 1L,
            submissionAttempt = 1L,
            grade = "B",
            score = 8.0,
            late = false,
            excused = false,
            missing = false,
            latePolicyStatus = null,
            customGradeStatusId = null,
            subAssignmentTag = "reply_to_topic",
            enteredScore = 8.0,
            enteredGrade = "B",
            userId = 1L,
            isGradeMatchesCurrentSubmission = true
        )

        val subAssignment2 = SubAssignmentSubmissionEntity(
            submissionId = 1L,
            submissionAttempt = 2L,
            grade = "A",
            score = 10.0,
            late = false,
            excused = false,
            missing = false,
            latePolicyStatus = null,
            customGradeStatusId = null,
            subAssignmentTag = "reply_to_topic",
            enteredScore = 10.0,
            enteredGrade = "A",
            userId = 1L,
            isGradeMatchesCurrentSubmission = true
        )

        subAssignmentSubmissionDao.insertAll(listOf(subAssignment1, subAssignment2))

        val attempt1Results = subAssignmentSubmissionDao.findBySubmissionIdAndAttempt(1L, 1L)
        val attempt2Results = subAssignmentSubmissionDao.findBySubmissionIdAndAttempt(1L, 2L)

        assertEquals(1, attempt1Results.size)
        assertEquals(8.0, attempt1Results[0].score)
        assertEquals(1, attempt2Results.size)
        assertEquals(10.0, attempt2Results[0].score)
    }

    @Test
    fun testToApiModel() {
        val entity = SubAssignmentSubmissionEntity(
            submissionId = 1L,
            submissionAttempt = 1L,
            grade = "A",
            score = 10.0,
            late = true,
            excused = false,
            missing = false,
            latePolicyStatus = "late",
            customGradeStatusId = 123L,
            subAssignmentTag = "reply_to_topic",
            enteredScore = 10.0,
            enteredGrade = "A",
            userId = 1L,
            isGradeMatchesCurrentSubmission = true
        )

        val apiModel = entity.toApiModel()

        assertEquals("A", apiModel.grade)
        assertEquals(10.0, apiModel.score)
        assertEquals(true, apiModel.late)
        assertEquals(false, apiModel.excused)
        assertEquals(false, apiModel.missing)
        assertEquals("late", apiModel.latePolicyStatus)
        assertEquals(123L, apiModel.customGradeStatusId)
        assertEquals("reply_to_topic", apiModel.subAssignmentTag)
        assertEquals(10.0, apiModel.enteredScore)
        assertEquals("A", apiModel.enteredGrade)
        assertEquals(1L, apiModel.userId)
        assertEquals(true, apiModel.isGradeMatchesCurrentSubmission)
    }

    @Test
    fun testConstructorFromApiModel() {
        val apiModel = SubAssignmentSubmission(
            grade = "A",
            score = 10.0,
            late = true,
            excused = false,
            missing = false,
            latePolicyStatus = "late",
            customGradeStatusId = 123L,
            subAssignmentTag = "reply_to_topic",
            enteredScore = 10.0,
            enteredGrade = "A",
            userId = 1L,
            isGradeMatchesCurrentSubmission = true
        )

        val entity = SubAssignmentSubmissionEntity(apiModel, 1L, 2L)

        assertEquals(1L, entity.submissionId)
        assertEquals(2L, entity.submissionAttempt)
        assertEquals("A", entity.grade)
        assertEquals(10.0, entity.score)
        assertEquals(true, entity.late)
        assertEquals("reply_to_topic", entity.subAssignmentTag)
    }

    private suspend fun setupSubmission(submissionId: Long, assignmentId: Long, courseId: Long) {
        val courseEntity = CourseEntity(Course(id = courseId))
        courseDao.insert(courseEntity)

        val assignmentGroupEntity = AssignmentGroupEntity(AssignmentGroup(id = 1L), courseId)
        assignmentGroupDao.insert(assignmentGroupEntity)

        val assignmentEntity = AssignmentEntity(
            Assignment(id = assignmentId, name = "Test Assignment", assignmentGroupId = 1L, courseId = courseId),
            null, null, null, null
        )
        assignmentDao.insert(assignmentEntity)

        val submissionEntity = SubmissionEntity(
            Submission(id = submissionId, assignmentId = assignmentId, attempt = 1L),
            null,
            null
        )
        submissionDao.insert(submissionEntity)
    }
}
