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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class AssignmentOverride {
    public int id;
    public int assignmentId;
    public ArrayList<Integer> studentIds;
    public int groupId;
    public int courseSectionId;
    public String title;
    public Date dueAt;
    public Date unlockAt;
    public Date lockAt;
    public Boolean allDay;

    public AssignmentOverride(int id, int assignmentId, Integer[] studentIds, int groupId, int courseSectionId,
                              String title, Date dueAt, Date unlockAt, Date lockAt, Boolean allDay) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.studentIds = new ArrayList<>(Arrays.asList(studentIds));
        this.groupId = groupId;
        this.courseSectionId = courseSectionId;
        this.title = title;
        this.dueAt = dueAt;
        this.unlockAt = unlockAt;
        this.lockAt = lockAt;
        this.allDay = allDay;
    }
}
