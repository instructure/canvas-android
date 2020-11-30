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

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_edit_syllabus.*

class EditSyllabusView(inflater: LayoutInflater, parent: ViewGroup) : MobiusView<EditSyllabusViewState, EditSyllabusEvent>(R.layout.fragment_edit_syllabus, inflater, parent) {

    init {
        contentRCEView?.hideEditorToolbar()
//        contentRCEView?.actionUploadImageCallback = { MediaUploadUtils.showPickImageDialog(this) }
    }

    override fun onConnect(output: Consumer<EditSyllabusEvent>) {

    }

    override fun render(state: EditSyllabusViewState) {
        renderContent((state as EditSyllabusViewState.Loaded).content ?: "")
        showSummarySwitch.isChecked = state.showSummary
    }

    override fun onDispose() {

    }

    private fun renderContent(content: String) {
        if (CanvasWebView.containsLTI(content, "UTF-8")) {
            contentRCEView?.setHtml(DiscussionUtils.createLTIPlaceHolders(context, content) { _, placeholder ->
//                placeHolderList.add(placeholder)
            },
                context.getString(R.string.pageDetails),
                context.getString(R.string.rce_empty_description),
                ThemePrefs.brandColor, ThemePrefs.buttonColor
            )
        } else {
            contentRCEView?.setHtml(
                content,
                context.getString(R.string.pageDetails),
                context.getString(R.string.rce_empty_description),
                ThemePrefs.brandColor, ThemePrefs.buttonColor
            )
        }
    }
}