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
import com.google.gson.annotations.SerializedName;

import java.util.Date;


public class AssignmentOverride extends CanvasModel<AssignmentOverride> {

    public long id;
    @SerializedName("assignment_id")
    public long assignmentId;
    public String title;
    @SerializedName("due_at")
    public Date dueAt;
    @SerializedName("all_day")
    boolean allDay;
    @SerializedName("all_day_date")
    public String allDayDate;
    @SerializedName("unlock_at")
    public Date unlockAt;
    @SerializedName("lock_at")
    public Date lockAt;
    @SerializedName("course_section_id")
    public long courseSectionId;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Date getComparisonDate() {
        return dueAt;
    }

    @Override
    public String getComparisonString() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.assignmentId);
        dest.writeString(this.title);
        dest.writeLong(this.dueAt != null ? this.dueAt.getTime() : -1);
        dest.writeByte(this.allDay ? (byte) 1 : (byte) 0);
        dest.writeString(this.allDayDate);
        dest.writeLong(this.unlockAt != null ? this.unlockAt.getTime() : -1);
        dest.writeLong(this.lockAt != null ? this.lockAt.getTime() : -1);
        dest.writeLong(this.courseSectionId);
    }

    public AssignmentOverride() {
    }

    protected AssignmentOverride(Parcel in) {
        this.id = in.readLong();
        this.assignmentId = in.readLong();
        this.title = in.readString();
        long tmpDueAt = in.readLong();
        this.dueAt = tmpDueAt == -1 ? null : new Date(tmpDueAt);
        this.allDay = in.readByte() != 0;
        this.allDayDate = in.readString();
        long tmpUnlockAt = in.readLong();
        this.unlockAt = tmpUnlockAt == -1 ? null : new Date(tmpUnlockAt);
        long tmpLockAt = in.readLong();
        this.lockAt = tmpLockAt == -1 ? null : new Date(tmpLockAt);
        this.courseSectionId = in.readLong();
    }

    public static final Parcelable.Creator<AssignmentOverride> CREATOR = new Parcelable.Creator<AssignmentOverride>() {
        @Override
        public AssignmentOverride createFromParcel(Parcel source) {
            return new AssignmentOverride(source);
        }

        @Override
        public AssignmentOverride[] newArray(int size) {
            return new AssignmentOverride[size];
        }
    };
}
