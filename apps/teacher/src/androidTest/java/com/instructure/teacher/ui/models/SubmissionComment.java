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

public class SubmissionComment {
    public int id;
    public int authorId;
    public String authorName;
    public String comment;
    public Date createdAt;
    public ArrayList<Attachment> attachments;

    public SubmissionComment(int id, int authorId, String authorName, String comment, Date createdAt, Attachment[] attachments) {
        this.id = id;
        this.authorId = authorId;
        this.authorName = authorName;
        this.comment = comment;
        this.createdAt = createdAt;
        this.attachments = new ArrayList<>(Arrays.asList(attachments));
    }
}
