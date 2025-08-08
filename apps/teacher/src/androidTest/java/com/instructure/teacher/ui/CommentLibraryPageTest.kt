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
package com.instructure.teacher.ui

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addSubmissionForAssignment
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
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@UninstallModules(GraphQlApiModule::class)
@HiltAndroidTest
class CommentLibraryPageTest : TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

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
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showAllItemsWhenCommentFieldIsClicked() {
        val commentLibraryItems = createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.clickCommentField()
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(commentLibraryItems.size)
        commentLibraryItems.forEach {
            commentLibraryPage.assertSuggestionVisible(it)
        }
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showAndSelectFilteredCommentCloseCommentLibrary() {
        createCommentLibraryMockData()
        val isTablet = isTabletDevice()
        val isLandScape = isLandscapeDevice()
        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.typeComment("great work")
        if(isTablet || isLandScape) Espresso.pressBack()
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)
        commentLibraryPage.selectSuggestion(filteredSuggestion)

        speedGraderCommentsPage.assertCommentFieldHasText(filteredSuggestion)
        speedGraderPage.assertCommentLibraryNotVisible()
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun selectCommentLibrarySuggestionAndSendComment() {
        createCommentLibraryMockData()
        val isTablet = isTabletDevice()
        val isLandScape = isLandscapeDevice()
        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.typeComment("great work")
        if(isTablet || isLandScape) Espresso.pressBack()
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)
        commentLibraryPage.selectSuggestion(filteredSuggestion)

        // Check that the input field was populated with the selected comment
        speedGraderCommentsPage.assertCommentFieldHasText(filteredSuggestion)
        speedGraderPage.assertCommentLibraryNotVisible()

        // Check sending selected comment
        speedGraderCommentsPage.sendComment()
        speedGraderCommentsPage.assertDisplaysCommentText(filteredSuggestion)
        speedGraderPage.assertCommentLibraryNotVisible()
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun sendCommentFromCommentLibraryWithoutSelectingSuggestion() {
        createCommentLibraryMockData()
        val isTablet = isTabletDevice()
        val isLandScape = isLandscapeDevice()
        goToSpeedGraderCommentsPage()
        val comment = "Great work"

        speedGraderCommentsPage.typeComment(comment)
        if(isTablet || isLandScape) Espresso.pressBack()
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)
        if(isTablet || isLandScape) Espresso.pressBack()
        speedGraderCommentsPage.sendComment()

        speedGraderCommentsPage.assertDisplaysCommentText(comment)
        speedGraderPage.assertCommentLibraryNotVisible()
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun reopenCommentLibraryWhenTextIsModified() {
        createCommentLibraryMockData()
        val isTablet = isTabletDevice()
        val isLandScape = isLandscapeDevice()
        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.typeComment("great ")
        if(isTablet || isLandScape) Espresso.pressBack()
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(3)

        commentLibraryPage.closeCommentLibrary()
        speedGraderPage.assertCommentLibraryNotVisible()

        speedGraderCommentsPage.typeComment("start")
        if(isTablet || isLandScape) Espresso.pressBack()
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "You are off to a great start. Please add more detail to justify your reasoning."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)
    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showEmptyViewWhenFilteringHasNoSuggestion() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.typeComment("great work, bro") // Type something that is not present in the comment library
        commentLibraryPage.assertSuggestionListNotVisible()
        commentLibraryPage.assertEmptyViewVisible()
    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun selectCommentLibrarySuggestionFromMultipleItemResult() {
        createCommentLibraryMockData()
        val isTablet = isTabletDevice()
        val isLandScape = isLandscapeDevice()
        goToSpeedGraderCommentsPage()
        speedGraderCommentsPage.typeComment("great")
        if(isTablet || isLandScape) Espresso.pressBack()
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCountGreaterThan(1) //Make sure that we have more than 1 filter result
        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        commentLibraryPage.selectSuggestion(filteredSuggestion)

        // Check that the input field was populated with the selected comment
        speedGraderCommentsPage.assertCommentFieldHasText(filteredSuggestion)
        speedGraderPage.assertCommentLibraryNotVisible()

        // Check sending selected comment
        speedGraderCommentsPage.sendComment()
        speedGraderCommentsPage.assertDisplaysCommentText(filteredSuggestion)
        speedGraderPage.assertCommentLibraryNotVisible()
    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showAllCommentLibraryItemsAfterClearingCommentFieldFilter() {
        val commentLibraryItems = createCommentLibraryMockData()
        val isTablet = isTabletDevice()
        val isLandScape = isLandscapeDevice()

        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.typeComment("Great work!")
        if(isTablet || isLandScape) Espresso.pressBack()
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        //Select filtered suggestion and verify if it's displayed within the comment text field.
        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)
        commentLibraryPage.selectSuggestion(filteredSuggestion)

        speedGraderCommentsPage.assertCommentFieldHasText(filteredSuggestion)
        speedGraderPage.assertCommentLibraryNotVisible()

        //Clearing the comment text field.
        speedGraderCommentsPage.clearComment()

        //Verify that after clearing the comment text field, all of the comment library suggestions are visible.
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(commentLibraryItems.size)
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
            CanvasContextPermission() // Just need to have some sort of permissions object registered
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
        speedGraderPage.selectCommentsTab()
    }
}