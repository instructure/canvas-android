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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.instructure.speedgrader.views.DateTextView;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
                            implements DatePickerDialog.OnDateSetListener {

    public final static String tag = "date_picker_tag";

    boolean wasCancelled;
    boolean shouldModifyTime;
    private Calendar calendar;

    public static DatePickerFragment getInstance(DateTextView dateTextView, boolean shouldModifyTime){

        DatePickerFragment datePickerFragment = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("dtv", dateTextView);
        bundle.putBoolean("time", shouldModifyTime);
        datePickerFragment.setArguments(bundle);

        return datePickerFragment;
    }

    private DateTextView dateTextView;

    //We need to prevent duplicates. If the user leaves the page, we don't want two DatePickerFragments.
    @Override
    public void onPause(){
        super.onPause();
        wasCancelled = true;
        dismiss();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        shouldModifyTime = getArguments().getBoolean("time",false);
        dateTextView = (DateTextView) getArguments().getSerializable("dtv");
        Date date = dateTextView.getDate();

        //Error Checking
        if(date == null){
            date = new Date();
        }

        //DatePickerDialog expects data in a way that calendar provides natively.
        calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, calendar.get(Calendar.YEAR),  calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        ((DatePickerDialog)getDialog()).getDatePicker().setCalendarViewShown(true);
        ((DatePickerDialog)getDialog()).getDatePicker().setSpinnersShown(false);

        ((DatePickerDialog)getDialog()).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                wasCancelled = true;
            }
        });

        ((DatePickerDialog)getDialog()).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(!wasCancelled){
                    dateTextView.setDate(calendar.getTime());

                    if (shouldModifyTime) {
                        TimePickerFragment timePickerFragment = TimePickerFragment.getInstance(dateTextView);
                        timePickerFragment.show(getFragmentManager(), "time_picker");
                    }
                }



            }
        });

    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Convert the data back to how the dateTextView expects.
            calendar.set(year, month, day);
    }
}