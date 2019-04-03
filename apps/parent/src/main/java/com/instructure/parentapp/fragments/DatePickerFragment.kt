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

    interface DatePickerFragmentListener {
        fun onDateSet(year: Int, month: Int, day: Int)

    }

    interface DatePickerCancelListener {
        fun onCancel()

    }

    protected fun notifyDatePickerCancelListener() {
        if (cancelListener != null) {
            cancelListener!!.onCancel()
        }
    }

    protected fun notifyDatePickerListener(year: Int, month: Int, day: Int) {
        if (datePickerListener != null) {
            datePickerListener!!.onDateSet(year, month, day)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //use the current day as the default
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, this, year, month, day)
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
    }
}
