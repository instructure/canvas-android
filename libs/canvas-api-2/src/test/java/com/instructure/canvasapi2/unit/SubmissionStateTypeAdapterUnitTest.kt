/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.SubmissionState
import com.instructure.canvasapi2.utils.parse
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.intellij.lang.annotations.Language
import org.junit.Test

/**
 * Regression tests for MBL-20194: cross-shard planner responses can return the `submissions`
 * booleans as JSON `null` instead of omitting the key. [SubmissionStateTypeAdapter] must treat
 * a JSON `null` the same as an absent key rather than throwing.
 */
class SubmissionStateTypeAdapterUnitTest {

    @Test
    fun `All booleans as JSON null deserializes without throwing and defaults to false`() {
        val submissionState: SubmissionState = allNullSubmissionsJSON.parse()

        assertNotNull(submissionState)
        assertFalse(submissionState.submitted)
        assertFalse(submissionState.missing)
        assertFalse(submissionState.late)
        assertFalse(submissionState.excused)
        assertFalse(submissionState.graded)
        assertFalse(submissionState.needsGrading)
        assertFalse(submissionState.withFeedback)
        assertFalse(submissionState.redoRequest)
        assertNull(submissionState.postedAt)
    }

    @Test
    fun `Populated booleans deserialize to their actual values`() {
        val submissionState: SubmissionState = populatedSubmissionsJSON.parse()

        assertEquals(true, submissionState.submitted)
        assertEquals(true, submissionState.missing)
        assertEquals(true, submissionState.late)
        assertEquals(false, submissionState.excused)
        assertEquals(true, submissionState.graded)
        assertEquals(false, submissionState.needsGrading)
        assertEquals(true, submissionState.withFeedback)
        assertEquals(false, submissionState.redoRequest)
        assertNotNull(submissionState.postedAt)
    }

    @Test
    fun `Absent keys default to false`() {
        val submissionState: SubmissionState = emptySubmissionsJSON.parse()

        assertFalse(submissionState.submitted)
        assertFalse(submissionState.missing)
        assertFalse(submissionState.late)
        assertFalse(submissionState.excused)
        assertFalse(submissionState.graded)
        assertFalse(submissionState.needsGrading)
        assertFalse(submissionState.withFeedback)
        assertFalse(submissionState.redoRequest)
        assertNull(submissionState.postedAt)
    }

    @Test
    fun `postedAt as JSON null yields null while a valid string parses to a Date`() {
        val nullPostedAt: SubmissionState = allNullSubmissionsJSON.parse()
        assertNull(nullPostedAt.postedAt)

        val validPostedAt: SubmissionState = populatedSubmissionsJSON.parse()
        assertNotNull(validPostedAt.postedAt)
    }

    /**
     * This is the exact shape of the `submissions` object captured from a device for a
     * cross-shard enrollment planner item (MBL-20194): every boolean is JSON `null`.
     */
    @Language("JSON")
    private val allNullSubmissionsJSON = """
      {
        "submitted": null,
        "excused": null,
        "graded": null,
        "posted_at": null,
        "late": null,
        "missing": null,
        "needs_grading": null,
        "with_feedback": null,
        "redo_request": null
      }"""

    @Language("JSON")
    private val populatedSubmissionsJSON = """
      {
        "submitted": true,
        "excused": false,
        "graded": true,
        "posted_at": "2026-07-25T05:59:00Z",
        "late": true,
        "missing": true,
        "needs_grading": false,
        "with_feedback": true,
        "redo_request": false
      }"""

    @Language("JSON")
    private val emptySubmissionsJSON = "{}"
}
