/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.database

import com.instructure.pandautils.room.common.createMigration

val horizonDatabaseMigrations = arrayOf(
    createMigration(1, 2) { database ->
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `pending_actions` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`actionType` TEXT NOT NULL, " +
                "`courseId` INTEGER, " +
                "`payload` TEXT NOT NULL, " +
                "`retryCount` INTEGER NOT NULL, " +
                "`lastError` TEXT, " +
                "`createdAt` INTEGER NOT NULL)"
        )
        database.execSQL(
            "ALTER TABLE `horizon_assignment_details` ADD COLUMN `allowedExtensions` TEXT NOT NULL DEFAULT ''"
        )
    },
)
