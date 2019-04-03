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


public class ResetParent extends CanvasModel<ResetParent> {

    @SerializedName("parent_id")
    private String parentId;

    private String token;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
        return parentId.hashCode();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.parentId);
        dest.writeString(this.token);
    }

    public ResetParent() {
    }

    protected ResetParent(Parcel in) {
        this.parentId = in.readString();
        this.token = in.readString();
    }

    public static final Creator<ResetParent> CREATOR = new Creator<ResetParent>() {
        @Override
        public ResetParent createFromParcel(Parcel source) {
            return new ResetParent(source);
        }

        @Override
        public ResetParent[] newArray(int size) {
            return new ResetParent[size];
        }
    };
}
