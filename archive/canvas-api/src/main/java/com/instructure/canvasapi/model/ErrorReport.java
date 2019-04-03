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


public class ErrorReport extends CanvasModel<ErrorReport> {

    //The users problem summary, like an email subject line
    private String subject;

    //long form documentation of what was witnessed
    private String comments;

    //categorization of how bad the user thinks the problem is.  Should be one of
    //[just_a_comment, not_urgent, workaround_possible, blocks_what_i_need_to_do,
    //extreme_critical_emergency].
    @SerializedName("user_perceived_severity")
    private String userPerceivedSeverity;

    //the email address of the reporting user
    private String email;

    //URL of the page on which the error was reported
    private String url;


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getUserPerceivedSeverity() {
        return userPerceivedSeverity;
    }

    public void setUserPerceivedSeverity(String userPerceivedSeverity) {
        this.userPerceivedSeverity = userPerceivedSeverity;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        return getSubject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.subject);
        dest.writeString(this.comments);
        dest.writeString(this.userPerceivedSeverity);
        dest.writeString(this.email);
        dest.writeString(this.url);
    }

    public ErrorReport() {
    }

    private ErrorReport(Parcel in) {
        this.subject = in.readString();
        this.comments = in.readString();
        this.userPerceivedSeverity = in.readString();
        this.email = in.readString();
        this.url = in.readString();
    }

    public static final Creator<ErrorReport> CREATOR = new Creator<ErrorReport>() {
        public ErrorReport createFromParcel(Parcel source) {
            return new ErrorReport(source);
        }

        public ErrorReport[] newArray(int size) {
            return new ErrorReport[size];
        }
    };
}
