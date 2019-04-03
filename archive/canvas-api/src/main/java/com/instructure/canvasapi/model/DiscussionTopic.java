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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DiscussionTopic implements Parcelable, Serializable {

    private static final long serialVersionUID = 1L;

	//The user can't see it unless they post a high level reply (requireinitialpost).
	private boolean forbidden = false;

	//List of all the ids of the unread discussion entries.
    private List<Long> unread_entries = new ArrayList<Long>();
	
	//List of the participants.
    private List<DiscussionParticipant> participants = new ArrayList<DiscussionParticipant>();
    private HashMap<Long, DiscussionParticipant> participantsMap = new HashMap<Long, DiscussionParticipant>();
    private HashMap<Long, Boolean> unread_entriesMap = new HashMap<Long, Boolean>();
    private HashMap<Long, Integer> entry_ratings = new HashMap<>();

    //List of all the discussion entries (views)
    private List<DiscussionEntry> view = new ArrayList<>();

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

	public boolean isForbidden() {
		return forbidden;
	}
	public void setForbidden(boolean forbidden) {
		this.forbidden = forbidden;
	}

    //This should only have to get built once.
    //    //MUCH faster for lookups.
    //So instead of n linear operations, we have 1 linear operations and (n-1) constant ones.
    public HashMap<Long,Boolean> getUnread_entriesMap(){
        if (unread_entries.size() != unread_entriesMap.size()) {
            for (Long unreadEntry : unread_entries) {
                unread_entriesMap.put(unreadEntry, true);
            }
        }
        return unread_entriesMap;
    }

    public List<Long> getUnreadEntries() {
		return unread_entries;
	}

    //This should only have to get built once.
    //MUCH faster for lookups.
    //So instead of n linear operations, we have 1 linear operations and (n-1) constant ones.
    public HashMap<Long,DiscussionParticipant> getParticipantsMap(){
        if(participantsMap == null || participantsMap.isEmpty()){
            participantsMap = new HashMap<Long, DiscussionParticipant>();
            if(participants != null){
                for(DiscussionParticipant discussionParticipant : participants){
                    participantsMap.put(discussionParticipant.getId(), discussionParticipant);
                }
            }
        }
        return participantsMap;
    }

    public void setUnreadEntries(List<Long> unread_entries) {
        this.unread_entries = unread_entries;
    }
    public List<DiscussionParticipant> getParticipants() {
        return participants;
    }
    public void setParticipants(List<DiscussionParticipant> participants) {
        this.participants = participants;
    }
	public List<DiscussionEntry> getViews() {
		return view;
	}

    public void setViews(List<DiscussionEntry> views) {
		this.view = views;
	}

    public HashMap<Long, Integer> getEntryRatings() {
        return entry_ratings;
    }

    public void setEntryRatings(HashMap<Long, Integer> entry_ratings) {
        this.entry_ratings = entry_ratings;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DiscussionTopic() {}


    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public static String getDiscussionURL(String api_protocol,String domain, long courseId, long topicId) {
        //https://mobiledev.instructure.com/api/v1/courses/24219/discussion_topics/1129998/
        return api_protocol + "://" + domain + "/courses/"+courseId+"/discussion_topics/"+topicId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(forbidden ? (byte) 1 : (byte) 0);
        dest.writeList(this.unread_entries);
        dest.writeList(this.participants);
        dest.writeSerializable(this.participantsMap);
        dest.writeSerializable(this.unread_entriesMap);
        dest.writeList(this.view);
        dest.writeSerializable(this.entry_ratings);
    }

    private DiscussionTopic(Parcel in) {
        this.forbidden = in.readByte() != 0;
        in.readList(this.unread_entries, Long.class.getClassLoader());
        in.readList(this.participants, DiscussionParticipant.class.getClassLoader());
        this.participantsMap = (HashMap<Long, DiscussionParticipant>)in.readSerializable();
        this.unread_entriesMap = (HashMap<Long, Boolean>)in.readSerializable();
        in.readList(this.view, DiscussionEntry.class.getClassLoader());
        this.entry_ratings = (HashMap<Long, Integer>)in.readSerializable();
    }

    public static Creator<DiscussionTopic> CREATOR = new Creator<DiscussionTopic>() {
        public DiscussionTopic createFromParcel(Parcel source) {
            return new DiscussionTopic(source);
        }

        public DiscussionTopic[] newArray(int size) {
            return new DiscussionTopic[size];
        }
    };
}
