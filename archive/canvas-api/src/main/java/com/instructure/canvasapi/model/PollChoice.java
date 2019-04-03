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

public class PollChoice extends CanvasComparable<PollChoice> implements Parcelable {

    private long id;
    private boolean is_correct;
    private String text;
    private long poll_id;
    private int position;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean is_correct() {
        return is_correct;
    }

    public void setIs_correct(boolean is_correct) {
        this.is_correct = is_correct;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getPollId() {
        return poll_id;
    }

    public void setPollId(long poll_id) {
        this.poll_id = poll_id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    public Date getComparisonDate() { return null; }
    public String getComparisonString() { return null; }

    @Override
    public int compareTo(PollChoice pollChoice) {
        return CanvasComparable.compare(this.getPosition(),pollChoice.getPosition());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Parcelable
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeByte(is_correct ? (byte) 1 : (byte) 0);
        dest.writeString(this.text);
        dest.writeLong(this.poll_id);
        dest.writeInt(this.position);
    }

    public PollChoice() {
    }

    private PollChoice(Parcel in) {
        this.id = in.readLong();
        this.is_correct = in.readByte() != 0;
        this.text = in.readString();
        this.poll_id = in.readLong();
        this.position = in.readInt();
    }

    public static Creator<PollChoice> CREATOR = new Creator<PollChoice>() {
        public PollChoice createFromParcel(Parcel source) {
            return new PollChoice(source);
        }

        public PollChoice[] newArray(int size) {
            return new PollChoice[size];
        }
    };
}
