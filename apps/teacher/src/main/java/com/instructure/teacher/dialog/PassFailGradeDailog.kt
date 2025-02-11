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
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.FragmentManager
import com.instructure.pandautils.analytics.SCREEN_VIEW_PASS_FAIL_GRADE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.dismissExisting
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.teacher.R
import com.instructure.teacher.utils.Const
import java.util.Locale
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_PASS_FAIL_GRADE)
class PassFailGradeDailog : BaseCanvasAppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var mPassFailGradeCallback: (String, Boolean) -> Unit by Delegates.notNull()

    companion object {
        fun getInstance(fragmentManager: FragmentManager, grade: String?, callback: (String, Boolean) -> Unit) : PassFailGradeDailog {
            fragmentManager.dismissExisting<PassFailGradeDailog>()
            val dialog = PassFailGradeDailog()
            val args = Bundle()
            args.putString(Const.GRADE, grade)
            dialog.arguments = args
            dialog.mPassFailGradeCallback = callback

            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(activity, R.layout.dialog_pass_fail_grade, null)
        val gradeOptions = resources.getStringArray(R.array.passFailArray)
        val grade: String? = nonNullArgs.getString(Const.GRADE)
        val passFailSpinner = view.findViewById<Spinner>(R.id.passFailSpinner)
        val excusedCheckBox = view.findViewById<AppCompatCheckBox>(R.id.excuseStudentCheckbox)
        val passFailAdapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_pass_fail_item, gradeOptions)

        //style views
        ViewStyler.themeCheckBox(requireContext(), excusedCheckBox, ThemePrefs.brandColor)

        //set spinner adapter
        passFailSpinner.adapter = passFailAdapter

        //listen for checkbox
        excusedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            passFailSpinner.isEnabled = !isChecked
        }

        //set the spinner selection to the user's current grade, default is complete (also 'complete' comes back as the grade regardless of the language)
        when(grade?.lowercase(Locale.getDefault())) {
            "complete" -> passFailSpinner.setSelection(0)
            "incomplete" -> passFailSpinner.setSelection(1)
            else -> passFailSpinner.setSelection(0)
        }

        val passFailDialog = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
                .setCancelable(true)
                .setTitle(getString(R.string.customize_grade))
                .setView(view)
                .setPositiveButton(getString(android.R.string.ok).uppercase(Locale.getDefault())) { _, _ ->
                    // The api needs the string "complete" or "incomplete" in English, not whatever language is currently selected
                    val complete = if (passFailSpinner.selectedItemPosition == 0) "complete" else "incomplete"

                    mPassFailGradeCallback(complete, excusedCheckBox.isChecked)
                }
            .setNegativeButton(getString(android.R.string.cancel).uppercase(Locale.getDefault()), null)
                .create()

        passFailDialog.setOnShowListener {
            passFailDialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            passFailDialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
        }

        return passFailDialog
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
