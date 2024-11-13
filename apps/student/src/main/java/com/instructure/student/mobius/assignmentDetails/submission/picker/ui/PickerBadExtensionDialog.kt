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
package com.instructure.student.mobius.assignmentDetails.submission.picker.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.blueprint.BaseCanvasDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.pandautils.utils.StringArrayArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.student.R
import java.util.Locale

class PickerBadExtensionDialog : BaseCanvasDialogFragment() {

    private var extensions by StringArrayArg()

    init {
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setTitle(R.string.fileTypeNotSupported)
            .setMessage(getString(R.string.fileTypeNotSupportedBody, extensions.joinToString(", ")))
            .setPositiveButton(getString(android.R.string.ok).uppercase(Locale.getDefault()), null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                }
            }
    }

    companion object {
        fun show(manager: FragmentManager, allowedExtensions: List<String>) =
            PickerBadExtensionDialog().apply {
                manager.dismissExisting<PickerBadExtensionDialog>()
                this.extensions = allowedExtensions.toTypedArray()
                show(manager, javaClass.simpleName)
            }
    }
}
