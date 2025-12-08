/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.pandautils.room.appdatabase

import com.instructure.pandautils.room.common.createMigration

val appDatabaseMigrations = arrayOf(

    createMigration(1, 2) { database ->
        database.execSQL("ALTER TABLE PendingSubmissionCommentEntity ADD COLUMN attemptId INTEGER")
        database.execSQL("ALTER TABLE FileUploadInputEntity ADD COLUMN attemptId INTEGER")
        database.execSQL("ALTER TABLE SubmissionCommentEntity ADD COLUMN attemptId INTEGER")
    },

    createMigration(2, 3) { database ->
        database.execSQL("CREATE TABLE IF NOT EXISTS DashboardFileUploadEntity (workerId TEXT NOT NULL, userId INTEGER NOT NULL, title TEXT, subtitle TEXT, PRIMARY KEY(workerId))")
    },

    createMigration(3, 4) { database ->
        database.execSQL("ALTER TABLE FileUploadInputEntity ADD COLUMN notificationId INTEGER")
    },

    createMigration(4, 5) { database ->
        database.execSQL("ALTER TABLE DashboardFileUploadEntity ADD COLUMN courseId INTEGER")
        database.execSQL("ALTER TABLE DashboardFileUploadEntity ADD COLUMN assignmentId INTEGER")
        database.execSQL("ALTER TABLE DashboardFileUploadEntity ADD COLUMN attemptId INTEGER")
        database.execSQL("ALTER TABLE DashboardFileUploadEntity ADD COLUMN folderId INTEGER")
    },

    createMigration(5, 6) { database ->
        database.execSQL("ALTER TABLE AttachmentEntity ADD COLUMN submissionId INTEGER")
        database.execSQL("ALTER TABLE SubmissionCommentEntity ADD COLUMN submissionId INTEGER")
    },

    createMigration(6, 7) { database ->
        database.execSQL("ALTER TABLE AttachmentEntity ADD COLUMN attempt INTEGER")
    },

    createMigration(7, 8) { database ->
        database.execSQL("CREATE TABLE IF NOT EXISTS EnvironmentFeatureFlags (userId INTEGER NOT NULL, featureFlags TEXT NOT NULL, PRIMARY KEY(userId))")
    },

    createMigration(8, 9) { database ->
        database.execSQL("CREATE TABLE IF NOT EXISTS ReminderEntity (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userId INTEGER NOT NULL, assignmentId INTEGER NOT NULL, htmlUrl TEXT NOT NULL, name TEXT NOT NULL, text TEXT NOT NULL, time INTEGER NOT NULL)")
    },

    createMigration(9, 10) { database ->
        database.execSQL("CREATE TABLE IF NOT EXISTS `ModuleBulkProgressEntity` (`progressId` INTEGER NOT NULL, `allModules` INTEGER NOT NULL, `skipContentTags` INTEGER NOT NULL, `action` TEXT NOT NULL, `courseId` INTEGER NOT NULL, `affectedIds` TEXT NOT NULL, PRIMARY KEY(`progressId`))")
    },

    createMigration(10, 11) { database ->
        database.execSQL("CREATE TABLE IF NOT EXISTS assignment_filter (selectedAssignmentFilters TEXT NOT NULL, selectedAssignmentStatusFilter TEXT, selectedGroupByOption TEXT NOT NULL, contextId INTEGER NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userDomain TEXT NOT NULL, userId INTEGER NOT NULL)")
    },

    createMigration(11, 12) { database ->
        database.execSQL("CREATE TABLE IF NOT EXISTS FileDownloadProgressEntity (workerId TEXT NOT NULL, fileName TEXT NOT NULL, progress INTEGER NOT NULL, progressState TEXT NOT NULL, filePath TEXT NOT NULL, PRIMARY KEY(workerId))")
    },

    createMigration(12, 13) { database ->
        database.execSQL("ALTER TABLE ReminderEntity ADD COLUMN tag TEXT")
    },

    createMigration(13, 14) { database ->
        database.execSQL("CREATE TABLE IF NOT EXISTS todo_filter (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, userDomain TEXT NOT NULL, userId INTEGER NOT NULL, personalTodos INTEGER NOT NULL DEFAULT 0, calendarEvents INTEGER NOT NULL DEFAULT 0, showCompleted INTEGER NOT NULL DEFAULT 0, favoriteCourses INTEGER NOT NULL DEFAULT 0, pastDateRange TEXT NOT NULL DEFAULT 'ONE_WEEK', futureDateRange TEXT NOT NULL DEFAULT 'ONE_WEEK')")
    },
)
