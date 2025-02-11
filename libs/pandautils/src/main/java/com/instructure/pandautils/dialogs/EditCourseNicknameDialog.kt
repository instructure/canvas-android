package com.instructure.pandautils.dialogs

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
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_COURSE_NICKNAME
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import java.util.Locale
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_EDIT_COURSE_NICKNAME)
class EditCourseNicknameDialog : BaseCanvasAppCompatDialogFragment() {

    private var mEditNicknameCallback: (String) -> Unit by Delegates.notNull()

    init {
        retainInstance = true
    }

    companion object {
        fun getInstance(manager: FragmentManager, course: Course, callback: (String) -> Unit) : EditCourseNicknameDialog {
            manager.dismissExisting<EditCourseNicknameDialog>()
            val dialog = EditCourseNicknameDialog()
            val args = Bundle()
            args.putParcelable(Const.COURSE, course)
            dialog.arguments = args
            dialog.mEditNicknameCallback = callback
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val course : Course = nonNullArgs.get(Const.COURSE) as Course
        val view = View.inflate(activity, R.layout.dialog_course_nickname, null)
        val editCourseNicknameEditText = view.findViewById<AppCompatEditText>(R.id.newCourseNickname)
        editCourseNicknameEditText.setText(course.name)
        ViewStyler.themeEditText(requireContext(), editCourseNicknameEditText, ThemePrefs.brandColor)
        editCourseNicknameEditText.inputType = EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
        editCourseNicknameEditText.selectAll()

        val nameDialog = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
                .setCancelable(true)
                .setTitle(getString(R.string.edit_course_nickname))
                .setView(view)
                .setPositiveButton(getString(android.R.string.ok).uppercase(Locale.getDefault())) { _, _ ->
                    mEditNicknameCallback(editCourseNicknameEditText.text.toString())
                }
            .setNegativeButton(getString(android.R.string.cancel).uppercase(Locale.getDefault()), null)
                .create()
        nameDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        editCourseNicknameEditText.onTextChanged {
            if (course.originalName == null && it.isEmpty()) {
                nameDialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = false
                nameDialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.increaseAlpha(ThemePrefs.textButtonColor, 128))
            } else {
                nameDialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = true
                nameDialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            }
        }

        nameDialog.setOnShowListener {
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
