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

import java.util.Date;

public class MasteryPathSelectResponse extends CanvasModel<MasteryPathSelectResponse> {

    private ModuleItem[] items;
    private Assignment[] assignments;

    public ModuleItem[] getItems() {
        return items;
    }

    public void setItems(ModuleItem[] items) {
        this.items = items;
    }

    public Assignment[] getAssignments() {
        return assignments;
    }

    public void setAssignments(Assignment[] assignments) {
        this.assignments = assignments;
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
        dest.writeTypedArray(this.items, flags);
        dest.writeTypedArray(this.assignments, flags);
    }

    public MasteryPathSelectResponse() {
    }

    protected MasteryPathSelectResponse(Parcel in) {
        this.items = in.createTypedArray(ModuleItem.CREATOR);
        this.assignments = in.createTypedArray(Assignment.CREATOR);
    }

    public static final Creator<MasteryPathSelectResponse> CREATOR = new Creator<MasteryPathSelectResponse>() {
        @Override
        public MasteryPathSelectResponse createFromParcel(Parcel source) {
            return new MasteryPathSelectResponse(source);
        }

        @Override
        public MasteryPathSelectResponse[] newArray(int size) {
            return new MasteryPathSelectResponse[size];
        }
    };
}
