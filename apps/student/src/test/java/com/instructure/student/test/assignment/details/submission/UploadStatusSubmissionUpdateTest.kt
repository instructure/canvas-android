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

import com.instructure.pandautils.models.FileSubmitObject
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEffect
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionEvent
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionModel
import com.instructure.student.mobius.assignmentDetails.submission.file.UploadStatusSubmissionUpdate
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

    private lateinit var initFile: FileSubmitObject
    private lateinit var initModel: UploadStatusSubmissionModel
    private val submissionId = 123L

    @Before
    fun setup() {
        initFile = FileSubmitObject("Test File", 1L, "contentType", "fullPath")
        initModel = UploadStatusSubmissionModel(
            submissionId = submissionId
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
        val expectedModel = startModel.copy(isLoading = false, files = listOf(initFile))

        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(
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
        val expectedModel =
            startModel.copy(isLoading = false, isFailed = true, files = listOf(initFile))

        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(
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
            isLoading = true,
            isFailed = false,
            files = listOf(FileSubmitObject("Bad File", 0L, "", ""))
        )
        val expectedModel = startModel.copy(isLoading = false, files = listOf(initFile))

        updateSpec
            .given(startModel)
            .whenEvent(
                UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded(
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
            files = listOf(FileSubmitObject("Bad File", 0L, "", ""))
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
                UploadStatusSubmissionEvent.OnUploadProgressChanged(0, submissionId, 1)
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
                UploadStatusSubmissionEvent.OnUploadProgressChanged(2, submissionId, 1)
            )
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }
}
