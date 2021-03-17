/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.mobius.settings.help

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.instructure.student.R

class HelpDialogFragment : DialogFragment() {

    @SuppressLint("InflateParams") // Suppress lint warning about null parent when inflating layout
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext()).setTitle(requireContext().getString(R.string.help))
        val view = LayoutInflater.from(activity).inflate(R.layout.help_dialog, null)

        builder.setView(view)

        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(true)

//        loadHelpLinks(view)

        return dialog
    }

    override fun onDestroyView() {
        dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    companion object {
        const val TAG = "helpDialog"

        fun show(activity: FragmentActivity): HelpDialogFragment =
            HelpDialogFragment().apply {
                show(activity.supportFragmentManager, TAG)
            }
    }
}