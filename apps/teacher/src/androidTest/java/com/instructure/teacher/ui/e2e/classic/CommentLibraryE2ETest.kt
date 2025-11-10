/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.ui.e2e.classic

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CommentLibraryApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.model.UserSettingsApiModel
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.extensions.seedData
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CommentLibraryE2ETest : TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testCommentLibraryE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, students = 1, courses = 2)
        val teacher = data.teachersList[0]
        val student = data.studentsList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Make an assignment with a submission for the '${course.name}' course and '${student.name}' student. Set the 'Show suggestions when typing' setting to see the comment library itself.")
        val testAssignment = prepareSettingsAndMakeAssignmentWithSubmission(course, student.token, teacher.token, teacher.id)

        Log.d(PREPARATION_TAG, "Generate comments for comment library.")
        val testComment = "Test Comment"
        val testComment2 = "This is another test comment."
        CommentLibraryApi.createComment(course.id, teacher.token, testComment)
        CommentLibraryApi.createComment(course.id, teacher.token, testComment2)

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)

        Log.d(STEP_TAG, "Open the '${course.name}' course.")
        dashboardPage.openCourse(course)

        Log.d(STEP_TAG, "Open the 'Assignments' tab.")
        courseBrowserPage.openAssignmentsTab()

        Log.d(STEP_TAG, "Click on the '${testAssignment.name}' assignment.")
        assignmentListPage.clickAssignment(testAssignment)

        Log.d(STEP_TAG, "Open the 'All Submissions' page.")
        assignmentDetailsPage.clickAllSubmissions()

        Log.d(STEP_TAG, "Click on the '${student.name}' student submission to open it in SpeedGrader.")
        assignmentSubmissionListPage.clickSubmission(student)

        Log.d(STEP_TAG, "Expand the SpeedGrader bottom sheet to see the comments section.")
        speedGraderPage.clickExpandPanelButton()

        Log.d(ASSERTION_TAG, "Assert that the 'Comments' label is displayed with the corresponding number of comments, which is 0 at the moment.")
        speedGraderPage.assertCommentsLabelDisplayed(0)

        Log.d(STEP_TAG, "Click on the 'Comment Library' icon to open the Comment Library.")
        speedGraderPage.clickCommentLibraryButton()

        Log.d(ASSERTION_TAG, "Assert that the comment library is opened and there are the 2 previously created comments displayed.")
        speedGraderPage.assertCommentLibraryItemCount(2)

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Comment Library'.")
        speedGraderPage.assertCommentLibraryTitle()

        val testText = "another"
        Log.d(STEP_TAG, "Type '$testText' word.")
        speedGraderPage.typeComment(testText)

        Log.d(ASSERTION_TAG, "Assert that there is only 1 comment displayed in the comment library, which matches the filter.")
        speedGraderPage.assertCommentLibraryItemCount(1)

        Log.d(STEP_TAG, "Select the comment which matches the entered (filter) text and send the comment.")
        speedGraderPage.selectCommentLibraryResultItem()

        Log.d(STEP_TAG, "Send the previously selected '$testText' comment.")
        speedGraderPage.clickSendCommentButton(commentLibraryOpened = true)

        Log.d(ASSERTION_TAG, "Assert that the 'Comments' label is displayed with the corresponding number of comments, which is 1 at the moment as we just sent a comment from the Comment Library.")
        speedGraderPage.assertCommentsLabelDisplayed(1)

        Log.d(ASSERTION_TAG, "Assert if the '$testComment2' (whole) comment is successfully sent and displayed in the comments section.")
        speedGraderPage.assertCommentDisplayed(testComment2)

        Log.d(STEP_TAG, "Click on the 'Comment Library' icon to open the Comment Library.")
        speedGraderPage.clickCommentLibraryButton()

        Log.d(ASSERTION_TAG, "Assert that the comment library is opened and there are the 2 previously created comments displayed.")
        speedGraderPage.assertCommentLibraryItemCount(2)

        val nonExistingCommentText = "csakafradi"
        Log.d(STEP_TAG, "Type a non-existing comment text, '$nonExistingCommentText' into the comment input field.")
        speedGraderPage.typeComment(nonExistingCommentText)

        Log.d(ASSERTION_TAG, "Assert that there is no comment displayed in the comment library as there's no matching comment with the entered (filter) text.")
        speedGraderPage.assertCommentLibraryItemCount(0)

        Log.d(STEP_TAG, "Clear comment input field.")
        speedGraderPage.clearComment()

        Log.d(ASSERTION_TAG, "Assert if all the (2) suggestions are displayed again.")
        speedGraderPage.assertCommentLibraryItemCount(2)

        Log.d(STEP_TAG, "Close the comment library.")
        speedGraderPage.clickCloseCommentLibraryButton()

        Log.d(STEP_TAG, "Click on the 'Comment Library' icon to open the Comment Library.")
        speedGraderPage.clickCommentLibraryButton()

        Log.d(ASSERTION_TAG, "Assert that the comment library is opened and there are the 2 previously created comments displayed.")
        speedGraderPage.assertCommentLibraryItemCount(2)

        val testText2 = "test"
        Log.d(STEP_TAG, "Type '$testText2' word.")
        speedGraderPage.typeComment(testText2)

        Log.d(ASSERTION_TAG, "Assert that there are 2 comments displayed in the comment library, which matches the filter.")
        speedGraderPage.assertCommentLibraryItemCount(2)

        Log.d(STEP_TAG, "Select the comment which matches the entered (filter) text and send the comment.")
        speedGraderPage.selectCommentLibraryResultItem(1)

        Log.d(STEP_TAG, "Send the previously selected '$testText2' comment.")
        speedGraderPage.clickSendCommentButton(commentLibraryOpened = true)

        Log.d(ASSERTION_TAG, "Assert that the 'Comments' label is displayed with the corresponding number of comments, which is 2 at the moment.")
        speedGraderPage.assertCommentsLabelDisplayed(2)

        Log.d(ASSERTION_TAG, "Assert assert both the '$testComment' and '$testComment2' (whole) comments are displayed in the comments section.")
        speedGraderPage.assertCommentDisplayed(testComment)
        speedGraderPage.assertCommentDisplayed(testComment2)
    }

    private fun prepareSettingsAndMakeAssignmentWithSubmission(
        course: CourseApiModel,
        studentToken: String,
        teacherToken: String,
        teacherId: Long
    ): AssignmentApiModel {

        val testAssignment = AssignmentsApi.createAssignment(course.id, teacherToken, pointsPossible = 25.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))
        SubmissionsApi.submitCourseAssignment(course.id, studentToken, testAssignment.id, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)

        val request = UserSettingsApiModel(
            manualMarkAsRead = false,
            collapseGlobalNav = false,
            hideDashCardColorOverlays = false,
            commentLibrarySuggestions = true
        )
        UserApi.putSelfSettings(
            teacherId,
            request
        ) // Set comment library "Show suggestions when typing" user settings to be able to see the library comments.

        return testAssignment
    }
}