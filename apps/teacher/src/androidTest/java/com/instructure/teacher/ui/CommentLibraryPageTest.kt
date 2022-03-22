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

import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvas.espresso.mockCanvas.fakes.FakeCommentLibraryManager
import com.instructure.canvasapi2.di.GraphQlApiModule
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Test

@UninstallModules(GraphQlApiModule::class)
@HiltAndroidTest
class CommentLibraryPageTest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    @BindValue
    @JvmField
    val commentLibraryManager: CommentLibraryManager = FakeCommentLibraryManager()

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
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

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showAndSelectFilteredCommentCloseCommentLibrary() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.typeComment("great work")
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)
        commentLibraryPage.selectSuggestion(filteredSuggestion)

        speedGraderCommentsPage.assertCommentFieldHasText(filteredSuggestion)
        speedGraderPage.assertCommentLibraryNotVisible()
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun selectCommentLibrarySuggestionAndSendComment() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.typeComment("great work")
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

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun sendCommentFromCommentLibraryWithoutSelectingSuggestion() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()
        val comment = "Great work"

        speedGraderCommentsPage.typeComment(comment)
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)

        speedGraderCommentsPage.sendComment()

        speedGraderCommentsPage.assertDisplaysCommentText(comment)
        speedGraderPage.assertCommentLibraryNotVisible()
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun reopenCommentLibraryWhenTextIsModified() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.typeComment("great ")
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(3)

        commentLibraryPage.closeCommentLibrary()
        speedGraderPage.assertCommentLibraryNotVisible()

        speedGraderCommentsPage.typeComment("start")
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "You are off to a great start. Please add more detail to justify your reasoning."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showEmptyViewWhenFilteringHasNoSuggestion() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.typeComment("great work, bro") // Type something that is not present in the comment library
        commentLibraryPage.assertSuggestionListNotVisible()
        commentLibraryPage.assertEmptyViewVisible()
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun selectCommentLibrarySuggestionFromMultipleItemResult() {
        createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()
        speedGraderCommentsPage.typeComment("great")
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

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showAllCommentLibraryItemsAfterClearingCommentFieldFilter() {
        val commentLibraryItems = createCommentLibraryMockData()
        goToSpeedGraderCommentsPage()

        speedGraderCommentsPage.typeComment("Great work!")
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
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        )

        data.addSubmissionForAssignment(
            assignmentId = assignment.id,
            userId = student.id,
            type = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
        )

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectCommentsTab()
    }
}