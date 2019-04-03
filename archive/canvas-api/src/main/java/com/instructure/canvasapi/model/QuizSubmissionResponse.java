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


public class QuizSubmissionResponse extends CanvasModel<QuizSubmissionResponse> implements android.os.Parcelable {

    @SerializedName("quiz_submissions")
    List<QuizSubmission> quizSubmissions = new ArrayList<>();

    public List<QuizSubmission> getQuizSubmissions() {
        return quizSubmissions;
    }

    public void setQuizSubmissions(List<QuizSubmission> quizSubmissions) {
        this.quizSubmissions = quizSubmissions;
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
    public int compareTo(QuizSubmissionResponse another) {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.quizSubmissions);
    }

    public QuizSubmissionResponse() {
    }

    private QuizSubmissionResponse(Parcel in) {
        this.quizSubmissions = new ArrayList<>();
        in.readList(this.quizSubmissions, QuizSubmission.class.getClassLoader());
    }

    public static final Creator<QuizSubmissionResponse> CREATOR = new Creator<QuizSubmissionResponse>() {
        public QuizSubmissionResponse createFromParcel(Parcel source) {
            return new QuizSubmissionResponse(source);
        }

        public QuizSubmissionResponse[] newArray(int size) {
            return new QuizSubmissionResponse[size];
        }
    };
}
