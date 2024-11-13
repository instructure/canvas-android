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
import com.instructure.pandautils.blueprint.BaseCanvasAppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.analytics.SCREEN_VIEW_CUSTOMIZE_GRADE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.databinding.DialogCustomizeGradeBinding
import java.util.*
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_CUSTOMIZE_GRADE)
class CustomizeGradeDialog : BaseCanvasAppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var customizeGradeCallback: (String, Boolean) -> Unit by Delegates.notNull()
    private var grade by NullableStringArg()
    private var gradingType by StringArg()
    private var pointsPossible by StringArg()
    private var isGroupSubmission by BooleanArg()
    private var shouldShowExcuse by BooleanArg()

    companion object {
        fun getInstance(fragmentManager: FragmentManager, pointsPossible: String, grade: String?, gradingType: String, isGroupSubmission: Boolean, shouldShowExcuse: Boolean, callback: (String, Boolean) -> Unit) = CustomizeGradeDialog().apply {
            fragmentManager.dismissExisting<CustomizeGradeDialog>()
            this.grade = grade
            this.gradingType = gradingType
            this.pointsPossible = pointsPossible
            this.isGroupSubmission = isGroupSubmission
            this.customizeGradeCallback = callback
            this.shouldShowExcuse = shouldShowExcuse
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogCustomizeGradeBinding.inflate(layoutInflater)
        val gradeEditText = binding.gradeEditText
        val gradeTextHint = binding.textHint
        val excusedCheckBox = binding.excuseStudentCheckbox

        //style views
        ViewStyler.themeEditText(requireContext(), gradeEditText, ThemePrefs.brandColor)
        ViewStyler.themeCheckBox(requireContext(), excusedCheckBox, ThemePrefs.brandColor)

        // Change wording for groups
        if (isGroupSubmission) excusedCheckBox.setText(R.string.excuseGroup)

        //We need to adjust the padding for the edit text so it doesn't run into our "hint view"
        gradeTextHint.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            gradeEditText.setPaddingRelative(gradeEditText.paddingStart, gradeEditText.paddingTop, right - left, gradeEditText.paddingBottom)
        }

        gradeEditText.setText(grade.orEmpty(), TextView.BufferType.EDITABLE)
        gradeEditText.setSelection(grade?.length ?: 0)
        gradeEditText.hint = ""

        when(Assignment.getGradingTypeFromAPIString(gradingType)) {
            Assignment.GradingType.PERCENT -> gradeTextHint.text = "%"
            Assignment.GradingType.GPA_SCALE -> gradeTextHint.text = getString(R.string.gpa)
            Assignment.GradingType.LETTER_GRADE -> gradeTextHint.text = getString(R.string.letter_grade)
            else -> gradeTextHint.text = getString(R.string.out_of_points, pointsPossible)
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
                .setView(binding.root)
                .setPositiveButton(getString(android.R.string.ok).uppercase(Locale.getDefault())) { _, _ ->
                    updateGrade(gradeEditText.text.toString(), gradingType, excusedCheckBox.isChecked)
                }
                .setNegativeButton(getString(android.R.string.cancel).uppercase(Locale.getDefault()), null)
                .create()

        gradeDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        gradeDialog.setOnShowListener {
            gradeDialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            gradeDialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
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

        if (!shouldShowExcuse) {
            excusedCheckBox.visibility = View.GONE
        }

        return gradeDialog
    }

    private fun updateGrade(gradeText: String, gradingType: String, isChecked: Boolean) {
        //need to handle the percent grade edge case
        if (Assignment.getGradingTypeFromAPIString(gradingType) == Assignment.GradingType.PERCENT) {
            //check to see if they already have a % sign at the end
            if(gradeText.isNotEmpty() && gradeText.last().toString() == "%") {
                customizeGradeCallback(gradeText, isChecked)
            } else {
                customizeGradeCallback("$gradeText%", isChecked)
            }
        } else {
            //Otherwise we handle the grade like normal
            customizeGradeCallback(gradeText, isChecked)
        }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
