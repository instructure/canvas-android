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
package com.instructure.student.fragment

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    var timePickerListener: TimePickerFragmentListener? = null

    var cancelListener: TimePickerCancelListener? = null

    private var hour = -1
    private var minute = -1


    public interface TimePickerFragmentListener {
        fun onTimeSet(hourOfDay: Int, minute: Int)
    }

    public interface TimePickerCancelListener {
        fun onCancel()
    }

    fun notifyTimePickerCancelListener() {
        if (cancelListener != null) {
            cancelListener!!.onCancel()
        }
    }

    fun notifyTimePickerListener(hourOfDay: Int, minute: Int) {
        if (this.timePickerListener != null) {
            this.timePickerListener!!.onTimeSet(hourOfDay, minute)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        if (hour != -1 && minute != -1) {
            return TimePickerDialog(requireActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(activity))
        } else {
            return TimePickerDialog(requireActivity(), this, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(activity))
        }
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

        fun newInstance(listener: TimePickerFragmentListener, hour: Int, minute: Int, cancelListener: TimePickerCancelListener): TimePickerFragment {
            val fragment = TimePickerFragment()
            fragment.timePickerListener = listener
            fragment.cancelListener = cancelListener
            fragment.hour = hour
            fragment.minute = minute
            return fragment
        }
    }
}
