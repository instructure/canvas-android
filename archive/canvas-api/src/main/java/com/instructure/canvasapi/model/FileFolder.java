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

public class FileFolder extends CanvasModel<FileFolder>{

    // Common Attributes
    private long id;
    private String created_at;
    private String updated_at;
    private String unlock_at;
    private String lock_at;
    private boolean locked;
    private boolean hidden;
    private boolean locked_for_user;
    private boolean hidden_for_user;

	// File Attributes
    private long folder_id;
    private long size;
    @SerializedName("content-type")
	private String content_type;
	private String url;
	private String display_name;
	private String thumbnail_url;
    private LockInfo lock_info;

    // Folder Attributes
    private long parent_folder_id;
    private long context_id;
	private int files_count;
	private int position;
	private int folders_count;
    private String context_type;
    private String name;
    private String folders_url;
	private String files_url;
	private String full_name;
	
    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    // Common
    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public Date getCreatedAt() {
        return APIHelpers.stringToDate(created_at);
    }
    public void setCreatedAt(Date created_at) {
        this.created_at = APIHelpers.dateToString(created_at);
    }
    public Date getUpdatedAt() {
        return APIHelpers.stringToDate(updated_at);
    }
    public void setUpdatedAt(Date updated_at) {
        this.updated_at = APIHelpers.dateToString(updated_at);
    }
    public Date getUnlockAt() {
        return APIHelpers.stringToDate(unlock_at);
    }
    public void setUnlockAt(Date unlock_at) {
        this.unlock_at = APIHelpers.dateToString(unlock_at);
    }
    public Date getLockAt(){
        return APIHelpers.stringToDate(lock_at);
    }
    public void setLockAt(Date lock_at){
        this.lock_at = APIHelpers.dateToString(lock_at);
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
    public void setLockedForUser(boolean locked_for_user) {
        this.locked_for_user = locked_for_user;
    }
    public boolean isHiddenForUser() {
        return hidden_for_user;
    }
    public void setHiddenForUser(boolean hidden_for_user) {
        this.hidden_for_user = hidden_for_user;
    }

    // Files
    public long getFolderId(){
        return this.folder_id;
    }
    public void setFolderId(long folderId){
        this.folder_id = folderId;
    }
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getContentType() {
		return content_type;
	}
	public void setContentType(String content_type) {
		this.content_type = content_type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDisplayName() {
		return display_name;
	}
	public void setDisplayName(String display_name) {
		this.display_name = display_name;
	}
	public String getThumbnailUrl(){
        return this.thumbnail_url;
    }
    public void setThumbnailUrl(String thumbnailUrl){
        this.thumbnail_url = thumbnailUrl;
    }
    public LockInfo getLockInfo() {
        return (lock_info == null || lock_info.isEmpty()) ? null : lock_info;
    }
    public void setLockInfo(LockInfo lockInfo) {
        this.lock_info = lockInfo;
    }


    // Folders
    public String getContextType() {
		return context_type;
	}
	public void setContextType(String context_type) {
		this.context_type = context_type;
	}
	public long getContextId() {
		return context_id;
	}
	public void setContextId(long context_id) {
		this.context_id = context_id;
	}
	public int getFilesCount() {
		return files_count;
	}
	public void setFilesCount(int files_count) {
		this.files_count = files_count;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getFolders_count() {
		return folders_count;
	}
	public void setFoldersCount(int folders_count) {
		this.folders_count = folders_count;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getParentFolderId() {
		return parent_folder_id;
	}
	public void setParentFolderId(long parent_folder_id) {
		this.parent_folder_id = parent_folder_id;
	}
	public String getFoldersUrl() {
		return folders_url;
	}
	public void setFoldersUrl(String folders_url) {
		this.folders_url = folders_url;
	}
	public String getFilesUrl() {
		return files_url;
	}
	public void setFilesUrl(String files_url) {
		this.files_url = files_url;
	}
	public String getFullName() {
		return full_name;
	}
	public void setFullName(String full_name) {
		this.full_name = full_name;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() { return null;}
    @Override
    public String getComparisonString() {return null;}

    // we override compareTo instead of using Canvas Comparable methods
    @Override
    public int compareTo(FileFolder other) {
        // folders go before files

        // this is a folder and other is a file
        if (getFullName() != null && other.getFullName() == null) {
            return -1;
        } // this is a file and other is a folder
        else if (getFullName() == null && other.getFullName() != null) {
            return 1;
        }
        // both are folders
        if (getFullName() != null && other.getFullName() != null) {
            return getFullName().compareTo(other.getFullName());
        }
        // both are files
        return getDisplayName().compareTo(other.getDisplayName());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

	public FileFolder() {}

    private FileFolder(Parcel in) {
        this.size = in.readLong();
        this.content_type = in.readString();
        this.url = in.readString();
        this.display_name = in.readString();
        this.context_type = in.readString();
        this.context_id = in.readLong();
        this.files_count = in.readInt();
        this.position = in.readInt();
        this.folders_count = in.readInt();
        this.name = in.readString();
        this.parent_folder_id = in.readLong();
        this.folders_url = in.readString();
        this.files_url = in.readString();
        this.full_name = in.readString();
        this.id = in.readLong();
        this.created_at = in.readString();
        this.updated_at = in.readString();
        this.unlock_at = in.readString();
        this.locked = in.readByte() != 0;
        this.hidden = in.readByte() != 0;
        this.lock_at = in.readString();
        this.locked_for_user = in.readByte() != 0;
        this.hidden_for_user = in.readByte() != 0;
        this.lock_info =  in.readParcelable(LockInfo.class.getClassLoader());
        this.folder_id = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.size);
        dest.writeString(this.content_type);
        dest.writeString(this.url);
        dest.writeString(this.display_name);
        dest.writeString(this.context_type);
        dest.writeLong(this.context_id);
        dest.writeInt(this.files_count);
        dest.writeInt(this.position);
        dest.writeInt(this.folders_count);
        dest.writeString(this.name);
        dest.writeLong(this.parent_folder_id);
        dest.writeString(this.folders_url);
        dest.writeString(this.files_url);
        dest.writeString(this.full_name);
        dest.writeLong(this.id);
        dest.writeString(this.created_at);
        dest.writeString(this.updated_at);
        dest.writeString(this.unlock_at);
        dest.writeByte(locked ? (byte) 1 : (byte) 0);
        dest.writeByte(hidden ? (byte) 1 : (byte) 0);
        dest.writeString(this.lock_at);
        dest.writeByte(locked_for_user ? (byte) 1 : (byte) 0);
        dest.writeByte(hidden_for_user ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.lock_info,flags);
        dest.writeLong(this.folder_id);
    }

    public static Creator<FileFolder> CREATOR = new Creator<FileFolder>() {
        public FileFolder createFromParcel(Parcel source) {
            return new FileFolder(source);
        }

        public FileFolder[] newArray(int size) {
            return new FileFolder[size];
        }
    };
}
