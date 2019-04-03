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

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GradingPeriodResponse extends CanvasModel<GradingPeriodResponse>{

    @SerializedName("grading_periods")
    ArrayList<GradingPeriod> gradingPeriodList = new ArrayList<>();

    public List<GradingPeriod> getGradingPeriodList() {
        return gradingPeriodList;
    }

    public void setGradingPeriodList(ArrayList<GradingPeriod> gradingPeriodList) {
        this.gradingPeriodList = gradingPeriodList;
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

    public GradingPeriodResponse() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(gradingPeriodList);
    }

    protected GradingPeriodResponse(Parcel in) {
        this.gradingPeriodList = in.createTypedArrayList(GradingPeriod.CREATOR);
    }

    public static final Creator<GradingPeriodResponse> CREATOR = new Creator<GradingPeriodResponse>() {
        public GradingPeriodResponse createFromParcel(Parcel source) {
            return new GradingPeriodResponse(source);
        }

        public GradingPeriodResponse[] newArray(int size) {
            return new GradingPeriodResponse[size];
        }
    };
}
