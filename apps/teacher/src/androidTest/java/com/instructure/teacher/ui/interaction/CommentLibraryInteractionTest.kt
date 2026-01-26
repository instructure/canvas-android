/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
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
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.pandautils.di.DifferentiationTagsModule
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@UninstallModules(GraphQlApiModule::class, DifferentiationTagsModule::class, CustomGradeStatusModule::class)
@HiltAndroidTest
class CommentLibraryInteractionTest : TeacherComposeTest() {

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

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showAllItemsWhenCommentFieldIsClicked() {
        val commentLibraryItems = createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderPage.clickCommentLibraryButton()

        speedGraderPage.assertCommentLibraryTitle()
        speedGraderPage.assertCommentLibraryItemCount(commentLibraryItems.size)
        commentLibraryItems.forEach {
            speedGraderPage.assertCommentLibraryItemDisplayed(it)
        }
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun selectCommentLibrarySuggestionComment() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderPage.clickCommentLibraryButton()
        speedGraderPage.assertCommentLibraryTitle()

        speedGraderPage.typeInCommentLibraryFilter("Great work")
        speedGraderPage.assertCommentLibraryItemCount(1)
        speedGraderPage.selectCommentLibraryResultItem(0)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        speedGraderPage.assertCommentLibraryFilterContains(filteredSuggestion)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun selectCommentLibrarySuggestionAndSendComment() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderPage.clickCommentLibraryButton()
        speedGraderPage.assertCommentLibraryTitle()

        speedGraderPage.typeInCommentLibraryFilter("Great work")
        speedGraderPage.assertCommentLibraryItemCount(1)
        speedGraderPage.selectCommentLibraryResultItem(0)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        speedGraderPage.assertCommentLibraryFilterContains(filteredSuggestion)

        speedGraderPage.clickSendCommentButton(commentLibraryOpened = true)
        speedGraderPage.assertCommentDisplayed(filteredSuggestion, author = null)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun sendCommentFromCommentLibraryWithoutSelectingSuggestion() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderPage.clickCommentLibraryButton()
        speedGraderPage.assertCommentLibraryTitle()

        speedGraderPage.typeInCommentLibraryFilter("Great work")
        speedGraderPage.assertCommentLibraryItemCount(1)

        speedGraderPage.clickCloseCommentLibraryButton()

        speedGraderPage.clickSendCommentButton()

        speedGraderPage.assertCommentDisplayed("Great work", author = null)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun reopenCommentLibraryWhenTextIsModified() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderPage.clickCommentLibraryButton()
        speedGraderPage.assertCommentLibraryTitle()
        speedGraderPage.typeInCommentLibraryFilter("great ")
        speedGraderPage.assertCommentLibraryItemCount(3)

        speedGraderPage.clickCloseCommentLibraryButton()

        speedGraderPage.clickCommentLibraryButton()
        speedGraderPage.assertCommentLibraryTitle()

        speedGraderPage.typeInCommentLibraryFilter("start")
        speedGraderPage.assertCommentLibraryItemCount(1)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showEmptyViewWhenFilteringHasNoSuggestion() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderPage.clickCommentLibraryButton()
        speedGraderPage.assertCommentLibraryTitle()

        speedGraderPage.typeInCommentLibraryFilter("Great work! bro")
        speedGraderPage.assertCommentLibraryItemCount(0)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun selectCommentLibrarySuggestionFromMultipleItemResult() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderPage.clickCommentLibraryButton()
        speedGraderPage.assertCommentLibraryTitle()

        speedGraderPage.typeInCommentLibraryFilter("great")
        speedGraderPage.assertCommentLibraryItemCount(3)
        speedGraderPage.selectCommentLibraryResultItem(1)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        speedGraderPage.assertCommentLibraryFilterContains(filteredSuggestion)

        speedGraderPage.clickSendCommentButton(commentLibraryOpened = true)
        speedGraderPage.assertCommentDisplayed(filteredSuggestion, author = null)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showAllCommentLibraryItemsAfterClearingCommentFieldFilter() {
        val commentLibraryItems = createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderPage.clickCommentLibraryButton()
        speedGraderPage.assertCommentLibraryTitle()

        speedGraderPage.typeInCommentLibraryFilter("Great work!")
        speedGraderPage.assertCommentLibraryItemCount(1)
        speedGraderPage.selectCommentLibraryResultItem(0)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        speedGraderPage.assertCommentLibraryFilterContains(filteredSuggestion)

        speedGraderPage.clearComment()
        speedGraderPage.assertCommentLibraryItemCount(commentLibraryItems.size)
    }

    private fun createCommentLibraryMockData(): List<String> {
        val data = MockCanvas.init(
            teacherCount = 1,
            studentCount = 1,
            courseCount = 1,
            favoriteCourseCount = 1
        )
        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        data.addCoursePermissions(
            course.id,
            CanvasContextPermission()
        )

        val settings = data.userSettings[teacher.id]!!.copy(commentLibrarySuggestions = true)
        data.userSettings[teacher.id] = settings

        val commentLibraryItems = listOf(
            "You are off to a great start. Please add more detail to justify your reasoning.",
            "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit.",
            "Nicely done, group! Great collaboration!"
        )
        data.commentLibraryItems[teacher.id] = commentLibraryItems

        return commentLibraryItems
    }

    private fun goToSpeedGraderCommentsPage() {
        val data = MockCanvas.data
        val teacher = data.teachers[0]
        val course = data.courses.values.first()
        val student = data.students[0]

        val assignment = data.addAssignment(
            courseId = course.id,
            submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        )

        data.addSubmissionForAssignment(
            assignmentId = assignment.id,
            userId = student.id,
            type = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
        )

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
        composeTestRule.waitForIdle()
    }
}