/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.loginapi.login.api.zendesk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ZendeskTicket implements Parcelable, Serializable{

    private ZendeskTicketData ticket;

    public ZendeskTicketData getTicket() {
        return ticket;
    }
    public void setTicket(ZendeskTicketData ticket) {
        this.ticket = ticket;
    }

    public ZendeskTicket(){}
    public ZendeskTicket(Parcel in){
        this.ticket = in.readParcelable(ZendeskTicketData.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(ticket, flags);
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new ZendeskTicket(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new ZendeskTicket[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
