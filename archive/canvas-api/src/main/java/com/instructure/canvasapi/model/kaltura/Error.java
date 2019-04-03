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

package com.instructure.canvasapi.model.kaltura;

import android.os.Parcel;
import android.os.Parcelable;

import org.simpleframework.xml.Element;

@Element
public class Error implements Parcelable {
    @Element (required = false)
    private String code;
    @Element (required = false)
    private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Error{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.message);
    }

    public Error() {
    }

    private Error(Parcel in) {
        this.code = in.readString();
        this.message = in.readString();
    }

    public static Parcelable.Creator<Error> CREATOR = new Parcelable.Creator<Error>() {
        public Error createFromParcel(Parcel source) {
            return new Error(source);
        }

        public Error[] newArray(int size) {
            return new Error[size];
        }
    };
}
