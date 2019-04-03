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
import android.os.Parcelable;

import java.util.Date;


public class ErrorReportResult extends CanvasModel<ErrorReportResult> {
    private boolean logged;
    private long id;

    public boolean isLogged() {
        return logged;
    }

    public long getId() {
        return id;
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
        dest.writeByte(logged ? (byte) 1 : (byte) 0);
        dest.writeLong(this.id);
    }

    public ErrorReportResult() {
    }

    private ErrorReportResult(Parcel in) {
        this.logged = in.readByte() != 0;
        this.id = in.readLong();
    }

    public static final Parcelable.Creator<ErrorReportResult> CREATOR = new Parcelable.Creator<ErrorReportResult>() {
        public ErrorReportResult createFromParcel(Parcel source) {
            return new ErrorReportResult(source);
        }

        public ErrorReportResult[] newArray(int size) {
            return new ErrorReportResult[size];
        }
    };
}
