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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEffect
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEvent
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadModel
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadUpdate
import com.instructure.student.test.util.matchesEffects
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

class PickerSubmissionUploadUpdateTest : Assert() {
    private val initSpec = InitSpec(PickerSubmissionUploadUpdate()::init)
    private val updateSpec = UpdateSpec(PickerSubmissionUploadUpdate()::update)

    private lateinit var initFile: FileSubmitObject
    private lateinit var initModel: PickerSubmissionUploadModel

    @Before
    fun setup() {
        initFile = FileSubmitObject("Test File", 1L, "contentType", "fullPath")
        initModel = PickerSubmissionUploadModel(
            assignmentName = "Test Assignment",
            assignmentId = 123L,
            assignmentGroupCategoryId = 321L,
            canvasContext = CanvasContext.emptyCourseContext(135L),
            allowedExtensions = listOf("pdf", "jpg"),
            mode = PickerSubmissionMode.FileSubmission
        )
    }

    @Test
    fun `Initializes without any changes`() {
        val startModel = initModel.copy()
        initSpec
            .whenInit(startModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(startModel),
                    FirstMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `Initializes with LoadFileContents effect given a media uri`() {
        val uri = mockk<Uri>()
        val startModel = initModel.copy(mediaFileUri = uri, isLoadingFile = false)
        val expectedModel = startModel.copy(isLoadingFile = true, mediaSource = "camera")
        initSpec
            .whenInit(startModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(expectedModel),
                    FirstMatchers.hasEffects<PickerSubmissionUploadModel, PickerSubmissionUploadEffect>(
                        PickerSubmissionUploadEffect.LoadFileContents(
                            uri,
                            startModel.allowedExtensions
                        )
                    )
                )
            )
    }

    @Test
    fun `SubmitClicked event results in HandleSubmit effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(PickerSubmissionUploadEvent.SubmitClicked)
            .then(
                assertThatNext(
                    matchesEffects<PickerSubmissionUploadModel, PickerSubmissionUploadEffect>(
                        PickerSubmissionUploadEffect.HandleSubmit(initModel)
                    )
                )
            )
    }

    @Test
    fun `CameraClicked event results in LaunchCamera effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(PickerSubmissionUploadEvent.CameraClicked)
            .then(
                assertThatNext(
                    matchesEffects<PickerSubmissionUploadModel, PickerSubmissionUploadEffect>(
                        PickerSubmissionUploadEffect.LaunchCamera
                    )
                )
            )
    }

    @Test
    fun `GalleryClicked event results in LaunchGallery effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(PickerSubmissionUploadEvent.GalleryClicked)
            .then(
                assertThatNext(
                    matchesEffects<PickerSubmissionUploadModel, PickerSubmissionUploadEffect>(
                        PickerSubmissionUploadEffect.LaunchGallery
                    )
                )
            )
    }

    @Test
    fun `SelectFileClicked event results in LaunchSelectFile effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(PickerSubmissionUploadEvent.SelectFileClicked)
            .then(
                assertThatNext(
                    matchesEffects<PickerSubmissionUploadModel, PickerSubmissionUploadEffect>(
                        PickerSubmissionUploadEffect.LaunchSelectFile
                    )
                )
            )
    }

    @Test
    fun `OnFileSelected event results in LoadFileContents effect`() {
        val uri = mockk<Uri>()

        val startModel = initModel.copy(isLoadingFile = false)
        val expectedModel = startModel.copy(isLoadingFile = true)

        updateSpec
            .given(startModel)
            .whenEvent(PickerSubmissionUploadEvent.OnFileSelected(uri))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects<PickerSubmissionUploadModel, PickerSubmissionUploadEffect>(
                        PickerSubmissionUploadEffect.LoadFileContents(
                            uri,
                            initModel.allowedExtensions
                        )
                    )
                )
            )
    }

    @Test
    fun `OnFileAdded event results in model change to files`() {
        val startModel = initModel.copy(files = emptyList(), isLoadingFile = true)
        val expectedModel = startModel.copy(files = listOf(initFile), isLoadingFile = false)

        updateSpec
            .given(startModel)
            .whenEvent(PickerSubmissionUploadEvent.OnFileAdded(initFile))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `OnFileAdded event with null file results in model change to loading`() {
        val startModel = initModel.copy(files = listOf(initFile), isLoadingFile = true)
        val expectedModel = startModel.copy(isLoadingFile = false)

        updateSpec
            .given(startModel)
            .whenEvent(PickerSubmissionUploadEvent.OnFileAdded(null))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `OnFileRemoved event results in model change to files and RemoveTempFile effect`() {
        val startModel = initModel.copy(files = listOf(initFile))
        val expectedModel = startModel.copy(files = emptyList())

        updateSpec
            .given(startModel)
            .whenEvent(PickerSubmissionUploadEvent.OnFileRemoved(0))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects<PickerSubmissionUploadModel, PickerSubmissionUploadEffect>(
                        PickerSubmissionUploadEffect.RemoveTempFile(initFile.fullPath)
                    )
                )
            )
    }
}
