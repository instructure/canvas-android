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

public class Placements implements Parcelable {

    @SerializedName("global_navigation")
    public GlobalNavigation globalNavigation;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.globalNavigation, flags);
    }

    public Placements() {
    }

    protected Placements(Parcel in) {
        this.globalNavigation = in.readParcelable(GlobalNavigation.class.getClassLoader());
    }

    public static final Parcelable.Creator<Placements> CREATOR = new Parcelable.Creator<Placements>() {
        @Override
        public Placements createFromParcel(Parcel source) {
            return new Placements(source);
        }

        @Override
        public Placements[] newArray(int size) {
            return new Placements[size];
        }
    };
}
