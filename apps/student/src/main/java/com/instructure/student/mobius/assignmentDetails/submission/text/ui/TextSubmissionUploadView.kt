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

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.onChangeDebounce
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEvent
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.mobius.common.ui.SubmissionService
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_text_submission_upload.*

class TextSubmissionUploadView(inflater: LayoutInflater, parent: ViewGroup) : MobiusView<TextSubmissionUploadViewState, TextSubmissionUploadEvent>(R.layout.fragment_text_submission_upload, inflater, parent) {

    init {
        toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        toolbar.title = context.getString(R.string.textEntry)
    }

    override fun onConnect(output: Consumer<TextSubmissionUploadEvent>) {
        toolbar.setMenu(R.menu.menu_submit_generic) {
            when (it.itemId) {
                R.id.menuSubmit -> {
                    output.accept(TextSubmissionUploadEvent.SubmitClicked(editText.text.toString()))
                }
            }
        }

        editText.onChangeDebounce(URL_MINIMUM_LENGTH, DELAY) {
            output.accept(TextSubmissionUploadEvent.TextChanged(it))
        }
    }

    override fun render(state: TextSubmissionUploadViewState) {
        editText.hint = state.textHint
    }

    override fun onDispose() { }

    override fun applyTheme() { }

    fun setInitialSubmissionText(text: String?) {
        editText.setText(text ?: "")
    }

    fun onTextSubmitted(text: String, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?) {
        SubmissionService.startTextSubmission(context, canvasContext, assignmentId, assignmentName, text)

        (context as? Activity)?.onBackPressed()
    }

    companion object {
        private const val DELAY = 0L
        private const val URL_MINIMUM_LENGTH = 3
    }

}