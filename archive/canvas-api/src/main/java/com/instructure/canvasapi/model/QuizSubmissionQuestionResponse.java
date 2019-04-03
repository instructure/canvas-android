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


public class QuizSubmissionQuestionResponse extends CanvasModel<QuizSubmissionQuestionResponse> {

    @SerializedName("quiz_submission_questions")
    private List<QuizSubmissionQuestion> quizSubmissionQuestions;

    public List<QuizSubmissionQuestion> getQuizSubmissionQuestions() {
        return quizSubmissionQuestions;
    }

    public void setQuizSubmissionQuestions(List<QuizSubmissionQuestion> quizSubmissionQuestions) {
        this.quizSubmissionQuestions = quizSubmissionQuestions;
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
    public int compareTo(QuizSubmissionQuestionResponse quizSubmissionQuestionResponse) {
        return 0;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.quizSubmissionQuestions);
    }

    public QuizSubmissionQuestionResponse() {
    }

    private QuizSubmissionQuestionResponse(Parcel in) {
        this.quizSubmissionQuestions = new ArrayList<>();
        in.readList(this.quizSubmissionQuestions, QuizSubmissionQuestion.class.getClassLoader());
    }

    public static final Creator<QuizSubmissionQuestionResponse> CREATOR = new Creator<QuizSubmissionQuestionResponse>() {
        public QuizSubmissionQuestionResponse createFromParcel(Parcel source) {
            return new QuizSubmissionQuestionResponse(source);
        }

        public QuizSubmissionQuestionResponse[] newArray(int size) {
            return new QuizSubmissionQuestionResponse[size];
        }
    };
}
