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
import android.os.Parcelable;

import java.util.Date;



public class DiscussionTopicPermission extends CanvasModel<DiscussionTopicPermission> {

	private static final long serialVersionUID = 1L;

	private boolean attach = false;
    private boolean update = false;
    private boolean delete = false;
    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

	public boolean canAttach() {
		return attach;
	}
	public void setCanAttach(boolean can_attach) {
		this.attach = can_attach;
	}

    public boolean canUpdate(){
        return update;
    }
    public void setCanUpdate(boolean update){
        this.update = update;
    }

    public boolean canDelete(){
        return delete;
    }
    public void setCanDelete(boolean delete){
        this.delete = delete;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(attach ? (byte) 1 : (byte) 0);
        dest.writeByte(update ? (byte) 1 : (byte) 0);
        dest.writeByte(delete ? (byte) 1 : (byte) 0);
    }

    public DiscussionTopicPermission() {}

    private DiscussionTopicPermission(Parcel in) {
        this.attach = in.readByte() != 0;
        this.update = in.readByte() != 0;
        this.delete = in.readByte() != 0;
    }

    public static Creator<DiscussionTopicPermission> CREATOR = new Creator<DiscussionTopicPermission>() {
        public DiscussionTopicPermission createFromParcel(Parcel source) {
            return new DiscussionTopicPermission(source);
        }

        public DiscussionTopicPermission[] newArray(int size) {
            return new DiscussionTopicPermission[size];
        }
    };

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
}
