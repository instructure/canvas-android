/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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
package com.instructure.parentapp.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

open class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    var timePickerListener: TimePickerFragmentListener? = null

    var cancelListener: TimePickerCancelListener? = null


    interface TimePickerFragmentListener {
        fun onTimeSet(hourOfDay: Int, minute: Int)
    }

    interface TimePickerCancelListener {
        fun onCancel()
    }

    private fun notifyTimePickerCancelListener() {
        if (cancelListener != null) {
            cancelListener!!.onCancel()
        }
    }

    private fun notifyTimePickerListener(hourOfDay: Int, minute: Int) {
        if (this.timePickerListener != null) {
            this.timePickerListener!!.onTimeSet(hourOfDay, minute)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute,
                DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        notifyTimePickerListener(hourOfDay, minute)
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        notifyTimePickerCancelListener()
    }

    companion object {

        fun newInstance(listener: TimePickerFragmentListener, cancelListener: TimePickerCancelListener): TimePickerFragment {
            val fragment = TimePickerFragment()
            fragment.timePickerListener = listener
            fragment.cancelListener = cancelListener
            return fragment
        }
    }
}
