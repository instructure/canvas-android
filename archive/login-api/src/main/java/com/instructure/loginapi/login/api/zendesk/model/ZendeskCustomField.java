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

public class ZendeskCustomField implements Parcelable {

    private long id;
    private String value;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }


    public ZendeskCustomField(){}
    public ZendeskCustomField(long id, String value){
        this.id = id;
        this.value = value;
    }

    ZendeskCustomField(Parcel in){
        this.id = in.readLong();
        this.value = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(value);
    }

    public static Creator<ZendeskCustomField> CREATOR = new Creator<ZendeskCustomField>() {
        public ZendeskCustomField createFromParcel(Parcel source) {
            return new ZendeskCustomField(source);
        }

        public ZendeskCustomField[] newArray(int size) {
            return new ZendeskCustomField[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
