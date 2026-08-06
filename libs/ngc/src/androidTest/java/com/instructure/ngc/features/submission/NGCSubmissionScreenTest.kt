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
package com.instructure.ngc.features.submission

import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.models.Assignment
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.text.Text
import com.instructure.ngc.R
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that every submission type the NGC type-selector can show resolves to a real, non-blank
 * localized string on-device — guarding the STUDENT_ANNOTATION regression where the missing branch fell
 * through to `type.apiString` and rendered the raw "student_annotation" token in the dropdown.
 *
 * Complements, rather than duplicates, `SubmissionTypeLabelTest`: that one is a plain JVM test over
 * [submissionTypeLabelRes] and can only prove the *mapping* is right. It cannot catch a resource that is
 * missing or empty, because it never resolves one. This renders the label through `stringResource` and
 * asserts the resulting text is actually displayed.
 *
 * Asserts through the node tree rather than by assigning to a captured `var` from inside a composable
 * (as an earlier version of this test did), so no composition side effect is needed and
 * `submissionTypeLabel` can stay private.
 */
@RunWith(AndroidJUnit4::class)
class NGCSubmissionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun assertResolvesToLocalizedLabel(
        type: Assignment.SubmissionType,
        @StringRes expectedRes: Int,
    ) {
        composeTestRule.setContent {
            InstUITheme {
                Text(text = submissionTypeLabelRes(type)?.let { stringResource(it) } ?: type.apiString)
            }
        }

        val expected = InstrumentationRegistry.getInstrumentation().targetContext.getString(expectedRes)
        assertNotEquals("", expected.trim())
        assertNotEquals(type.apiString, expected)
        composeTestRule.onNodeWithText(expected).assertIsDisplayed()
    }

    @Test
    fun studentAnnotationResolvesToLocalizedLabel_notRawApiToken() {
        assertResolvesToLocalizedLabel(
            Assignment.SubmissionType.STUDENT_ANNOTATION,
            R.string.canvasAPI_studentAnnotation,
        )
    }

    @Test
    fun onlineTextEntryResolvesToLocalizedLabel() {
        assertResolvesToLocalizedLabel(
            Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
            R.string.ngc_submission_textEntry,
        )
    }

    @Test
    fun onlineUploadResolvesToLocalizedLabel() {
        assertResolvesToLocalizedLabel(
            Assignment.SubmissionType.ONLINE_UPLOAD,
            R.string.ngc_submission_fileUpload,
        )
    }

    @Test
    fun onlineUrlResolvesToLocalizedLabel() {
        assertResolvesToLocalizedLabel(
            Assignment.SubmissionType.ONLINE_URL,
            R.string.websiteUrl,
        )
    }

    @Test
    fun mediaRecordingResolvesToLocalizedLabel() {
        assertResolvesToLocalizedLabel(
            Assignment.SubmissionType.MEDIA_RECORDING,
            R.string.ngc_submission_mediaRecording,
        )
    }
}
