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

import java.util.Date;


public class QuizSubmissionAnswer extends CanvasModel<QuizSubmissionAnswer> {

    //id of the answer
    private long id;
    private String text;
    private String html;
    private String comments;
    private int weight;
    @SerializedName("blank_id")
    private String blankId;

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getBlankId() {
        return blankId;
    }

    public void setBlankId(String blankId) {
        this.blankId = blankId;
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
    public int compareTo(QuizSubmissionAnswer quizSubmissionAnswer) {
        return 0;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.text);
        dest.writeString(this.html);
        dest.writeString(this.comments);
        dest.writeInt(this.weight);
        dest.writeString(this.blankId);
    }

    public QuizSubmissionAnswer() {
    }

    protected QuizSubmissionAnswer(Parcel in) {
        this.id = in.readLong();
        this.text = in.readString();
        this.html = in.readString();
        this.comments = in.readString();
        this.weight = in.readInt();
        this.blankId = in.readString();
    }

    public static final Creator<QuizSubmissionAnswer> CREATOR = new Creator<QuizSubmissionAnswer>() {
        @Override
        public QuizSubmissionAnswer createFromParcel(Parcel source) {
            return new QuizSubmissionAnswer(source);
        }

        @Override
        public QuizSubmissionAnswer[] newArray(int size) {
            return new QuizSubmissionAnswer[size];
        }
    };
}
