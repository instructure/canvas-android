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
import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.Date;


public class DiscussionAttachment extends CanvasModel<DiscussionAttachment> {

    private long id;
    private boolean locked;
    private boolean hidden;
    private boolean locked_for_user;
    private boolean hidden_for_user;
    private int size;
    private String lock_at;
    private String unlock_at;
    private String updated_at;
    private String created_at;
    private String display_name;
    private String filename;
    private String url;

    @SerializedName("content-type")
    private String content_type;

    @SerializedName("folder_id")
    private long folderId;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
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
    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public boolean isHidden() {
        return hidden;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    public boolean isLockedForUser() {
        return locked_for_user;
    }
    public void setLockedForUser(boolean lockedForUser) {
        this.locked_for_user = lockedForUser;
    }
    public boolean isHiddenForUser() {
        return hidden_for_user;
    }
    public void setHiddenForUser(boolean hiddenForUser) {
        this.hidden_for_user = hiddenForUser;
    }
    public int getFileSize() {
        return size;
    }
    public void setFileSize(int fileSize) {
        this.size = fileSize;
    }
    public Date getLockAt() {
        return APIHelpers.stringToDate(lock_at);
    }
    public void setLockAt(Date lockAt) {
        this.lock_at = APIHelpers.dateToString(lockAt);
    }
    public Date getUnlockAt() {
        return APIHelpers.stringToDate(unlock_at);
    }
    public void setUnlockAt(Date unlockAt) {
        this.unlock_at = APIHelpers.dateToString(unlockAt);
    }
    public Date getUpdatedAt() {
        return APIHelpers.stringToDate(updated_at);
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updated_at = APIHelpers.dateToString(updatedAt);
    }
    public Date getCreatedAt() {
        return APIHelpers.stringToDate(created_at);
    }
    public void setCreatedAt(Date createdAt) {
        this.created_at = APIHelpers.dateToString(createdAt);
    }
    public String getDisplayName() {
        return display_name;
    }
    public void setDisplayName(String displayName) {
        this.display_name = displayName;
    }
    public String getFileName() {
        return filename;
    }
    public void setFileName(String fileName) {
        this.filename = fileName;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getMimeType() {
        return content_type;
    }
    public void setMimeType(String mimeType) {
        this.content_type = mimeType;
    }
    public long getFolderId(){
        return folderId;
    }
    public void setFolderId(long folderId){
        this.folderId = folderId;
    }
    public String getThumbnailUrl(){
        return thumbnailUrl;
    }
    public void setThumbnailUrl(String thumbnailUrl){
        this.thumbnailUrl = thumbnailUrl;
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
        return filename;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DiscussionAttachment() {}

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public boolean shouldShowToUser() {
        if (hidden || hidden_for_user) {
            return false;
        } else if (locked || locked_for_user) {
            Date unlockAt = APIHelpers.stringToDate(unlock_at);

            if (unlock_at == null) {
                return false;
            } else {
                return new Date().after(unlockAt);
            }
        } else {
            return true;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeByte(locked ? (byte) 1 : (byte) 0);
        dest.writeByte(hidden ? (byte) 1 : (byte) 0);
        dest.writeByte(locked_for_user ? (byte) 1 : (byte) 0);
        dest.writeByte(hidden_for_user ? (byte) 1 : (byte) 0);
        dest.writeInt(this.size);
        dest.writeString(this.lock_at);
        dest.writeString(this.unlock_at);
        dest.writeString(this.updated_at);
        dest.writeString(this.created_at);
        dest.writeString(this.display_name);
        dest.writeString(this.filename);
        dest.writeString(this.url);
        dest.writeString(this.content_type);
    }

    private DiscussionAttachment(Parcel in) {
        this.id = in.readLong();
        this.locked = in.readByte() != 0;
        this.hidden = in.readByte() != 0;
        this.locked_for_user = in.readByte() != 0;
        this.hidden_for_user = in.readByte() != 0;
        this.size = in.readInt();
        this.lock_at = in.readString();
        this.unlock_at = in.readString();
        this.updated_at = in.readString();
        this.created_at = in.readString();
        this.display_name = in.readString();
        this.filename = in.readString();
        this.url = in.readString();
        this.content_type = in.readString();
    }

    public static Creator<DiscussionAttachment> CREATOR = new Creator<DiscussionAttachment>() {
        public DiscussionAttachment createFromParcel(Parcel source) {
            return new DiscussionAttachment(source);
        }

        public DiscussionAttachment[] newArray(int size) {
            return new DiscussionAttachment[size];
        }
    };
}
