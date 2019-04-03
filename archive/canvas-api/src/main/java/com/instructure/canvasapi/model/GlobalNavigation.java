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

public class GlobalNavigation implements Parcelable {

    @SerializedName("message_type")
    public String messageType;

    @SerializedName("url")
    public String url;

    @SerializedName("title")
    public String title;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.messageType);
        dest.writeString(this.url);
        dest.writeString(this.title);
    }

    public GlobalNavigation() {
    }

    protected GlobalNavigation(Parcel in) {
        this.messageType = in.readString();
        this.url = in.readString();
        this.title = in.readString();
    }

    public static final Parcelable.Creator<GlobalNavigation> CREATOR = new Parcelable.Creator<GlobalNavigation>() {
        @Override
        public GlobalNavigation createFromParcel(Parcel source) {
            return new GlobalNavigation(source);
        }

        @Override
        public GlobalNavigation[] newArray(int size) {
            return new GlobalNavigation[size];
        }
    };
}
