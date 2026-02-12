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

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignment
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.addSubmissionForAssignment
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeAssignmentDetailsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCommentLibraryManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeDifferentiationTagsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeInboxSettingsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakePostPolicyManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeRecentGradedSubmissionsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeStudentContextManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionCommentsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionContentManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionDetailsManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionGradeManager
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeSubmissionRubricManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.canvasapi2.managers.PostPolicyManager
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.managers.SubmissionRubricManager
import com.instructure.canvasapi2.managers.graphql.AssignmentDetailsManager
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.managers.graphql.DifferentiationTagsManager
import com.instructure.canvasapi2.managers.graphql.RecentGradedSubmissionsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.SubmissionType.EXTERNAL_TOOL
import com.instructure.canvasapi2.models.Assignment.SubmissionType.NONE
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ONLINE_TEXT_ENTRY
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ONLINE_URL
import com.instructure.canvasapi2.models.Assignment.SubmissionType.ON_PAPER
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.type.GradingType
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.iso8601
import com.instructure.pandautils.di.DifferentiationTagsModule
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@HiltAndroidTest
@UninstallModules(
    GraphQlApiModule::class,
    DifferentiationTagsModule::class,
    CustomGradeStatusModule::class
)
class SpeedGraderInteractionTest : TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    @BindValue
    @JvmField
    val commentLibraryManager: CommentLibraryManager = FakeCommentLibraryManager()

    @BindValue
    @JvmField
    val differentiationTagsManager: DifferentiationTagsManager = FakeDifferentiationTagsManager()

    @BindValue
    @JvmField
    val postPolicyManager: PostPolicyManager = FakePostPolicyManager()

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

    @BindValue
    @JvmField
    val recentGradedSubmissionsManager: RecentGradedSubmissionsManager = FakeRecentGradedSubmissionsManager()

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    @Test
    fun displaysSubmissionDropDown() {
        goToSpeedGraderPage(attempts = 2)
        speedGraderPage.assertHasSubmissionDropDown()
    }

    @Test
    fun opensToCorrectSubmission() {
        val data = goToSpeedGraderPage(studentCount = 4)
        speedGraderPage.clickBackButton()
        val students = data.students
        students.forEach {
            assignmentSubmissionListPage.clickSubmission(it)
            speedGraderPage.assertGradingStudent(it)
            speedGraderPage.clickBackButton()
        }
    }

    @Test
    fun displaysTextSubmission() {
        goToSpeedGraderPage(submissionTypeList = listOf(ONLINE_TEXT_ENTRY))
        speedGraderPage.assertDisplaysTextSubmissionView()
    }

    @Test
    fun displaysUnsubmittedEmptyState() {
        goToSpeedGraderPage(attempts = 0)
        speedGraderPage.assertDisplaysEmptyState(R.string.noSubmissionTeacher)
    }

    @Test
    fun displaysNoSubmissionsAllowedEmptyState() {
        goToSpeedGraderPage(submissionTypeList = listOf(NONE), attempts = 0)
        speedGraderPage.assertDisplaysEmptyState(R.string.speedGraderNoneMessage)
    }

    @Test
    fun displaysOnPaperEmptyState() {
        goToSpeedGraderPage(submissionTypeList = listOf(ON_PAPER), attempts = 0)
        speedGraderPage.assertDisplaysEmptyState(R.string.speedGraderOnPaperMessage)
    }

    @Test
    fun displaysExternalToolEmptyState() {
        goToSpeedGraderPage(submissionTypeList = listOf(EXTERNAL_TOOL), attempts = 0)
        speedGraderPage.assertDisplaysEmptyState(R.string.noSubmissionTeacher)
    }

    @Test
    fun displaysUrlSubmission() {
        val data = goToSpeedGraderPage(submissionTypeList = listOf(ONLINE_URL))
        val assignment = data.assignments.values.first()
        val submission = data.submissions[assignment.id]!!.first()
        speedGraderPage.assertDisplaysUrlSubmissionLink(submission)
        speedGraderPage.assertDisplaysUrlWebView()
    }

    private fun goToSpeedGraderPage(
        gradingType: GradingType = GradingType.points,
        pointsPossible: Int = 20,
        score: Double = 12.0,
        grade: String = "60%",
        attempts: Int = 1,
        studentCount: Int = 1,
        submissionTypeList: List<Assignment.SubmissionType> = listOf(ONLINE_TEXT_ENTRY)
    ): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, studentCount = studentCount, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val student = data.students[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission()
        )

        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = submissionTypeList,
            pointsPossible = pointsPossible,
            gradingType = gradingType.rawValue,
            dueAt = 1.days.ago.iso8601
        )

        data.students.forEach { currentStudent ->
            repeat(attempts) { index ->
                val submissionType = submissionTypeList.firstOrNull() ?: ONLINE_TEXT_ENTRY
                when (submissionType) {
                    ONLINE_URL -> {
                        data.addSubmissionForAssignment(
                            assignmentId = assignment.id,
                            userId = currentStudent.id,
                            type = submissionType.apiString,
                            url = "www.google.com",
                            score = score,
                            grade = grade,
                            attempt = (index + 1).toLong()
                        )
                    }
                    else -> {
                        data.addSubmissionForAssignment(
                            assignmentId = assignment.id,
                            userId = currentStudent.id,
                            type = submissionType.apiString,
                            body = "Submission attempt ${index + 1} for ${currentStudent.shortName}",
                            score = score,
                            grade = grade,
                            attempt = (index + 1).toLong()
                        )
                    }
                }
            }
        }

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)
        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickAllSubmissions()
        assignmentSubmissionListPage.clickSubmission(student)
        composeTestRule.waitForIdle()
        if (isCompactDevice()) speedGraderPage.clickExpandPanelButton()
        speedGraderPage.selectTab("Grade & Rubric")

        return data
    }
}
