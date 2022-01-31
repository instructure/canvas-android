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
import com.instructure.canvas.espresso.mockCanvas.*
import com.instructure.canvasapi2.models.*
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CommentLibraryPageTest : TeacherTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showAllItemsWhenCommentFieldIsClicked() {
        val commentLibraryItems = goToSpeedGraderCommentsPage()
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
        goToSpeedGraderCommentsPage()
        speedGraderCommentsPage.typeComment("great work")
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)
        commentLibraryPage.selectSuggestion(filteredSuggestion)

        speedGraderCommentsPage.assertCommentFieldHasText(filteredSuggestion)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun selectCommentLibrarySuggestionAndSendComment() {
        goToSpeedGraderCommentsPage()
        speedGraderCommentsPage.typeComment("great work")
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)
        commentLibraryPage.selectSuggestion(filteredSuggestion)
        speedGraderCommentsPage.sendComment()

        speedGraderCommentsPage.assertDisplaysCommentText(filteredSuggestion)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun sendCommentFromCommentLibraryWithoutSelectingSuggestion() {
        goToSpeedGraderCommentsPage()
        val comment = "Great work"

        speedGraderCommentsPage.typeComment(comment)
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)

        speedGraderCommentsPage.sendComment()

        speedGraderCommentsPage.assertDisplaysCommentText(comment)
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun reopenCommentLibraryWhenTextIsModified() {
        goToSpeedGraderCommentsPage()
        speedGraderCommentsPage.typeComment("great ")
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(3)

        commentLibraryPage.closeCommentLibrary()

        speedGraderCommentsPage.typeComment("start")
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        val filteredSuggestion = "You are off to a great start. Please add more detail to justify your reasoning."
        commentLibraryPage.assertSuggestionVisible(filteredSuggestion)
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SPEED_GRADER, TestCategory.INTERACTION)
    fun showEmptyViewWhenFilteringHasNoSuggestion() {
        goToSpeedGraderCommentsPage()
        speedGraderCommentsPage.typeComment("great work, bro")
        commentLibraryPage.assertSuggestionListNotVisible()
        commentLibraryPage.assertEmptyViewVisible()
    }

    private fun goToSpeedGraderCommentsPage(): List<String> {

        val data = MockCanvas.init(
            teacherCount = 1,
            studentCount = 1,
            courseCount = 1,
            favoriteCourseCount = 1
        )
        val teacher = data.teachers[0]
        val course = data.courses.values.first()
        val student = data.students[0]

        data.addCoursePermissions(
            course.id,
            CanvasContextPermission() // Just need to have some sort of permissions object registered
        )

        val settings = data.userSettings[teacher.id]!!.copy(commentLibrarySuggestions = true)
        data.userSettings[teacher.id] = settings

        val commentLibrarayItems = listOf(
            "You are off to a great start. Please add more detail to justify your reasoning.",
            "Great work! But it seems that you may have submitted the wrong file. Please double-check, attach the correct file, and resubmit.",
            "Nicely done, group! Great collaboration!"
        )
        data.commentLibraryItems[teacher.id] = commentLibrarayItems

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

        return commentLibrarayItems
    }
}