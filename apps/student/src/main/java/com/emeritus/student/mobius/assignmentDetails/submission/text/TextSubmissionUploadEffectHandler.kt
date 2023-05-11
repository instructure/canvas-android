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
package com.emeritus.student.mobius.assignmentDetails.submission.text

import com.instructure.canvasapi2.utils.exhaustive
import com.emeritus.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadView
import com.emeritus.student.mobius.common.ui.EffectHandler

class TextSubmissionUploadEffectHandler :
    EffectHandler<TextSubmissionUploadView, TextSubmissionUploadEvent, TextSubmissionUploadEffect>() {
    override fun accept(effect: TextSubmissionUploadEffect) {
        when (effect) {
            is TextSubmissionUploadEffect.SubmitText -> view?.onTextSubmitted(
                effect.text,
                effect.canvasContext,
                effect.assignmentId,
                effect.assignmentName
            )
            is TextSubmissionUploadEffect.InitializeText -> view?.setInitialSubmissionText(effect.text)
            is TextSubmissionUploadEffect.AddImage -> view?.addImageToSubmission(
                effect.uri,
                effect.canvasContext
            )
            is TextSubmissionUploadEffect.SaveDraft -> view?.saveDraft(
                effect.text,
                effect.canvasContext,
                effect.assignmentId,
                effect.assignmentName
            )
            TextSubmissionUploadEffect.ProcessCameraImage -> processCameraImage()
            TextSubmissionUploadEffect.ShowFailedImageMessage -> view?.showFailedImageMessage()
        }.exhaustive
    }

    private fun processCameraImage() {
        view?.retrieveCameraImage()?.let { uri ->
            consumer.accept(TextSubmissionUploadEvent.ImageAdded(uri))
        } ?: consumer.accept(TextSubmissionUploadEvent.ImageFailed)
    }
}