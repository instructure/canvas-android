/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi.model;

import android.os.Parcel;

import java.util.Date;

public class GradingPeriod extends CanvasModel<GradingPeriod> {

    private long id;
    private String title;
    private String start_date;
    private String end_date;
    private int weight;

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartDate() {
        return start_date;
    }

    public void setStartDate(String start_date) {
        this.start_date = start_date;
    }

    public String getEndDate() {
        return end_date;
    }

    public void setEndDate(String end_date) {
        this.end_date = end_date;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Date getComparisonDate() {
        return null;
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
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.start_date);
        dest.writeString(this.end_date);
        dest.writeInt(this.weight);
    }

    public GradingPeriod() {
    }

    protected GradingPeriod(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.start_date = in.readString();
        this.end_date = in.readString();
        this.weight = in.readInt();
    }

    public static final Creator<GradingPeriod> CREATOR = new Creator<GradingPeriod>() {
        public GradingPeriod createFromParcel(Parcel source) {
            return new GradingPeriod(source);
        }

        public GradingPeriod[] newArray(int size) {
            return new GradingPeriod[size];
        }
    };
}
