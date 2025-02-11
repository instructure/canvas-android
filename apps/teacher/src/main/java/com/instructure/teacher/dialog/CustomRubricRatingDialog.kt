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
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.analytics.SCREEN_VIEW_CUSTOM_RUBRIC_RATING
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.view.edit_rubric.RatingSelectedEvent
import org.greenrobot.eventbus.EventBus
import java.util.Locale

@ScreenView(SCREEN_VIEW_CUSTOM_RUBRIC_RATING)
class CustomRubricRatingDialog : BaseCanvasAppCompatDialogFragment() {

    var mCriterionId by StringArg()
    var mStudentId by LongArg(-1L)
    var mMaxValue by DoubleArg()
    var mOldValue by DoubleArg()

    init {
        retainInstance = true
    }

    companion object {
        fun show(manager: FragmentManager, criterionId: String, studentId: Long, oldValue: Double, maxValue: Double) = CustomRubricRatingDialog().apply {
            manager.dismissExisting<CustomRubricRatingDialog>()
            mCriterionId = criterionId
            mStudentId = studentId
            mMaxValue = maxValue
            mOldValue = oldValue
            show(manager, javaClass.simpleName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val editText = AppCompatEditText(requireContext()).apply {
            ViewStyler.themeEditText(requireContext(), this, ThemePrefs.brandColor)
            setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
            setText(NumberHelper.formatDecimal(mOldValue, 4, true))
            selectAll()
        }

        val container = FrameLayout(requireContext()).apply {
            val padding: Int = requireContext().DP(16f).toInt()
            setPaddingRelative(padding, 0, padding, 0)
            addView(editText)
        }

        val onSave = { _: DialogInterface, _: Int ->
            val newScore = editText.text.toString().toDoubleOrNull()?.coerceAtMost(mMaxValue)
            EventBus.getDefault().post(RatingSelectedEvent(newScore, mCriterionId, null, mStudentId))
        }

        return AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
                .setCancelable(true)
                .setTitle(getString(R.string.criterion_rating_customize_score))
                .setView(container)
                .setPositiveButton(getString(android.R.string.ok).uppercase(Locale.getDefault()), onSave)
                .setNegativeButton(getString(android.R.string.cancel).uppercase(Locale.getDefault()), null)
                .create()
                .apply {
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    setOnShowListener {
                        getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                        getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
                    }
                }
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

}
