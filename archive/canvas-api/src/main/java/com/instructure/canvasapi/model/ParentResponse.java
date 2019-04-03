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


public class ParentResponse extends CanvasModel<ParentResponse> {

    private String token;

    @SerializedName("parent_id")
    private String parentId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public long getId() {
        return token.hashCode();
    }

    public String getParentId() {
        return parentId;
    }
    public void setId(String id) {
        this.parentId = id;
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.token);
        dest.writeString(this.parentId);
    }

    public ParentResponse() {
    }

    protected ParentResponse(Parcel in) {
        this.token = in.readString();
        this.parentId = in.readString();
    }

    public static final Creator<ParentResponse> CREATOR = new Creator<ParentResponse>() {
        @Override
        public ParentResponse createFromParcel(Parcel source) {
            return new ParentResponse(source);
        }

        @Override
        public ParentResponse[] newArray(int size) {
            return new ParentResponse[size];
        }
    };
}
