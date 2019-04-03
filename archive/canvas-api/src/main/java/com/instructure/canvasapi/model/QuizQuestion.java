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


public class QuizQuestion extends CanvasModel<QuizQuestion> {
    public enum QUESTION_TYPE { CALCULATED, ESSAY, FILE_UPLOAD, FILL_IN_MULTIPLE_BLANKS, MATCHING, MULTIPLE_ANSWERS, MUTIPLE_CHOICE, MULTIPLE_DROPDOWNS, NUMERICAL, SHORT_ANSWER, TEXT_ONLY, TRUE_FALSE, UNKNOWN }

    //The ID of the quiz question.
    private long id;

    //The ID of the Quiz the question belongs to.
    @SerializedName("quiz_id")
    private long quizId;

    //The order in which the question will be retrieved and displayed.
    private int position;

    //The name of the question.
    @SerializedName("question_name")
    private String questionName;

    //The type of the question.
    @SerializedName("question_type")
    private String questionType;

    //The text of the question.
    @SerializedName("question_text")
    private String questionText;

    //The maximum amount of points possible received for getting this question
    //correct.
    @SerializedName("points_possible")
    private int pointsPossible;

    //The comments to display if the student answers the question correctly.
    @SerializedName("correct_comments")
    private String correctComments;

    //The comments to display if the student answers incorrectly.
    @SerializedName("incorrect_comments")
    private String incorrectComments;

    //The comments to display regardless of how the student answered.
    @SerializedName("neutral_comments")
    private String neutralComments;

    //An array of available answers to display to the student.
    private QuizAnswer[] answers;

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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getQuestionName() {
        return questionName;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    public String getQuestionTypeString() {
        return this.questionType;
    }
    public QUESTION_TYPE getQuestionType() {
        return parseQuestionType(this.questionType);
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getPointsPossible() {
        return pointsPossible;
    }

    public void setPointsPossible(int pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    public String getCorrectComments() {
        return correctComments;
    }

    public void setCorrectComments(String correctComments) {
        this.correctComments = correctComments;
    }

    public String getIncorrectComments() {
        return incorrectComments;
    }

    public void setIncorrectComments(String incorrectComments) {
        this.incorrectComments = incorrectComments;
    }

    public String getNeutralComments() {
        return neutralComments;
    }

    public void setNeutralComments(String neutralComments) {
        this.neutralComments = neutralComments;
    }

    public QuizAnswer[] getAnswers() {
        return answers;
    }

    public void setAnswers(QuizAnswer[] answers) {
        this.answers = answers;
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
    public int compareTo(QuizQuestion another) {
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
        dest.writeInt(this.position);
        dest.writeString(this.questionName);
        dest.writeString(this.questionType);
        dest.writeString(this.questionText);
        dest.writeInt(this.pointsPossible);
        dest.writeString(this.correctComments);
        dest.writeString(this.incorrectComments);
        dest.writeString(this.neutralComments);
        dest.writeParcelableArray(this.answers, flags);
    }

    public QuizQuestion() {
    }

    private QuizQuestion(Parcel in) {
        this.id = in.readLong();
        this.quizId = in.readLong();
        this.position = in.readInt();
        this.questionName = in.readString();
        this.questionType = in.readString();
        this.questionText = in.readString();
        this.pointsPossible = in.readInt();
        this.correctComments = in.readString();
        this.incorrectComments = in.readString();
        this.neutralComments = in.readString();
        this.answers = (QuizAnswer[])in.readParcelableArray(QuizAnswer[].class.getClassLoader());
    }

    public static final Creator<QuizQuestion> CREATOR = new Creator<QuizQuestion>() {
        public QuizQuestion createFromParcel(Parcel source) {
            return new QuizQuestion(source);
        }

        public QuizQuestion[] newArray(int size) {
            return new QuizQuestion[size];
        }
    };

    public static QUESTION_TYPE parseQuestionType(String questionType) {

        switch(questionType) {
            case "calculated_question":
                return QUESTION_TYPE.CALCULATED;
            case "essay_question":
                return QUESTION_TYPE.ESSAY;
            case "file_upload_question":
                return QUESTION_TYPE.FILE_UPLOAD;
            case "fill_in_multiple_blanks_question":
                return QUESTION_TYPE.FILL_IN_MULTIPLE_BLANKS;
            case "matching_question":
                return QUESTION_TYPE.MATCHING;
            case "multiple_answers_question":
                return QUESTION_TYPE.MULTIPLE_ANSWERS;
            case "multiple_choice_question":
                return QUESTION_TYPE.MUTIPLE_CHOICE;
            case "multiple_dropdowns_question":
                return QUESTION_TYPE.MULTIPLE_DROPDOWNS;
            case "numerical_question":
                return QUESTION_TYPE.NUMERICAL;
            case "short_answer_question":
                return QUESTION_TYPE.SHORT_ANSWER;
            case "text_only_question":
                return QUESTION_TYPE.TEXT_ONLY;
            case "true_false_question":
                return QUESTION_TYPE.TRUE_FALSE;

        }
        return QUESTION_TYPE.UNKNOWN;
    }
}
