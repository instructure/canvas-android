/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.ui.features.moduleitemsequence

import android.net.Uri
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.account.filepreview.FilePreviewUiState
import com.instructure.horizon.features.moduleitemsequence.content.file.FileDetailsContentScreen
import com.instructure.horizon.features.moduleitemsequence.content.file.FileDetailsUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.room.appdatabase.entities.FileDownloadProgressState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileDetailsUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testFileNameDisplays() {
        val uiState = FileDetailsUiState(
            fileName = "document.pdf",
            url = "https://example.com/file.pdf",
            filePreview = FilePreviewUiState.NoPreview,
            loadingState = LoadingState(isLoading = false),
            mimeType = "application/pdf"
        )

        composeTestRule.setContent {
            FileDetailsContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithText("document.pdf")
            .assertIsDisplayed()
    }

    @Test
    fun testDownloadButtonDisplays() {
        val uiState = FileDetailsUiState(
            fileName = "document.pdf",
            url = "https://example.com/file.pdf",
            filePreview = FilePreviewUiState.NoPreview,
            loadingState = LoadingState(isLoading = false),
            mimeType = "application/pdf"
        )

        composeTestRule.setContent {
            FileDetailsContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithText("Download")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val uiState = FileDetailsUiState(
            fileName = "document.pdf",
            url = "https://example.com/file.pdf",
            filePreview = FilePreviewUiState.NoPreview,
            loadingState = LoadingState(isLoading = true),
            mimeType = "application/pdf"
        )

        composeTestRule.setContent {
            FileDetailsContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testDownloadProgressDisplays() {
        val uiState = FileDetailsUiState(
            fileName = "document.pdf",
            url = "https://example.com/file.pdf",
            filePreview = FilePreviewUiState.NoPreview,
            downloadState = FileDownloadProgressState.IN_PROGRESS,
            downloadProgress = 0.5f,
            loadingState = LoadingState(isLoading = false),
            mimeType = "application/pdf"
        )

        composeTestRule.setContent {
            FileDetailsContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithTag("DownloadProgress")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("50%")
            .assertIsDisplayed()
    }

    @Test
    fun testCancelDownloadButtonDisplaysDuringDownload() {
        val uiState = FileDetailsUiState(
            fileName = "document.pdf",
            url = "https://example.com/file.pdf",
            filePreview = FilePreviewUiState.NoPreview,
            downloadState = FileDownloadProgressState.IN_PROGRESS,
            loadingState = LoadingState(isLoading = false),
            mimeType = "application/pdf"
        )

        composeTestRule.setContent {
            FileDetailsContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithText("Cancel")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun testFilePreviewDisplaysForPDF() {
        val uiState = FileDetailsUiState(
            fileName = "document.pdf",
            url = "https://example.com/file.pdf",
            filePreview = FilePreviewUiState.Pdf(Uri.EMPTY),
            loadingState = LoadingState(isLoading = false),
            mimeType = "application/pdf"
        )

        composeTestRule.setContent {
            FileDetailsContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithTag("PdfPreview")
            .assertIsDisplayed()
    }

    @Test
    fun testFilePreviewDisplaysForImage() {
        val uiState = FileDetailsUiState(
            fileName = "photo.jpg",
            url = "https://example.com/photo.jpg",
            filePreview = FilePreviewUiState.Image(
                displayName = "photo.jpg",
                uri = Uri.EMPTY
            ),
            loadingState = LoadingState(isLoading = false),
            mimeType = "image/jpeg"
        )

        composeTestRule.setContent {
            FileDetailsContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithTag("ImagePreview")
            .assertIsDisplayed()
    }

    @Test
    fun testNoPreviewMessageDisplays() {
        val uiState = FileDetailsUiState(
            fileName = "data.csv",
            url = "https://example.com/data.csv",
            filePreview = FilePreviewUiState.NoPreview,
            loadingState = LoadingState(isLoading = false),
            mimeType = "text/csv"
        )

        composeTestRule.setContent {
            FileDetailsContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithText("No preview available")
            .assertIsDisplayed()
    }

    @Test
    fun testErrorStateDisplaysMessage() {
        val uiState = FileDetailsUiState(
            fileName = "file.pdf",
            url = "https://example.com/file.pdf",
            filePreview = FilePreviewUiState.NoPreview,
            loadingState = LoadingState(isLoading = false, isError = true),
            mimeType = "application/pdf"
        )

        composeTestRule.setContent {
            FileDetailsContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithText("Failed to load file")
            .assertIsDisplayed()
    }

    @Test
    fun testDownloadCompleteDisplaysOpenButton() {
        val uiState = FileDetailsUiState(
            fileName = "document.pdf",
            url = "https://example.com/file.pdf",
            filePreview = FilePreviewUiState.NoPreview,
            downloadState = FileDownloadProgressState.COMPLETED,
            filePathToOpen = "/path/to/file",
            loadingState = LoadingState(isLoading = false),
            mimeType = "application/pdf"
        )

        composeTestRule.setContent {
            FileDetailsContentScreen(uiState = uiState)
        }

        composeTestRule.onNodeWithText("Open")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
