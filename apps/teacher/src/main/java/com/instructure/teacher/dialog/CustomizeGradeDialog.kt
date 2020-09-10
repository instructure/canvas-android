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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import kotlinx.android.synthetic.main.dialog_customize_grade.view.*
import kotlin.properties.Delegates

class CustomizeGradeDialog : AppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var mCustomizeGradeCallback: (String, Boolean) -> Unit by Delegates.notNull()
    private var mGrade by NullableStringArg()
    private var mGradingType by StringArg()
    private var mPointsPossible by StringArg()
    private var mIsGroupSubmission by BooleanArg()

    companion object {
        fun getInstance(fragmentManager: FragmentManager, pointsPossible: String, grade: String?, gradingType: String, isGroupSubmission: Boolean, callback: (String, Boolean) -> Unit) = CustomizeGradeDialog().apply {
            fragmentManager.dismissExisting<CustomizeGradeDialog>()
            mGrade = grade
            mGradingType = gradingType
            mPointsPossible = pointsPossible
            mIsGroupSubmission = isGroupSubmission
            mCustomizeGradeCallback = callback
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(requireActivity(), R.layout.dialog_customize_grade, null)
        val gradeEditText = view.gradeEditText
        val gradeTextHint = view.textHint
        val excusedCheckBox = view.excuseStudentCheckbox

        //style views
        ViewStyler.themeEditText(requireContext(), gradeEditText, ThemePrefs.brandColor)
        ViewStyler.themeCheckBox(requireContext(), excusedCheckBox, ThemePrefs.brandColor)

        // Change wording for groups
        if (mIsGroupSubmission) excusedCheckBox.setText(R.string.excuseGroup)

        //We need to adjust the padding for the edit text so it doesn't run into our "hint view"
        gradeTextHint.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            gradeEditText.setPaddingRelative(gradeEditText.paddingStart, gradeEditText.paddingTop, right - left, gradeEditText.paddingBottom)
        }

        gradeEditText.setText(mGrade ?: "", TextView.BufferType.EDITABLE)
        gradeEditText.setSelection(mGrade?.length ?: 0)
        gradeEditText.hint = ""

        when(Assignment.getGradingTypeFromAPIString(mGradingType)) {
            Assignment.GradingType.PERCENT -> gradeTextHint.text = "%"
            Assignment.GradingType.GPA_SCALE -> gradeTextHint.text = getString(R.string.gpa)
            Assignment.GradingType.LETTER_GRADE -> gradeTextHint.text = getString(R.string.letter_grade)
            else -> gradeTextHint.text = getString(R.string.out_of_points, mPointsPossible)
        }

        //listen for checkbox
        excusedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                gradeEditText.setText(getString(R.string.excused), TextView.BufferType.EDITABLE)
                gradeEditText.isEnabled = false
                gradeTextHint.setVisible(false)
            } else {
                gradeEditText.isEnabled = true
                gradeEditText.setText("", TextView.BufferType.EDITABLE)
                gradeTextHint.setVisible(true)
            }
        }

        val gradeDialog = AlertDialog.Builder(requireActivity())
                .setCancelable(true)
                .setTitle(getString(R.string.customize_grade))
                .setView(view)
                .setPositiveButton(getString(android.R.string.ok).toUpperCase()) { _, _ ->
                    updateGrade(gradeEditText.text.toString(), mGradingType, excusedCheckBox.isChecked)
                }
                .setNegativeButton(getString(android.R.string.cancel).toUpperCase(), null)
                .create()

        gradeDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        gradeDialog.setOnShowListener {
            gradeDialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            gradeDialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }

        // Close and update the grade when the user hits the 'Done' button on the keyboard
        gradeEditText.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                gradeDialog.getButton(AlertDialog.BUTTON_POSITIVE).callOnClick()
                handled = true
            }
            handled
        }

        return gradeDialog
    }

    private fun updateGrade(gradeText: String, gradingType: String, isChecked: Boolean) {
        //need to handle the percent grade edge case
        if (Assignment.getGradingTypeFromAPIString(gradingType) == Assignment.GradingType.PERCENT) {
            //check to see if they already have a % sign at the end
            if(gradeText.isNotEmpty() && gradeText.last().toString() == "%") {
                mCustomizeGradeCallback(gradeText, isChecked)
            } else {
                mCustomizeGradeCallback("$gradeText%", isChecked)
            }
        } else {
            //Otherwise we handle the grade like normal
            mCustomizeGradeCallback(gradeText, isChecked)
        }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
