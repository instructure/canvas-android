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
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.globalName
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_COURSE_NAME
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_EDIT_COURSE_NAME)
class EditCourseNameDialog : BaseCanvasAppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var mEditNameCallback: (String) -> Unit by Delegates.notNull()

    companion object {
        fun getInstance(manager: FragmentManager, course: Course, callback: (String) -> Unit) : EditCourseNameDialog {
            manager.dismissExisting<EditCourseNameDialog>()
            val dialog = EditCourseNameDialog()
            val args = Bundle()
            args.putParcelable(Const.COURSE, course)
            dialog.arguments = args
            dialog.mEditNameCallback = callback
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val course : Course = nonNullArgs.get(Const.COURSE) as Course
        val view = View.inflate(requireActivity(), R.layout.dialog_rename_course, null)
        val editCourseNameEditText = view.findViewById<AppCompatEditText>(R.id.newCourseName)
        editCourseNameEditText.setText(course.globalName)
        ViewStyler.themeEditText(requireContext(), editCourseNameEditText, ThemePrefs.brandColor)
        editCourseNameEditText.inputType = EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
        editCourseNameEditText.selectAll()

        val nameDialog = AlertDialog.Builder(requireActivity())
                .setCancelable(true)
                .setTitle(getString(R.string.course_name))
                .setView(view)
                .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                    mEditNameCallback(editCourseNameEditText.text.toString())
                }
            .setNegativeButton(getString(R.string.cancel), null)
                .create()

        //Adjust the dialog to the top so keyboard does not cover it up, issue happens on tablets in landscape
        nameDialog.window?.let { window ->
            window.attributes = window.attributes.apply {
                gravity = Gravity.CENTER or Gravity.TOP
                y = 120
            }
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
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
