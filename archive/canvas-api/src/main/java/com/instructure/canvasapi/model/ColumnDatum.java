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


public class ColumnDatum extends CanvasModel<ColumnDatum> {

    private String content;
    private long user_id;

    public String getContent() {
        return content;
    }

    public long getUser_id() {
        return user_id;
    }

    @Override
    public long getId() {
        return 0;
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
    public int compareTo(ColumnDatum another) {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeLong(this.user_id);
    }

    private ColumnDatum(Parcel in) {
        this.content = in.readString();
        this.user_id = in.readLong();
    }

    public static final Creator<ColumnDatum> CREATOR = new Creator<ColumnDatum>() {
        public ColumnDatum createFromParcel(Parcel source) {
            return new ColumnDatum(source);
        }

        public ColumnDatum[] newArray(int size) {
            return new ColumnDatum[size];
        }
    };
}
