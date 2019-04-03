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
package com.instructure.loginapi.login.snicker;

import android.os.Parcel;
import android.os.Parcelable;

public class SnickerDoodle implements Parcelable {

    public String title;
    public String subtitle;
    public String username;
    public String password;

    public SnickerDoodle() {
    }

    public SnickerDoodle(String title, String subtitle, String username, String password) {
        this.title = title;
        this.subtitle = subtitle;
        this.username = username;
        this.password = password;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.subtitle);
        dest.writeString(this.username);
        dest.writeString(this.password);
    }

    protected SnickerDoodle(Parcel in) {
        this.title = in.readString();
        this.subtitle = in.readString();
        this.username = in.readString();
        this.password = in.readString();
    }

    public static final Creator<SnickerDoodle> CREATOR = new Creator<SnickerDoodle>() {
        @Override
        public SnickerDoodle createFromParcel(Parcel source) {
            return new SnickerDoodle(source);
        }

        @Override
        public SnickerDoodle[] newArray(int size) {
            return new SnickerDoodle[size];
        }
    };
}
