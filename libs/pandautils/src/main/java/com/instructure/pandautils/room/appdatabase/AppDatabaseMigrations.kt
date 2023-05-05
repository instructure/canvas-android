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

package com.instructure.pandautils.room.appdatabase

import com.instructure.pandautils.room.createMigration

val appDatabaseMigrations = arrayOf(

    createMigration(1, 2) { database ->
        database.execSQL("ALTER TABLE PendingSubmissionCommentEntity ADD COLUMN attemptId INTEGER")
        database.execSQL("ALTER TABLE FileUploadInputEntity ADD COLUMN attemptId INTEGER")
        database.execSQL("ALTER TABLE SubmissionCommentEntity ADD COLUMN attemptId INTEGER")
    },

    createMigration(2, 3) { database ->
        database.execSQL("CREATE TABLE IF NOT EXISTS DashboardFileUploadEntity (workerId TEXT NOT NULL, userId INTEGER NOT NULL, title TEXT, assignmentName TEXT, PRIMARY KEY(workerId))")
    },

    createMigration(3, 4) { database ->
        database.execSQL("ALTER TABLE FileUploadInputEntity ADD COLUMN notificationId INTEGER")
    },

    createMigration(4, 5) {database ->
        database.execSQL("ALTER TABLE DashboardFileUploadEntity ADD COLUMN courseId INTEGER")
        database.execSQL("ALTER TABLE DashboardFileUploadEntity ADD COLUMN assignmentId INTEGER")
        database.execSQL("ALTER TABLE DashboardFileUploadEntity ADD COLUMN attemptId INTEGER")
        database.execSQL("ALTER TABLE DashboardFileUploadEntity ADD COLUMN folderId INTEGER")
    }

)

