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

public class Group {
    public String avatarUrl;
    public String contextType;
    public int courseId;
    public String description;
    public int groupCategoryId;
    public int id;
    public boolean isPublic;
    public String joinLevel;
    public int membersCount;
    public String name;
    public String role;

    public Group(String avatarUrl, String contextType, int courseId, String description, int groupCategoryId, int id,
                 boolean isPublic, String joinLevel, int membersCount, String name, String role) {
        this.avatarUrl = avatarUrl;
        this.contextType = contextType;
        this.courseId = courseId;
        this.description = description;
        this.groupCategoryId = groupCategoryId;
        this.id = id;
        this.isPublic = isPublic;
        this.joinLevel = joinLevel;
        this.membersCount = membersCount;
        this.name = name;
        this.role = role;
    }
}
