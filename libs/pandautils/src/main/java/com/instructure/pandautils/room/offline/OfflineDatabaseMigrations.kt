/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.room.offline

import com.instructure.pandautils.room.createMigration

val offlineDatabaseMigrations = arrayOf(

    createMigration(1, 2) { _db ->
        _db.execSQL("CREATE TABLE IF NOT EXISTS `CourseSyncSettingsEntity` (`courseId` INTEGER NOT NULL, `assignments` INTEGER NOT NULL, `pages` INTEGER NOT NULL, `grades` INTEGER NOT NULL, PRIMARY KEY(`courseId`))")
        _db.execSQL("CREATE TABLE IF NOT EXISTS `CourseFilesEntity` (`courseId` INTEGER NOT NULL, `url` TEXT NOT NULL, PRIMARY KEY(`courseId`, `url`), FOREIGN KEY(`courseId`) REFERENCES `CourseSyncSettingsEntity`(`courseId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
    }
)