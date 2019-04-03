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

package com.instructure.pandautils.utils;

public class RequestCodes {
    public static final int NONE = 0;

    // Dashboard
    public final static int NOTIFICATIONS = 1000;
    public final static int TODO = 1001;
    public final static int MESSAGES = 1002;

    // Messages
    public final static int COMPOSE_MESSAGE = 2000;
    public final static int DETAILED_CONVERSATION = 2001;
    public final static int CHOOSE_RECIPIENTS = 2002;
    public final static int CHOOSE_RECIPIENTS_SELECT_CONTEXT = 2003;

    // Notorious Request
    public final static int NOTORIOUS_REQUEST = 3000;
    public final static int SELECT_MEDIA = 3001;
    public final static int TAKE_VIDEO = 3002;

    // Profile Fragment
    public final static int CAMERA_PIC_REQUEST = 4000;
    public final static int PICK_IMAGE_GALLERY = 4001;
    public final static int PROFILE_SETTINGS = 4002;
    public final static int CROP_IMAGE = 4003;

    public final static int SIGNED_IN = 5000;

    // Discussions--=--=---=-=
    public final static int COMPOSE_DISCUSSION = 6000;
    public final static int DETAILED_DISCUSSION = 6002;

    // Files
    public final static int PICK_FILE_FROM_DEVICE = 7000;
    public final static int SUBMIT_FILE_RESULT = 7001;
    public final static int ADD_SUBMISSION = 7002;
    public static final int FILES_LIST_FRAGMENT = 7003;

    //Canvas Context
    public final static int CANVAS_CONTEXT = 8000;

    //AssignmentEditing
    public final static int EDIT_ASSIGNMENT = 9000;

    //Assignment List
    public final static int ASSIGNMENT_FRAGMENT = 10000;
}
