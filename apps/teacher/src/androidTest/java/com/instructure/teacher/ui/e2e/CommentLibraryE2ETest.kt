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

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.instructure.canvas.espresso.E2E
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.dataseeding.CreateCommentMutation
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.AuthorizationInterceptor
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
import okhttp3.Callback
import okhttp3.OkHttpClient
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

   /*     val okHttpClient = OkHttpClient.Builder()
            .build()
*/
     //   CanvasRestAdapter.retrofitWithToken(teacher.token).create(CommentLibraryE2ETest::class.java)
        val apolloClient = ApolloClient.builder()
            .serverUrl("https://mobileqa.beta.instructure.com/graphql")
            .okHttpClient(CanvasRestAdapter.okHttpClientWithToken(teacher.token))
            .build()

        tokenLogin(teacher)

      /*  val mutationCall =  CreateCommentMutation(course.id.toString(), "Comment Test Mutation")
        val response = apolloClient.mutate(mutationCall)
*/
        val mutationCall =  CreateCommentMutation(course.id.toString(), "Comment")
        apolloClient.mutate(mutationCall).enqueue(object : ApolloCall.Callback<CreateCommentMutation.Data>() {
            override fun onResponse(response: Response<CreateCommentMutation.Data>) = Unit
            override fun onFailure(e: ApolloException) = Unit
        })


      //  CallBack.addCall(adapter.build(AccountNotificationAPI.AccountNotificationInterface::class.java, params).deleteAccountNotification(notificationId)).enqueue(callback)


        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()
        assignmentListPage.clickAssignment(testAssignment)
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.selectCommentsTab()
        speedGraderCommentsPage.focusOnCommentEditTextField()
    }
}