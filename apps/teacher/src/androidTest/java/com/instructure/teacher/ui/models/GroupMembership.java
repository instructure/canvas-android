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

public class GroupMembership {
    public int id;
    public int groupId;
    public int userId;
    public String workflowState;
    public boolean moderator;

    public GroupMembership(int id, int groupId, int userId, String workflowState, boolean moderator) {
        this.id = id;
        this.groupId = groupId;
        this.userId = userId;
        this.workflowState = workflowState;
        this.moderator = moderator;
    }
}
