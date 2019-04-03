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


public class CourseNickname extends CanvasComparable<CourseNickname> {

    @SerializedName("course_id")
    public long id;
    public String name;
    public String nickname;

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
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.nickname);
    }

    public CourseNickname() {
    }

    private CourseNickname(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.nickname = in.readString();
    }

    public static final Creator<CourseNickname> CREATOR = new Creator<CourseNickname>() {
        public CourseNickname createFromParcel(Parcel source) {
            return new CourseNickname(source);
        }

        public CourseNickname[] newArray(int size) {
            return new CourseNickname[size];
        }
    };
}
