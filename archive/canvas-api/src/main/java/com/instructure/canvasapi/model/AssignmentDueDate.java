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

import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.Date;

public class AssignmentDueDate extends CanvasModel<AssignmentDueDate> implements Parcelable{

    private long id;
    private String due_at;
    private String title;
    private String unlock_at;
    private String lock_at;
    private boolean base;

    public Date getDueDate() {
        if(this.due_at == null) {return null;}
        return APIHelpers.stringToDate(this.due_at);
    }

    public Date getUnlockDate() {
        if(this.unlock_at == null) {return null;}
        return APIHelpers.stringToDate(this.unlock_at);
    }

    public Date getLockDate() {
        if(this.lock_at == null) {return null;}
        return APIHelpers.stringToDate(this.lock_at);
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Date getComparisonDate() {
        return getDueDate();
    }

    @Override
    public String getComparisonString() {
        return due_at;
    }

    private AssignmentDueDate(){}

    private AssignmentDueDate(Parcel in){
        this.id = in.readLong();
        this.due_at = in.readString();
        this.title = in.readString();
        this.unlock_at = in.readString();
        this.lock_at = in.readString();
        this.base = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.due_at);
        dest.writeString(this.title);
        dest.writeString(this.unlock_at);
        dest.writeString(this.lock_at);
        dest.writeByte(this.base ? (byte) 1 : (byte) 0);
    }

    public static Creator<AssignmentDueDate> CREATOR = new Creator<AssignmentDueDate>() {
        @Override
        public AssignmentDueDate createFromParcel(Parcel source) {
            return new AssignmentDueDate(source);
        }

        @Override
        public AssignmentDueDate[] newArray(int size) {
            return new AssignmentDueDate[size];
        }
    };
}
