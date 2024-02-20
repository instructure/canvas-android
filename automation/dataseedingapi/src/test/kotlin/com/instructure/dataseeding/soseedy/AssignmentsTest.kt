//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package com.instructure.dataseeding.soseedy

import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.dataseeding.api.SectionsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.AssignmentOverrideApiModel
import com.instructure.dataseeding.model.AttachmentApiModel
import com.instructure.dataseeding.model.FileType
import com.instructure.dataseeding.model.GradingType.PERCENT
import com.instructure.dataseeding.model.SubmissionApiModel
import com.instructure.dataseeding.model.SubmissionCommentApiModel
import com.instructure.dataseeding.model.SubmissionType.ONLINE_TEXT_ENTRY
import com.instructure.dataseeding.model.SubmissionType.ONLINE_UPLOAD
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AssignmentsTest {
    private val course = CoursesApi.createCourse()
    private val teacher = UserApi.createCanvasUser()
    private val student = UserApi.createCanvasUser()
    private val dueAt = "2020-02-01T11:59:59Z"
    private val unlockAt = "2020-01-01T11:59:59Z"
    private val lockAt = "2020-03-01T11:59:59Z"

    @Before
    fun setUp() {
        EnrollmentsApi.enrollUserAsTeacher(course.id, teacher.id)
        EnrollmentsApi.enrollUserAsStudent(course.id, student.id)
    }

    @Test
    fun createAssignment() {
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = false,
                lockAt = lockAt,
                unlockAt = unlockAt,
                dueAt = dueAt,
                submissionTypes = listOf(ONLINE_UPLOAD),
                allowedExtensions = listOf("pdf","docx"),
                pointsPossible = 100.0,
                gradingType = PERCENT,
                teacherToken = teacher.token
        ))
        assertThat(assignment, instanceOf(AssignmentApiModel::class.java))
        assertTrue(assignment.id >= 1)
        assertEquals(course.id.toString(), assignment.courseId)
        assertTrue(assignment.name.isNotEmpty())
        assertTrue(assignment.published)
        assertNotNull(assignment.pointsPossible)
        assertEquals(100.0, assignment.pointsPossible ?: 0.0, 0.01)
        assertEquals("percent", assignment.gradingType)
        assertEquals(listOf("online_upload"), assignment.submissionTypes)
        assertEquals(listOf("pdf", "docx"), assignment.allowedExtensions)
        assertEquals(dueAt, assignment.dueAt)
        assertEquals(lockAt, assignment.lockAt)
        assertEquals(unlockAt, assignment.unlockAt)
    }

    @Test
    fun submitCourseAssignment() {
        val assignment = AssignmentsApi.createAssignment( AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token
        ))
        val submission = SubmissionsApi.submitCourseAssignment(
                submissionType = ONLINE_TEXT_ENTRY,
                courseId = course.id,
                assignmentId = assignment.id,
                studentToken = student.token,
                fileIds = mutableListOf()
        )
        assertThat(submission, instanceOf(SubmissionApiModel::class.java))
        assertTrue(submission.id >= 1)
        assertTrue(submission.body?.isNotEmpty() ?: false) // fail on null body
        assertEquals(0, submission.submissionComments.size)
        assertEquals(0, submission.submissionAttachments?.size ?: 0 ) // null attachment list OK
    }

    @Test
    fun createCourseAssignmentSubmissionComment() {
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token
        ))
        val submission = SubmissionsApi.commentOnSubmission(course.id,student.token,assignment.id,ArrayList())
        assertThat(submission, instanceOf(AssignmentApiModel::class.java))
        assertEquals(1, submission.submissionComments?.size ?: 0)
        val comment = submission.submissionComments?.get(0)
        assertThat(comment, instanceOf(SubmissionCommentApiModel::class.java))
        assertEquals(student.shortName, comment?.authorName)
        assertTrue(comment?.comment?.isNotEmpty() ?: false)
        assertNull(comment?.attachments)
    }

    @Test
    fun gradeSubmission() {
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(ONLINE_TEXT_ENTRY),
                pointsPossible = 100.0,
                teacherToken = teacher.token
        ))
        val submission = SubmissionsApi.gradeSubmission(
                courseId = course.id,
                teacherToken = teacher.token,
                assignmentId = assignment.id,
                studentId = student.id,
                postedGrade = "90",
                excused = false
        )
        assertThat(submission, instanceOf(SubmissionApiModel::class.java))
        assertEquals(submission.grade, "90")
        assertEquals(submission.excused, false)
    }

    @Test
    fun createAssignmentOverride() {
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token
        ))
        val assignmentOverride = AssignmentsApi.createAssignmentOverride(AssignmentsApi.CreateAssignmentOverrideRequest(
                courseId = course.id,
                assignmentId = assignment.id,
                token = teacher.token,
                studentIds = listOf(student.id),
                dueAt = dueAt,
                unlockAt = unlockAt,
                lockAt = lockAt
        ))
        assertThat(assignmentOverride, instanceOf(AssignmentOverrideApiModel::class.java))
        assertTrue(assignmentOverride.id >= 1)
        assertEquals(assignment.id, assignmentOverride.assignmentId)
        assertTrue(assignmentOverride.title.isNotEmpty())
        assertEquals(1, assignmentOverride.studentIds?.size ?: 0)
        assertNull(assignmentOverride.groupId)
        assertNull(assignmentOverride.courseSectionId)
        assertEquals(student.id, assignmentOverride.studentIds?.get(0))
        assertEquals(dueAt, assignmentOverride.dueAt)
        assertEquals(unlockAt, assignmentOverride.unlockAt)
        assertEquals(lockAt, assignmentOverride.lockAt)
    }

    @Test
    fun createAssignmentOverride_courseGroup() {
        val category = GroupsApi.createCourseGroupCategory(
                courseId = course.id,
                teacherToken = teacher.token
        )
        val group = GroupsApi.createGroup(
                groupCategoryId = category.id,
                teacherToken = teacher.token
        )
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                groupCategoryId = category.id
        ))
        val assignmentOverride = AssignmentsApi.createAssignmentOverride(AssignmentsApi.CreateAssignmentOverrideRequest(
                courseId = course.id,
                assignmentId = assignment.id,
                token = teacher.token,
                groupId = group.id,
                dueAt = dueAt,
                unlockAt = unlockAt,
                lockAt = lockAt
        ))
        assertThat(assignmentOverride, instanceOf(AssignmentOverrideApiModel::class.java))
        assertTrue(assignmentOverride.id >= 1)
        assertEquals(assignment.id, assignmentOverride.assignmentId)
        assertTrue(assignmentOverride.title.isNotEmpty())
        assertEquals(0, assignmentOverride.studentIds?.size ?: 0)
        assertEquals(group.id, assignmentOverride.groupId)
        assertNull(assignmentOverride.courseSectionId)
        assertEquals(dueAt, assignmentOverride.dueAt)
        assertEquals(unlockAt, assignmentOverride.unlockAt)
        assertEquals(lockAt, assignmentOverride.lockAt)
    }

    @Test
    fun createAssignmentOverride_courseSection() {
        val section = SectionsApi.createSection(
                courseId = course.id
        )
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                withDescription = true,
                submissionTypes = listOf(ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token
        ))
        val assignmentOverride = AssignmentsApi.createAssignmentOverride(AssignmentsApi.CreateAssignmentOverrideRequest(
                courseId = course.id,
                assignmentId = assignment.id,
                token = teacher.token,
                courseSectionId = section.id,
                dueAt = dueAt,
                unlockAt = unlockAt,
                lockAt = lockAt
        ))
        assertThat(assignmentOverride, instanceOf(AssignmentOverrideApiModel::class.java))
        assertTrue(assignmentOverride.id >= 1)
        assertEquals(assignment.id, assignmentOverride.assignmentId)
        assertTrue(assignmentOverride.title.isNotEmpty())
        assertEquals(0, assignmentOverride.studentIds?.size ?: 0)
        assertNull(assignmentOverride.groupId)
        assertEquals(section.id, assignmentOverride.courseSectionId)
        assertEquals(dueAt, assignmentOverride.dueAt)
        assertEquals(unlockAt, assignmentOverride.unlockAt)
        assertEquals(lockAt, assignmentOverride.lockAt)
    }


    @Test
    fun seedAssignments() {
        for (assignmentCount in 0..2) {
            val request = AssignmentsApi.CreateAssignmentRequest (
                    courseId = course.id,
                    submissionTypes = listOf(),
                    teacherToken = teacher.token
            )

            val assignments = AssignmentsApi.seedAssignments(request, assignmentCount)
            if(assignments.isNotEmpty()) {
                assertThat(assignments[0], instanceOf(AssignmentApiModel::class.java))
            }
            assertEquals(assignmentCount, assignments.size)
        }
    }

    @Test
    fun seedAssignmentSubmission() {
        for (submissionCount in 0..2) {
            val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                    courseId = course.id,
                    submissionTypes = listOf(ONLINE_TEXT_ENTRY),
                    teacherToken = teacher.token
            ))
            val attachment = AttachmentApiModel(id = 1, displayName = "TestAttachment", fileName = "TestAttachmentFilename")
            val submissionSeed = SubmissionsApi.SubmissionSeedInfo(
                    submissionType = ONLINE_TEXT_ENTRY,
                    amount = submissionCount,
                    fileType = FileType.TEXT,
                    attachmentsList = mutableListOf(attachment)
            )
            val request = SubmissionsApi.SubmissionSeedRequest(
                    assignmentId = assignment.id,
                    courseId = course.id,
                    studentToken = student.token,
                    submissionSeedsList = listOf(submissionSeed)
            )
            val submissions = SubmissionsApi.seedAssignmentSubmission( course.id, student.token, assignment.id, submissionSeedsList = listOf(submissionSeed))
            if(submissions.isNotEmpty()) {
                assertThat(submissions[0], instanceOf(SubmissionApiModel::class.java))
            }
            assertEquals(submissionCount, submissions.size)
        }
    }
}
