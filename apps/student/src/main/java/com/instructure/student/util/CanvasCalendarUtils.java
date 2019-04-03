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

package com.instructure.student.util;

import com.instructure.student.model.DateWindow;
import com.instructure.canvasapi2.utils.DateHelper;

import java.util.Date;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class CanvasCalendarUtils {

    /**
     * Another helper method, used to determine and select the date window to select to properly
     * highlight a full week in the calendar grid.
     *
     * @param date - selected date
     * @return DateWindow object
     */
    public static DateWindow setSelectedWeekWindow(Date date, boolean isStartDayMonday) {
        DateTime start = DateTime.forInstant(date.getTime(), TimeZone.getDefault());
        DateTime end = DateTime.forInstant(date.getTime(), TimeZone.getDefault());
        DateTime s1 = start;
        DateTime e1 = end;
        if (isStartDayMonday) {
            int s = start.getWeekDay(); //returns 1 - 7, sun - sat
            switch (s) {
                case 1:
                    s1 = start.minus(0, 0, 6, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    break;
                case 2:
                    e1 = start.plus(0, 0, 6, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    break;
                case 3:
                    s1 = start.minus(0, 0, 1, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    e1 = end.plus(0, 0, 5, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                    break;
                case 4:
                    s1 = start.minus(0, 0, 2, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    e1 = end.plus(0, 0, 4, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                    break;
                case 5:
                    s1 = start.minus(0, 0, 3, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    e1 = end.plus(0, 0, 3, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                    break;
                case 6:
                    s1 = start.minus(0, 0, 4, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    e1 = end.plus(0, 0, 2, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                    break;
                case 7:
                    s1 = start.minus(0, 0, 5, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    e1 = end.plus(0, 0, 1, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                    break;
            }
        } else {
            int s = start.getWeekDay(); //returns 1 - 7, sun - sat
            switch (s) {
                case 1:
                    e1 = start.plus(0, 0, 6, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    break;
                case 2:
                    s1 = start.minus(0, 0, 1, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    e1 = end.plus(0, 0, 5, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                    break;
                case 3:
                    s1 = start.minus(0, 0, 2, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    e1 = end.plus(0, 0, 4, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                    break;
                case 4:
                    s1 = start.minus(0, 0, 3, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    e1 = end.plus(0, 0, 3, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                    break;
                case 5:
                    s1 = start.minus(0, 0, 4, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    e1 = end.plus(0, 0, 2, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                    break;
                case 6:
                    s1 = start.minus(0, 0, 5, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    e1 = end.plus(0, 0, 1, 0, 0, 0, 0, DateTime.DayOverflow.LastDay);
                    break;
                case 7:
                    s1 = start.minus(0, 0, 6, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
                    break;
            }
        }

        Date startDate = new Date(s1.getEndOfDay().getMilliseconds(TimeZone.getDefault()));
        Date endDate = new Date(e1.getEndOfDay().getMilliseconds(TimeZone.getDefault()));
        return new DateWindow(startDate, endDate);
    }

    /**
     * Another helper method, used to determine and select the date window to select to properly
     * highlight a full week in the calendar grid.
     *
     * @param date - selected date
     * @return DateWindow object
     */
    public static boolean isWithinWeekWindow(Date date, Date startDate, Date endDate) {
        DateTime targetDT = DateTime.forInstant(date.getTime(), TimeZone.getDefault());
        Date target = new Date(targetDT.getEndOfDay().getMilliseconds(TimeZone.getDefault()));

        DateTime startDT = DateTime.forInstant(startDate.getTime(), TimeZone.getDefault());
        Date start = new Date(startDT.getEndOfDay().getMilliseconds(TimeZone.getDefault()));

        DateTime endDT = DateTime.forInstant(endDate.getTime(), TimeZone.getDefault());
        Date end = new Date(endDT.getEndOfDay().getMilliseconds(TimeZone.getDefault()));

        return target.compareTo(start) == 0 || target.compareTo(end) == 0 || (target.after(start) && target.before(end));
    }

    public static String getSimpleDate(Date date){
        return DateHelper.getFullMonthAndDateFormat().format(date.getTime());
    }


}
