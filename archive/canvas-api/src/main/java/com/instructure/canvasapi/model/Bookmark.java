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

/**
 * A bookmark object used for storing Canvas URLs and meta data about said url.
 */
public class Bookmark extends CanvasModel<Bookmark> implements android.os.Parcelable {

    private long id = 0;
    private String name;
    private String url;
    private int position = 0;

    //A helper for storing a course id, not part of the API
    private long courseId = 0;

    public Bookmark() {
    }

    public Bookmark(String name, String url, int position) {
        this.name = name;
        this.url = url;
        this.position = position;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    @Override
    public long getId() {
        return id;
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
        dest.writeString(this.name);
        dest.writeString(this.url);
        dest.writeInt(this.position);
    }

    private Bookmark(Parcel in) {
        this.name = in.readString();
        this.url = in.readString();
        this.position = in.readInt();
    }

    public static final Creator<Bookmark> CREATOR = new Creator<Bookmark>() {
        public Bookmark createFromParcel(Parcel source) {
            return new Bookmark(source);
        }

        public Bookmark[] newArray(int size) {
            return new Bookmark[size];
        }
    };
}
