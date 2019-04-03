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

package com.instructure.speedgrader.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.instructure.speedgrader.views.DateTextView;

import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    private Calendar calendar;

    public static TimePickerFragment getInstance(DateTextView dateTextView){

        TimePickerFragment datePickerFragment = new TimePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("dtv", dateTextView);
        datePickerFragment.setArguments(bundle);

        return datePickerFragment;
    }

    private DateTextView dateTextView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        dateTextView = (DateTextView) getArguments().getSerializable("dtv");
        Date date = dateTextView.getDate();

        //Error Checking
        if(date == null){
            date = new Date();
        }

        //DatePickerDialog expects data in a way that calendar provides natively.
        calendar = Calendar.getInstance();
        calendar.setTime(date);

        boolean is24HourDateFormat = DateFormat.is24HourFormat(getActivity());

        return new TimePickerDialog(getActivity(), this, calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE), is24HourDateFormat);
    }


    //We need to prevent duplicates. If the user leaves the page, we don't want two DatePickerFragments.
    @Override
    public void onPause(){
        super.onPause();
        dismiss();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        dateTextView.setDate(calendar.getTime());
    }
}