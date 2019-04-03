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

public class KalturaConfig implements Parcelable, Serializable {


    private String domain;
    private boolean enabled;
    private long partner_id;
    private String resource_domain;
    private String rtmp_domain;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getPartner_id() {
        return partner_id;
    }

    public void setPartner_id(long partner_id) {
        this.partner_id = partner_id;
    }

    public String getResource_domain() {
        return resource_domain;
    }

    public void setResource_domain(String resource_domain) {
        this.resource_domain = resource_domain;
    }

    public String getRtmp_domain() {
        return rtmp_domain;
    }

    public void setRtmp_domain(String rtmp_domain) {
        this.rtmp_domain = rtmp_domain;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.domain);
        dest.writeByte(enabled ? (byte) 1 : (byte) 0);
        dest.writeLong(this.partner_id);
        dest.writeString(this.resource_domain);
        dest.writeString(this.rtmp_domain);
    }

    public KalturaConfig() {
    }

    private KalturaConfig(Parcel in) {
        this.domain = in.readString();
        this.enabled = in.readByte() != 0;
        this.partner_id = in.readLong();
        this.resource_domain = in.readString();
        this.rtmp_domain = in.readString();
    }

    public static final Parcelable.Creator<KalturaConfig> CREATOR = new Parcelable.Creator<KalturaConfig>() {
        public KalturaConfig createFromParcel(Parcel source) {
            return new KalturaConfig(source);
        }

        public KalturaConfig[] newArray(int size) {
            return new KalturaConfig[size];
        }
    };
}
