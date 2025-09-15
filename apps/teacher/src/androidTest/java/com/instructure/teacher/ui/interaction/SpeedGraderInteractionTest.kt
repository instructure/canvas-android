/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.annotations.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addSubmissionsForAssignment
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeAssignmentDetailsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCommentLibraryManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeInboxSettingsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeStudentContextManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionCommentsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionContentManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionDetailsManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionGradeManager
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeSubmissionRubricManager
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.managers.SubmissionRubricManager
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.SubmissionType.EXTERNAL_TOOL
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ONLINE_TEXT_ENTRY
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ONLINE_URL
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ON_PAPER
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@HiltAndroidTest
@UninstallModules(GraphQlApiModule::class)
class SpeedGraderInteractionTest : TeacherComposeTest() {

    @BindValue
    @JvmField
    val commentLibraryManager: CommentLibraryManager = FakeCommentLibraryManager()

    @BindValue
    @JvmField
    val inboxSettingsManager: InboxSettingsManager = FakeInboxSettingsManager()

    @BindValue
    @JvmField
    val personContextManager: StudentContextManager = FakeStudentContextManager()

    @BindValue
    @JvmField
    val assignmentDetailsManager: AssignmentDetailsManager = FakeAssignmentDetailsManager()

    @BindValue
    @JvmField
    val submissionContentManager: SubmissionContentManager = FakeSubmissionContentManager()

    @BindValue
    @JvmField
    val submissionGradeManager: SubmissionGradeManager = FakeSubmissionGradeManager()

    @BindValue
    @JvmField
    val submissionDetailsManager: SubmissionDetailsManager = FakeSubmissionDetailsManager()

    @BindValue
    @JvmField
    val submissionRubricManager: SubmissionRubricManager = FakeSubmissionRubricManager()

    @BindValue
    @JvmField
    val submissionCommentsManager: SubmissionCommentsManager = FakeSubmissionCommentsManager()

    @Stub
    @Test
    override fun displaysPageObjects() {
        goToSpeedGraderPage()
        speedGraderPage.assertPageObjects()
    }

    @Stub
    @Test
    fun displaysSubmissionDropDown() {
        goToSpeedGraderPage(submissionTypeList = listOf(ONLINE_TEXT_ENTRY), students = 1, submissions = listOf(2))
        speedGraderPage.assertHasSubmissionDropDown()
    }

    @Stub
    @Test
    fun opensToCorrectSubmission() {
        val data = goToSpeedGraderPage(students = 4, submissionTypeList = listOf(ONLINE_TEXT_ENTRY))
        speedGraderPage.clickBackButton()
        val students = data.students
        students.forEach {
            assignmentSubmissionListPage.clickSubmission(it)
            speedGraderPage.assertGradingStudent(it)
            speedGraderPage.clickBackButton()
        }
    }

    @Stub
    @Test
    fun hasCorrectPageCount() {
        goToSpeedGraderPage(students = 4)
        speedGraderPage.assertPageCount(4)
    }

    @Stub
    @Test
    fun displaysTextSubmission() {
        goToSpeedGraderPage(submissionTypeList = listOf(ONLINE_TEXT_ENTRY), submissions = listOf(1))
        speedGraderPage.assertDisplaysTextSubmissionView()
    }

    @Stub
    @Test
    fun displaysUnsubmittedEmptyState() {
        goToSpeedGraderPage(submissionTypeList = listOf(ONLINE_TEXT_ENTRY))
        speedGraderPage.assertDisplaysEmptyState(R.string.noSubmissionTeacher)
    }

    @Stub
    @Test
    fun displaysNoSubmissionsAllowedEmptyState() {
        goToSpeedGraderPage(submissionTypeList = listOf(Assignment.SubmissionType.NONE))
        speedGraderPage.assertDisplaysEmptyState(R.string.speedGraderNoneMessage)
    }

    @Stub
    @Test
    fun displaysOnPaperEmptyState() {
        goToSpeedGraderPage(submissionTypeList = listOf(ON_PAPER))
        speedGraderPage.assertDisplaysEmptyState(R.string.speedGraderOnPaperMessage)
    }

    @Stub
    @Test
    fun displaysExternalToolEmptyState() {
        goToSpeedGraderPage(submissionTypeList = listOf(EXTERNAL_TOOL))
        speedGraderPage.assertDisplaysEmptyState(R.string.noSubmissionTeacher)
    }

    @Stub
    @Test
    fun displaysUrlSubmission() {
        val data = goToSpeedGraderPage(submissionTypeList = listOf(ONLINE_URL), submissions = listOf(1))
        val assignment = data.assignments.values.first()
        val submission = data.submissions[assignment.id]!!.first()
        speedGraderPage.assertDisplaysUrlSubmissionLink(submission)
        speedGraderPage.assertDisplaysUrlWebView()
    }

    private fun goToSpeedGraderPage(
        students: Int = 1,
        submissionTypeList: List<Assignment.SubmissionType> = listOf(Assignment.SubmissionType.NONE),
        submissions: List<Int> = listOf(0),
        selectStudent: Int = 0
    ): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, studentCount = students, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                submissionTypeList = submissionTypeList
        )

        (0 until submissions.size).map {
            if(students < it + 1) throw Exception("student count does not agree with submissions")
            val student = data.students[it]
            val submissionCount = submissions[it]
            val submissionTypesRaw = submissionTypeList.map { it.apiString }
            repeat(submissionCount) { index ->
                data.addSubmissionsForAssignment(
                        assignmentId = assignment.id,
                        userId = student.id,
                        types = submissionTypesRaw,
                        body = if(submissionTypesRaw.contains(Assignment.SubmissionType.ONLINE_URL.apiString)) null else "AssignmentBody $index",
                        url = if(submissionTypesRaw.contains(Assignment.SubmissionType.ONLINE_URL.apiString)) "www.google.com" else null
                )
            }
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickAllSubmissions()
        assignmentSubmissionListPage.clickSubmission(data.students[selectStudent])

        return data
    }
}
