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
package com.instructure.parentapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.instructure.parentapp.database.DatabaseHandler;
import com.instructure.parentapp.models.CalendarWrapper;

import java.sql.SQLException;
import java.util.ArrayList;

public class BootReceiver extends BroadcastReceiver {
    AlarmReceiver alarm = new AlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            //set alarms here
            DatabaseHandler mDatabaseHandler = new DatabaseHandler(context);
            try {
                mDatabaseHandler.open();
                ArrayList<CalendarWrapper> calendars = mDatabaseHandler.getAllAlarms();
                if(calendars != null) {
                    for(CalendarWrapper wrapper : calendars) {
                        alarm.setAlarm(context, wrapper.getCalendar(), wrapper.getAssignmentId(), wrapper.getTitle(), wrapper.getSubTitle());
                    }
                }
                mDatabaseHandler.close();
            } catch (SQLException e) {
                //do nothing
            }
        }
    }
}
