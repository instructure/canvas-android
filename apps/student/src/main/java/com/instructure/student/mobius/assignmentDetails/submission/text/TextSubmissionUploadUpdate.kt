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
package com.instructure.student.mobius.assignmentDetails.submission.text

import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.Effects.effects
import com.spotify.mobius.First
import com.spotify.mobius.Next

class TextSubmissionUploadUpdate :
    UpdateInit<TextSubmissionUploadModel, TextSubmissionUploadEvent, TextSubmissionUploadEffect>() {
    override fun performInit(model: TextSubmissionUploadModel): First<TextSubmissionUploadModel, TextSubmissionUploadEffect> {
        return First.first(model, setOf<TextSubmissionUploadEffect>(TextSubmissionUploadEffect.InitializeText(model.initialText ?: "")))
    }

    override fun update(
        model: TextSubmissionUploadModel,
        event: TextSubmissionUploadEvent
    ): Next<TextSubmissionUploadModel, TextSubmissionUploadEffect> {
        return when(event) {
            is TextSubmissionUploadEvent.TextChanged -> Next.next(
                model.copy(isSubmittable = event.text.isNotEmpty())
            )
            is TextSubmissionUploadEvent.SubmitClicked -> Next.dispatch(
                effects(
                    TextSubmissionUploadEffect.SubmitText(
                        event.text,
                        model.canvasContext,
                        model.assignmentId,
                        model.assignmentName,
                        model.attempt
                    )
                )
            )
            is TextSubmissionUploadEvent.ImageAdded -> Next.dispatch(
                effects(TextSubmissionUploadEffect.AddImage(event.uri, model.canvasContext))
            )
            is TextSubmissionUploadEvent.SaveDraft -> Next.dispatch(
                effects(TextSubmissionUploadEffect.SaveDraft(
                    event.text,
                    model.canvasContext,
                    model.assignmentId,
                    model.assignmentName
                ))
            )
            TextSubmissionUploadEvent.CameraImageTaken -> Next.dispatch(
                effects(TextSubmissionUploadEffect.ProcessCameraImage)
            )
            TextSubmissionUploadEvent.ImageFailed -> Next.dispatch(
                effects(TextSubmissionUploadEffect.ShowFailedImageMessage)
            )
        }
    }
}