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

import com.google.gson.annotations.SerializedName;

public class ZendeskComment implements Parcelable{

    private long id;
    private String body;
    @SerializedName("public")
    private boolean isPublic;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public boolean isPublic() {
        return isPublic;
    }
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public ZendeskComment(){}
    public ZendeskComment(long id, String body, boolean isPublic){
        this.id = id;
        this.body = body;
        this.isPublic = isPublic;
    }

    ZendeskComment(Parcel in){
        this.id = in.readLong();
        this.body = in.readString();
        this.isPublic = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(body);
        dest.writeByte(isPublic ? (byte) 1 : (byte) 0);
    }

    public static Creator<ZendeskComment> CREATOR = new Creator<ZendeskComment>() {
        @Override
        public ZendeskComment createFromParcel(Parcel source) {
            return new ZendeskComment(source);
        }

        @Override
        public ZendeskComment[] newArray(int size) {
            return new ZendeskComment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
