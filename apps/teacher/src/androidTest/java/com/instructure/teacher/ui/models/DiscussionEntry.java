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

public class DiscussionEntry {
    public int id;
    public int parentId;
    public Date createdAt;
    public Date updatedAt;
    public String message;
    public int userId;
    public String userName;
    public String userDisplayName;
    public String readState;
    public boolean forcedReadState;

    public DiscussionEntry(int id, int parentId, Date createdAt, Date updatedAt, String message, int userId,
                           String userName, String userDisplayName, String readState, boolean forcedReadState) {
        this.id = id;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.message = message;
        this.userId = userId;
        this.userName = userName;
        this.userDisplayName = userDisplayName;
        this.readState = readState;
        this.forcedReadState = forcedReadState;
    }
}
