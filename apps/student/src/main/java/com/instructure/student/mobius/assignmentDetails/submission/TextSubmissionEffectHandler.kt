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

import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.student.mobius.assignmentDetails.submission.ui.TextSubmissionView
import com.instructure.student.mobius.common.ui.EffectHandler

class TextSubmissionEffectHandler : EffectHandler<TextSubmissionView, TextSubmissionEvent, TextSubmissionEffect>() {
    override fun accept(effect: TextSubmissionEffect) {
        when (effect) {
            is TextSubmissionEffect.SubmitText -> {
                view?.onTextSubmitted(effect.text, effect.canvasContext, effect.assignmentId)
            }
            is TextSubmissionEffect.InitializeText -> {
                view?.setInitialSubmissionText(effect.text)
            }
        }.exhaustive
    }
}