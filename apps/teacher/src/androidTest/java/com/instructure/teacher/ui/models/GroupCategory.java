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

public class GroupCategory {
    public int id;
    public String name;
    public String role;
    public boolean selfSignup;
    public boolean autoLeader;
    public String contextType;
    public int groupLimit;

    public GroupCategory(int id, String name, String role, boolean selfSignup, boolean autoLeader, String contextType, int groupLimit) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.selfSignup = selfSignup;
        this.autoLeader = autoLeader;
        this.contextType = contextType;
        this.groupLimit = groupLimit;
    }
}
