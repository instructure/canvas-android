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
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.instructure.pandautils.analytics.SCREEN_VIEW_CONFIRM_DELETE_FILE_FOLDER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.teacher.R
import com.instructure.teacher.fragments.EditFileFolderFragment

@ScreenView(SCREEN_VIEW_CONFIRM_DELETE_FILE_FOLDER)
class ConfirmDeleteFileFolderDialog : BaseCanvasAppCompatDialogFragment() {

    private var deleteCallback: (() -> Unit)? = null
    private var isFile: Boolean by BooleanArg(false)

    init { retainInstance = true }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(if (isFile) R.string.deleteFile else R.string.deleteFolder)
                .setMessage(if (isFile) R.string.deleteFileMessage else R.string.deleteFolderMessage)
                .setPositiveButton(R.string.delete, { _, _ -> deleteCallback?.invoke() })
                .setNegativeButton(R.string.cancel, null)
                .create()
        return dialog.apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
            }
        }
    }

    override fun onDetach() {
        deleteCallback = null
        super.onDetach()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        deleteCallback = getDeleteCallback()
    }

    private fun getDeleteCallback(): (() -> Unit)? {
        val parent = parentFragment
        return if (parent is EditFileFolderFragment) {
            parent.deleteCallback
        } else {
            null
        }
    }

    companion object {
        fun show(manager: FragmentManager, isFile: Boolean) = ConfirmDeleteFileFolderDialog().apply {
            manager.dismissExisting<ConfirmDeleteFileFolderDialog>()
            this.isFile = isFile
            show(manager, javaClass.simpleName)
        }
    }
}
