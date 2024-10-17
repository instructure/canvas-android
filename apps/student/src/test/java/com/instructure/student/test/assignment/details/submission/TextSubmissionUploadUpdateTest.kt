/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.test.assignment.details.submission

import android.net.Uri
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEffect
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEvent
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadModel
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadUpdate
import com.instructure.student.test.util.matchesEffects
import com.instructure.student.test.util.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TextSubmissionUploadUpdateTest : Assert() {
    private val initSpec = InitSpec(TextSubmissionUploadUpdate()::init)
    private val updateSpec = UpdateSpec(TextSubmissionUploadUpdate()::update)

    private lateinit var course: Course
    private lateinit var assignment: Assignment
    private lateinit var initModel: TextSubmissionUploadModel

    @Before
    fun setup() {
        course = Course()
        assignment = Assignment(id = 1234L, courseId = course.id, name = "name")
        initModel = TextSubmissionUploadModel(
            assignmentId = assignment.id,
            canvasContext = course,
            assignmentName = assignment.name
        )
    }

    @Test
    fun `Initializes with an InitializeText effect`() {
        val text = "Some text from a save"
        val startModel = initModel.copy(initialText = text)
        initSpec
            .whenInit(startModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(startModel),
                    matchesFirstEffects<TextSubmissionUploadModel, TextSubmissionUploadEffect>(
                        TextSubmissionUploadEffect.InitializeText(text)
                    )
                )
            )
    }

    @Test
    fun `TextChanged event with non empty text results in model change to isSubmittable`() {
        val text = "Some text to submit"
        val startModel = initModel.copy(isSubmittable = false)
        val expectedModel = startModel.copy(isSubmittable = true)

        updateSpec
            .given(startModel)
            .whenEvent(TextSubmissionUploadEvent.TextChanged(text))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `TextChanged event with empty text results in model change to not isSubmittable`() {
        val text = ""
        val startModel = initModel.copy(isSubmittable = true)
        val expectedModel = startModel.copy(isSubmittable = false)

        updateSpec
            .given(startModel)
            .whenEvent(TextSubmissionUploadEvent.TextChanged(text))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `SubmitClicked event results in SubmitText effect`() {
        val text = "Some text to submit"

        updateSpec
            .given(initModel)
            .whenEvent(TextSubmissionUploadEvent.SubmitClicked(text))
            .then(
                assertThatNext(
                    matchesEffects<TextSubmissionUploadModel, TextSubmissionUploadEffect>(
                        TextSubmissionUploadEffect.SubmitText(
                            text,
                            course,
                            assignment.id,
                            assignment.name
                        )
                    )
                )
            )
    }

    @Test
    fun `SubmitClicked event with new lines in text results in SubmitText effect`() {
        val text = "Some text to submit\nWith a new line"

        updateSpec
            .given(initModel)
            .whenEvent(TextSubmissionUploadEvent.SubmitClicked(text))
            .then(
                assertThatNext(
                    matchesEffects<TextSubmissionUploadModel, TextSubmissionUploadEffect>(
                        TextSubmissionUploadEffect.SubmitText(
                            text,
                            course,
                            assignment.id,
                            assignment.name
                        )
                    )
                )
            )
    }

    @Test
    fun `ImageAdded results in AddImage effect`() {
        val uri = mockk<Uri>()

        updateSpec
            .given(initModel)
            .whenEvent(TextSubmissionUploadEvent.ImageAdded(uri))
            .then(
                assertThatNext(
                    matchesEffects<TextSubmissionUploadModel, TextSubmissionUploadEffect>(
                        TextSubmissionUploadEffect.AddImage(uri, initModel.canvasContext)
                    )
                )
            )
    }

    @Test
    fun `CameraImageTaken results in ProcessCameraImage effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(TextSubmissionUploadEvent.CameraImageTaken)
            .then(
                assertThatNext(
                    matchesEffects<TextSubmissionUploadModel, TextSubmissionUploadEffect>(
                        TextSubmissionUploadEffect.ProcessCameraImage
                    )
                )
            )
    }

    @Test
    fun `ImageFailed results in ShowFailedImageMessage effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(TextSubmissionUploadEvent.ImageFailed)
            .then(
                assertThatNext(
                    matchesEffects<TextSubmissionUploadModel, TextSubmissionUploadEffect>(
                        TextSubmissionUploadEffect.ShowFailedImageMessage
                    )
                )
            )
    }

}