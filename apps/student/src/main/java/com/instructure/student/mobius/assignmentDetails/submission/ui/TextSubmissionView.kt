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
package com.instructure.student.mobius.assignmentDetails.submission.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.student.mobius.assignmentDetails.submission.TextSubmissionEvent
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.util.Const
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

    fun onTextSubmitted(text: String, canvasContext: CanvasContext, assignmentId: Long) {
        // Create the bundle in the view, so we don't leak android resources into testable classes
        val bundle = Bundle().apply {
            putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            putLong(Const.ASSIGNMENT_ID, assignmentId)
            putString(Const.MESSAGE, text)
        }

        // TODO: Call submission service
//        SubmissionService.enqueueWork(context, SubmissionAction.SUBMIT_TEXT_ENTRY, bundle)
        // TODO: close screen (back press)
    }
}