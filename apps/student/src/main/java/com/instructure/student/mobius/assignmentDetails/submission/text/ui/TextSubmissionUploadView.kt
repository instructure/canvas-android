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
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.*
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
                    output.accept(TextSubmissionUploadEvent.SubmitClicked(rce.html))
                }
            }
        }

        toolbar.menu.findItem(R.id.menuSubmit).isEnabled = false

        rce.setOnTextChangeListener {
            output.accept(TextSubmissionUploadEvent.TextChanged(it))
        }
    }

    override fun render(state: TextSubmissionUploadViewState) {
        toolbar.menu.findItem(R.id.menuSubmit).isEnabled = state.submitEnabled
        errorMsg.setVisible(state.isFailure)
        errorDivider.setVisible(state.isFailure)
    }

    override fun onDispose() { }

    override fun applyTheme() {
        ViewStyler.themeToolbarBottomSheet(context as Activity, false, toolbar, Color.BLACK, false)
    }

    fun setInitialSubmissionText(text: String?) {
        rce.setHtml(text ?: "", context.getString(R.string.textEntry), context.getString(R.string.submissionWrite), ThemePrefs.brandColor, ThemePrefs.buttonColor)
        toolbar.menu.findItem(R.id.menuSubmit).isEnabled = !text.isNullOrBlank()
    }

    fun onTextSubmitted(text: String, canvasContext: CanvasContext, assignmentId: Long, assignmentName: String?) {
        SubmissionService.startTextSubmission(context, canvasContext, assignmentId, assignmentName, text)

        (context as? Activity)?.onBackPressed()
    }
}