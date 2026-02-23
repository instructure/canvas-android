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
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.pandautils.di.DifferentiationTagsModule
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@UninstallModules(
    GraphQlApiModule::class,
    CustomGradeStatusModule::class,
    DifferentiationTagsModule::class
)
@HiltAndroidTest
class SpeedGraderFilesInteractionTest : TeacherComposeTest() {

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
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    @BindValue
    @JvmField
    val recentGradedSubmissionsManager: RecentGradedSubmissionsManager = FakeRecentGradedSubmissionsManager()

    // Just good enough to mock the *representation* of a file, not to mock the file itself.
    val attachment = Attachment(
            id = 131313,
            contentType = "video/mp4",
            filename = "sampleVideo.mp4",
            displayName = "sampleVideo.mp4",
            url = "http://fake.blah/somePath" // Code/Test will crash w/o a non-null url
    )

    @Test
    fun displaysEmptyFilesView() {
        goToSpeedGraderContentPage()
        speedGraderPage.assertEmptyViewDisplayed()
    }

    @Test
    fun displaysSelectedFile() {
        goToSpeedGraderContentPage(submissionCount = 1)
        attachment.displayName?.let { speedGraderPage.assertSelectedAttachmentItemDisplayed(it) }
    }

    @Test
    fun selectBetweenMultipleFiles() {
        goToSpeedGraderContentPage(multipleFiles = true)

        speedGraderPage.assertSelectedAttachmentItemDisplayed("sampleVideo1.mp4")
        speedGraderPage.clickAttachmentSelector()
        speedGraderPage.selectAttachment("sampleVideo2.mp4")
        speedGraderPage.assertSelectedAttachmentItemDisplayed("sampleVideo2.mp4")
        speedGraderPage.clickAttachmentSelector()
        speedGraderPage.selectAttachment("sampleVideo3.mp4")
        speedGraderPage.assertSelectedAttachmentItemDisplayed("sampleVideo3.mp4")
    }

    private fun goToSpeedGraderContentPage(submissionCount: Int = 0, multipleFiles: Boolean = false): MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, studentCount = 1, courseCount = 1, favoriteCourseCount = 1)
        val teacher = data.teachers[0]
        val course = data.courses.values.first()
        val student = data.students[0]

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission()
        )

        val assignment = data.addAssignment(
                courseId = course.id,
                submissionTypeList = listOf(Assignment.SubmissionType.MEDIA_RECORDING)
        )

        if (multipleFiles) {
            val attachments = listOf(
                Attachment(
                    id = 131313,
                    contentType = "video/mp4",
                    filename = "sampleVideo1.mp4",
                    displayName = "sampleVideo1.mp4",
                    url = "http://fake.blah/somePath1"
                ),
                Attachment(
                    id = 131314,
                    contentType = "video/mp4",
                    filename = "sampleVideo2.mp4",
                    displayName = "sampleVideo2.mp4",
                    url = "http://fake.blah/somePath2"
                ),
                Attachment(
                    id = 131315,
                    contentType = "video/mp4",
                    filename = "sampleVideo3.mp4",
                    displayName = "sampleVideo3.mp4",
                    url = "http://fake.blah/somePath3"
                )
            )

            data.addSubmissionForAssignment(
                assignmentId = assignment.id,
                userId = student.id,
                type = Assignment.SubmissionType.MEDIA_RECORDING.apiString,
                attachment = attachments[0]
            )

            val submission = data.submissions[assignment.id]?.firstOrNull { it.userId == student.id }
            submission?.let {
                it.attachments.addAll(attachments.subList(1, attachments.size))
                it.submissionHistory.firstOrNull()?.attachments?.addAll(attachments.subList(1, attachments.size))
            }
        } else {
            repeat(submissionCount) {
                data.addSubmissionForAssignment(
                    assignmentId = assignment.id,
                    userId = student.id,
                    type = Assignment.SubmissionType.MEDIA_RECORDING.apiString,
                    attachment = attachment
                )
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

        return data
    }
}
