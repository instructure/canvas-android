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

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatDialog
import com.instructure.pandautils.base.BaseCanvasAppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.instructure.pandautils.analytics.SCREEN_VIEW_TIME_PICKER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.SerializableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.dismissExisting
import java.util.*
import kotlin.properties.Delegates

@ScreenView(SCREEN_VIEW_TIME_PICKER)
class TimePickerDialogFragment : BaseCanvasAppCompatDialogFragment(), TimePickerDialog.OnTimeSetListener {

    var mCallback: (hourOfDay: Int, minute: Int) -> Unit by Delegates.notNull()
    var mDefaultDate by SerializableArg(Date())

    @Suppress("unused")
    private fun TimePickerDialog() { }

    companion object {
        fun getInstance(manager: FragmentManager, defaultDate: Date? = null, callback: (Int, Int) -> Unit) : TimePickerDialogFragment {
            manager.dismissExisting<TimePickerDialogFragment>()
            val dialog = TimePickerDialogFragment()
            dialog.mCallback = callback
            defaultDate?.let { dialog.mDefaultDate = it }
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Default time
        val c = Calendar.getInstance()
        c.time = mDefaultDate
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val dialog = TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))

        dialog.setOnShowListener {
            dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
        }

        return dialog
    }


    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        mCallback(hourOfDay, minute)
    }

}
