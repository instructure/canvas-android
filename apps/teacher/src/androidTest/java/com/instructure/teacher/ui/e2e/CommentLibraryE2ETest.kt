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
package com.instructure.teacher.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
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
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CommentLibraryE2ETest : TeacherComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Stub
    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testCommentLibraryE2E() {

        val data = seedData(teachers = 1, students = 1, courses = 2)
        val teacher = data.teachersList[0]
        val student = data.studentsList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Make an assignment with a submission for the '${course.name}' course and '${student.name}' student. Set the 'Show suggestions when typing' setting to see the comment library itself.")
        val testAssignment = prepareSettingsAndMakeAssignmentWithSubmission(course, student.token, teacher.token, teacher.id)

        Log.d(PREPARATION_TAG,"Generate comments for comment library.")
        val testComment = "Test Comment"
        val testComment2 = "This is another test comment."
        CommentLibraryApi.createComment(course.id, teacher.token, testComment)
        CommentLibraryApi.createComment(course.id, teacher.token, testComment2)

        tokenLogin(teacher)

        Log.d(STEP_TAG,"Navigate to submission's comments tab.")
        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(testAssignment)
        assignmentDetailsPage.openAllSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectCommentsTab()

        val testText = "another"
        Log.d(STEP_TAG,"Type '$testText' word and check if there is only one matching suggestion visible.")
        speedGraderCommentsPage.typeComment(testText)
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        commentLibraryPage.closeCommentLibrary()
        speedGraderPage.assertCommentLibraryNotVisible()

        Log.d(STEP_TAG, "Clear comment input field and verify if all the suggestions is displayed again.")
        speedGraderCommentsPage.clearComment()
        commentLibraryPage.assertSuggestionsCount(2)

        val testText2 = "test"
        Log.d(STEP_TAG,"Type '$testText2' word and check if there are two matching suggestion visible.")
        commentLibraryPage.closeCommentLibrary()
        speedGraderCommentsPage.typeComment(testText2)
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(2)
        commentLibraryPage.assertSuggestionVisible(testComment)
        commentLibraryPage.assertSuggestionVisible(testComment2)

        Log.d(STEP_TAG, "Select a suggestion and check if it's filled into the comment text field and the comment library is closed.")
        commentLibraryPage.selectSuggestion(testComment2)
        speedGraderCommentsPage.assertCommentFieldHasText(testComment2)
        speedGraderPage.assertCommentLibraryNotVisible()

        Log.d(STEP_TAG,"Send the previously selected comment and check if it's successfully sent.")
        speedGraderCommentsPage.sendComment()
        speedGraderCommentsPage.assertDisplaysCommentText(testComment2)

        Log.d(STEP_TAG, "Clear the comment, check if all suggestions are displayed and the comment library is closed.")
        speedGraderCommentsPage.clearComment()
        commentLibraryPage.assertSuggestionsCount(2)
        commentLibraryPage.closeCommentLibrary()

        Log.d(STEP_TAG,"Type some words which does not match with any of the suggestions in the comment library. Check that suggestions are not visible and empty view is visible.")
        speedGraderCommentsPage.typeComment("empty filter")
        commentLibraryPage.assertSuggestionListNotVisible()
        commentLibraryPage.assertEmptyViewVisible()
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