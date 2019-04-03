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

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi.model.CanvasModel;
import com.instructure.canvasapi.model.FileUploadParams;

import java.io.Serializable;
import java.util.Date;
import java.util.List;



public class FileUploadParamsWrapper extends CanvasModel<FileUploadParamsWrapper> {

    @SerializedName("attachments")
    private List<FileUploadParams> uploadParams;

    public List<FileUploadParams> getUploadParams() {
        return uploadParams;
    }

    public void setUploadParams(List<FileUploadParams> uploadParams) {
        this.uploadParams = uploadParams;
    }

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
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /////////////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////////////

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.uploadParams);
    }

    public FileUploadParamsWrapper() {
    }

    private FileUploadParamsWrapper(Parcel in) {
        this.uploadParams = in.createTypedArrayList(FileUploadParams.CREATOR);
    }

    public static final Creator<FileUploadParamsWrapper> CREATOR = new Creator<FileUploadParamsWrapper>() {
        @Override
        public FileUploadParamsWrapper createFromParcel(Parcel source) {
            return new FileUploadParamsWrapper(source);
        }

        @Override
        public FileUploadParamsWrapper[] newArray(int size) {
            return new FileUploadParamsWrapper[size];
        }
    };
}
