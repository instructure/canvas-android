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

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener, DialogInterface.OnCancelListener {

    var datePickerListener: DatePickerFragmentListener? = null
    var cancelListener: DatePickerCancelListener? = null

    private var year = -1
    private var month = -1
    private var day = -1

    interface DatePickerFragmentListener {
        fun onDateSet(year: Int, month: Int, day: Int)

    }

    interface DatePickerCancelListener {
        fun onCancel()

    }

    fun notifyDatePickerCancelListener() {
        if (cancelListener != null) {
            cancelListener!!.onCancel()
        }
    }

    fun notifyDatePickerListener(year: Int, month: Int, day: Int) {
        if (datePickerListener != null) {
            datePickerListener!!.onDateSet(year, month, day)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //use the current day as the default
        val c = Calendar.getInstance()
        if (year != -1 && month != -1 && day != -1) {
            return DatePickerDialog(requireContext(), this, year, month, day)
        } else {
            return DatePickerDialog(requireContext(), this,
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH))
        }
    }

    override fun onDateSet(datePicker: DatePicker, year: Int, month: Int, day: Int) {
        // Here we call the listener and pass the info back to it.
        notifyDatePickerListener(year, month, day)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        notifyDatePickerCancelListener()
    }

    companion object {

        fun newInstance(listener: DatePickerFragmentListener, cancelListener: DatePickerCancelListener): DatePickerFragment {
            val fragment = DatePickerFragment()
            fragment.datePickerListener = listener
            fragment.cancelListener = cancelListener
            return fragment
        }

        fun newInstance(listener: DatePickerFragmentListener, cancelListener: DatePickerCancelListener, year: Int, month: Int, day: Int): DatePickerFragment {
            val fragment = DatePickerFragment()
            fragment.datePickerListener = listener
            fragment.cancelListener = cancelListener
            fragment.year = year
            fragment.month = month
            fragment.day = day
            return fragment
        }
    }
}
