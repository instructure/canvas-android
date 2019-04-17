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
package com.instructure.student.mobius.assignmentDetails.submission

import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.Effects.effects
import com.spotify.mobius.First
import com.spotify.mobius.Next
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class TextSubmissionUpdate : UpdateInit<TextSubmissionModel, TextSubmissionEvent, TextSubmissionEffect>() {
    override fun performInit(model: TextSubmissionModel): First<TextSubmissionModel, TextSubmissionEffect> {
        return First.first(model, setOf<TextSubmissionEffect>(TextSubmissionEffect.InitializeText(model.initialText ?: "")))
    }

    override fun update(model: TextSubmissionModel, event: TextSubmissionEvent): Next<TextSubmissionModel, TextSubmissionEffect> {
        return when(event) {
            is TextSubmissionEvent.TextChanged -> {
                Next.next(model.copy(isSubmittable = event.text.isNotEmpty()))
            }
            is TextSubmissionEvent.SubmitClicked -> {
                // Get the text, replace all line breaks with <br/> tags so they are preserved when displayed in a webview
                var textToSubmit = event.text.replace("\\n".toRegex(), "<br/>")
                try {
                    textToSubmit = URLEncoder.encode(textToSubmit, "UTF-8")
                } catch (e: UnsupportedEncodingException) {}

                Next.dispatch(effects(TextSubmissionEffect.SubmitText(textToSubmit, model.canvasContext, model.assignmentId, model.assignmentName)))
            }
        }
    }
}