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
import android.os.Parcelable;

import java.util.Date;


public class Enrollment extends CanvasModel<Enrollment> implements Parcelable {

    public Enrollment(){
        type = "";
    }


    private String role;
    private String type;

    // only included when we get enrollments using the user's url:
    // /users/self/enrollments
    private long id;
    private long course_id;
    private long course_section_id;
    private String enrollment_state;
    private long user_id;
    private Grades grades;

    // only included when we get the enrollment with a course object
    private double computed_current_score;
    private double computed_final_score;
    private String computed_current_grade;
    private String computed_final_grade;
    private boolean multiple_grading_periods_enabled;
    private boolean totals_for_all_grading_periods_option;
    private double current_period_computed_current_score;
    private double current_period_computed_final_score;
    private String current_period_computed_current_grade;
    private String current_period_computed_final_grade;
    private long current_grading_period_id;
    private String current_grading_period_title;

    //The unique id of the associated user. Will be null unless type is
    //ObserverEnrollment.
    private long associated_user_id;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getType() {
        String enrollment = "enrollment";
        if(type.toLowerCase().endsWith(enrollment)){
            type = type.substring(0, type.length() - enrollment.length());
        }

        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getCourseId() {
        return course_id;
    }
    public void setCourseId(long course_id) {
        this.course_id = course_id;
    }
    public long getCourseSectionId() {
        return course_section_id;
    }
    public void setCourseSectionId(long course_section_id) {
        this.course_section_id = course_section_id;
    }
    public String getEnrollmentState() {
        return enrollment_state;
    }
    public void setEnrollmentState(String enrollment_state) {
        this.enrollment_state = enrollment_state;
    }
    public long getUserId() {
        return user_id;
    }
    public void setUserId(long user_id) {
        this.user_id = user_id;
    }
    public double getCurrentScore() {
        if (grades != null) {
            return grades.getCurrentScore();
        }
        return computed_current_score;
    }
    public double getFinalScore() {
        if (grades != null) {
            return grades.getFinalScore();
        }
        return computed_final_score;
    }
    public String getCurrentGrade() {
        if (grades != null) {
            return grades.getCurrentGrade();
        }
        return computed_current_grade;
    }
    public String getFinalGrade() {
        if (grades != null) {
            return grades.getFinalGrade();
        }
        return computed_final_grade;
    }


    public String getCurrentGradingPeriodTitle() {
        return current_grading_period_title;
    }

    public void setCurrentGradingPeriodTitle(String current_grading_period_title) {
        this.current_grading_period_title = current_grading_period_title;
    }

    public boolean isMultipleGradingPeriodsEnabled() {
        return multiple_grading_periods_enabled;
    }

    public void setMultipleGradingPeriodsEnabled(boolean multiple_grading_periods_enabled) {
        this.multiple_grading_periods_enabled = multiple_grading_periods_enabled;
    }

    public boolean isTotalsForAllGradingPeriodsOption() {
        return totals_for_all_grading_periods_option;
    }

    public void setTotalsForAllGradingPeriodsOption(boolean totals_for_all_grading_periods_option) {
        this.totals_for_all_grading_periods_option = totals_for_all_grading_periods_option;
    }

    public Double getCurrentPeriodComputedCurrentScore() {
        return current_period_computed_current_score;
    }

    public void setCurrentPeriodComputedCurrentScore(Double current_period_computed_current_score) {
        this.current_period_computed_current_score = current_period_computed_current_score;
    }

    public Double getCurrentPeriodComputedFinalScore() {
        return current_period_computed_final_score;
    }

    public void setCurrentPeriodComputedFinalScore(Double current_period_computed_final_score) {
        this.current_period_computed_final_score = current_period_computed_final_score;
    }

    public String getCurrentPeriodComputedCurrentGrade() {
        return current_period_computed_current_grade;
    }

    public void setCurrentPeriodComputedCurrentGrade(String current_period_computed_current_grade) {
        this.current_period_computed_current_grade = current_period_computed_current_grade;
    }

    public String getCurrentPeriodComputedFinalGrade() {
        return current_period_computed_final_grade;
    }

    public void setCurrentPeriodComputedFinalGrade(String current_period_computed_final_grade) {
        this.current_period_computed_final_grade = current_period_computed_final_grade;
    }

    public long getCurrentGradingPeriodId() {
        return current_grading_period_id;
    }

    public void setCurrentGradingPeriodId(long current_grading_period_id) {
        this.current_grading_period_id = current_grading_period_id;
    }

    public long getAssociatedUserId() {
        return associated_user_id;
    }

    public void setAssociatedUserId(long associated_user_id) {
        this.associated_user_id = associated_user_id;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public boolean isStudent() {
        return type.equalsIgnoreCase("student") || type.equalsIgnoreCase("studentenrollment");
    }

    public boolean isTeacher() {
        return type.equalsIgnoreCase("teacher") || type.equalsIgnoreCase("teacherenrollment");
    }

    public boolean isObserver() {
        return type.equalsIgnoreCase("observer") || type.equalsIgnoreCase("observerenrollment");
    }

    public boolean isTA() {
        return type.equalsIgnoreCase("ta") || type.equalsIgnoreCase("taenrollment");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return getType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Enrollment that = (Enrollment) o;

        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.role);
        dest.writeString(this.type);
        dest.writeLong(this.id);
        dest.writeLong(this.course_id);
        dest.writeLong(this.course_section_id);
        dest.writeString(this.enrollment_state);
        dest.writeLong(this.user_id);
        dest.writeParcelable(this.grades, 0);
        dest.writeDouble(this.computed_current_score);
        dest.writeDouble(this.computed_final_score);
        dest.writeString(this.computed_current_grade);
        dest.writeString(this.computed_final_grade);
        dest.writeByte(multiple_grading_periods_enabled ? (byte) 1 : (byte) 0);
        dest.writeByte(totals_for_all_grading_periods_option ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.current_period_computed_current_score);
        dest.writeDouble(this.current_period_computed_final_score);
        dest.writeString(this.current_period_computed_current_grade);
        dest.writeString(this.current_period_computed_final_grade);
        dest.writeLong(this.current_grading_period_id);
        dest.writeString(this.current_grading_period_title);
        dest.writeLong(this.associated_user_id);
    }

    protected Enrollment(Parcel in) {
        this.role = in.readString();
        this.type = in.readString();
        this.id = in.readLong();
        this.course_id = in.readLong();
        this.course_section_id = in.readLong();
        this.enrollment_state = in.readString();
        this.user_id = in.readLong();
        this.grades = in.readParcelable(Grades.class.getClassLoader());
        this.computed_current_score = in.readDouble();
        this.computed_final_score = in.readDouble();
        this.computed_current_grade = in.readString();
        this.computed_final_grade = in.readString();
        this.multiple_grading_periods_enabled = in.readByte() != 0;
        this.totals_for_all_grading_periods_option = in.readByte() != 0;
        this.current_period_computed_current_score = in.readDouble();
        this.current_period_computed_final_score = in.readDouble();
        this.current_period_computed_current_grade = in.readString();
        this.current_period_computed_final_grade = in.readString();
        this.current_grading_period_id = in.readLong();
        this.current_grading_period_title = in.readString();
        this.associated_user_id = in.readLong();
    }

    public static final Creator<Enrollment> CREATOR = new Creator<Enrollment>() {
        public Enrollment createFromParcel(Parcel source) {
            return new Enrollment(source);
        }

        public Enrollment[] newArray(int size) {
            return new Enrollment[size];
        }
    };
}