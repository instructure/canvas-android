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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class Message extends CanvasModel<Message> {

    private long id;
    private String created_at;
    private String body;
    private long author_id;
    private boolean generated;
    private List<Attachment> attachments = new ArrayList<>();
    private MediaComment media_comment;
    private Submission submission;
    private List<Message> forwarded_messages = new ArrayList<>();
    private List<Long> participating_user_ids = new ArrayList<>();
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
    public Date getCreationDate() {
        return APIHelpers.stringToDate(created_at);
    }
    public void setCreationDate(String createdAt) {
        this.created_at = createdAt;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public long getAuthorID() {
        return author_id;
    }
    public void setAuthorId(long authorId) {
        this.author_id = authorId;
    }
    public boolean isGenerated() {
        return generated;
    }
    public MediaComment getMediaComment() {
        return media_comment;
    }
    public void setMediaComment(MediaComment mediaComment) {
        this.media_comment = mediaComment;
    }
    public List<Attachment> getAttachments() {
        return attachments;
    }
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
    public List<Message> getForwardedMessages() {
        return forwarded_messages;
    }
    public void setForwardedMessages(List<Message> forwardedMessages) {
        this.forwarded_messages = forwardedMessages;
    }
    public Submission getSubmission() {
        return submission;
    }
    public void setSubmission(Submission submission) {
        this.submission = submission;
    }
    public Date getMessageDate() {
        return APIHelpers.stringToDate(created_at);
    }
    public List<Long> getParticipatingUserIds() {
        return participating_user_ids;
    }
    public void setParticipatingUserIds(List<Long> participating_user_ids) {
        this.participating_user_ids = participating_user_ids;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public Date getComparisonDate() {
        return APIHelpers.stringToDate(created_at);
    }

    @Override
    public String getComparisonString() {
        return body;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.created_at);
        dest.writeString(this.body);
        dest.writeLong(this.author_id);
        dest.writeByte(generated ? (byte) 1 : (byte) 0);
        dest.writeList(this.attachments);
        dest.writeParcelable(this.media_comment, flags);
        dest.writeParcelable(this.submission, flags);
        dest.writeList(this.forwarded_messages);
        dest.writeList(this.participating_user_ids);
    }

    public Message() {}

    private Message(Parcel in) {
        this.id = in.readLong();
        this.created_at = in.readString();
        this.body = in.readString();
        this.author_id = in.readLong();
        this.generated = in.readByte() != 0;
        in.readList(this.attachments, Attachment.class.getClassLoader());
        this.media_comment = in.readParcelable(MediaComment.class.getClassLoader());
        this.submission = in.readParcelable(Submission.class.getClassLoader());
        in.readList(this.forwarded_messages, Message.class.getClassLoader());
        in.readList(this.participating_user_ids, Long.class.getClassLoader());
    }

    public static Creator<Message> CREATOR = new Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
