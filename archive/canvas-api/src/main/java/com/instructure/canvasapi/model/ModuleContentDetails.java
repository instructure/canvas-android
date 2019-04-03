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

public class ModuleContentDetails extends CanvasComparable<ModuleContentDetails> {

    private String points_possible;
    private String due_at;
    private String unlock_at;
    private String lock_at;
    private boolean locked_for_user;
    private String lock_explanation;
    private LockInfo lock_info;

    public String getPointsPossible() {
        return points_possible;
    }

    public Date getDueDate() {
        return APIHelpers.stringToDate(due_at);
    }

    public Date getUnlockDate() {
        return APIHelpers.stringToDate(unlock_at);
    }

    public Date getLockDate() {
        return APIHelpers.stringToDate(lock_at);
    }

    public boolean isLockedForUser() {
        return locked_for_user;
    }

    public String getLockExplanation() {
        return lock_explanation;
    }

    public LockInfo getLockInfo() {
        return lock_info;
    }

    @Override
    public int compareTo(ModuleContentDetails comparable) {
        return super.compareTo(comparable);
    }

    @Override
    public long getId() {
        return super.getId();
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.points_possible);
        dest.writeString(this.due_at);
        dest.writeString(this.unlock_at);
        dest.writeString(this.lock_at);
        dest.writeByte(locked_for_user ? (byte) 1 : (byte) 0);
        dest.writeString(this.lock_explanation);
        dest.writeParcelable(this.lock_info, 0);
    }

    public ModuleContentDetails() {
    }

    protected ModuleContentDetails(Parcel in) {
        this.points_possible = in.readString();
        this.due_at = in.readString();
        this.unlock_at = in.readString();
        this.lock_at = in.readString();
        this.locked_for_user = in.readByte() != 0;
        this.lock_explanation = in.readString();
        this.lock_info = in.readParcelable(LockInfo.class.getClassLoader());
    }

    public static final Creator<ModuleContentDetails> CREATOR = new Creator<ModuleContentDetails>() {
        public ModuleContentDetails createFromParcel(Parcel source) {
            return new ModuleContentDetails(source);
        }

        public ModuleContentDetails[] newArray(int size) {
            return new ModuleContentDetails[size];
        }
    };
}
