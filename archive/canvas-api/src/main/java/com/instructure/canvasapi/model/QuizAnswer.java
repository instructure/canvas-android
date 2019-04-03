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


public class QuizAnswer extends CanvasModel<QuizAnswer> implements android.os.Parcelable {

    public enum ANSWER_TYPE { EXACT_ANSWER, RANGE_ANSWER }
    //The unique identifier for the answer.  Do not supply if this answer is part of a
    //new question
    private long id;

    //The text of the answer.
    @SerializedName("text")
    private String answerText;

    //An integer to determine correctness of the answer. Incorrect answers should be
    //0, correct answers should be non-negative.
    @SerializedName("answer_weight")
    private int answerWeight;

    //Specific contextual comments for a particular answer.
    @SerializedName("answer_comments")
    private String answerComments;

    //Used in missing word questions.  The text to follow the missing word
    @SerializedName("text_after_answers")
    private String textAfterAnswers;

    //Used in matching questions.  The static value of the answer that will be
    //displayed on the left for students to match for.
    @SerializedName("answer_match_left")
    private String answerMatchLeft;

    //Used in matching questions. The correct match for the value given in
    //answer_match_left.  Will be displayed in a dropdown with the other
    //answer_match_right values..
    @SerializedName("answer_match_right")
    private String answerMatchRight;

    //Used in matching questions. A list of distractors, delimited by new lines (
    //) that will be seeded with all the answer_match_right values.
    @SerializedName("matching_answer_incorrect_matches")
    private String[] matchingAnswerIncorrectMatches;

    //Used in numerical questions.  Values can be 'exact_answer' or 'range_answer'.
    @SerializedName("numerical_answer_type")
    private String numericalAnswerType;

    //Used in numerical questions of type 'exact_answer'.  The value the answer should
    //equal.
    private int exact;

    //Used in numerical questions of type 'exact_answer'. The margin of error allowed
    //for the student's answer.
    private int margin;

    //Used in numerical questions of type 'range_answer'. The start of the allowed
    //range (inclusive).
    private int start;

    //Used in numerical questions of type 'range_answer'. The end of the allowed range
    //(inclusive).
    private int end;

    //Used in fill in multiple blank and multiple dropdowns questions.
    @SerializedName("blank_id")
    private long blankId;

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public int getAnswerWeight() {
        return answerWeight;
    }

    public void setAnswerWeight(int answerWeight) {
        this.answerWeight = answerWeight;
    }

    public String getAnswerComments() {
        return answerComments;
    }

    public void setAnswerComments(String answerComments) {
        this.answerComments = answerComments;
    }

    public String getTextAfterAnswers() {
        return textAfterAnswers;
    }

    public void setTextAfterAnswers(String textAfterAnswers) {
        this.textAfterAnswers = textAfterAnswers;
    }

    public String getAnswerMatchLeft() {
        return answerMatchLeft;
    }

    public void setAnswerMatchLeft(String answerMatchLeft) {
        this.answerMatchLeft = answerMatchLeft;
    }

    public String getAnswerMatchRight() {
        return answerMatchRight;
    }

    public void setAnswerMatchRight(String answerMatchRight) {
        this.answerMatchRight = answerMatchRight;
    }

    public String[] getMatchingAnswerIncorrectMatches() {
        return matchingAnswerIncorrectMatches;
    }

    public void setMatchingAnswerIncorrectMatches(String[] matchingAnswerIncorrectMatches) {
        this.matchingAnswerIncorrectMatches = matchingAnswerIncorrectMatches;
    }

    public ANSWER_TYPE getNumericalAnswerType() {
        if(numericalAnswerType.equals("range_answer")) {
            return ANSWER_TYPE.RANGE_ANSWER;
        } else {
            return ANSWER_TYPE.EXACT_ANSWER;
        }
    }

    public void setNumericalAnswerType(String numericalAnswerType) {
        this.numericalAnswerType = numericalAnswerType;
    }

    public int getExact() {
        return exact;
    }

    public void setExact(int exact) {
        this.exact = exact;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public long getBlankId() {
        return blankId;
    }

    public void setBlankId(long blankId) {
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
    public int compareTo(QuizAnswer another) {
        return ((Long)another.getId()).compareTo(this.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.answerText);
        dest.writeInt(this.answerWeight);
        dest.writeString(this.answerComments);
        dest.writeString(this.textAfterAnswers);
        dest.writeString(this.answerMatchLeft);
        dest.writeString(this.answerMatchRight);
        dest.writeStringArray(this.matchingAnswerIncorrectMatches);
        dest.writeString(this.numericalAnswerType);
        dest.writeInt(this.exact);
        dest.writeInt(this.margin);
        dest.writeInt(this.start);
        dest.writeInt(this.end);
        dest.writeLong(this.blankId);
    }

    public QuizAnswer() {
    }

    private QuizAnswer(Parcel in) {
        this.id = in.readLong();
        this.answerText = in.readString();
        this.answerWeight = in.readInt();
        this.answerComments = in.readString();
        this.textAfterAnswers = in.readString();
        this.answerMatchLeft = in.readString();
        this.answerMatchRight = in.readString();
        this.matchingAnswerIncorrectMatches = in.createStringArray();
        this.numericalAnswerType = in.readString();
        this.exact = in.readInt();
        this.margin = in.readInt();
        this.start = in.readInt();
        this.end = in.readInt();
        this.blankId = in.readLong();
    }

    public static final Creator<QuizAnswer> CREATOR = new Creator<QuizAnswer>() {
        public QuizAnswer createFromParcel(Parcel source) {
            return new QuizAnswer(source);
        }

        public QuizAnswer[] newArray(int size) {
            return new QuizAnswer[size];
        }
    };
}
