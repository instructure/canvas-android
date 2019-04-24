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
package com.instructure.student.mobius.assignmentDetails.submission.text.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionEvent
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.mobius.common.ui.SubmissionService
import com.spotify.mobius.functions.Consumer

class TextSubmissionView(inflater: LayoutInflater, parent: ViewGroup) : MobiusView<TextSubmissionViewState, TextSubmissionEvent>(0, inflater, parent) {
    override fun onConnect(output: Consumer<TextSubmissionEvent>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun render(state: TextSubmissionViewState) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDispose() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun applyTheme() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun setInitialSubmissionText(text: String) {
        TODO("not implemented")
    }

    fun onTextSubmitted(text: String, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?) {
        SubmissionService.startTextSubmission(context, canvasContext, assignmentId, assignmentName, text)
        // TODO: close screen (back press)
    }
}