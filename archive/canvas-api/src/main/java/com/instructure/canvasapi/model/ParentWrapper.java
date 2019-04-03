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

public class ParentWrapper extends CanvasModel<ParentWrapper> {

    @SerializedName("parent")
    Parent parent;

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    @Override
    public long getId() {
        return 0;
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
        dest.writeParcelable(this.parent, 0);
    }

    public ParentWrapper() {
    }

    protected ParentWrapper(Parcel in) {
        this.parent = in.readParcelable(Parent.class.getClassLoader());
    }

    public static final Creator<ParentWrapper> CREATOR = new Creator<ParentWrapper>() {
        public ParentWrapper createFromParcel(Parcel source) {
            return new ParentWrapper(source);
        }

        public ParentWrapper[] newArray(int size) {
            return new ParentWrapper[size];
        }
    };
}
