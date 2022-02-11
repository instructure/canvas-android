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

package com.instructure.pandautils.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.pandautils.analytics.SCREEN_VIEW_DATE_PICKER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.SerializableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.dismissExisting
import java.util.*
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_DATE_PICKER)
class DatePickerDialogFragment : AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {

    var mCallback: (year: Int, month: Int, dayOfMonth: Int) -> Unit by Delegates.notNull()
    var mDefaultDate by SerializableArg(Date())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Setup default date
        val c = Calendar.getInstance()
        c.time = mDefaultDate
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dialog = DatePickerDialog(requireContext(), this, year, month, day)

        dialog.setOnShowListener {
            dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }

        return dialog
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mCallback(year, month, dayOfMonth)
    }

    companion object {
        fun getInstance(manager: FragmentManager, defaultDate: Date? = null, callback: (Int, Int, Int) -> Unit) : DatePickerDialogFragment {
            manager.dismissExisting<DatePickerDialogFragment>()
            val dialog = DatePickerDialogFragment()
            dialog.mCallback = callback
            defaultDate?.let { dialog.mDefaultDate = it }
            return dialog
        }
    }
}
