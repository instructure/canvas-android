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


public class Favorite extends CanvasComparable<Favorite> implements android.os.Parcelable {

    private long context_id;
    private String context_type;

    public long getContext_id() {
        return context_id;
    }

    public void setContext_id(long context_id) {
        this.context_id = context_id;
    }

    public String getContext_type() {
        return context_type;
    }

    public void setContext_type(String context_type) {
        this.context_type = context_type;
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
        dest.writeLong(this.context_id);
        dest.writeString(this.context_type);
    }

    private Favorite(Parcel in) {
        this.context_id = in.readLong();
        this.context_type = in.readString();
    }

    public static Creator<Favorite> CREATOR = new Creator<Favorite>() {
        public Favorite createFromParcel(Parcel source) {
            return new Favorite(source);
        }

        public Favorite[] newArray(int size) {
            return new Favorite[size];
        }
    };
}
