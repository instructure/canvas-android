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
package com.instructure.pandautils.utils

object RequestCodes {
    const val NONE = 0

    // Dashboard
    const val TODO = 1001

    // Messages
    const val COMPOSE_MESSAGE = 2000

    // Notorious Request
    const val SELECT_MEDIA = 3001
    const val TAKE_VIDEO = 3002

    // Profile Fragment
    const val CAMERA_PIC_REQUEST = 4000
    const val PICK_IMAGE_GALLERY = 4001
    const val PICK_FILE_FROM_DEVICE = 7000
    const val CROP_IMAGE = 4003

    // Canvas Context
    const val CANVAS_CONTEXT = 8000
}
