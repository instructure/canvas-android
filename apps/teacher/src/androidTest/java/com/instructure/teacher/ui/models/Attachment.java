/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* This is an auto-generated file. */

package com.instructure.teacher.ui.models;

import java.util.Date;

public class Attachment {
    public int id;
    public int folderId;
    public String displayName;
    public String filename;
    public String contentType;
    public String url;
    public int size;
    public Date createdAt;
    public Date updatedAt;
    public Date unlockAt;
    public boolean locked;
    public boolean hidden;
    public Date lockAt;
    public String thumbnailUrl;
    public Date modifiedAt;
    public String mimeClass;
    public int mediaEntryId;
    public String previewUrl;

    public Attachment(int id, int folderId, String displayName, String filename, String contentType, String url, int size,
                      Date createdAt, Date updatedAt, Date unlockAt, boolean locked, boolean hidden, Date lockAt,
                      String thumbnailUrl, Date modifiedAt, String mimeClass, int mediaEntryId, String previewUrl) {
        this.id = id;
        this.folderId = folderId;
        this.displayName = displayName;
        this.filename = filename;
        this.contentType = contentType;
        this.url = url;
        this.size = size;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.unlockAt = unlockAt;
        this.locked = locked;
        this.hidden = hidden;
        this.lockAt = lockAt;
        this.thumbnailUrl = thumbnailUrl;
        this.modifiedAt = modifiedAt;
        this.mimeClass = mimeClass;
        this.mediaEntryId = mediaEntryId;
        this.previewUrl = previewUrl;
    }
}
