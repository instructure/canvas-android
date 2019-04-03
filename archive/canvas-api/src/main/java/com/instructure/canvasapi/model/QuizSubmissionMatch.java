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


public class QuizSubmissionMatch extends CanvasModel<QuizSubmissionMatch> {

    private String text;
    private int match_id;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getMatchId() {
        return match_id;
    }

    public void setMatchId(int match_id) {
        this.match_id = match_id;
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
        dest.writeString(this.text);
        dest.writeInt(this.match_id);
    }

    public QuizSubmissionMatch() {
    }

    protected QuizSubmissionMatch(Parcel in) {
        this.text = in.readString();
        this.match_id = in.readInt();
    }

    public static final Creator<QuizSubmissionMatch> CREATOR = new Creator<QuizSubmissionMatch>() {
        public QuizSubmissionMatch createFromParcel(Parcel source) {
            return new QuizSubmissionMatch(source);
        }

        public QuizSubmissionMatch[] newArray(int size) {
            return new QuizSubmissionMatch[size];
        }
    };
}
