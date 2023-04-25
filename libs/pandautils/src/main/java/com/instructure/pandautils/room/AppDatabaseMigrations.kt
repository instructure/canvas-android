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

package com.instructure.pandautils.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

fun createMigration(from: Int, to: Int, migrationBlock: (SupportSQLiteDatabase) -> Unit): Migration {
    return object : Migration(from, to) {
        override fun migrate(database: SupportSQLiteDatabase) {
            migrationBlock(database)
        }
    }
}

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
