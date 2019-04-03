/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi.model;

import com.instructure.canvasapi.utilities.NumberHelper;

import java.io.Serializable;


public class UnreadConversationCount implements Serializable {

    private static final long serialVersionUID = 1L;

    private String unread_count;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public String getUnreadCount() {
        int count;
        try {
            count = Integer.parseInt(unread_count);
            if (count < 0) count = 0;
        } catch (Exception e) {
            count = 0;
        }
        return NumberHelper.formatInt(count);
    }

    public void setUnreadCount(int int_unread_count) {
        unread_count =  Integer.toString(int_unread_count);
    }
}
