/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.FragmentManager
import com.instructure.pandautils.analytics.SCREEN_VIEW_CREATE_FOLDER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.teacher.R
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_CREATE_FOLDER)
class CreateFolderDialog : BaseCanvasAppCompatDialogFragment() {
    init {
        retainInstance = true
    }

    private var mCreateFolderCallback: (String) -> Unit by Delegates.notNull()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(requireActivity(), R.layout.dialog_create_folder, null)
        val folderNameEditText = view.findViewById<AppCompatEditText>(R.id.newFolderName)
        ViewStyler.themeEditText(requireContext(), folderNameEditText, ThemePrefs.brandColor)

        val nameDialog = AlertDialog.Builder(requireActivity())
                .setCancelable(true)
                .setTitle(getString(R.string.createFolder))
                .setView(view)
                .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                    mCreateFolderCallback(folderNameEditText.text.toString())
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .create()

        // Adjust the dialog to the top so keyboard does not cover it up, issue happens on tablets in landscape
        val params = nameDialog.window?.attributes
        params?.gravity = Gravity.CENTER or Gravity.TOP
        params?.y = 120
        nameDialog.window?.attributes = params
        nameDialog.window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)


        nameDialog.setOnShowListener {
            nameDialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            nameDialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
        }
        return nameDialog
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

    companion object {
        fun show(fm: FragmentManager, callback: (String) -> Unit) {
            fm.dismissExisting<CreateFolderDialog>()
            CreateFolderDialog().apply {
                mCreateFolderCallback = callback
            }.show(fm, CreateFolderDialog::class.java.simpleName)
        }
    }
}
