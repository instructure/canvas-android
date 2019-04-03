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
import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.Date;


public class QuizSubmissionTime extends CanvasModel<QuizSubmissionTime> {

    @SerializedName("end_at")
    private String endAt;

    @SerializedName("time_left")
    private int timeLeft;

    public Date getEndAt() {
        return APIHelpers.stringToDate(endAt);
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.endAt);
        dest.writeInt(this.timeLeft);
    }

    public QuizSubmissionTime() {
    }

    private QuizSubmissionTime(Parcel in) {
        this.endAt = in.readString();
        this.timeLeft = in.readInt();
    }

    public static final Creator<QuizSubmissionTime> CREATOR = new Creator<QuizSubmissionTime>() {
        public QuizSubmissionTime createFromParcel(Parcel source) {
            return new QuizSubmissionTime(source);
        }

        public QuizSubmissionTime[] newArray(int size) {
            return new QuizSubmissionTime[size];
        }
    };
}
