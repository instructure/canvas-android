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


public class BasicUser extends CanvasModel<BasicUser> {

    private long id;
    private String name;
    private String avatar_url;
    //TODO: Common courses
    //TODO: Common groups

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getUsername() {
        return name;
    }
    public void setUsername(String username) {
        this.name = username;
    }
    public String getAvatarUrl() {
        return avatar_url;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatar_url = avatarUrl;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return getUsername();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public BasicUser(long _id, String _username) {
        id = _id;
        name = _username;
        avatar_url = "";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.avatar_url);
    }

    private BasicUser(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.avatar_url = in.readString();
    }

    public static Creator<BasicUser> CREATOR = new Creator<BasicUser>() {
        public BasicUser createFromParcel(Parcel source) {
            return new BasicUser(source);
        }

        public BasicUser[] newArray(int size) {
            return new BasicUser[size];
        }
    };
}
