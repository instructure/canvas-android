/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.features.syllabus.edit

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.dialogs.UnsavedChangesExitDialog
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.withRequireNetwork
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_edit_syllabus.*

class EditSyllabusView(val fragmentManager: FragmentManager, inflater: LayoutInflater, parent: ViewGroup, uploadImageCallback: () -> Unit)
    : MobiusView<EditSyllabusViewState, EditSyllabusEvent>(R.layout.fragment_edit_syllabus, inflater, parent) {

    private val placeHolderList = mutableListOf<Placeholder>()

    private val saveMenuButton get() = toolbar.menu.findItem(R.id.menuSaveSyllabus)
    private val saveMenuView: TextView? get() = rootView?.findViewById(R.id.menuSaveSyllabus)

    private var consumer: Consumer<EditSyllabusEvent>? = null
    private var shouldClose = false

    init {
        contentRCEView?.hideEditorToolbar()
        contentRCEView?.actionUploadImageCallback = uploadImageCallback
        setupToolbar()
        showSummarySwitch.applyTheme()
    }

    fun setupToolbar() {
        val activity = context as? FragmentActivity
        toolbar.setupCloseButton { activity?.onBackPressed() }
        toolbar.setupMenu(R.menu.menu_edit_syllabus) { menuItem ->
            when (menuItem.itemId) {
                R.id.menuSaveSyllabus -> activity?.withRequireNetwork { savePage() }
            }
        }
        ViewStyler.themeToolbarLight(activity!!, toolbar)
        saveMenuView?.setTextColor(ThemePrefs.buttonColor)
    }

    private fun savePage() {
        consumer?.accept(EditSyllabusEvent.SaveClicked(contentRCEView.html, showSummarySwitch.isChecked))
    }

    override fun onConnect(output: Consumer<EditSyllabusEvent>) {
        consumer = output
    }

    override fun render(state: EditSyllabusViewState) {
        when (state) {
            is EditSyllabusViewState.Loaded -> renderLoadedContent(state)
            is EditSyllabusViewState.Saving -> renderSavingState()
        }
    }

    private fun renderLoadedContent(state: EditSyllabusViewState.Loaded) {
        savingProgressBar.setGone()
        saveMenuButton.isVisible = true
        saveMenuView?.setTextColor(ThemePrefs.buttonColor)
        renderContent(state.content ?: "")
        showSummarySwitch.isChecked = state.showSummary
    }

    private fun renderSavingState() {
        savingProgressBar.setVisible()
        savingProgressBar.announceForAccessibility(context.getString(R.string.saving))
        saveMenuButton.isVisible = false
    }

    override fun onDispose() {
        consumer = null
    }

    private fun renderContent(content: String) {
        if (CanvasWebView.containsLTI(content, "UTF-8")) {
            contentRCEView?.setHtml(DiscussionUtils.createLTIPlaceHolders(context, content) { _, placeholder ->
                placeHolderList.add(placeholder)
            },
                context.getString(R.string.editSyllabusContentAccessibilityLabel),
                context.getString(R.string.rce_empty_description),
                ThemePrefs.brandColor, ThemePrefs.buttonColor
            )
        } else {
            contentRCEView?.setHtml(
                content,
                context.getString(R.string.editSyllabusContentAccessibilityLabel),
                context.getString(R.string.rce_empty_description),
                ThemePrefs.brandColor, ThemePrefs.buttonColor
            )
        }
    }

    fun uploadRceImage(imageUri: Uri, activity: Activity, course: Course) {
        MediaUploadUtils.uploadRceImageJob(imageUri, course, activity) { imageUrl -> insertImage(imageUrl) }
    }

    private fun insertImage(imageUrl: String) {
        val activity = context as? Activity
        if (activity != null) {
            contentRCEView?.insertImage(activity, imageUrl)
        } else {
            contentRCEView?.insertImage(imageUrl, "")
        }
    }

    fun closeEditSyllabus() {
        shouldClose = true
        val activity = context as? FragmentActivity
        activity?.onBackPressed()
    }

    fun showSaveSuccess() {
        context.toast(R.string.syllabusSuccessfullyUpdated, Toast.LENGTH_SHORT)
    }

    fun showSaveError() {
        context.toast(R.string.errorSavingSyllabus, Toast.LENGTH_SHORT)
    }

    fun showCloseConfirmationDialog() {
        UnsavedChangesExitDialog.show(fragmentManager) {
            closeEditSyllabus()
        }
    }

    fun onHandleBackPressed(): Boolean {
        return if (shouldClose) {
            false
        } else {
            consumer?.accept(EditSyllabusEvent.BackClicked(contentRCEView.html, showSummarySwitch.isChecked))
            true
        }
    }

    fun saveState() {
        consumer?.accept(EditSyllabusEvent.SaveState(contentRCEView.html, showSummarySwitch.isChecked))
    }
}