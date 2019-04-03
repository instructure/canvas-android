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

public class Poll extends CanvasComparable<Poll> implements Parcelable {

    private long id;
    private String question;
    private String description;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Date getComparisonDate() {
        return created_at;
    }

    public String getComparisonString() {
        return null;
    }

    @Override
    public int compareTo(Poll poll) {
        return CanvasComparable.compare(poll.getId(), this.getId());
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
        dest.writeString(this.question);
        dest.writeString(this.description);
        dest.writeLong(created_at != null ? created_at.getTime() : -1);
    }

    public Poll() {
    }

    private Poll(Parcel in) {
        this.id = in.readLong();
        this.question = in.readString();
        this.description = in.readString();
        long tmpCreated_at = in.readLong();
        this.created_at = tmpCreated_at == -1 ? null : new Date(tmpCreated_at);
    }

    public static Creator<Poll> CREATOR = new Creator<Poll>() {
        public Poll createFromParcel(Parcel source) {
            return new Poll(source);
        }

        public Poll[] newArray(int size) {
            return new Poll[size];
        }
    };
}
