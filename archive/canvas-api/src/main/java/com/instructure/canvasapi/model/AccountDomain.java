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



public class AccountDomain extends CanvasModel<AccountDomain> {

    private String domain;
    private String name;
    private Double distance;

    public String getDomain() {
        return domain;
    }

    public String getName() {
        return name;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return domain;
    }

    @Override
    public int compareTo(AccountDomain another) {
        return this.getDistance().compareTo(another.getDistance());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.domain);
        dest.writeString(this.name);
        dest.writeDouble(this.distance);
    }

    public AccountDomain() {
    }

    private AccountDomain(Parcel in) {
        this.domain = in.readString();
        this.name = in.readString();
        this.distance = in.readDouble();
    }

    public static final Creator<AccountDomain> CREATOR = new Creator<AccountDomain>() {
        public AccountDomain createFromParcel(Parcel source) {
            return new AccountDomain(source);
        }

        public AccountDomain[] newArray(int size) {
            return new AccountDomain[size];
        }
    };
}
