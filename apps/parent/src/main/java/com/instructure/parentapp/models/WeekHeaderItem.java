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
import android.os.Parcelable;
import androidx.annotation.Nullable;

import com.instructure.canvasapi2.models.CanvasModel;

import java.util.Calendar;
import java.util.Date;

public class WeekHeaderItem extends CanvasModel<WeekHeaderItem> implements Parcelable {

    @Override
    public long getId() {
        return dayOfWeek;
    }

    @Nullable private Calendar date;
    public int dayOfWeek = -1;

    public WeekHeaderItem() {
    }

    public WeekHeaderItem(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public WeekHeaderItem(@Nullable Calendar date) {
        this.date = date;
    }

    public void setDate(@Nullable Calendar date) {
        this.date = date;
    }

    @Nullable
    public Calendar getDate() {
        return date;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        if(date == null) return null;
        return new Date(date.getTimeInMillis());
    }

    @Nullable
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
        dest.writeSerializable(this.date);
        dest.writeInt(this.dayOfWeek);
    }

    protected WeekHeaderItem(Parcel in) {
        this.date = (Calendar) in.readSerializable();
        this.dayOfWeek = in.readInt();
    }

    public static final Creator<WeekHeaderItem> CREATOR = new Creator<WeekHeaderItem>() {
        @Override
        public WeekHeaderItem createFromParcel(Parcel source) {
            return new WeekHeaderItem(source);
        }

        @Override
        public WeekHeaderItem[] newArray(int size) {
            return new WeekHeaderItem[size];
        }
    };
}
