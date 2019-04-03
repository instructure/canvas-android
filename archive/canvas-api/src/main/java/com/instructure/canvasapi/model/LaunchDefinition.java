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

public class LaunchDefinition implements Parcelable {

    @SerializedName("definition_type")
    public String definitionType;

    @SerializedName("definition_id")
    public Integer definitionId;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("domain")
    public String domain;

    @SerializedName("placements")
    public Placements placements;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.definitionType);
        dest.writeValue(this.definitionId);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.domain);
        dest.writeParcelable(this.placements, flags);
    }

    public LaunchDefinition() {
    }

    protected LaunchDefinition(Parcel in) {
        this.definitionType = in.readString();
        this.definitionId = (Integer) in.readValue(Integer.class.getClassLoader());
        this.name = in.readString();
        this.description = in.readString();
        this.domain = in.readString();
        this.placements = in.readParcelable(Placements.class.getClassLoader());
    }

    public static final Parcelable.Creator<LaunchDefinition> CREATOR = new Parcelable.Creator<LaunchDefinition>() {
        @Override
        public LaunchDefinition createFromParcel(Parcel source) {
            return new LaunchDefinition(source);
        }

        @Override
        public LaunchDefinition[] newArray(int size) {
            return new LaunchDefinition[size];
        }
    };
}
