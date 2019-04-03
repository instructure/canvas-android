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


public class Grades extends CanvasModel<Grades> {

    private String html_url;
    private double current_score;
    private double final_score;
    private String current_grade;
    private String final_grade;

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

    public String getHtmlUrl() {
        return html_url;
    }

    public void setHtml_url(String htmlUrl) {
        this.html_url = htmlUrl;
    }

    public double getCurrentScore() {
        return current_score;
    }

    public void setCurrentScore(double currentScore) {
        this.current_score = currentScore;
    }

    public double getFinalScore() {
        return final_score;
    }

    public void setFinalScore(double finalScore) {
        this.final_score = finalScore;
    }

    public String getCurrentGrade() {
        return current_grade;
    }

    public void setCurrentGrade(String currentGrade) {
        this.current_grade = currentGrade;
    }

    public String getFinalGrade() {
        return final_grade;
    }

    public void setFinalGrade(String finalGrade) {
        this.final_grade = finalGrade;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.html_url);
        dest.writeDouble(this.current_score);
        dest.writeDouble(this.final_score);
        dest.writeString(this.current_grade);
        dest.writeString(this.final_grade);
    }

    public Grades() {
    }

    protected Grades(Parcel in) {
        this.html_url = in.readString();
        this.current_score = in.readDouble();
        this.final_score = in.readDouble();
        this.current_grade = in.readString();
        this.final_grade = in.readString();
    }

    public static final Creator<Grades> CREATOR = new Creator<Grades>() {
        public Grades createFromParcel(Parcel source) {
            return new Grades(source);
        }

        public Grades[] newArray(int size) {
            return new Grades[size];
        }
    };
}
