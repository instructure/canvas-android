/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instructure.student.R;
import com.instructure.student.model.EventData;
import com.instructure.pandautils.utils.Const;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import hirondelle.date4j.DateTime;

public class CanvasCalendarGridAdapter extends CaldroidGridAdapter {

    public CanvasCalendarGridAdapter(Context context, int month, int year, HashMap<String, Object> caldroidData, HashMap<String, Object> extraData) {
        super(context, month, year, caldroidData, extraData);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View cellView = convertView;

        //Grabs list of ExtraData objects for events
        ArrayList<EventData> eventList = (ArrayList<EventData>) extraData.get(Const.EVENT_LIST);

        if (convertView == null) {
            cellView = inflater.inflate(R.layout.calendar_grid_textview, null);
        }
        TextView tv1 = (TextView) cellView.findViewById(R.id.tv1);

        // Get dateTime of this cell
        DateTime dateTime = this.datetimeList.get(position);

        boolean noEvent = false;
        boolean notSelected = false;
        EventData.EventCount eventCount = EventData.EventCount.NONE;

        // Customize for dates with events
        if (eventList != null && eventList.size() > 0) {
            EventData eventData = getExtraData(eventList, dateTime);
            if (eventData != null) {
                eventCount = eventData.getEventCount();
                switch (eventCount) {
                    case MIN:
                        tv1.setBackgroundResource(R.drawable.calendar_min_events);
                        break;
                    case MID:
                        tv1.setBackgroundResource(R.drawable.calendar_mid_events);
                        break;
                    case MAX:
                        tv1.setBackgroundResource(R.drawable.calendar_max_events);
                        break;
                }
            } else {
                noEvent = true;
            }
        } else {
            noEvent = true;
        }

        // Customize for selected dates
        if (selectedDates != null && selectedDates.indexOf(dateTime) != -1) {
            switch (eventCount) {
                case NONE:
                    tv1.setBackgroundResource(R.drawable.calendar_selected_event);
                    break;
                case MIN:
                    tv1.setBackgroundResource(R.drawable.calendar_selected_min_events);
                    break;
                case MID:
                    tv1.setBackgroundResource(R.drawable.calendar_selected_mid_events);
                    break;
                case MAX:
                    tv1.setBackgroundResource(R.drawable.calendar_selected_max_events);
                    break;
            }
        } else {
            notSelected = true;
        }

        //If a date is not selected or has no events we will reset the background resource
        if (noEvent && notSelected) {
            tv1.setBackgroundResource(0);
        }

        //select "today"
        if (dateTime.equals(getToday())) {
            if (!notSelected && !noEvent) {
                switch (eventCount) {
                    case NONE:
                        tv1.setBackgroundResource(R.drawable.calendar_today_selected);
                        break;
                    case MIN:
                        tv1.setBackgroundResource(R.drawable.calendar_today_selected_min_events);
                        break;
                    case MID:
                        tv1.setBackgroundResource(R.drawable.calendar_today_selected_mid_events);
                        break;
                    case MAX:
                        tv1.setBackgroundResource(R.drawable.calendar_today_selected_max_events);
                        break;
                }
            } else if (!notSelected) {
                tv1.setBackgroundResource(R.drawable.calendar_today_selected);
            } else if (!noEvent) {
                switch (eventCount) {
                    case MIN:
                        tv1.setBackgroundResource(R.drawable.calendar_today_min_events);
                        break;
                    case MID:
                        tv1.setBackgroundResource(R.drawable.calendar_today_mid_events);
                        break;
                    case MAX:
                        tv1.setBackgroundResource(R.drawable.calendar_today_max_events);
                        break;
                }
            } else {
                tv1.setBackgroundResource(R.drawable.calendar_today);
            }
        }

        // Set color of the dates in previous / next month
//        if (dateTime.getMonth() != month) {
//            tv1.setTextColor(resources
//                    .getColor(com.caldroid.R.color.caldroid_darker_gray));
//        }

        tv1.setText("" + dateTime.getDay());

        return cellView;
    }

    private EventData getExtraData(ArrayList<EventData> list, DateTime date) {
        for (EventData d : list) {
            if (d.getDateTime().getMonth().equals(date.getMonth()) && d.getDateTime().getDay().equals(date.getDay()) && d.getDateTime().getYear().equals(date.getYear())) {
                return d;
            }
        }
        return null;
    }

}
