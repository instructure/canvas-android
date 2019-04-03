/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.loginapi.login.api.zendesk.model;


import android.os.Parcel;
import android.os.Parcelable;

public class ZendeskRequester implements Parcelable {

    /**
     * requester":{"name":"The Customer", "email":"thecustomer@domain.com"
     */
    private String name;
    private String email;
    private int locale_id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getLocale_id() {
        return locale_id;
    }

    public void setLocale_id(int locale_id) {
        this.locale_id = locale_id;
    }

    public ZendeskRequester(String name, String email, int locale_id) {
        this.name = name;
        this.email = email;
        this.locale_id = locale_id;
    }

    ZendeskRequester(Parcel in){
        this.name = in.readString();
        this.email = in.readString();
        this.locale_id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeInt(locale_id);
    }

    public static Creator<ZendeskRequester> CREATOR = new Creator<ZendeskRequester>() {
        @Override
        public ZendeskRequester createFromParcel(Parcel source) {
            return new ZendeskRequester(source);
        }

        @Override
        public ZendeskRequester[] newArray(int size) {
            return new ZendeskRequester[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
