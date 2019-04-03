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

import java.util.Date;


public class DiscussionParticipant extends CanvasModel<DiscussionParticipant>{

    private long id;
    private String display_name;
    private String avatar_image_url;
    private String html_url;

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
	public String getDisplayName() {
		return display_name;
	}
	public void setDisplayName(String display_name) {
		this.display_name = display_name;
	}
	public String getAvatarUrl() {
		return avatar_image_url;
	}
	public void setAvatarUrl(String avatar_url) {
		this.avatar_image_url = avatar_url;
	}
	public String getHtmlUrl() {
		return html_url;
	}
	public void setHtmlUrl(String html_url) {
		this.html_url = html_url;
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
        return getDisplayName();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////
	
	public DiscussionParticipant(long id) {
		this.id = id;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return display_name+":"+id;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.display_name);
        dest.writeString(this.avatar_image_url);
        dest.writeString(this.html_url);
    }

    private DiscussionParticipant(Parcel in) {
        this.id = in.readLong();
        this.display_name = in.readString();
        this.avatar_image_url = in.readString();
        this.html_url = in.readString();
    }

    public static Creator<DiscussionParticipant> CREATOR = new Creator<DiscussionParticipant>() {
        public DiscussionParticipant createFromParcel(Parcel source) {
            return new DiscussionParticipant(source);
        }

        public DiscussionParticipant[] newArray(int size) {
            return new DiscussionParticipant[size];
        }
    };
}
