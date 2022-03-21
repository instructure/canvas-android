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

import androidx.test.espresso.Espresso
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.CreateCommentMutation
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.model.UserSettingsApiModel
import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CommentLibraryE2ETest : TeacherTest() {

    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DASHBOARD, TestCategory.E2E)
    fun testCommentLibraryE2E() {

        val data = seedData(teachers = 1, students = 1, courses = 2)
        val teacher = data.teachersList[0]
        val student = data.studentsList[0]
        val course = data.coursesList[0]

        val testAssignment = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                gradingType = GradingType.POINTS,
                teacherToken = teacher.token,
                pointsPossible = 25.0,
                dueAt = 1.days.fromNow.iso8601
            )
        )

        SubmissionsApi.submitCourseAssignment(
            submissionType = SubmissionType.ONLINE_TEXT_ENTRY,
            courseId = course.id,
            assignmentId = testAssignment.id,
            fileIds = emptyList<Long>().toMutableList(),
            studentToken = student.token
        )

        val request = UserSettingsApiModel(
            manualMarkAsRead = false,
            collapseGlobalNav = false,
            hideDashCardColorOverlays = false,
            commentLibrarySuggestions = true
        )
        UserApi.putSelfSettings(teacher.id, request) // Set comment library "Show suggestions when typing" user settings to be able to see the library comments.

        val apolloClient = ApolloClient.builder()
            .serverUrl("https://mobileqa.beta.instructure.com/api/graphql/")
            .okHttpClient(CanvasRestAdapter.okHttpClientForApollo(teacher.token))
            .build()

        val testComment = "Test Comment"
        val testComment2 = "This is another test comment."
        val mutationCall = CreateCommentMutation(course.id.toString(), testComment)
        val mutationCall2 = CreateCommentMutation(course.id.toString(), testComment2)
        apolloClient.mutate(mutationCall).enqueue(object : ApolloCall.Callback<CreateCommentMutation.Data>() {
            override fun onResponse(response: Response<CreateCommentMutation.Data>) = Unit
            override fun onFailure(e: ApolloException) = Unit
        })

        apolloClient.mutate(mutationCall2).enqueue(object : ApolloCall.Callback<CreateCommentMutation.Data>() {
            override fun onResponse(response: Response<CreateCommentMutation.Data>) = Unit
            override fun onFailure(e: ApolloException) = Unit
        })

        tokenLogin(teacher)

        //Open the test course, and then open the test assignment. After that, grade the submission, and navigate to comments tab, and focus on comment input text field.
        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(testAssignment)
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectCommentsTab()

        //Type 'another' word and check if there is only one matching suggestion visible.
        speedGraderCommentsPage.typeComment("another")
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(1)

        //Close the comment library and assert if it's closed.
        commentLibraryPage.closeCommentLibrary()
        speedGraderPage.assertCommentLibraryNotVisible()

        //Clear comment input field, and check that after clearing, all the suggestions are displayed.
        speedGraderCommentsPage.clearComment()
        commentLibraryPage.assertSuggestionsCount(2)

        //Type the word 'test' into the comments input field, and check that the corresponding suggestion are displayed.
        commentLibraryPage.closeCommentLibrary()
        speedGraderCommentsPage.typeComment("test")
        commentLibraryPage.assertPageObjects()
        commentLibraryPage.assertSuggestionsCount(2)

        commentLibraryPage.assertSuggestionVisible(testComment)
        commentLibraryPage.assertSuggestionVisible(testComment2)

        //Select one of the suggestions and assert if that it is filled into the input text field and the comment library is closed.
        commentLibraryPage.selectSuggestion(testComment2)
        speedGraderCommentsPage.assertCommentFieldHasText(testComment2)
        speedGraderPage.assertCommentLibraryNotVisible()

        //Send the previously selected comment and check if it's successfully sent.
        speedGraderCommentsPage.sendComment()
        speedGraderCommentsPage.assertDisplaysCommentText(testComment2)

        //Clear the comment again, and check if all the suggestion are displayed, then close the comment library.
        speedGraderCommentsPage.clearComment()
        commentLibraryPage.assertSuggestionsCount(2)
        commentLibraryPage.closeCommentLibrary()

        //Type some words which does not match with any of the suggestions in the comment library. Check that suggestions are not visible and empty view is visible.
        speedGraderCommentsPage.typeComment("empty filter")
        commentLibraryPage.assertSuggestionListNotVisible()
        commentLibraryPage.assertEmptyViewVisible()
    }
}