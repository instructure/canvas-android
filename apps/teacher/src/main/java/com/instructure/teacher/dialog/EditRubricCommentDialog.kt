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

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_RUBRIC_COMMENT
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.databinding.ViewEditGradeCommentBinding
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.view.edit_rubric.RubricCommentEditedEvent
import org.greenrobot.eventbus.EventBus

@ScreenView(SCREEN_VIEW_EDIT_RUBRIC_COMMENT)
class EditRubricCommentDialog : BaseCanvasAppCompatDialogFragment() {

    private lateinit var binding: ViewEditGradeCommentBinding

    var mCriterionId by StringArg()
    var mStudentId by LongArg(-1L)
    var mAssigneeName by StringArg()
    var mAssigneePronouns by NullableStringArg()
    var mDefaultString by StringArg()
    var mGradeAnonymously by BooleanArg()

    init {
        retainInstance = true
    }

    companion object {
        fun show(
                manager: FragmentManager,
                criterionId: String,
                studentId: Long,
                assigneeName: String,
                assigneePronouns: String?,
                gradeAnonymously: Boolean,
                defaultString: String = ""
        ) = EditRubricCommentDialog().apply {
            manager.dismissExisting<EditRubricCommentDialog>()
            mCriterionId = criterionId
            mStudentId = studentId
            mAssigneeName = assigneeName
            mAssigneePronouns = assigneePronouns
            mDefaultString = defaultString
            mGradeAnonymously = gradeAnonymously
            show(manager, javaClass.simpleName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = AppCompatDialog(requireContext(), R.style.Theme_AppCompat_DayNight_Translucent).apply {
        binding = ViewEditGradeCommentBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        // Send event bus on save, dismiss dialog. Send null if text is blank (i.e. delete comment)
        binding.saveCommentButton.onClick {
            val text = binding.commentEditText.text.toString()
            EventBus.getDefault().post(RubricCommentEditedEvent(mCriterionId, if (text.isBlank()) null else text, mStudentId))
            dismiss()
        }

        ViewStyler.themeButton(binding.dismissEditCommentButton)
        // Dismiss on outside click
        binding.dismissEditCommentButton.onClick { dismiss() }

        // Set up EditText
        if (!mGradeAnonymously) binding.commentEditText.hint = Pronouns.resource(
            context,
            R.string.sendMessageToHint,
            mAssigneePronouns,
            Pronouns.span(mAssigneeName, mAssigneePronouns)
        )
        binding.commentEditText.highlightColor = ThemePrefs.increaseAlpha(ThemePrefs.brandColor)
        binding.commentEditText.setText(mDefaultString)

        // Theme save button
        DrawableCompat.setTint(binding.saveCommentButton.drawable, ThemePrefs.textButtonColor)

        // Style the dialog
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.statusBarColor = requireActivity().getColorCompat(com.instructure.pandautils.R.color.dimLighterGray)
        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
