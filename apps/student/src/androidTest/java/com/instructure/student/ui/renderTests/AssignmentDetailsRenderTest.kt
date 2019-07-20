/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.renderTests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.espresso.StudentRenderTest
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsModel
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.spotify.mobius.runners.WorkRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class AssignmentDetailsRenderTest : StudentRenderTest() {

    private lateinit var baseModel: AssignmentDetailsModel

    @Before
    fun setup() {
        ApiPrefs.user = User()
        baseModel = AssignmentDetailsModel(
            assignmentId = 0,
            course = Course(name = "Test Course"),
            isLoading = false,
            assignmentResult = DataResult.Fail()
        )
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.ASSIGNMENTS, TestCategory.RENDER)
    fun displaysToolbarTitles() {
        val model = baseModel.copy()
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysToolbarTitle("Assignment Details")
        assignmentDetailsRenderPage.assertDisplaysToolbarSubtitle(model.course.name)
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.ASSIGNMENTS, TestCategory.RENDER)
    fun displaysTitleDataNotSubmitted() {
        val assignment = Assignment(
            name = "Test Assignment",
            pointsPossible = 35.0
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysAssignmentName(assignment.name!!)
        assignmentDetailsRenderPage.assertDisplaysPoints("35 pts")
        assignmentDetailsRenderPage.assertDisplaysSubmissionStatus("Not Submitted")
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.ASSIGNMENTS, TestCategory.RENDER)
    fun displaysTitleDataSubmitted() {
        val submission = Submission(workflowState = "submitted")
        val assignment = Assignment(
            name = "Test Assignment",
            pointsPossible = 35.0,
            submission = submission
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysAssignmentName(assignment.name!!)
        assignmentDetailsRenderPage.assertDisplaysPoints("35 pts")
        assignmentDetailsRenderPage.assertDisplaysSubmissionStatus("Submitted")
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.ASSIGNMENTS, TestCategory.RENDER)
    fun displaysDueDate() {
        val expectedDueDate = "January 31, 2050 at 11:59 PM"
        val calendar = Calendar.getInstance().apply { set(2050, 0, 31, 23, 59, 0) }
        val assignment = Assignment(
            name = "Test Assignment",
            dueAt = calendar.time.toApiString()
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysDate(expectedDueDate)
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.ASSIGNMENTS, TestCategory.RENDER)
    fun displaysNoDueDate() {
        val model = baseModel.copy(assignmentResult = DataResult.Success(Assignment(name = "Test Assignment")))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysDate("This assignment doesn't have a due date.")
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.ASSIGNMENTS, TestCategory.RENDER)
    fun displaysNoneSubmissionType() {
        val assignment = Assignment(
            name = "Test Assignment",
            submissionTypesRaw = listOf("none")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysSubmissionTypes("None")
    }

    @Test
    @TestMetaData(Priority.P2, FeatureCategory.ASSIGNMENTS, TestCategory.RENDER)
    fun displaysNotGradedSubmissionType() {
        val assignment = Assignment(
            name = "Test Assignment",
            submissionTypesRaw = listOf("not_graded")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysSubmissionTypes("Not Graded")
    }

    @Test
    fun displaysQuizSubmissionType() {
        val assignment = Assignment(
            name = "Test Assignment",
            submissionTypesRaw = listOf("online_quiz")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysSubmissionTypes("Online Quiz")
    }

    @Test
    fun displaysDiscussionSubmissionType() {
        val assignment = Assignment(
            name = "Test Assignment",
            submissionTypesRaw = listOf("discussion_topic")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysSubmissionTypes("Discussion Topic")
    }

    @Test
    fun displaysOnPaperSubmissionType() {
        val assignment = Assignment(
            name = "Test Assignment",
            submissionTypesRaw = listOf("on_paper")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysSubmissionTypes("On Paper")
    }

    @Test
    fun displaysExternalToolSubmissionType() {
        val assignment = Assignment(
            name = "Test Assignment",
            submissionTypesRaw = listOf("external_tool")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysSubmissionTypes("External Tool")
    }

    @Test
    fun displaysOtherSubmissionTypes() {
        val assignment = Assignment(
            name = "Test Assignment",
            submissionTypesRaw = listOf(
                "basic_lti_launch",
                "online_upload",
                "online_text_entry",
                "online_url",
                "media_recording",
                "attendance"
            )
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        val expected = "External Tool, Online Upload, Online Text Entry, Online URL, Media Recording, Attendance"
        assignmentDetailsRenderPage.assertDisplaysSubmissionTypes(expected)
    }

    @Test
    fun displaysFileTypes() {
        val assignment = Assignment(
            name = "Test Assignment",
            allowedExtensions = listOf("PNG", "JPG", "PDF", "APK", "DOC")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        val expected = "PNG, JPG, PDF, APK, DOC"
        assignmentDetailsRenderPage.assertDisplaysFileTypes(expected)
    }

    @Test
    fun displaysDescription() {
        val descriptionText = "This is a description!"
        val assignment = Assignment(description = "<p>$descriptionText</p>")
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysDescription(descriptionText)
    }

    @Test
    fun displaysNoDescription() {
        val model = baseModel.copy(assignmentResult = DataResult.Success(Assignment()))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysNoDescription()
    }

    @Test
    fun displaysGradeCell() {
        val assignment = Assignment(
            name = "Test Assignment",
            submission = Submission(
                attempt = 1L,
                workflowState = "graded",
                enteredGrade = "85",
                enteredScore = 85.0,
                grade = "85.0",
                score = 85.0
            )
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysGrade()
    }

    @Test
    fun displaysSubmitted() {
        val assignment = Assignment(
            name = "Test Assignment",
            submission = Submission(attempt = 1L, workflowState = "submitted")
        )
        val model = baseModel.copy(assignmentResult = DataResult.Success(assignment))
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysSuccessfulSubmit()
    }

    @Test
    fun displaysUploading() {
        val assignment = Assignment(name = "Test Assignment")
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(assignment),
            databaseSubmission = mockkSubmission()
        )
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysUploadingSubmission()
    }


    @Test
    fun displaysFailed() {
        val assignment = Assignment(name = "Test Assignment")
        val model = baseModel.copy(
            assignmentResult = DataResult.Success(assignment),
            databaseSubmission = mockkSubmission(true)
        )
        loadPageWithModel(model)
        assignmentDetailsRenderPage.assertDisplaysFailedSubmission()
    }

    private fun mockkSubmission(failed: Boolean = false) = com.instructure.student.Submission.Impl(
        123L,
        null,
        null,
        null,
        null,
        null,
        null,
        failed,
        null,
        null
    )

    private fun loadPageWithModel(model: AssignmentDetailsModel) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val route = AssignmentDetailsFragment.makeRoute(model.course, model.assignmentId)
        val fragment = AssignmentDetailsFragment.newInstance(route)!!.apply {
            overrideInitModel = model
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
    }

}
