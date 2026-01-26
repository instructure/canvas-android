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
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.MediaUploadUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.setMenu
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.student.R
import com.instructure.student.databinding.FragmentTextSubmissionUploadBinding
import com.instructure.student.mobius.assignmentDetails.submission.text.TextSubmissionUploadEvent
import com.instructure.student.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer

class TextSubmissionUploadView(inflater: LayoutInflater, parent: ViewGroup) :
    MobiusView<TextSubmissionUploadViewState, TextSubmissionUploadEvent, FragmentTextSubmissionUploadBinding>(
        inflater,
        FragmentTextSubmissionUploadBinding::inflate,
        parent
    ) {

    private lateinit var confirmationDialog: AlertDialog

    private var initialText: String? = ""

    private var canPressBack = false

    init {
        binding.toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        binding.toolbar.title = context.getString(R.string.textEntry)
        binding.toolbar.applyTopSystemBarInsets()
        binding.rce.applyBottomSystemBarInsets()
    }

    override fun onConnect(output: Consumer<TextSubmissionUploadEvent>) = with(binding) {
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
        rce.actionUploadImageCallback = {
            MediaUploadUtils.showPickImageDialog(null, context as Activity)
        }

        confirmationDialog = AlertDialog.Builder(context)
            .setTitle(R.string.textSubmissionExitConfirmationTitle)
            .setMessage(R.string.textSubmissionExitConfirmationMessage)
            .setPositiveButton(R.string.save) {dialog, _ ->
                canPressBack = true
                output.accept(TextSubmissionUploadEvent.SaveDraft(rce.html))
            }
            .setNegativeButton(R.string.dontSave) {dialog, _ ->
                canPressBack = true
                dialog.dismiss()
                (context as? Activity)?.onBackPressed()
            }
            .setNeutralButton(R.string.cancel) { dialog, _ ->
                canPressBack = false
                dialog.dismiss()
            }
            .create()
    }

    override fun render(state: TextSubmissionUploadViewState) {
        binding.toolbar.menu.findItem(R.id.menuSubmit).isEnabled = state.submitEnabled
        binding.errorMsg.setVisible(state.isFailure)
        binding.divider.setVisible(state.isFailure)
    }

    override fun onDispose() { }

    override fun applyTheme() {
        ViewStyler.themeToolbarLight(context as Activity, binding.toolbar)
    }

    fun setInitialSubmissionText(text: String?) {
        initialText = text
        binding.rce.setHtml(text ?: "", context.getString(R.string.textEntry), context.getString(R.string.submissionWrite), ThemePrefs.brandColor, ThemePrefs.textButtonColor)
        binding.toolbar.menu.findItem(R.id.menuSubmit).isEnabled = !text.isNullOrBlank()
    }

    fun retrieveCameraImage(): Uri? {
        return (context as? Activity)?.let { activity ->
            MediaUploadUtils.handleCameraPicResult(activity, null)
        }
    }

    /**
     * Add the image here, rather than in the loop, so that it is placed at the cursor position
     */
    fun addImageToSubmission(uri: Uri, canvasContext: CanvasContext) {
        (context as? Activity)?.let { activity ->
            MediaUploadUtils.uploadRceImageJob(uri, canvasContext, activity, insertImageCallback = ::insertImage)
        }
    }

    private fun insertImage(imageUrl: String) {
        val activity = context as? Activity
        if (activity != null) {
            binding.rce.insertImage(activity, imageUrl)
        } else {
            binding.rce.insertImage(imageUrl, "")
        }
    }

    fun showFailedImageMessage() {
        Toast.makeText(context, R.string.errorGettingPhoto, Toast.LENGTH_LONG).show()
    }

    fun onBackPressed(): Boolean {
        return if (binding.rce.html.isNotEmpty() && binding.rce.html.isNotBlank() && initialText != binding.rce.html && !canPressBack) {
            confirmationDialog.show()
            true
        } else {
            false
        }
    }

    fun goBack() {
        canPressBack = true
        (context as? Activity)?.onBackPressed()
    }
}