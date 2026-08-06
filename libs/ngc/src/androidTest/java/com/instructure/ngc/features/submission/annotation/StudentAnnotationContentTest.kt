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
package com.instructure.ngc.features.submission.annotation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.instui.compose.InstUITheme
import com.instructure.ngc.features.submission.StudentAnnotationUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * Covers only the loading and error Compose states of [StudentAnnotationContent]. The "loaded"
 * branch embeds [PdfAnnotationHostFragment] via [androidx.fragment.compose.AndroidFragment], whose
 * `childFragmentManager` hosts a real PSPDFKit [com.pspdfkit.ui.PdfFragment] (see
 * [PdfAnnotationSubmissionView]). Exercising it needs a live canvadocs session/document, a PSPDFKit
 * license, and (since [PdfAnnotationSubmissionView]'s base class casts its `Context` to
 * `AppCompatActivity`) an `AppCompatActivity` test host rather than [createComposeRule]'s default
 * `ComponentActivity` — impractical for a lightweight Compose UI test, so it is not covered here.
 * This is consistent with the type being dormant for MBL-19937 Phase 3 (not yet gated into
 * `ngcImplementedTypes` / routed).
 */
@RunWith(AndroidJUnit4::class)
class StudentAnnotationContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val courseColor = ComposeColor(0xFF2573DF)

    private fun setContent(uiState: StudentAnnotationUiState) {
        composeTestRule.setContent {
            InstUITheme(courseColor = courseColor) {
                StudentAnnotationContent(
                    uiState = uiState,
                    courseId = 1L,
                )
            }
        }
    }

    @Test
    fun loadingStateShowsProgressIndicator() {
        setContent(StudentAnnotationUiState(sessionLoading = true))

        composeTestRule.onNodeWithTag("studentAnnotation_loading").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsMessageAndRetry() {
        setContent(StudentAnnotationUiState(sessionLoadError = "Failed to load the document"))

        composeTestRule.onNodeWithTag("studentAnnotation_error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Failed to load the document").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun errorStateRetryInvokesCallback() {
        var retried = false
        setContent(
            StudentAnnotationUiState(
                sessionLoadError = "Failed to load the document",
                onRetrySessionLoad = { retried = true },
            )
        )

        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }

    @Test
    fun blankSessionUrlWithoutErrorOrLoadingShowsErrorState() {
        // Default StudentAnnotationUiState: not loading, no explicit error, but sessionUrl is blank
        // (the state before any load has run). Treated the same as an explicit load error.
        setContent(StudentAnnotationUiState())

        composeTestRule.onNodeWithTag("studentAnnotation_error").assertIsDisplayed()
    }
}
