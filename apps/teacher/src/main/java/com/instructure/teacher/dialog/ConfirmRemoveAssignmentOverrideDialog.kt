/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.blueprint.BaseCanvasAppCompatDialogFragment
import com.instructure.pandautils.analytics.SCREEN_VIEW_CONFIRM_REMOVE_ASSIGNMENT_OVERRIDE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.teacher.R
import com.instructure.pandautils.utils.BlindSerializableArg
import com.instructure.pandautils.utils.dismissExisting

@ScreenView(SCREEN_VIEW_CONFIRM_REMOVE_ASSIGNMENT_OVERRIDE)
class ConfirmRemoveAssignmentOverrideDialog : BaseCanvasAppCompatDialogFragment() {

    private var mListener: (() -> Unit)? by BlindSerializableArg()

    init { retainInstance = true }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.removeDueDate)
                .setMessage(R.string.removeDueDateMessage)
                .setPositiveButton(R.string.remove, { _, _ -> mListener?.invoke() })
                .setNegativeButton(R.string.cancel, null)
                .create()
        return dialog.apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
            }
        }
    }

    companion object {
        fun show(manager: FragmentManager, listener: () -> Unit) = ConfirmRemoveAssignmentOverrideDialog().apply {
            manager.dismissExisting<ConfirmRemoveAssignmentOverrideDialog>()
            mListener = listener
            show(manager, javaClass.simpleName)
        }
    }

    override fun onDestroyView() {
        mListener = null
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
