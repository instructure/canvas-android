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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PollSession extends CanvasComparable<PollSession> implements Parcelable {

    private long id;
    private long poll_id;
    private long course_id;
    private long course_section_id;
    private boolean is_published;
    private boolean has_public_results;
    private Map<Long, Integer> results;
    private Date created_at;
    private List<PollSubmission> poll_submissions;
    private boolean has_submitted;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPoll_id() {
        return poll_id;
    }

    public void setPoll_id(long poll_id) {
        this.poll_id = poll_id;
    }

    public long getCourse_id() {
        return course_id;
    }

    public void setCourse_id(long course_id) {
        this.course_id = course_id;
    }

    public long getCourse_section_id() {
        return course_section_id;
    }

    public void setCourse_section_id(long course_section_id) {
        this.course_section_id = course_section_id;
    }

    public boolean is_published() {
        return is_published;
    }

    public void setIs_published(boolean is_published) {
        this.is_published = is_published;
    }

    public boolean has_public_results() {
        return has_public_results;
    }

    public void setHas_public_results(boolean has_public_results) {
        this.has_public_results = has_public_results;
    }

    public Map<Long, Integer> getResults() {
        return results;
    }

    public void setResults(Map<Long, Integer> results) {
        this.results = results;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public List<PollSubmission> getPoll_submissions() {
        return poll_submissions;
    }

    public void setPoll_submissions(ArrayList<PollSubmission> poll_submissions) {
        this.poll_submissions = poll_submissions;
    }

    public boolean isHas_submitted() {
        return has_submitted;
    }

    public void setHas_submitted(boolean has_submitted) {
        this.has_submitted = has_submitted;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    public Date getComparisonDate() { return created_at; }
    public String getComparisonString() { return null; }

    @Override
    public int compareTo(PollSession pollSession) {
        return CanvasComparable.compare(pollSession.getId(),this.getId());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Parcelable
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.poll_id);
        dest.writeLong(this.course_id);
        dest.writeLong(this.course_section_id);
        dest.writeByte(is_published ? (byte) 1 : (byte) 0);
        dest.writeByte(has_public_results ? (byte) 1 : (byte) 0);
        dest.writeMap(this.results);
        dest.writeLong(created_at != null ? created_at.getTime() : -1);
        dest.writeList(this.poll_submissions);
    }

    public PollSession() {
    }

    private PollSession(Parcel in) {
        this.id = in.readLong();
        this.poll_id = in.readLong();
        this.course_id = in.readLong();
        this.course_section_id = in.readLong();
        this.is_published = in.readByte() != 0;
        this.has_public_results = in.readByte() != 0;
        this.results = in.readHashMap(Map.class.getClassLoader());
        long tmpCreated_at = in.readLong();
        this.created_at = tmpCreated_at == -1 ? null : new Date(tmpCreated_at);
        this.poll_submissions = new ArrayList<PollSubmission>();
        in.readList(this.poll_submissions, PollSubmission.class.getClassLoader());
    }

    public static Creator<PollSession> CREATOR = new Creator<PollSession>() {
        public PollSession createFromParcel(Parcel source) {
            return new PollSession(source);
        }

        public PollSession[] newArray(int size) {
            return new PollSession[size];
        }
    };
}
