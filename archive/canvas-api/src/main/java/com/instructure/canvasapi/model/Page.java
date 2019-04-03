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

import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.Date;

public class Page extends CanvasModel<Page> {

    private static final long serialVersionUID = 1L;

    public static final String FRONT_PAGE_NAME = "front-page";

    /* Example JSON response
 *
 * {
      // the unique locator for the page
      url: "my-page-title",

      // the title of the page
      title: "My Page Title",

      // the creation date for the page
      created_at: "2012-08-06T16:46:33-06:00",

      // the date the page was last updated
      updated_at: "2012-08-08T14:25:20-06:00",

      // whether this page is hidden from students
      // (note: students will never see this true; pages hidden from them will be omitted from results)
      hide_from_students: false,

      // the page content, in HTML
      // (present when requesting a single page; omitted when listing pages)
      body: "<p>Page Content</p>"
    }
 */

	private String url;
    private long page_id;
	private String title;
	private String created_at;
	private String updated_at;
	private boolean hide_from_students;
    private String status;
	private String body;
    private LockInfo lock_info;
    private boolean front_page;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public long getPageId() {
        return page_id;
    }

    public void setPageId(long pageId) {
        this.page_id = pageId;
    }

    public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getCreate_at() {
		return APIHelpers.stringToDate(created_at);
	}
	public void setCreate_at(Date create_at) {
		this.created_at = APIHelpers.dateToString(create_at);
	}
	public Date getUpdated_at() {
        return APIHelpers.stringToDate(updated_at);
	}
	public void setUpdated_at(Date updated_at) {
        this.created_at = APIHelpers.dateToString(updated_at);
	}
	public boolean isHide_from_students() {
		return hide_from_students || (status != null && status.equalsIgnoreCase("unauthorized"));
	}
	public void setHide_from_students(boolean hide_from_students) {
		this.hide_from_students = hide_from_students;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}


    //During parsing, GSON will try. Which means sometimes we get 'empty' objects
    //They're non-null, but don't have any information.
    public LockInfo getLockInfo() {

        //Check for null or empty lock info.
        if(lock_info == null || lock_info.isEmpty()){
            return null;
        }

        return lock_info;
    }

    public void setLockInfo(LockInfo lockInfo) {
        this.lock_info = lockInfo;
    }

    public boolean isFrontPage(){
        return front_page;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    public Date getComparisonDate() { return getCreate_at(); }
    public String getComparisonString() { return title; }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public Page() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Page page = (Page) o;

        return url != null ? url.equals(page.url) : page.url == null;

    }

    @Override
    public long getId() {
        return getPageId();
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.title);
        dest.writeString(this.created_at);
        dest.writeString(this.updated_at);
        dest.writeByte(hide_from_students ? (byte) 1 : (byte) 0);
        dest.writeString(this.status);
        dest.writeString(this.body);
        dest.writeParcelable(this.lock_info, flags);
        dest.writeByte(front_page ? (byte) 1 : (byte) 0);
    }

    private Page(Parcel in) {
        this.url = in.readString();
        this.title = in.readString();
        this.created_at = in.readString();
        this.updated_at = in.readString();
        this.hide_from_students = in.readByte() != 0;
        this.status = in.readString();
        this.body = in.readString();
        this.lock_info =  in.readParcelable(LockInfo.class.getClassLoader());
        this.front_page = in.readByte() != 0;
    }

    public static Creator<Page> CREATOR = new Creator<Page>() {
        public Page createFromParcel(Parcel source) {
            return new Page(source);
        }

        public Page[] newArray(int size) {
            return new Page[size];
        }
    };
}
