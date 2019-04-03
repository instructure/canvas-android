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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubmissionComment extends CanvasComparable<SubmissionComment> implements Serializable{
    private static final long serialVersionUID = 1L;

    private long author_id;
    private String author_name;
    private String comment;
    private String created_at;
    private MediaComment media_comment;
    private List<Attachment> attachments = new ArrayList<Attachment>();
    private Author author;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

	public long getAuthorID() {
		return author_id;
	}
	public String getAuthorName() {
		return author_name;
	}
	public String getComment() {
		return comment;
	}
	public Date getCreatedAt() {
        if(created_at == null) {
            return null;
        }
        return APIHelpers.stringToDate(created_at);
	}

	public MediaComment getMedia_comment() {
		return media_comment;
	}
    public List<Attachment> getAttachments(){
        if(attachments == null) {
            return new ArrayList<Attachment>();
        }
        return attachments;
    }
	public void setAuthorID(long authorID) {
		this.author_id = authorID;
	}
	public void setAuthorName(String authorName) {
		this.author_name = authorName;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public void setCreatedAt(String createdAt) {
		this.created_at = createdAt;
	}
	public void setMedia_comment(MediaComment media_comment) {
		this.media_comment = media_comment;
	}
	public Author getAuthor() {
	    return author;
	}
	public void setAuthor(Author author) {
	    this.author = author;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return getCreatedAt();
    }

    @Override
    public String getComparisonString() {
        return getAuthorName();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public SubmissionComment() {}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.author_id);
        dest.writeString(this.author_name);
        dest.writeString(this.comment);
        dest.writeString(this.created_at);
        dest.writeParcelable(this.media_comment, flags);
        dest.writeList(this.attachments);
        dest.writeParcelable(this.author, flags);
    }

    private SubmissionComment(Parcel in) {
        this.author_id = in.readLong();
        this.author_name = in.readString();
        this.comment = in.readString();
        this.created_at = in.readString();
        this.media_comment = in.readParcelable(MediaComment.class.getClassLoader());
        in.readList(this.attachments, Attachment.class.getClassLoader());
        this.author = in.readParcelable(Author.class.getClassLoader());
    }

    public static Creator<SubmissionComment> CREATOR = new Creator<SubmissionComment>() {
        public SubmissionComment createFromParcel(Parcel source) {
            return new SubmissionComment(source);
        }

        public SubmissionComment[] newArray(int size) {
            return new SubmissionComment[size];
        }
    };
}
