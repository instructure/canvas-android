/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import com.instructure.pandautils.analytics.SCREEN_VIEW_FATAL_ERROR
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.student.R

@ScreenView(SCREEN_VIEW_FATAL_ERROR)
class FatalErrorDialogStyled : BaseCanvasDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val activity = activity
        val args = nonNullArgs

        val builder = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
                .setTitle(args.getInt(TITLE))
                .setPositiveButton(requireContext().getString(R.string.okay)) { dialog, _ ->
                    val shouldDismiss = args.getBoolean(SHOULD_DISMISS, false)
                    if (shouldDismiss) {
                        dialog.dismiss()
                    } else {
                        activity?.finish()
                    }
                }
                .setMessage(args.getInt(MESSAGE))

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.brandColor);
        }

        return dialog
    }

    override fun onDestroyView() {
        if (retainInstance) dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    companion object {
        const val TAG = "fatalErrorDialog"

        private const val TITLE = "title"
        private const val MESSAGE = "message"
        private const val SHOULD_DISMISS = "shouldDismiss"

        fun newInstance(title: Int, message: Int): FatalErrorDialogStyled {
            val frag = FatalErrorDialogStyled()
            val args = Bundle()
            args.putInt(TITLE, title)
            args.putInt(MESSAGE, message)
            args.putBoolean(SHOULD_DISMISS, false)
            frag.arguments = args
            return frag
        }

        /* @param shouldDismiss: if true dismiss the dialog, otherwise finish the activity */
        fun newInstance(title: Int, message: Int, shouldDismiss: Boolean): FatalErrorDialogStyled {
            val frag = newInstance(title, message)
            val args = frag.nonNullArgs
            args.putBoolean(SHOULD_DISMISS, shouldDismiss)
            frag.arguments = args
            return frag
        }
    }
}
