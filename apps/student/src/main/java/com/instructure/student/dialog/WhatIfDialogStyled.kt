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

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.analytics.SCREEN_VIEW_WHAT_IF
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.IntArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.student.R
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_WHAT_IF)
class WhatIfDialogStyled : BaseCanvasDialogFragment() {

    private var callback: (Double?, Double) -> Unit by Delegates.notNull()
    private var assignment: Assignment by ParcelableArg()
    private var textButtonColor: Int by IntArg()

    private var currentScoreView: AppCompatEditText? = null

    interface WhatIfDialogCallback {
        fun onClick(assignment: Assignment, position: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
                .setTitle(getString(R.string.whatIfDialogText))
                .setPositiveButton(R.string.done) { _, _ ->
                    try {
                        val whatIfText = currentScoreView?.text.toString()
                        callback(if(whatIfText.isBlank()) null else whatIfText.toDouble(), assignment.pointsPossible)
                    } catch (e: Throwable) {
                        callback(null, assignment.pointsPossible)
                    }
                    dismissAllowingStateLoss()
                }
                .setNegativeButton(R.string.cancel) { _, _ -> dismissAllowingStateLoss() }

        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_what_if, null)
        view.findViewById<TextView>(R.id.totalScore)?.text = assignment.pointsPossible.toString()
        currentScoreView = view.findViewById(R.id.currentScore)
        builder.setView(view)

        val dialog = builder.create()
        dialog.setOnShowListener {
            if (textButtonColor != 0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(textButtonColor)
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textButtonColor)
            }
        }

        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    override fun onDestroyView() {
        val dialog = dialog
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && retainInstance) dialog.setDismissMessage(null)
        super.onDestroyView()
    }

    companion object {
        fun show(fragmentManager: FragmentManager, assignment: Assignment, textButtonColor: Int, callback: (Double?, Double) -> Unit) {
            (fragmentManager.findFragmentByTag(WhatIfDialogStyled::class.java.simpleName) as? WhatIfDialogStyled)?.dismissAllowingStateLoss()

            WhatIfDialogStyled().apply {
                this.assignment = assignment
                this.textButtonColor = textButtonColor
                this.callback = callback
            }.show(fragmentManager, WhatIfDialogStyled::class.java.simpleName)
        }
    }
}
