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
import java.util.HashMap;
import java.util.List;


public class DiscussionEntry extends CanvasModel<DiscussionEntry>{

    private long id;                      //Entry id.
    private boolean unread = false;
    private String updated_at;
    private String created_at;
    private DiscussionEntry parent;         //Parent of the entry;
    private DiscussionParticipant author;
    private String description;             //HTML formatted string used for an edge case. Converting header to entry
    private long user_id;                   //Id of the user that posted it.
    private long parent_id = -1;            //Parent id. -1 if there isn't one.
    private String message;                 //HTML message.
    private boolean deleted;                //Whether the quthor deleted the message. If true, the message will be null.
    private int totalChildren = 0;
    private int unreadChildren = 0;
    private List<DiscussionEntry> replies = new ArrayList<DiscussionEntry>();
    private List<DiscussionAttachment> attachments = new ArrayList<DiscussionAttachment>();
    private int rating_count;
    private int rating_sum;

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

    public List<DiscussionAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<DiscussionAttachment> attachments) {
        this.attachments = attachments;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public void setTotalChildren(int total) {
        totalChildren = total;
    }

    public int getTotalChildren() {
        return totalChildren;
    }

    public void setUnreadChildren(int unread) {
        unreadChildren = unread;
    }

    public int getUnreadChildren() {
        return unreadChildren;
    }

    public void setAuthor(DiscussionParticipant discussionParticipant) {
        author = discussionParticipant;
    }

    public DiscussionParticipant getAuthor() {
        return author;
    }

    public Date getCreatedAt() {
        return APIHelpers.stringToDate(created_at);
    }

    public void setCreatedAt(Date date) {
        created_at = APIHelpers.dateToString(date);
    }

    public Date getLastUpdated() {
        return APIHelpers.stringToDate(updated_at);
    }

    public void setLastUpdated(Date date) {
        updated_at = APIHelpers.dateToString(date);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        description = d;
    }

    public DiscussionEntry getParent() {
        return parent;
    }

    public void setParent(DiscussionEntry parent) {
        this.parent = parent;
    }

    public long getUserId() {
        return user_id;
    }

    public void setUserId(long user_id) {
        this.user_id = user_id;
    }

    public long getParentId() {
        return parent_id;
    }

    public void setParentId(long parent_id) {
        this.parent_id = parent_id;
    }

    public String getMessage(String localizedDeletedString) {
        if (message == null || message.equals("null")) {
            if (deleted)
                return localizedDeletedString;
            else
                return "";
        }
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void addReply (DiscussionEntry entry){

        if(this.replies == null) {
            this.replies = new ArrayList<DiscussionEntry>();
        }

        this.replies.add(entry);
    }


    public List<DiscussionEntry> getReplies() {
        return replies;
    }

    public void setReplies(List<DiscussionEntry> replies) {

        if(this.replies == null) {
            this.replies = new ArrayList<DiscussionEntry>();
        }

        this.replies = replies;
    }

    public int getRatingCount() {
        return rating_count;
    }

    public void setRatingCount(int rating_count) {
        this.rating_count = rating_count;
    }

    public int getRatingSum() {
        return rating_sum;
    }

    public void setRatingSum(int rating_sum) {
        this.rating_sum = rating_sum;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return getLastUpdated();
    }

    @Override
    public String getComparisonString() {
        return message;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DiscussionEntry() {
    }

    public void init(DiscussionTopic topic, DiscussionEntry parent) {
        this.parent = parent;


        HashMap<Long, DiscussionParticipant> participantHashMap = topic.getParticipantsMap();
        DiscussionParticipant discussionParticipant = participantHashMap.get(this.getUserId());
        if(discussionParticipant != null){
            author = discussionParticipant;
        }

        //Get whether or not the topic is unread;
        unread = topic.getUnread_entriesMap().containsKey(this.getId());

        for(DiscussionEntry reply : replies){
            reply.init(topic,this);

            //Handle total and unread children.
            unreadChildren += reply.getUnreadChildren();
            if (reply.isUnread())
                unreadChildren++;

            totalChildren++;
            totalChildren += reply.getTotalChildren();
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public int getDepth() {
        int depth = 0;
        DiscussionEntry temp = this;

        while (temp.getParent() != null) {
            depth++;
            temp = temp.getParent();
        }

        return depth;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeByte(unread ? (byte) 1 : (byte) 0);
        dest.writeString(this.updated_at);
        dest.writeString(this.created_at);
        //can't have a circular reference with parcelable, so it needs to be serializable
        dest.writeSerializable(this.parent);
        dest.writeParcelable(this.author, 0);
        dest.writeString(this.description);
        dest.writeLong(this.user_id);
        dest.writeLong(this.parent_id);
        dest.writeString(this.message);
        dest.writeByte(deleted ? (byte) 1 : (byte) 0);
        dest.writeInt(this.totalChildren);
        dest.writeInt(this.unreadChildren);
        //can't have a circular reference with parcelable, so it needs to be serializable
        dest.writeSerializable((Serializable)this.replies);
        dest.writeList(this.attachments);
        dest.writeInt(this.rating_count);
        dest.writeInt(this.rating_sum);
    }

    private DiscussionEntry(Parcel in) {
        this.id = in.readLong();
        this.unread = in.readByte() != 0;
        this.updated_at = in.readString();
        this.created_at = in.readString();
        this.parent = (DiscussionEntry)in.readSerializable();
        this.author = in.readParcelable(DiscussionParticipant.class.getClassLoader());
        this.description = in.readString();
        this.user_id = in.readLong();
        this.parent_id = in.readLong();
        this.message = in.readString();
        this.deleted = in.readByte() != 0;
        this.totalChildren = in.readInt();
        this.unreadChildren = in.readInt();
        this.replies = (List<DiscussionEntry>)in.readSerializable();
        in.readList(this.attachments, DiscussionAttachment.class.getClassLoader());
        this.rating_count = in.readInt();
        this.rating_sum = in.readInt();
    }

    public static Creator<DiscussionEntry> CREATOR = new Creator<DiscussionEntry>() {
        public DiscussionEntry createFromParcel(Parcel source) {
            return new DiscussionEntry(source);
        }

        public DiscussionEntry[] newArray(int size) {
            return new DiscussionEntry[size];
        }
    };
}
