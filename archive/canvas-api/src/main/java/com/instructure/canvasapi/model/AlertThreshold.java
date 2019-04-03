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


public class AlertThreshold extends CanvasModel<AlertThreshold> {

    private String observer_id;
    private String alert_type;	//Description and key of the threshold this record defines.
    private String student_id;
    private String id;            //AlertThreshold unique ID.
    private String threshold;   //Threshold for creating an alert.

    public String getObserver_id() {
        return observer_id;
    }

    public String getAlertType() {
        return alert_type;
    }

    public String getStudent_id() {
        return student_id;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    @Override
    public long getId() {
        return id.hashCode();
    }

    public String getStringId() {
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
        dest.writeString(this.observer_id);
        dest.writeString(this.alert_type);
        dest.writeString(this.student_id);
        dest.writeString(this.id);
        dest.writeString(this.threshold);
    }

    public AlertThreshold() {
    }

    protected AlertThreshold(Parcel in) {
        this.observer_id = in.readString();
        this.alert_type = in.readString();
        this.student_id = in.readString();
        this.id = in.readString();
        this.threshold = in.readString();
    }

    public static final Creator<AlertThreshold> CREATOR = new Creator<AlertThreshold>() {
        public AlertThreshold createFromParcel(Parcel source) {
            return new AlertThreshold(source);
        }

        public AlertThreshold[] newArray(int size) {
            return new AlertThreshold[size];
        }
    };
}
