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
 */
package com.instructure.student.test.assignment.details

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.student.mobius.assignmentDetails.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.test.util.matchesEffects
import com.instructure.student.test.util.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AssignmentDetailsUpdateTest : Assert() {
    private val initSpec = InitSpec(AssignmentDetailsUpdate()::init)
    private val updateSpec = UpdateSpec(AssignmentDetailsUpdate()::update)

    private lateinit var initModel: AssignmentDetailsModel
    private lateinit var course: Course
    private lateinit var assignment: Assignment
    private var assignmentId: Long = 0
    private var courseId: Long = 0

    @Before
    fun setup() {
        assignmentId = 4321L
        courseId = 1234L
        course = Course(id = courseId)
        assignment = Assignment(id = assignmentId)
        initModel = AssignmentDetailsModel(assignmentId = assignmentId, course = course)
    }

    @Test
    fun `Initializes into a loading state`() {
        val expectedModel = initModel.copy(isLoading = true)
        initSpec
                .whenInit(initModel)
                .then(
                        assertThatFirst(
                                FirstMatchers.hasModel(expectedModel),
                                matchesFirstEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(AssignmentDetailsEffect.LoadData(assignmentId, course.id, false))
                        )
                )
    }

    @Test
    fun `PullToRefresh event forces network reload of assignment`() {
        val expectedModel = initModel.copy(isLoading = true)
        updateSpec
                .given(initModel)
                .whenEvent(AssignmentDetailsEvent.PullToRefresh)
                .then(
                        assertThatNext(
                                NextMatchers.hasModel(expectedModel),
                                matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(AssignmentDetailsEffect.LoadData(assignmentId, course.id, true))
                        )
                )
    }

    @Test
    fun `SubmitAssignmentClicked event with multiple submission types results in ShowSubmitDialogView effect`() {
        val submissionTypes = listOf("online_upload", "online_text_entry", "media_recording")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes)
        val givenModel = initModel.copy(assignmentResult = DataResult.Success(assignmentCopy))
        updateSpec
                .given(givenModel)
                .whenEvent(AssignmentDetailsEvent.SubmitAssignmentClicked)
                .then(
                        assertThatNext(
                                matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(AssignmentDetailsEffect.ShowSubmitDialogView(assignmentCopy, course))
                        )
                )
    }

    @Test
    fun `SubmitAssignmentClicked event with one submission type results in ShowCreateSubmissionView effect`() {
        val submissionType = Assignment.SubmissionType.ONLINE_UPLOAD
        val submissionTypes = listOf("online_upload")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes)
        val givenModel = initModel.copy(assignmentResult = DataResult.Success(assignmentCopy))
        updateSpec
                .given(givenModel)
                .whenEvent(AssignmentDetailsEvent.SubmitAssignmentClicked)
                .then(
                        assertThatNext(
                                matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, courseId, assignmentCopy))
                        )
                )
    }

    @Test
    fun `ViewSubmissionClicked event results in ShowSubmissionView effect`() {
        updateSpec
                .given(initModel)
                .whenEvent(AssignmentDetailsEvent.ViewSubmissionClicked)
                .then(
                        assertThatNext(
                                matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(AssignmentDetailsEffect.ShowSubmissionView(assignmentId, course))
                        )
                )
    }

    @Test
    fun `ViewUploadStatusClicked event results in ShowUploadStatusView effect`() {
        updateSpec
                .given(initModel)
                .whenEvent(AssignmentDetailsEvent.ViewUploadStatusClicked)
                .then(
                        assertThatNext(
                                matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(AssignmentDetailsEffect.ShowUploadStatusView(assignmentId, course))
                        )
                )
    }

    @Test
    fun `SubmissionStatusUpdated event with Empty results in model update`() {
        testStatusUpdate(
                status = SubmissionUploadStatus.Empty,
                model = initModel.copy(status = SubmissionUploadStatus.Failure) // Add init model with different status as we initialize to empty
        )
    }

    @Test
    fun `SubmissionStatusUpdated event with Failure results in model update`() {
        testStatusUpdate(SubmissionUploadStatus.Failure)
    }

    @Test
    fun `SubmissionStatusUpdated event with Uploading results in model update`() {
        testStatusUpdate(SubmissionUploadStatus.Uploading)
    }

    @Test
    fun `SubmissionStatusUpdated event with Finished results in model update`() {
        testStatusUpdate(SubmissionUploadStatus.Finished)
    }

    @Test
    fun `DataLoaded event updates the model`() {
        val assignment = Assignment(id = assignmentId)
        val startModel = initModel.copy(status = SubmissionUploadStatus.Uploading)
        val expectedModel = initModel.copy(isLoading = false, status = SubmissionUploadStatus.Uploading, assignmentResult = DataResult.Success(assignment))
        updateSpec
                .given(startModel)
                .whenEvent(AssignmentDetailsEvent.DataLoaded(
                        assignmentResult = expectedModel.assignmentResult
                ))
                .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }

    @Test
    fun `DataLoaded event with assignment load failure updates the model`() {
        val startModel = initModel.copy(status = SubmissionUploadStatus.Uploading)
        val expectedModel = initModel.copy(isLoading = false, status = SubmissionUploadStatus.Uploading, assignmentResult = DataResult.Fail())
        updateSpec
                .given(startModel)
                .whenEvent(AssignmentDetailsEvent.DataLoaded(
                        assignmentResult = expectedModel.assignmentResult
                ))
                .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }

    @Test
    fun `DataLoaded event with a null assignment updates the model`() {
        val startModel = initModel.copy(status = SubmissionUploadStatus.Uploading)
        val expectedModel = initModel.copy(isLoading = false, status = SubmissionUploadStatus.Uploading, assignmentResult = null)
        updateSpec
                .given(startModel)
                .whenEvent(AssignmentDetailsEvent.DataLoaded(
                        assignmentResult = expectedModel.assignmentResult
                ))
                .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }

    @Test
    fun `SubmissionTypeClicked event results in ShowCreateSubmissionView effect`() {
        val submissionType = Assignment.SubmissionType.ONLINE_UPLOAD
        val submissionTypes = listOf("online_upload")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes)
        val givenModel = initModel.copy(assignmentResult = DataResult.Success(assignmentCopy))
        updateSpec
                .given(givenModel)
                .whenEvent(AssignmentDetailsEvent.SubmissionTypeClicked(submissionType))
                .then(
                        assertThatNext(
                            matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, courseId, assignmentCopy))
                        )
                )
    }

    private fun testStatusUpdate(status: SubmissionUploadStatus, model: AssignmentDetailsModel = initModel) {
        val expectedModel = model.copy(status = status)
        updateSpec
                .given(model)
                .whenEvent(AssignmentDetailsEvent.SubmissionStatusUpdated(expectedModel.status))
                .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }
}
