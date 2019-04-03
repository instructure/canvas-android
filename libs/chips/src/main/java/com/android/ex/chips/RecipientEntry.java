/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ex.chips;

import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;

import java.io.Serializable;
import java.util.Set;

/**
 * Represents one entry inside recipient auto-complete list.
 */
public class RecipientEntry implements Serializable{

    static final int INVALID_CONTACT = -1;
    static final int GENERATED_CONTACT = -2;

    private long id;
    private String destination; // Holds id string value returned by api
    private final String name;
    private final String info;

    private final String avatarUrl;
    private final int userCount;
    private final int itemCount;

    private boolean isValid;

    private Set<String> mCourses;
    private Set<String> mGroups;

    /**
     * This can be updated after this object being constructed, when the photo is fetched
     * from remote directories.
     */
    private transient byte[] mPhotoBytes;

    public RecipientEntry(long id, String name, String destination, String info, String avatarUrl, int userCount, int itemCount, boolean isValid, Set<String> courses, Set<String> groups) {
        this.id          = id;
        this.name        = name;
        this.destination = destination;
        this.avatarUrl   = avatarUrl;
        this.userCount   = userCount;
        this.itemCount   = itemCount;
        this.info        = info;
        this.isValid     = isValid;
        mCourses = courses;
        mGroups = groups;
    }

    /**
     * Construct a RecipientEntry from just an address that has been entered.
     * This address has not been resolved to a contact and therefore does not
     * have a contact id or photo.
     */
    public static RecipientEntry constructFakeEntry(final String address, boolean isValid) {
        final Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(address);
        final String tokenizedAddress = tokens.length > 0 ? tokens[0].getAddress() : address;

        return new RecipientEntry(INVALID_CONTACT, address, tokenizedAddress,
                 "", null, 0, 0, isValid, null, null);
    }

    public static RecipientEntry constructFakeEntry(final String name, final String address, boolean isValid) {

        return new RecipientEntry(GENERATED_CONTACT,name, address,"", null, 0, 0, isValid, null, null);
    }

    public boolean isValid() {
        return isValid;
    }

    public long getId() {
        return this.id;
    }

    public String getDestination(){
        return this.destination;
    }

    public String getName() {
        return this.name;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public String getInfo(){
        return this.info;
    }

    public int getUserCount(){
        return this.userCount;
    }

    public int getItemCount(){
        return this.itemCount;
    }

    public boolean isInCourseOrGroup(long id) {
        return (mCourses != null && mCourses.contains("" + id)) || (mGroups != null && mGroups.contains("" + id));
    }

    /**
     * Determine if this was a RecipientEntry created from recipient info or
     * an entry from contacts.
     */
    public static boolean isCreatedRecipient(long id) {
        return id == RecipientEntry.INVALID_CONTACT || id == RecipientEntry.GENERATED_CONTACT;
    }

    /** This can be called outside main Looper thread. */
    public synchronized void setPhotoBytes(byte[] photoBytes) {
        mPhotoBytes = photoBytes;
    }

    /** This can be called outside main Looper thread. */
    public synchronized byte[] getPhotoBytes() {
        return mPhotoBytes;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof RecipientEntry){
            return  this.getId() == ((RecipientEntry) o).getId();
        }
        return false;
    }
}