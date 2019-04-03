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

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.Date;

public class AccountNotification extends CanvasModel<AccountNotification> {

    public static final String ACCOUNT_NOTIFICATION_WARNING = "warning";
    public static final String ACCOUNT_NOTIFICATION_INFORMATION = "information";
    public static final String ACCOUNT_NOTIFICATION_QUESTION = "question";
    public static final String ACCOUNT_NOTIFICATION_ERROR = "error";
    public static final String ACCOUNT_NOTIFICATION_CALENDAR = "calendar";



    @SerializedName("id")
    private long account_notification_id;
    private String subject;
    private String message;

    private String start_at;
    private String end_at;

    private String icon;

    public long getAccountNotificationId() {
        return account_notification_id;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getIcon() {
        return icon;
    }

    public Date getStartDate() {
        if(start_at == null) {
            return null;
        }
        return APIHelpers.stringToDate(start_at);
    }

    public Date getEndDate() {
        if(end_at == null) {
            return null;
        }
        return APIHelpers.stringToDate(end_at);
    }

    @Override
    public String getComparisonString() {
        return subject;
    }

    @Override
    public Date getComparisonDate() {
        return getStartDate();
    }

    @Override
    public long getId() {
        return account_notification_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.account_notification_id);
        dest.writeString(this.subject);
        dest.writeString(this.message);
        dest.writeString(this.start_at);
        dest.writeString(this.end_at);
        dest.writeString(this.icon);
    }

    public AccountNotification() {
    }

    private AccountNotification(Parcel in) {
        this.account_notification_id = in.readLong();
        this.subject = in.readString();
        this.message = in.readString();
        this.start_at = in.readString();
        this.end_at = in.readString();
        this.icon = in.readString();
    }

    public static final Creator<AccountNotification> CREATOR = new Creator<AccountNotification>() {
        public AccountNotification createFromParcel(Parcel source) {
            return new AccountNotification(source);
        }

        public AccountNotification[] newArray(int size) {
            return new AccountNotification[size];
        }
    };
}
