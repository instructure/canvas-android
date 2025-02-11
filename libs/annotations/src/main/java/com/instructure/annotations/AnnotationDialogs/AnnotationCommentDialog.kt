/*
 * Copyright (C) 2018 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.annotations.AnnotationDialogs

import android.app.Dialog
import android.content.res.Configuration
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
import com.instructure.annotations.R
import com.instructure.pandautils.utils.*
import kotlin.properties.Delegates


class AnnotationCommentDialog : BaseCanvasAppCompatDialogFragment() {

    init {
        retainInstance = true
    }

    private var mFreeTextCallback: (cancelled: Boolean, text: String?) -> Unit by Delegates.notNull()
    private var mCurrentText by StringArg()
    private var mTitle by StringArg()

    companion object {
        fun getInstance(manager: FragmentManager, currentText: String = "", title: String, callback: (Boolean, String?) -> Unit)  = AnnotationCommentDialog().apply {
            manager.dismissExisting<AnnotationCommentDialog>()
            mFreeTextCallback = callback
            mCurrentText = currentText
            mTitle = title
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        val view = View.inflate(activity, R.layout.dialog_annotation_comment, null)
        val freeTextEditText = view.findViewById<AppCompatEditText>(R.id.freeTextInput)
        freeTextEditText.setText(mCurrentText)
        ViewStyler.themeEditText(requireContext(), freeTextEditText, ThemePrefs.brandColor)
        if(mCurrentText.isNotEmpty()) {
            freeTextEditText.selectAll()
        }
        freeTextEditText.inputType = EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
        freeTextEditText.hint = requireContext().getString(R.string.comment)

        val dialog = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
                .setTitle(mTitle)
                .setView(view)
                .setPositiveButton(requireContext().getString(android.R.string.ok)) { _, _ ->
                    mFreeTextCallback(false, freeTextEditText.text.toString())
                }
            .setNegativeButton(if(mCurrentText.isNotEmpty()) requireContext().getString(R.string.cancel) else requireContext().getString(R.string.skip)) { _, _ ->
                mFreeTextCallback(true, null)
            }
            .create()

        //Adjust the dialog to the top so keyboard does not cover it up, issue happens on tablets in landscape
        if (isTablet && requireContext().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            val params = dialog.window?.attributes
            params?.gravity = Gravity.CENTER or Gravity.TOP
            params?.y = requireContext().resources.getDimensionPixelSize(R.dimen.utils_landscapeTabletDialogAdjustment)
            dialog.window?.attributes = params
            dialog.window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }

        dialog.setOnShowListener {
            dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
        }

        return dialog
    }

    override fun onDestroyView() {
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }
}
