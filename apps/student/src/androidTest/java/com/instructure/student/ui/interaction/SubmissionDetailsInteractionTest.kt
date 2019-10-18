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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class SubmissionDetailsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testRubrics_showCriterionDescription() {
        // Clicking the "Description" button on a rubric criterion item should show a new page with the full description
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_addCommentToSubmission() {
        // Should be able to add a comment on a submission
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testAssignments_previewAttachment() {
        // Student can preview an assignment attachment

    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_addVideoCommentToSubmission() {
        // Should be able to add a video comment on a submission
    }

    @Stub
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_addAudioCommentToSubmission() {
        // Should be able to add a audio comment on a submission
    }

    @Stub
    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_videoCommentPlayback() {
        // After recording a video comment, user should be able to view a replay
    }

    @Stub
    @Test
    @TestMetaData(Priority.P2, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testComments_audioCommentPlayback() {
        // After recording an audio comment, user should be able to hear an audio playback
    }
}
