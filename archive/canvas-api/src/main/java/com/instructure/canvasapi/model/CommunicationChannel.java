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

public class CommunicationChannel extends CanvasModel<CommunicationChannel> {

    public long id;
    public String address;
    public String type;
    public long position;
    public long user_id;
    public String workflow_state;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return null;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getUserId() {
        return user_id;
    }

    public void setUserId(long user_id) {
        this.user_id = user_id;
    }

    public String getWorkflowState() {
        return workflow_state;
    }

    public void setWorkflowState(String workflow_state) {
        this.workflow_state = workflow_state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.address);
        dest.writeString(this.type);
        dest.writeLong(this.position);
        dest.writeLong(this.user_id);
        dest.writeString(this.workflow_state);
    }

    public CommunicationChannel() {
    }

    private CommunicationChannel(Parcel in) {
        this.id = in.readLong();
        this.address = in.readString();
        this.type = in.readString();
        this.position = in.readLong();
        this.user_id = in.readLong();
        this.workflow_state = in.readString();
    }

    public static final Creator<CommunicationChannel> CREATOR = new Creator<CommunicationChannel>() {
        public CommunicationChannel createFromParcel(Parcel source) {
            return new CommunicationChannel(source);
        }

        public CommunicationChannel[] newArray(int size) {
            return new CommunicationChannel[size];
        }
    };
}
