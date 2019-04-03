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

public class CourseModuleItem {
    public static final String ASSIGNMENT = "Assignment";
    public static final String DISCUSSION = "Discussion";
    public static final String PAGE = "Page";
    public static final String QUIZ = "Quiz";

    public int contentId;
    public String externalUrl;
    public String htmlUrl;
    public int id;
    public int indent;
    public int moduleId;
    public boolean newTab;
    public String pageUrl;
    public int position;
    public String title;
    public String type;
    public String url;

    public CourseModuleItem(int contentId, String externalUrl, String htmlUrl, int id, int indent, int moduleId,
                            boolean newTab, String pageUrl, int position, String title, String type, String url) {
        this.contentId = contentId;
        this.externalUrl = externalUrl;
        this.htmlUrl = htmlUrl;
        this.id = id;
        this.indent = indent;
        this.moduleId = moduleId;
        this.newTab = newTab;
        this.pageUrl = pageUrl;
        this.position = position;
        this.title = title;
        this.type = type;
        this.url = url;
    }
}
