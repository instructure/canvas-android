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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.ngc.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Covers [submissionTypeLabelRes] for every submission type the NGC type-selector can show.
 *
 * Guards the regression where the missing STUDENT_ANNOTATION branch fell through to `type.apiString`,
 * rendering the raw "student_annotation" token in the dropdown and the selected-type row instead of a
 * localized label. Previously an androidTest that had to observe the result by assigning to a captured
 * `var` from inside a composable; the mapping is now a plain function, so this is a plain JVM test.
 */
class SubmissionTypeLabelTest {

    @Test
    fun `student annotation maps to a localized label, not the raw api token`() {
        assertEquals(
            R.string.canvasAPI_studentAnnotation,
            submissionTypeLabelRes(Assignment.SubmissionType.STUDENT_ANNOTATION),
        )
    }

    @Test
    fun `text entry maps to a localized label`() {
        assertEquals(
            R.string.ngc_submission_textEntry,
            submissionTypeLabelRes(Assignment.SubmissionType.ONLINE_TEXT_ENTRY),
        )
    }

    @Test
    fun `file upload maps to a localized label`() {
        assertEquals(
            R.string.ngc_submission_fileUpload,
            submissionTypeLabelRes(Assignment.SubmissionType.ONLINE_UPLOAD),
        )
    }

    @Test
    fun `website url maps to a localized label`() {
        assertEquals(
            R.string.websiteUrl,
            submissionTypeLabelRes(Assignment.SubmissionType.ONLINE_URL),
        )
    }

    @Test
    fun `media recording maps to a localized label`() {
        assertEquals(
            R.string.ngc_submission_mediaRecording,
            submissionTypeLabelRes(Assignment.SubmissionType.MEDIA_RECORDING),
        )
    }

    /**
     * Types NGC does not implement have no label of their own; the composable wrapper falls back to the
     * raw apiString for them, which is acceptable because they never reach the selector.
     */
    @Test
    fun `types NGC does not implement have no label`() {
        assertNull(submissionTypeLabelRes(Assignment.SubmissionType.DISCUSSION_TOPIC))
        assertNull(submissionTypeLabelRes(Assignment.SubmissionType.EXTERNAL_TOOL))
        assertNull(submissionTypeLabelRes(Assignment.SubmissionType.ON_PAPER))
    }
}
