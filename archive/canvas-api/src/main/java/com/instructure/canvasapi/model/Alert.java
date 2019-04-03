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

import java.util.Date;



public class Alert extends CanvasModel<Alert>{

    //Variables from API
    private String id;
    private boolean marked_read;
    private boolean dismissed;
    private String alert_type;
    private String title;
    private String action_date;
    private String creation_date;
    private String observer_id;
    private String student_id;
    private String course_id;
    private String alert_criteria_id;
    private String asset_url;

    public enum ALERT_TYPE { COURSE_ANNOUNCEMENT, INSTITUTION_ANNOUNCEMENT, ASSIGNMENT_GRADE_HIGH,
                        ASSIGNMENT_GRADE_LOW, ASSIGNMENT_MISSING, COURSE_GRADE_HIGH, COURSE_GRADE_LOW }

    @Override
    public long getId() {
        return id.hashCode();
    }
    @Override
    public Date getComparisonDate() {
        return null;
    }
    @Override
    public String getComparisonString() {
        return null;
    }

    //region Getters/Setters

    public String getStringId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setMarkedRead(boolean isRead){
        this.marked_read = isRead;
    }

    public boolean isMarkedRead(){
        return this.marked_read;
    }

    public void setDismissed(boolean dismissed){
        this.dismissed = dismissed;
    }

    public boolean isDismissed(){
        return this.dismissed;
    }

    public void setAlertType(ALERT_TYPE alert_type){
        this.alert_type = alertTypeToAPIString(alert_type);
    }

    public ALERT_TYPE getAlertType(){
        return getAlertTypeFromString(alert_type);
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){
        return this.title;
    }

    public void setActionDate(String actionDate){
        this.action_date = actionDate;
    }

    public Date getActionDate(){
        return APIHelpers.stringToDate(this.action_date);
    }

    public void setCreationDate(String creationDate){
        this.creation_date = creationDate;
    }

    public Date getCreationDate(){
        return APIHelpers.stringToDate(this.creation_date);
    }

    public void setObserverId(String observerId){
        this.observer_id = observerId;
    }

    public String getObserverId(){
        return this.observer_id;
    }

    public void setStudentId(String studentId){
        this.student_id = studentId;
    }

    public String getStudentId(){
        return this.student_id;
    }

    public void setCourseId(String courseId){
        this.course_id = courseId;
    }

    public String getCourseId(){
        return this.course_id;
    }

    public void setAssetUrl(String assetUrl){
        this.asset_url = assetUrl;
    }

    public String getAssetUrl(){
        return this.asset_url;
    }

    public String getAlertCriteriaId() {
        return alert_criteria_id;
    }

    public void setAlertCriteriaId(String alert_criteria_id) {
        this.alert_criteria_id = alert_criteria_id;
    }


    //endregion

    public static ALERT_TYPE getAlertTypeFromString(String alert_type){
        switch(alert_type){
            case("course_announcement"):
                return ALERT_TYPE.COURSE_ANNOUNCEMENT;
            case("institution_announcement"):
                return ALERT_TYPE.INSTITUTION_ANNOUNCEMENT;
            case("assignment_grade_high"):
                return ALERT_TYPE.ASSIGNMENT_GRADE_HIGH;
            case("assignment_grade_low"):
                return ALERT_TYPE.ASSIGNMENT_GRADE_LOW;
            case("assignment_missing"):
                return ALERT_TYPE.ASSIGNMENT_MISSING;
            case("course_grade_high"):
                return ALERT_TYPE.COURSE_GRADE_HIGH;
            case("course_grade_low"):
                return ALERT_TYPE.COURSE_GRADE_LOW;
            default:
                return null;
        }
    }

    public static String alertTypeToAPIString(ALERT_TYPE alert_type){
        switch(alert_type){
            case COURSE_ANNOUNCEMENT:
                return "course_announcement";
            case INSTITUTION_ANNOUNCEMENT:
                return "institution_announcement";
            case ASSIGNMENT_GRADE_HIGH:
                return "assignment_grade_high";
            case ASSIGNMENT_GRADE_LOW:
                return "assignment_grade_low";
            case ASSIGNMENT_MISSING:
                return "assignment_missing";
            case COURSE_GRADE_HIGH:
                return "course_grade_high";
            case COURSE_GRADE_LOW:
                return "course_grade_low";
            default:
                return null;
        }
    }


    //region Parcel stuffs

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.id);
        parcel.writeString(this.alert_criteria_id);
        parcel.writeByte(this.marked_read ? (byte) 1 : (byte) 0);
        parcel.writeByte(this.dismissed ? (byte) 1 : (byte) 0);
        parcel.writeString(this.alert_type);
        parcel.writeString(this.title);
        parcel.writeString(this.action_date);
        parcel.writeString(this.creation_date);
        parcel.writeString(this.observer_id);
        parcel.writeString(this.student_id);
        parcel.writeString(this.course_id);
        parcel.writeString(this.asset_url);
    }

    private Alert(Parcel parcel){
        this.id = parcel.readString();
        this.alert_criteria_id = parcel.readString();
        this.marked_read = parcel.readByte() != 0;
        this.dismissed = parcel.readByte() != 0;
        this.alert_type = parcel.readString();
        this.title = parcel.readString();
        this.action_date = parcel.readString();
        this.creation_date = parcel.readString();
        this.observer_id = parcel.readString();
        this.student_id = parcel.readString();
        this.course_id = parcel.readString();
        this.asset_url = parcel.readString();
    }

    public static Creator<Alert> CREATOR = new Creator<Alert>() {
        public Alert createFromParcel(Parcel parcel) {
            return new Alert(parcel);
        }

        public Alert[] newArray(int size) {
            return new Alert[size];
        }
    };

    //endregion
}
