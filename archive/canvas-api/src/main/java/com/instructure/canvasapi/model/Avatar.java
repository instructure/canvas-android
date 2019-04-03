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

public class Avatar extends CanvasComparable<Avatar> {

    public static final long serialVersionUID = 1L;

	private String type;
	private String url;
	private String token;
	private String display_name;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getDisplayName() {
		return display_name;
	}
	public void setDisplayName(String displayName) {
		this.display_name = displayName;
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.url);
        dest.writeString(this.token);
        dest.writeString(this.display_name);
    }

    public Avatar() {
    }

    private Avatar(Parcel in) {
        this.type = in.readString();
        this.url = in.readString();
        this.token = in.readString();
        this.display_name = in.readString();
    }

    public static Creator<Avatar> CREATOR = new Creator<Avatar>() {
        public Avatar createFromParcel(Parcel source) {
            return new Avatar(source);
        }

        public Avatar[] newArray(int size) {
            return new Avatar[size];
        }
    };
}
