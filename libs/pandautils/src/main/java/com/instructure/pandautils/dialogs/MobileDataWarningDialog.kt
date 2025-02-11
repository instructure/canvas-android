/*
 * Copyright (C) 2018 - present  Instructure, Inc.
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
 */
package com.instructure.pandautils.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.FragmentManager
import com.instructure.canvasapi2.utils.NetworkUtils
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_MOBILE_DATA_WARNING
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*

@ScreenView(SCREEN_VIEW_MOBILE_DATA_WARNING)
class MobileDataWarningDialog : BaseCanvasAppCompatDialogFragment() {

    private var mOnProceed: (() -> Unit)? by BlindSerializableArg()

    init { retainInstance = true }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val checkBox = AppCompatCheckBox(requireContext()).apply {
            isChecked = false
            setText(R.string.utils_doNotShowMessageAgain)
            textSize = 12f
        }

        val checkBoxContainer = FrameLayout(requireContext()).apply {
            val pad = context.DP(16).toInt()
            setPadding(pad, pad, pad, 0)
            addView(checkBox)
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.AccessibleAlertDialog)
                .setTitle(R.string.utils_dataUsageWarningTitle)
                .setMessage(R.string.utils_dataUsageWarningMessage)
                .setView(checkBoxContainer)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    PandaPrefs.warnForMobileData = !checkBox.isChecked
                    mOnProceed?.invoke()
                }
                .setNegativeButton(R.string.utils_cancel) { _, _ ->
                    PandaPrefs.warnForMobileData = !checkBox.isChecked
                }
                .create()
        return dialog.apply {
            setOnShowListener {
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
            }
        }
    }

    override fun onDestroyView() {
        mOnProceed = null
        // Fix for rotation bug
        dialog?.let { if (retainInstance) it.setDismissMessage(null) }
        super.onDestroyView()
    }

    companion object {
        fun showIfNeeded(manager: FragmentManager, onProceed: () -> Unit) {
            if (NetworkUtils.isUsingMobileData && PandaPrefs.warnForMobileData) {
                MobileDataWarningDialog().apply {
                    manager.dismissExisting<MobileDataWarningDialog>()
                    mOnProceed = onProceed
                    show(manager, javaClass.simpleName)
                }
            } else {
                onProceed()
            }
        }
    }
}
