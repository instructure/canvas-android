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

package com.instructure.pandautils.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.instructure.canvasapi2.models.Attachment;


public class FileSubmitObject implements Parcelable {

    public enum STATE {
        NORMAL, UPLOADING, COMPLETE
    }

    private String name;
    private long size;
    private String contentType;
    private String fullPath;
    private String errorMessage; // used when loading files in an asynctask
    private STATE currentState = STATE.NORMAL;

    public FileSubmitObject(String name, long size, String contentType, String fullPath) {
        init(name, size, contentType, fullPath, "");
    }

    public FileSubmitObject(String name, long size, String contentType, String fullPath, String errorMessage) {
        init(name, size, contentType, fullPath, errorMessage);
    }

    private void init(String name, long size, String contentType, String fullPath, String errorMessage){
        this.name = name;
        this.size = size;
        this.contentType = contentType;
        this.fullPath = fullPath;
        this.errorMessage = errorMessage;
    }

    /**
     * Used to get a basic attachment object for display.
     * @return A skin & bones attachment object
     */
    @NonNull
    public Attachment toAttachment() {
        Attachment attachment = new Attachment();
        attachment.setContentType(contentType);
        attachment.setDisplayName(name);
        attachment.setThumbnailUrl(fullPath);
        return attachment;
    }

    public STATE getCurrentState() {
        return currentState;
    }

    public void setState(STATE newState){
        this.currentState = newState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null){
            return false;
        }

        if(getClass() != o.getClass()){
            return false;
        }

        FileSubmitObject fso = (FileSubmitObject) o;
        return this.getName().equals(((FileSubmitObject) o).getName());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeLong(this.size);
        parcel.writeString(this.contentType);
        parcel.writeString(this.fullPath);
        parcel.writeString(this.errorMessage);
    }

    /////////////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////////////
    public FileSubmitObject() {}

    private FileSubmitObject(Parcel in) {
        this.name = in.readString();
        this.size = in.readLong();
        this.contentType = in.readString();
        this.fullPath = in.readString();
        this.errorMessage = in.readString();
    }

    public static final Creator<FileSubmitObject> CREATOR = new Creator<FileSubmitObject>() {
        public FileSubmitObject createFromParcel(Parcel source) {
            return new FileSubmitObject(source);
        }

        public FileSubmitObject[] newArray(int size) {
            return new FileSubmitObject[size];
        }
    };
}
