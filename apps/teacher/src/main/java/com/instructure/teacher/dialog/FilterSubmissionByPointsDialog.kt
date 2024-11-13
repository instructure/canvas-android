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
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import com.instructure.pandautils.blueprint.BaseCanvasAppCompatDialogFragment
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import java.util.*
import kotlin.properties.Delegates

class FilterSubmissionByPointsDialog : BaseCanvasAppCompatDialogFragment() {

    private var mFilterCallback: (Double) -> Unit by Delegates.notNull()

    init {
        retainInstance = true
    }

    companion object {
        fun getInstance(manager: FragmentManager, title: String, points: Double, callback: (Double) -> Unit) : FilterSubmissionByPointsDialog {
            manager.dismissExisting<FilterSubmissionByPointsDialog>()
            val dialog = FilterSubmissionByPointsDialog()
            val args = Bundle()
            args.putString(Const.TITLE, title)
            args.putDouble(Const.POINTS, points)
            dialog.arguments = args
            dialog.mFilterCallback = callback
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title : String = nonNullArgs.getString(Const.TITLE).orEmpty()
        val points : Double = nonNullArgs.getDouble(Const.POINTS)

        val view = View.inflate(requireActivity(), R.layout.dialog_filter_submission_by_points, null)
        val editPointsEditText = view.findViewById<AppCompatEditText>(R.id.filterByPoints)
        ViewStyler.themeEditText(requireContext(), editPointsEditText, ThemePrefs.brandColor)
        editPointsEditText.inputType = EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL

        val pointsTextView = view.findViewById<TextView>(R.id.pointsTextView)
        pointsTextView.text = requireActivity().resources.getQuantityString(R.plurals.submission_points_possible, if(points == 1.0) 1 else 2, NumberHelper.formatDecimal(points, 2, true))
        val nameDialog = AlertDialog.Builder(requireActivity())
                .setCancelable(true)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(getString(android.R.string.ok).uppercase(Locale.getDefault()), null)
                .setNegativeButton(getString(android.R.string.cancel).uppercase(Locale.getDefault()), null)
                .create()
        nameDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)


        nameDialog.setOnShowListener {
            //move the listener here so we can check for empty before we close the dialog
            val positiveButton = nameDialog.getButton(AppCompatDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener { _ ->
                if(editPointsEditText.text.isNullOrBlank()) {
                    Toast.makeText(requireActivity(), R.string.filterPointsMustBeSet, Toast.LENGTH_SHORT).show()
                } else {
                    mFilterCallback(editPointsEditText.text.toString().toDouble())
                    nameDialog.dismiss()
                }
            }

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
}
