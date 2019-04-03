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
import java.util.LinkedHashMap;


public class FileUploadParams extends CanvasModel<FileUploadParams>{

    @SerializedName("upload_url")
    private String uploadUrl;

    @SerializedName("upload_params")
    private LinkedHashMap<String, String> uploadParams;

    /////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    /////////////////////////////////////////////////////////////////////////
    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public LinkedHashMap<String,String> getUploadParams() {
        return uploadParams;
    }

    public void setUploadParams(LinkedHashMap<String, String> uploadParams) {
        this.uploadParams = uploadParams;
    }

    /////////////////////////////////////////////////////////////////////////
    // CanvasModel Overrides
    /////////////////////////////////////////////////////////////////////////
    @Override
    public long getId() {
        return 0;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return uploadUrl;
    }

    @Override
    public int compareTo(FileUploadParams fileUploadToken) {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uploadUrl);
        dest.writeSerializable(this.uploadParams);
    }

    /////////////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////////////
    public FileUploadParams() {}

    private FileUploadParams(Parcel in) {
        this.uploadUrl = in.readString();
        this.uploadParams = (LinkedHashMap<String,String>)in.readSerializable();

    }

    public static final Creator<FileUploadParams> CREATOR = new Creator<FileUploadParams>() {
        public FileUploadParams createFromParcel(Parcel source) {
            return new FileUploadParams(source);
        }

        public FileUploadParams[] newArray(int size) {
            return new FileUploadParams[size];
        }
    };
}
