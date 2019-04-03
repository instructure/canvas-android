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
import java.util.List;

public class Quiz extends CanvasModel<Quiz> {

    // constants

    public final static String TYPE_PRACTICE = "practice_quiz";
    public final static String TYPE_ASSIGNMENT = "assignment";
    public final static String TYPE_GRADED_SURVEY = "graded_survey";
    public final static String TYPE_SURVEY = "survey";

    public enum HIDE_RESULTS_TYPE { NULL, ALWAYS, AFTER_LAST_ATTEMPT }
    // API variables

    private long id;
    private String title;
    private String mobile_url;
    private String html_url;

    private String description;
    private String quiz_type;
    private LockInfo lock_info;
    private QuizPermission permissions;
    private int allowed_attempts;
    private int question_count;
    private String points_possible;
    private String due_at;
    private int time_limit;
    private String access_code;
    private String ip_filter;
    private boolean locked_for_user;
    private String lock_explanation;
    private String hide_results;
    private String unlock_at;
    private boolean one_time_results;
    private String lock_at;
    private List<String> question_types = new ArrayList<>();
    private boolean has_access_code;
    private boolean one_question_at_a_time;
    private boolean require_lockdown_browser;
    private boolean require_lockdown_browser_for_results;
    // Helper variables

    private Assignment assignment;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {

        if (mobile_url != null && !mobile_url.equals("")) {
            return mobile_url;
        }
        return html_url;
    }

    public String getDescription() {
        if (description != null) {
            return description;
        }
        return "";
    }

    public String getType() {
        return quiz_type;
    }

    //During parsing, GSON will try. Which means sometimes we get 'empty' objects
    //They're non-null, but don't have any information.
    public LockInfo getLockInfo() {
        if(lock_info == null || lock_info.isEmpty()){
            return null;
        }
        return lock_info;
    }

    public void setLockInfo(LockInfo lockInfo) {
        this.lock_info = lockInfo;
    }

    public int getAllowedAttempts() {
        return allowed_attempts;
    }

    public void setAllowedAttempts(int allowed_attempts) {
        this.allowed_attempts = allowed_attempts;
    }

    public int getQuestionCount() {
        return question_count;
    }

    public void setQuestionCount(int question_count) {
        this.question_count = question_count;
    }

    public String getPointsPossible() {
        return points_possible;
    }

    public void setPointsPossible(String points_possible) {
        this.points_possible = points_possible;
    }

    public Date getDueAt() {
        return APIHelpers.stringToDate(due_at);
    }

    public void setDueAt(String due_at) {
        this.due_at = due_at;
    }

    public int getTimeLimit() {
        return time_limit;
    }

    public void setTimeLimit(int time_limit) {
        this.time_limit = time_limit;
    }

    public String getAccessCode() {
        return access_code;
    }

    public void setAccessCode(String access_code) {
        this.access_code = access_code;
    }

    public String getIPFilter() {
        return ip_filter;
    }

    public void setIPFilter(String ip_filter) {
        this.ip_filter = ip_filter;
    }

    public boolean isLockedForUser() {
        return locked_for_user;
    }

    public void setLockedForUser(boolean locked_for_user) {
        this.locked_for_user = locked_for_user;
    }

    public String getLockExplanation() {
        return lock_explanation;
    }

    public void setLockExplanation(String lock_explanation) {
        this.lock_explanation = lock_explanation;
    }

    public HIDE_RESULTS_TYPE getHideResults() {

        if(hide_results == null || hide_results.equals("null")) {
            return HIDE_RESULTS_TYPE.NULL;
        } else if(hide_results.equals("always")) {
            return HIDE_RESULTS_TYPE.ALWAYS;
        } else if(hide_results.equals("until_after_last_attempt")) {
            return HIDE_RESULTS_TYPE.AFTER_LAST_ATTEMPT;
        }
        return HIDE_RESULTS_TYPE.NULL;
    }

    public void setHideResults(String hide_results) {
        this.hide_results = hide_results;
    }

    public Date getUnlockAt() {
        return APIHelpers.stringToDate(unlock_at);
    }

    public void setUnlockAt(String unlock_at) {
        this.unlock_at = unlock_at;
    }

    public boolean isOneTimeResults() {
        return one_time_results;
    }

    public void setOneTimeResults(boolean one_time_results) {
        this.one_time_results = one_time_results;
    }

    public Date getLockAt() {
        return APIHelpers.stringToDate(lock_at);
    }

    public void setLockAt(String lock_at) {
        this.lock_at = lock_at;
    }

    public ArrayList<QuizQuestion.QUESTION_TYPE> getQuestionTypes() {
        return parseQuestionTypes(question_types);
    }

    public void setQuestionTypes(List<String> question_types) {
        this.question_types = question_types;
    }

    public boolean hasAccessCode() {
        return has_access_code;
    }

    public boolean isOneQuestionAtATime() {
        return one_question_at_a_time;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public boolean getRequireLockdownBrowser() {
        return require_lockdown_browser;
    }

    public void setRequireLockdownBrowser(boolean require_lockdown_browser) {
        this.require_lockdown_browser = require_lockdown_browser;
    }

    public boolean getRequireLockdownBrowserForResults() {
        return require_lockdown_browser_for_results;
    }

    public void setRequireLockdownBrowserForResults(boolean require_lockdown_browser_for_results) {
        this.require_lockdown_browser_for_results = require_lockdown_browser_for_results;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helper Methods
    ///////////////////////////////////////////////////////////////////////////

    private ArrayList<QuizQuestion.QUESTION_TYPE> parseQuestionTypes(List<String> question_types) {
        ArrayList<QuizQuestion.QUESTION_TYPE> questionTypesList = new ArrayList<>();
        for(String question_type : question_types) {
            if(question_type != null) {
                questionTypesList.add(QuizQuestion.parseQuestionType(question_type));
            }
        }

        return questionTypesList;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        if (getAssignment() != null) {
            return getAssignment().getName();
        }
        return getTitle();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.mobile_url);
        dest.writeString(this.html_url);
        dest.writeString(this.description);
        dest.writeString(this.quiz_type);
        dest.writeParcelable(this.lock_info, flags);
        dest.writeParcelable(this.assignment, flags);
        dest.writeParcelable(this.permissions, flags);
        dest.writeInt(this.allowed_attempts);
        dest.writeInt(this.question_count);
        dest.writeString(this.points_possible);
        dest.writeString(this.due_at);
        dest.writeInt(this.time_limit);
        dest.writeString(this.access_code);
        dest.writeString(this.ip_filter);
        dest.writeByte(this.locked_for_user ? (byte) 1 : (byte) 0);
        dest.writeString(this.lock_explanation);
        dest.writeString(this.hide_results);
        dest.writeString(this.unlock_at);
        dest.writeByte(this.one_time_results ? (byte) 1 : (byte) 0);
        dest.writeString(this.lock_at);
        dest.writeList(this.question_types);
        dest.writeByte(this.has_access_code ? (byte) 1 : (byte) 0);
        dest.writeByte(this.one_question_at_a_time ? (byte) 1 : (byte) 0);
        dest.writeByte(this.require_lockdown_browser ? (byte) 1 : (byte) 0);
        dest.writeByte(this.require_lockdown_browser ? (byte) 1 : (byte) 0);
    }

    public Quiz() {
    }

    private Quiz(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.mobile_url = in.readString();
        this.html_url = in.readString();
        this.description = in.readString();
        this.quiz_type = in.readString();
        this.lock_info =  in.readParcelable(LockInfo.class.getClassLoader());
        this.assignment = in.readParcelable(Assignment.class.getClassLoader());
        this.permissions = in.readParcelable(QuizPermission.class.getClassLoader());
        this.allowed_attempts = in.readInt();
        this.question_count = in.readInt();
        this.points_possible = in.readString();
        this.due_at = in.readString();
        this.time_limit = in.readInt();
        this.access_code = in.readString();
        this.ip_filter = in.readString();
        this.locked_for_user = in.readByte() != 0;
        this.lock_explanation = in.readString();
        this.hide_results = in.readString();
        this.unlock_at = in.readString();
        this.one_time_results = in.readByte() != 0;
        this.lock_at = in.readString();
        in.readList(this.question_types, String.class.getClassLoader());
        this.has_access_code = in.readByte() != 0;
        this.one_question_at_a_time = in.readByte() != 0;
        this.require_lockdown_browser = in.readByte() != 0;
        this.require_lockdown_browser_for_results = in.readByte() != 0;
    }

    public static Creator<Quiz> CREATOR = new Creator<Quiz>() {
        public Quiz createFromParcel(Parcel source) {
            return new Quiz(source);
        }

        public Quiz[] newArray(int size) {
            return new Quiz[size];
        }
    };
}
