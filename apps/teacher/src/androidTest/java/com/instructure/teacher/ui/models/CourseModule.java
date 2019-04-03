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

public class CourseModule {
    public int id;
    public ArrayList<CourseModuleItem> items;
    public String name;
    public int position;
    public ArrayList<Integer> prerequisiteModuleIds;
    public boolean publishFinalGrade;
    public boolean requireSequentialProgress;
    public String state;
    public Date unlockAt;
    public String workflowState;
    public boolean published;

    public CourseModule(int id, CourseModuleItem[] items, String name, int position, Integer[] prerequisiteModuleIds,
                        boolean publishFinalGrade, boolean requireSequentialProgress, String state, Date unlockAt,
                        String workflowState, boolean published) {
        this.id = id;
        this.items = new ArrayList<>(Arrays.asList(items));
        this.name = name;
        this.position = position;
        this.prerequisiteModuleIds = new ArrayList<>(Arrays.asList(prerequisiteModuleIds));
        this.publishFinalGrade = publishFinalGrade;
        this.requireSequentialProgress = requireSequentialProgress;
        this.state = state;
        this.unlockAt = unlockAt;
        this.workflowState = workflowState;
        this.published = published;
    }
}
