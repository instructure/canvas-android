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

public class Section {
    public int id;
    public String name;
    public int courseId;
    public Date startAt;
    public Date endAt;
    public int totalStudents;

    public Section(int id, String name, int courseId, Date startAt, Date endAt, int totalStudents) {
        this.id = id;
        this.name = name;
        this.courseId = courseId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.totalStudents = totalStudents;
    }
}
