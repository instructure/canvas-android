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
import org.simpleframework.xml.Root;

@Root
public class xml implements Parcelable {

    /*
            <?xml version="1.0" encoding="UTF-8"?>
        <xml>
           <xml>
              <objectType></objectType>
              <id></id>
              <partnerId></partnerId>
              <userId></userId>
              <status></status>
              <fileName></fileName>
              <fileSize />
              <uploadedFileSize></uploadedFileSize>
              <createdAt></createdAt>
              <updatedAt></updatedAt>
           </xml>
           <executionTime></executionTime>
        </xml>
             */
    @Element
    private Result result;

    @Element(required = false)
    private float executionTime;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.result, flags);
        dest.writeFloat(this.executionTime);
    }

    public xml() {
    }

    private xml(Parcel in) {
        this.result = in.readParcelable(Result.class.getClassLoader());
        this.executionTime = in.readFloat();
    }

    public static Parcelable.Creator<xml> CREATOR = new Parcelable.Creator<xml>() {
        public xml createFromParcel(Parcel source) {
            return new xml(source);
        }

        public xml[] newArray(int size) {
            return new xml[size];
        }
    };

    @Override
    public String toString() {
        return "xml{" +
                "result=" + result +
                ", executionTime=" + executionTime +
                '}';
    }
}

