/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.room.studentdb.entities

/**
 * State transitions:
 * QUEUED -> UPLOADING_FILES -> SUBMITTING -> VERIFYING -> COMPLETED
 *                  |               |             |
 *                  v               v             v
 *              RETRYING -----> RETRYING ----> RETRYING
 *                  |               |             |
 *                  v               v             v
 *              FAILED          FAILED        FAILED
 */
enum class SubmissionState {
    QUEUED,
    UPLOADING_FILES,
    SUBMITTING,
    VERIFYING,
    COMPLETED,
    FAILED,
    RETRYING;

    val isActive: Boolean
        get() = this in listOf(QUEUED, UPLOADING_FILES, SUBMITTING, VERIFYING, RETRYING)

    val isTerminal: Boolean
        get() = this in listOf(COMPLETED, FAILED)

    val isError: Boolean
        get() = this in listOf(FAILED, RETRYING)
}