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


public class MasteryPath extends CanvasModel<MasteryPath> {

    private boolean locked;

    @SerializedName("assignment_sets")
    private AssignmentSet[] assignmentSets;

    @SerializedName("selected_set_id")
    private long selectedSetId;

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public AssignmentSet[] getAssignmentSets() {
        return assignmentSets;
    }

    public void setAssignmentSets(AssignmentSet[] assignmentSets) {
        this.assignmentSets = assignmentSets;
    }

    public long getSelectedSetId() {
        return selectedSetId;
    }

    public void setSelectedSetId(long selectedSetId) {
        this.selectedSetId = selectedSetId;
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
        return 0;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.locked ? (byte) 1 : (byte) 0);
        dest.writeTypedArray(this.assignmentSets, flags);
        dest.writeLong(this.selectedSetId);
    }

    public MasteryPath() {
    }

    protected MasteryPath(Parcel in) {
        this.locked = in.readByte() != 0;
        this.assignmentSets = in.createTypedArray(AssignmentSet.CREATOR);
        this.selectedSetId = in.readLong();
    }

    public static final Creator<MasteryPath> CREATOR = new Creator<MasteryPath>() {
        @Override
        public MasteryPath createFromParcel(Parcel source) {
            return new MasteryPath(source);
        }

        @Override
        public MasteryPath[] newArray(int size) {
            return new MasteryPath[size];
        }
    };
}
