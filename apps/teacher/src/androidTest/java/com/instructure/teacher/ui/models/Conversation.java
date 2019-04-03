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

public class Conversation {
    public static final String READ = "read";
    public static final String UNREAD = "unread";
    public static final String STARRED = "starred";
    public static final String ARCHIVED = "archived";

    public int id;
    public String subject;
    public String workflowState;
    public String lastMessage;
    public Date lastMessageAt;
    public String lastAuthoredMessage;
    public Date lastAuthoredMessageAt;
    public int messageCount;
    public boolean isPrivate;
    public boolean starred;
    public ArrayList<ConversationMessage> messages;
    public ArrayList<Integer> participatingUserIds;
    public String contextCode;

    public Conversation(int id, String subject, String workflowState, String lastMessage, Date lastMessageAt,
                        String lastAuthoredMessage, Date lastAuthoredMessageAt, int messageCount, boolean isPrivate,
                        boolean starred, ConversationMessage[] messages, Integer[] participatingUserIds, String contextCode) {
        this.id = id;
        this.subject = subject;
        this.workflowState = workflowState;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.lastAuthoredMessage = lastAuthoredMessage;
        this.lastAuthoredMessageAt = lastAuthoredMessageAt;
        this.messageCount = messageCount;
        this.isPrivate = isPrivate;
        this.starred = starred;
        this.messages = new ArrayList<>(Arrays.asList(messages));
        this.participatingUserIds = new ArrayList<>(Arrays.asList(participatingUserIds));
        this.contextCode = contextCode;
    }
}
