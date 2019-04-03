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

import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;



public class Course extends CanvasContext implements Comparable<CanvasContext>{

    // Variables from API
    private long id;
    private String name;
    private String original_name;
    private String course_code;
    private String start_at;
    private String end_at;
    private String syllabus_body;
    private boolean hide_final_grades;
    private boolean is_public;
    private String license;
    private Term term;
    private List<Enrollment> enrollments = new ArrayList<Enrollment>();
    private long needs_grading_count;
    private boolean apply_assignment_group_weights;
    // Helper variables
    private Double currentScore;

    private Double finalScore;
    private boolean checkedCurrentGrade;
    private boolean checkedFinalGrade;
    private String currentGrade;
    private String finalGrade;
    private boolean is_favorite;
    private boolean access_restricted_by_date;
    private boolean restrict_enrollments_to_course_dates;
    private boolean has_weighted_grading_periods;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Type getType(){return Type.COURSE;}

    @Override
    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return this.original_name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getCourseCode() {
        return course_code;
    }
    public void setCourseCode(String courseCode) {
        course_code = courseCode;
    }
    public Date getStartDate() {
        return APIHelpers.stringToDate(start_at);
    }
    public Date getEndDate() {
        return APIHelpers.stringToDate(end_at);
    }
    public String getSyllabusBody() {
        return syllabus_body;
    }
    public void setSyllabusBody(String syllabusBody) {
        syllabus_body = syllabusBody;
    }
    public boolean isPublic() {
        return is_public;
    }
    public void setIsPublic(boolean isPublic) {
        this.is_public = isPublic;
    }
    public boolean isFinalGradeHidden() {
        return hide_final_grades;
    }

    public void setHideFinalGrades(boolean hide_final_grades) {
        this.hide_final_grades = hide_final_grades;
    }

    public Term getTerm() {
        return term;
    }
    public List<Enrollment> getEnrollments() {
        return enrollments;
    }
    public String getHomePage() {
        return default_view;
    }
    public boolean isFavorite() {
        return is_favorite;
    }
    public void setFavorite(boolean isFavorite) {
        this.is_favorite = isFavorite;
    }

    public boolean isAccessRestrictedByDate() {
        return access_restricted_by_date;
    }

    public void setAccessRestrictedByDate(boolean accessRestrictedByDate) {
        this.access_restricted_by_date = accessRestrictedByDate;
    }

    public boolean isWeightedGradingPeriods() {
        return has_weighted_grading_periods;
    }

    public void setHas_weighted_grading_periods(boolean has_weighted_grading_periods) {
        this.has_weighted_grading_periods = has_weighted_grading_periods;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public Course() {}

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public boolean isStudent() {
        for(Enrollment enrollment : enrollments) {
            if(enrollment.isStudent()) {
                return true;
            }
        }
        return false;
    }

    public boolean isTeacher() {

        if (enrollments == null){
            return false;
        }

        for(Enrollment enrollment : enrollments) {
            if(enrollment.isTeacher()) {
                return true;
            }
        }
        return false;
    }

    public boolean isTA() {

        if (enrollments == null){
            return false;
        }

        for(Enrollment enrollment : enrollments) {
            if(enrollment.isTA()) {
                return true;
            }
        }
        return false;
    }

    public boolean isObserver()
    {
        for(Enrollment enrollment : enrollments) {
            if(enrollment.isObserver()) {
                return true;
            }
        }
        return false;
    }

    public List<Enrollment> getEnrollmentsNoDuplicates() {
        if(enrollments == null) {
            return null;
        }
        if(enrollments.size() <= 1) {
            return enrollments;
        }

        return new ArrayList<Enrollment>(new LinkedHashSet<Enrollment>(enrollments));
    }

    public double getCurrentScore() {
        if (currentScore == null) {
            for (Enrollment enrollment : enrollments) {
                if (enrollment.isStudent() || enrollment.isObserver()) {
                    if(enrollment.isMultipleGradingPeriodsEnabled()) {
                        currentScore = enrollment.getCurrentPeriodComputedCurrentScore();
                    } else {
                        currentScore = enrollment.getCurrentScore();
                    }
                    return currentScore;
                }
            }
            currentScore = 0.0;
        }

        return currentScore;
    }

    public String getCurrentGrade() {
        if (!checkedCurrentGrade) {
            checkedCurrentGrade = true;
            for (Enrollment enrollment : enrollments) {
                if (enrollment.isStudent() || enrollment.isObserver()) {
                    if (enrollment.isMultipleGradingPeriodsEnabled()) {
                        currentGrade = enrollment.getCurrentPeriodComputedCurrentGrade();
                    } else {
                        currentGrade = enrollment.getCurrentGrade();
                    }
                    return currentGrade;
                }
            }
        }

        return currentGrade;

    }
    public double getFinalScore() {
        if (finalScore == null) {
            for (Enrollment enrollment : enrollments) {
                if (enrollment.isStudent() || enrollment.isObserver()) {
                    if (enrollment.isMultipleGradingPeriodsEnabled()) {
                        finalScore = enrollment.getCurrentPeriodComputedFinalScore();
                    } else {
                        finalScore = enrollment.getFinalScore();
                    }
                    return finalScore;
                }
            }
            finalScore = 0.0;
        }

        return finalScore;
    }

    public String getFinalGrade() {
        if (!checkedFinalGrade) {
            checkedFinalGrade = true;
            for (Enrollment enrollment : enrollments) {
                if (enrollment.isStudent() || enrollment.isObserver()) {
                    if (enrollment.isMultipleGradingPeriodsEnabled()) {
                       finalGrade = enrollment.getCurrentPeriodComputedFinalGrade();
                    } else {
                        finalGrade = enrollment.getFinalGrade();
                    }
                }
            }
        }

        return finalGrade;
    }

    public void addEnrollment(Enrollment enrollment) {
        if (enrollments == null || enrollments.size() == 0) {
            enrollments = new ArrayList<Enrollment>();
            enrollments.add(enrollment);
        } else {

            enrollments.add(enrollment);
        }
    }

    public long getNeedsGradingCount() { return needs_grading_count; }
    public void setNeedsGradingCount(long needs_grading_count) { this.needs_grading_count = needs_grading_count; }

    public void setApplyAssignmentGroupWeights(boolean b){ apply_assignment_group_weights = b;}

    public boolean getApplyAssignmentGroupWeights(){ return apply_assignment_group_weights; }

    /**
     * License
     */
    public enum LICENSE {PRIVATE_COPYRIGHTED, CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE, CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE,
        CC_ATTRIBUTION_NON_COMMERCIAL, CC_ATTRIBUTION_NO_DERIVATIVE, CC_ATTRIBUTION_SHARE_ALIKE, CC_ATTRIBUTION, PUBLIC_DOMAIN}

    public static String licenseToAPIString(LICENSE license){
        if(license == null){
            return null;
        }

        switch (license){
            case PRIVATE_COPYRIGHTED:
                return "private";
            case CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE:
                return "cc_by_nc_nd";
            case CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE:
                return "c_by_nc_sa";
            case CC_ATTRIBUTION_NON_COMMERCIAL:
                return "cc_by_nc";
            case CC_ATTRIBUTION_NO_DERIVATIVE:
                return "cc_by_nd";
            case CC_ATTRIBUTION_SHARE_ALIKE:
                return "cc_by_sa";
            case CC_ATTRIBUTION:
                return "cc_by";
            case PUBLIC_DOMAIN:
                return "public_domain";
            default:
                return "";
        }
    }

    public static String licenseToPrettyPrint(LICENSE license){
        switch (license){
            case PRIVATE_COPYRIGHTED:
                return "Private (Copyrighted)";
            case CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE:
                return "CC Attribution Non-Commercial No Derivatives";
            case CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE:
                return "CC Attribution Non-Commercial Share Alike";
            case CC_ATTRIBUTION_NON_COMMERCIAL:
                return "CC Attribution Non-Commercial";
            case CC_ATTRIBUTION_NO_DERIVATIVE:
                return "CC Attribution No Derivatives";
            case CC_ATTRIBUTION_SHARE_ALIKE:
                return "CC Attribution Share Alike";
            case CC_ATTRIBUTION:
                return "CC Attribution";
            case PUBLIC_DOMAIN:
                return "Public Domain";
            default:
                return "";
        }
    }


    public String getLicensePrettyPrint(){
        return licenseToPrettyPrint(getLicense());
    }


    public void setLicense(LICENSE license){
        this.license = licenseToAPIString(license);
    }


    public LICENSE getLicense(){

        if("public_domain".equals(license)){
            return LICENSE.PUBLIC_DOMAIN;
        } else if ("cc_by_nc_nd".equals(license)){
            return LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE;
        } else if ("c_by_nc_sa".equals(license)){
            return LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE;
        } else if ("cc_by_nc".equals(license)){
            return LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL;
        } else if ("cc_by_nd".equals(license)){
            return LICENSE.CC_ATTRIBUTION_NO_DERIVATIVE;
        } else if ("cc_by_sa".equals(license)){
            return LICENSE.CC_ATTRIBUTION_SHARE_ALIKE;
        } else if ("cc_by".equals(license)){
            return LICENSE.CC_ATTRIBUTION;
        } else {
            return LICENSE.PRIVATE_COPYRIGHTED;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.original_name);
        dest.writeString(this.course_code);
        dest.writeString(this.start_at);
        dest.writeString(this.end_at);
        dest.writeString(this.syllabus_body);
        dest.writeByte(this.hide_final_grades ? (byte) 1 : (byte) 0);
        dest.writeByte(this.is_public ? (byte) 1 : (byte) 0);
        dest.writeString(this.license);
        dest.writeParcelable(this.term, flags);
        dest.writeTypedList(this.enrollments);
        dest.writeLong(this.needs_grading_count);
        dest.writeByte(this.apply_assignment_group_weights ? (byte) 1 : (byte) 0);
        dest.writeValue(this.currentScore);
        dest.writeValue(this.finalScore);
        dest.writeByte(this.checkedCurrentGrade ? (byte) 1 : (byte) 0);
        dest.writeByte(this.checkedFinalGrade ? (byte) 1 : (byte) 0);
        dest.writeString(this.currentGrade);
        dest.writeString(this.finalGrade);
        dest.writeByte(this.is_favorite ? (byte) 1 : (byte) 0);
        dest.writeByte(this.access_restricted_by_date ? (byte) 1 : (byte) 0);
        dest.writeByte(this.restrict_enrollments_to_course_dates ? (byte) 1 : (byte) 0);
        dest.writeByte(this.has_weighted_grading_periods ? (byte) 1 : (byte) 0);
    }

    protected Course(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.original_name = in.readString();
        this.course_code = in.readString();
        this.start_at = in.readString();
        this.end_at = in.readString();
        this.syllabus_body = in.readString();
        this.hide_final_grades = in.readByte() != 0;
        this.is_public = in.readByte() != 0;
        this.license = in.readString();
        this.term = in.readParcelable(Term.class.getClassLoader());
        this.enrollments = in.createTypedArrayList(Enrollment.CREATOR);
        this.needs_grading_count = in.readLong();
        this.apply_assignment_group_weights = in.readByte() != 0;
        this.currentScore = (Double) in.readValue(Double.class.getClassLoader());
        this.finalScore = (Double) in.readValue(Double.class.getClassLoader());
        this.checkedCurrentGrade = in.readByte() != 0;
        this.checkedFinalGrade = in.readByte() != 0;
        this.currentGrade = in.readString();
        this.finalGrade = in.readString();
        this.is_favorite = in.readByte() != 0;
        this.access_restricted_by_date = in.readByte() != 0;
        this.restrict_enrollments_to_course_dates = in.readByte() != 0;
        this.has_weighted_grading_periods = in.readByte() != 0;
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel source) {
            return new Course(source);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };
}
