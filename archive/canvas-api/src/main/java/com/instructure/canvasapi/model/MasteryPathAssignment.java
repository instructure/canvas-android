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


public class MasteryPathAssignment extends CanvasModel<MasteryPathAssignment> {

    private long id;
    @SerializedName("assignment_id")
    private long assignmentId;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;
    @SerializedName("override_id")
    private long overrideId;
    @SerializedName("assignment_set_id")
    private long assignmentSetId;
    private int position;
    private Assignment model;


    public void setId(long id) {
        this.id = id;
    }

    public long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getOverrideId() {
        return overrideId;
    }

    public void setOverrideId(long overrideId) {
        this.overrideId = overrideId;
    }

    public long getAssignmentSetId() {
        return assignmentSetId;
    }

    public void setAssignmentSetId(long assignmentSetId) {
        this.assignmentSetId = assignmentSetId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Assignment getModel() {
        return model;
    }

    public void setModel(Assignment model) {
        this.model = model;
    }

    //Additional getter for assignment
    public Assignment getAssignment() {
        return model;
    }

    @Override
    public String getComparisonString() {
        return null;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public long getId() {
        return id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.assignmentId);
        dest.writeString(this.createdAt);
        dest.writeString(this.updatedAt);
        dest.writeLong(this.overrideId);
        dest.writeLong(this.assignmentSetId);
        dest.writeInt(this.position);
        dest.writeParcelable(this.model, flags);
    }

    public MasteryPathAssignment() {
    }

    protected MasteryPathAssignment(Parcel in) {
        this.id = in.readLong();
        this.assignmentId = in.readLong();
        this.createdAt = in.readString();
        this.updatedAt = in.readString();
        this.overrideId = in.readLong();
        this.assignmentSetId = in.readLong();
        this.position = in.readInt();
        this.model = in.readParcelable(Assignment.class.getClassLoader());
    }

    public static final Creator<MasteryPathAssignment> CREATOR = new Creator<MasteryPathAssignment>() {
        @Override
        public MasteryPathAssignment createFromParcel(Parcel source) {
            return new MasteryPathAssignment(source);
        }

        @Override
        public MasteryPathAssignment[] newArray(int size) {
            return new MasteryPathAssignment[size];
        }
    };
}
