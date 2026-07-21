/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.HorizonSubmissionHelper
import com.instructure.horizon.offline.HorizonOfflineState
import com.instructure.horizon.offline.HorizonPendingFileStaging
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

class AddSubmissionViewModelTest {

    private val context: Context = mockk(relaxed = true)
    private val submissionHelper: HorizonSubmissionHelper = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val createSubmissionDao: CreateSubmissionDao = mockk(relaxed = true)
    private val createFileSubmissionDao: CreateFileSubmissionDao = mockk(relaxed = true)
    private val fileUploadUtilsHelper: FileUploadUtilsHelper = mockk(relaxed = true)
    private val horizonOfflineState: HorizonOfflineState = mockk(relaxed = true)
    private val pendingFileStaging: HorizonPendingFileStaging = mockk(relaxed = true)

    private fun createViewModel(offline: Boolean): AddSubmissionViewModel {
        every { horizonOfflineState.isOffline } returns MutableStateFlow(offline)
        return AddSubmissionViewModel(
            context,
            submissionHelper,
            apiPrefs,
            createSubmissionDao,
            createFileSubmissionDao,
            fileUploadUtilsHelper,
            horizonOfflineState,
            pendingFileStaging
        )
    }

    @Test
    fun `submit button shows offline warning modal when offline`() {
        val viewModel = createViewModel(offline = true)

        viewModel.uiState.value.onSubmissionButtonClicked()

        assertTrue(viewModel.uiState.value.showOfflineSubmissionWarning)
        assertFalse(viewModel.uiState.value.showSubmissionConfirmation)
    }

    @Test
    fun `submit button shows the normal confirmation when online`() {
        val viewModel = createViewModel(offline = false)

        viewModel.uiState.value.onSubmissionButtonClicked()

        assertTrue(viewModel.uiState.value.showSubmissionConfirmation)
        assertFalse(viewModel.uiState.value.showOfflineSubmissionWarning)
    }

    @Test
    fun `dismissing the offline warning hides it`() {
        val viewModel = createViewModel(offline = true)
        viewModel.uiState.value.onSubmissionButtonClicked()

        viewModel.uiState.value.onDismissOfflineSubmissionWarning()

        assertFalse(viewModel.uiState.value.showOfflineSubmissionWarning)
    }
}
