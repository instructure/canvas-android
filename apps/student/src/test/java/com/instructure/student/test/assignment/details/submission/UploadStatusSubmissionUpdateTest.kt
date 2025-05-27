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

import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEffect
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEvent
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionModel
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionUpdate
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
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

class UploadStatusSubmissionUpdateTest : Assert() {
    private val initSpec = InitSpec(UploadStatusSubmissionUpdate()::init)
    private val updateSpec = UpdateSpec(UploadStatusSubmissionUpdate()::update)

    private lateinit var initFile: CreateFileSubmissionEntity
    private lateinit var initModel: UploadStatusSubmissionModel
    private lateinit var assignmentName: String
    private val submissionId = 123L

    @Before
    fun setup() {
        assignmentName = "Assignment"
        initModel = UploadStatusSubmissionModel(
            submissionId = submissionId
        )
        initFile = CreateFileSubmissionEntity(
            12L,
            submissionId,
            null,
            "Test File",
            1L,
            "contentType",
            "fullPath",
            null,
            false
        )
    }

    @Test
    fun `Initializes with a LoadPersistedFiles effect`() {
        val startModel = initModel.copy(isLoading = false)
        initSpec
            .whenInit(startModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(startModel.copy(isLoading = true)),
                    matchesFirstEffects<UploadStatusSubmissionModel, UploadStatusSubmissionEffect>(
                        UploadStatusSubmissionEffect.LoadPersistedFiles(submissionId)
                    )
                )
            )
    }

    @Test
    fun `OnPersistedSubmissionLoaded event results in model change`() {
        val startModel = initModel.copy(isLoading = true, isFailed = false, files = emptyList())
        val expectedModel = startModel.copy(
            isLoading = false,
            files = listOf(initFile),
            assignmentName = assignmentName
        )

        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(
                    assignmentName,
                    false,
                    listOf(initFile)
                )
            )
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `OnPersistedSubmissionLoaded with failed submission event results in model change`() {
        val startModel = initModel.copy(isLoading = true, isFailed = false, files = emptyList())
        val expectedModel = startModel.copy(
            isLoading = false,
            isFailed = true,
            files = listOf(initFile),
            assignmentName = assignmentName
        )

        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(
                    assignmentName,
                    true,
                    listOf(initFile)
                )
            )
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `OnPersistedSubmissionLoaded event results in model files getting reset`() {
        val startModel = initModel.copy(
            assignmentName = assignmentName + "bad",
            isLoading = true,
            isFailed = false,
            files = listOf(CreateFileSubmissionEntity(0, 0, null, null, null, null, null, null, false))
        )
        val expectedModel = startModel.copy(
            isLoading = false,
            files = listOf(initFile),
            assignmentName = assignmentName
        )

        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(
                    assignmentName,
                    false,
                    listOf(initFile)
                )
            )
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `OnFilesRefreshed event results in model file states getting reset`() {
        val startModel = initModel.copy(
            isFailed = false,
            uploadedBytes = 1,
            files = listOf(CreateFileSubmissionEntity(0, 0, null, null, null, null, null, null, false))
        )
        val expectedModel = startModel.copy(uploadedBytes = null, files = listOf(initFile))

        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnFilesRefreshed(
                    false,
                    submissionId,
                    listOf(initFile)
                )
            )
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `OnFilesRefreshed event with failed submission results in model change`() {
        val startModel = initModel.copy(
            isFailed = false,
            files = emptyList()
        )
        val expectedModel =
            startModel.copy(isFailed = true, files = listOf(initFile))

        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnFilesRefreshed(
                    true,
                    submissionId,
                    listOf(initFile)
                )
            )
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `OnUploadProgressChanged event results in model change to uploadedBytes`() {
        val startModel = initModel.copy(
            uploadedBytes = null,
            files = listOf(initFile)
        )
        val expectedModel = startModel.copy(uploadedBytes = 1)

        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnUploadProgressChanged(0, submissionId, 1.0)
            )
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `OnUploadProgressChanged event with multiple files results in model change to uploadedBytes`() {
        val startModel = initModel.copy(
            uploadedBytes = null,
            files = listOf(initFile, initFile, initFile)
        )
        val expectedModel =
            startModel.copy(uploadedBytes = 3)

        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnUploadProgressChanged(2, submissionId, 1.0)
            )
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `OnCancelClicked results in an OnDeleteSubmission effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnCancelClicked
            )
            .then(
                assertThatNext(
                    NextMatchers.hasNoModel(),
                    NextMatchers.hasEffects<UploadStatusSubmissionModel, UploadStatusSubmissionEffect>(
                        UploadStatusSubmissionEffect.OnDeleteSubmission(submissionId)
                    )
                )
            )
    }

    @Test
    fun `OnRetryClicked results in a RetrySubmission effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnRetryClicked
            )
            .then(
                assertThatNext(
                    NextMatchers.hasNoModel(),
                    NextMatchers.hasEffects<UploadStatusSubmissionModel, UploadStatusSubmissionEffect>(
                        UploadStatusSubmissionEffect.RetrySubmission(submissionId)
                    )
                )
            )
    }

    @Test
    fun `OnDeleteFile results in a OnDeleteSubmission effect when only 1 file exists`() {
        val startModel = initModel.copy(files = listOf(initFile))
        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnDeleteFile(0)
            )
            .then(
                assertThatNext(
                    NextMatchers.hasNoModel(),
                    NextMatchers.hasEffects<UploadStatusSubmissionModel, UploadStatusSubmissionEffect>(
                        UploadStatusSubmissionEffect.OnDeleteSubmission(submissionId)
                    )
                )
            )
    }

    @Test
    fun `OnDeleteFile results in a OnDeleteFileFromSubmission effect with a model change`() {
        val deleteFile = CreateFileSubmissionEntity(409L, submissionId, null, null, null, null, null, null, false)
        val startModel = initModel.copy(files = listOf(initFile, deleteFile))
        val expectedModel = initModel.copy(files = listOf(initFile))
        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnDeleteFile(1)
            )
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasEffects<UploadStatusSubmissionModel, UploadStatusSubmissionEffect>(
                        UploadStatusSubmissionEffect.OnDeleteFileFromSubmission(deleteFile.id)
                    )
                )
            )
    }

    @Test
    fun `OnRequestCancelClicked results in ShowCancelDialog effect`() {
        val startModel = initModel.copy(files = listOf(initFile))
        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnRequestCancelClicked
            )
            .then(
                assertThatNext(
                    NextMatchers.hasNoModel(),
                    NextMatchers.hasEffects<UploadStatusSubmissionModel, UploadStatusSubmissionEffect>(
                        UploadStatusSubmissionEffect.ShowCancelDialog
                    )
                )
            )
    }

    @Test
    fun `RequestLoad event results in LoadPersistedFiles effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(UploadStatusSubmissionEvent.RequestLoad)
            .then(
                assertThatNext(
                    NextMatchers.hasEffects<UploadStatusSubmissionModel, UploadStatusSubmissionEffect>(
                        UploadStatusSubmissionEffect.LoadPersistedFiles(initModel.submissionId)
                    )
                )
            )

    }
}
