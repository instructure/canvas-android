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

public class PollSubmission extends CanvasComparable<PollSubmission> implements Parcelable {

    private long id;
    private long poll_choice_id;
    private long user_id;
    private Date created_at;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPoll_choice_id() {
        return poll_choice_id;
    }

    public void setPoll_choice_id(long poll_choice_id) {
        this.poll_choice_id = poll_choice_id;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    public Date getComparisonDate() { return null; }
    public String getComparisonString() { return null; }

    @Override
    public int compareTo(PollSubmission pollSubmission) {
        return CanvasComparable.compare(this.getId(),pollSubmission.getId());
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
        dest.writeLong(this.poll_choice_id);
        dest.writeLong(this.user_id);
        dest.writeLong(created_at != null ? created_at.getTime() : -1);
    }

    public PollSubmission() {
    }

    private PollSubmission(Parcel in) {
        this.id = in.readLong();
        this.poll_choice_id = in.readLong();
        this.user_id = in.readLong();
        long tmpCreated_at = in.readLong();
        this.created_at = tmpCreated_at == -1 ? null : new Date(tmpCreated_at);
    }

    public static Creator<PollSubmission> CREATOR = new Creator<PollSubmission>() {
        public PollSubmission createFromParcel(Parcel source) {
            return new PollSubmission(source);
        }

        public PollSubmission[] newArray(int size) {
            return new PollSubmission[size];
        }
    };
}
