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


public class QuizGroup extends CanvasModel<QuizGroup> {
    //The ID of the question group.
    private long id;

    //The ID of the Quiz the question group belongs to.
    @SerializedName("quiz_id")
    private long quizId;

    //The name of the question group.
    private String name;

    //The number of questions to pick from the group to display to the student.
    @SerializedName("pick_count")
    private int pickCount;

    //The amount of points allotted to each question in the group.
    @SerializedName("question_points")
    private int questionPoints;

    //The ID of the Assessment question bank to pull questions from.
    @SerializedName("assessment_question_bank_id")
    private long assessmentQuestionBankId;

    //The order in which the question group will be retrieved and displayed.
    private int position;

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getQuizId() {
        return quizId;
    }

    public void setQuizId(long quizId) {
        this.quizId = quizId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPickCount() {
        return pickCount;
    }

    public void setPickCount(int pickCount) {
        this.pickCount = pickCount;
    }

    public int getQuestionPoints() {
        return questionPoints;
    }

    public void setQuestionPoints(int questionPoints) {
        this.questionPoints = questionPoints;
    }

    public long getAssessmentQuestionBankId() {
        return assessmentQuestionBankId;
    }

    public void setAssessmentQuestionBankId(long assessmentQuestionBankId) {
        this.assessmentQuestionBankId = assessmentQuestionBankId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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
    public int compareTo(QuizGroup another) {
        return ((Long)another.getId()).compareTo(this.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.quizId);
        dest.writeString(this.name);
        dest.writeInt(this.pickCount);
        dest.writeInt(this.questionPoints);
        dest.writeLong(this.assessmentQuestionBankId);
        dest.writeInt(this.position);
    }

    public QuizGroup() {
    }

    private QuizGroup(Parcel in) {
        this.id = in.readLong();
        this.quizId = in.readLong();
        this.name = in.readString();
        this.pickCount = in.readInt();
        this.questionPoints = in.readInt();
        this.assessmentQuestionBankId = in.readLong();
        this.position = in.readInt();
    }

    public static final Creator<QuizGroup> CREATOR = new Creator<QuizGroup>() {
        public QuizGroup createFromParcel(Parcel source) {
            return new QuizGroup(source);
        }

        public QuizGroup[] newArray(int size) {
            return new QuizGroup[size];
        }
    };
}
