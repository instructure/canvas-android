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
package com.instructure.parentapp.models;

import android.os.Parcel;

import com.instructure.canvasapi2.models.CanvasModel;

import java.util.Calendar;
import java.util.Date;

public class CalendarWrapper extends CanvasModel<CalendarWrapper> {

    private Calendar calendar;
    private long assignmentId;
    private String title;
    private String subTitle;

    public CalendarWrapper(Calendar calendar, long assignmentId, String title, String subTitle) {
        this.calendar = calendar;
        this.assignmentId = assignmentId;
        this.title = title;
        this.subTitle = subTitle;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    @Override
    public long getId() {
        return assignmentId;
    }

    @Override
    public Date getComparisonDate() {
        return calendar.getTime();
    }

    @Override
    public String getComparisonString() {
        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.calendar);
        dest.writeLong(this.assignmentId);
    }

    public CalendarWrapper() {
    }

    protected CalendarWrapper(Parcel in) {
        this.calendar = (Calendar) in.readSerializable();
        this.assignmentId = in.readLong();
    }

    public static final Creator<CalendarWrapper> CREATOR = new Creator<CalendarWrapper>() {
        public CalendarWrapper createFromParcel(Parcel source) {
            return new CalendarWrapper(source);
        }

        public CalendarWrapper[] newArray(int size) {
            return new CalendarWrapper[size];
        }
    };
}
