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
package com.instructure.student.test.assignment.details.submissionDetails.commentTab

import com.instructure.canvasapi2.models.Submission
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsUpdate
import com.instructure.student.test.util.matchesEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class SubmissionCommentsUpdateTest : Assert() {

    private val initSpec = InitSpec(SubmissionCommentsUpdate()::init)
    private val updateSpec = UpdateSpec(SubmissionCommentsUpdate()::update)

    private lateinit var initModel: SubmissionCommentsModel

    @Before
    fun setup() {
        initModel = SubmissionCommentsModel(
                comments = arrayListOf(),
                submissionHistory = Submission(),
                submissionId = 123L,
                courseId = 123L,
                assignmentId = 123L,
                isGroupMessage = false
        )
    }

    @Test
    fun `Initializes without model changes and without effects`() {
        val expectedModel = initModel.copy()
        initSpec
                .whenInit(initModel)
                .then(
                        assertThatFirst(
                                FirstMatchers.hasModel(expectedModel),
                                FirstMatchers.hasNoEffects()
                        )
                )
    }

    @Test
    fun `AddMediaCommentClicked results in ShowMediaCommentDialog effect and model change`() {
        val expectedModel = initModel.copy(isMediaCommentEnabled = false)
        updateSpec
                .given(initModel)
                .whenEvent(SubmissionCommentsEvent.AddMediaCommentClicked)
                .then(
                        assertThatNext(
                                hasModel(expectedModel),
                                matchesEffects<SubmissionCommentsModel, SubmissionCommentsEffect>(SubmissionCommentsEffect.ShowMediaCommentDialog)
                        )
                )
    }

    @Test
    fun `AddAudioCommentClicked results in ShowAudioRecordingView effect`() {
        updateSpec
                .given(initModel)
                .whenEvent(SubmissionCommentsEvent.AddAudioCommentClicked)
                .then(
                        assertThatNext(
                                matchesEffects<SubmissionCommentsModel, SubmissionCommentsEffect>(SubmissionCommentsEffect.ShowAudioRecordingView)
                        )
                )
    }

    @Test
    fun `AddVideoCommentClicked results in ShowVideoRecordingView effect`() {
        updateSpec
                .given(initModel)
                .whenEvent(SubmissionCommentsEvent.AddVideoCommentClicked)
                .then(
                        assertThatNext(
                                matchesEffects<SubmissionCommentsModel, SubmissionCommentsEffect>(SubmissionCommentsEffect.ShowVideoRecordingView)
                        )
                )
    }

    @Test
    fun `SendMediaCommentClicked results in UploadMediaComment effect`() {
        val file = File("test")
        val expectedEffect = SubmissionCommentsEffect.UploadMediaComment(file, initModel.assignmentId, initModel.courseId)
        updateSpec
                .given(initModel)
                .whenEvent(SubmissionCommentsEvent.SendMediaCommentClicked(file))
                .then(
                        assertThatNext(
                                matchesEffects<SubmissionCommentsModel, SubmissionCommentsEffect>(expectedEffect)
                        )
                )
    }

    @Test
    fun `MediaCommentDialogClosed results in no effects and a model change`() {
        val givenModel = initModel.copy(isMediaCommentEnabled = false)
        val expectedModel = initModel.copy(isMediaCommentEnabled = true)
        updateSpec
                .given(givenModel)
                .whenEvent(SubmissionCommentsEvent.MediaCommentDialogClosed)
                .then(
                        assertThatNext(
                                hasModel(expectedModel)
                        )
                )
    }

}