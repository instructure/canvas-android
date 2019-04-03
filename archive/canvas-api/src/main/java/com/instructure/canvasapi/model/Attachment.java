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

public class Attachment extends CanvasModel<Attachment> {

    private long id;
    @SerializedName("content-type")
    private String content_type;
    private String filename;
    private String display_name;
    private String url;
    private String thumbnail_url;
    private String preview_url;
    private String created_at;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public long getId(){
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getDisplayName() {
        return display_name;
    }
    public String getMimeType() {
        return content_type;
    }
    public String getFilename() {
        return filename;
    }
    public String getUrl() {
        return url;
    }
    public String getThumbnailUrl() {
        return thumbnail_url;
    }
    public String getPreviewURL(){
        return preview_url;
    }
    public void setPreviewURL(String preview_url){
        this.preview_url = preview_url;
    }
    public Date getCreatedAt(){
        return APIHelpers.stringToDate(created_at);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Date getComparisonDate() {
        return getCreatedAt();
    }

    @Override
    public String getComparisonString() {
        return getDisplayName();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public Attachment() {}

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.display_name);
        dest.writeString(this.content_type);
        dest.writeString(this.filename);
        dest.writeString(this.url);
        dest.writeString(this.thumbnail_url);
        dest.writeString(this.preview_url);
        dest.writeString(this.created_at);
    }

    private Attachment(Parcel in) {
        this.id = in.readLong();
        this.display_name = in.readString();
        this.content_type = in.readString();
        this.filename = in.readString();
        this.url = in.readString();
        this.thumbnail_url = in.readString();
        this.preview_url = in.readString();
        this.created_at = in.readString();
    }

    public static Creator<Attachment> CREATOR = new Creator<Attachment>() {
        public Attachment createFromParcel(Parcel source) {
            return new Attachment(source);
        }

        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
}
