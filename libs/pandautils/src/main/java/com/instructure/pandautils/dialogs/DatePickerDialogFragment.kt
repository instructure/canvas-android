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
import com.instructure.pandautils.blueprint.BaseCanvasAppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.pandautils.analytics.SCREEN_VIEW_DATE_PICKER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.NullableSerializableArg
import com.instructure.pandautils.utils.SerializableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.dismissExisting
import java.util.*
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_DATE_PICKER)
class DatePickerDialogFragment : BaseCanvasAppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {

    var callback: (year: Int, month: Int, dayOfMonth: Int) -> Unit by Delegates.notNull()
    var defaultDate by SerializableArg(Date())
    var minDate by NullableSerializableArg<Date>()
    var maxDate by NullableSerializableArg<Date>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Setup default date
        val c = Calendar.getInstance()
        c.time = defaultDate
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dialog = DatePickerDialog(requireContext(), this, year, month, day)
        minDate?.let { dialog.datePicker.minDate = it.time }
        maxDate?.let { dialog.datePicker.maxDate = it.time }

        dialog.setOnShowListener {
            dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
        }

        return dialog
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        callback(year, month, dayOfMonth)
    }

    companion object {
        fun getInstance(manager: FragmentManager, defaultDate: Date? = null, minDate: Date? = null, maxDate: Date? = null, callback: (Int, Int, Int) -> Unit) : DatePickerDialogFragment {
            manager.dismissExisting<DatePickerDialogFragment>()
            val dialog = DatePickerDialogFragment()
            dialog.callback = callback
            defaultDate?.let { dialog.defaultDate = it }
            minDate?.let { dialog.minDate = it }
            maxDate?.let { dialog.maxDate = it }
            return dialog
        }
    }
}
