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

public class ConversationMessage {
    public int id;
    public int authorId;
    public Date createdAt;
    public boolean generated;
    public String body;
    public ArrayList<Integer> participatingUserIds;

    public ConversationMessage(int id, int authorId, Date createdAt, boolean generated, String body,
                               Integer[] participatingUserIds) {
        this.id = id;
        this.authorId = authorId;
        this.createdAt = createdAt;
        this.generated = generated;
        this.body = body;
        this.participatingUserIds = new ArrayList<>(Arrays.asList(participatingUserIds));
    }
}
