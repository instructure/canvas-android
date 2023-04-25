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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.pandautils.room.Converters
import com.instructure.pandautils.room.appdatabase.daos.*
import com.instructure.pandautils.room.appdatabase.entities.*

@Database(
    entities = [
        AttachmentEntity::class,
        AuthorEntity::class,
        DashboardFileUploadEntity::class,
        FileUploadInputEntity::class,
        MediaCommentEntity::class,
        PendingSubmissionCommentEntity::class,
        SubmissionCommentEntity::class,
    ], version = 4
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun attachmentDao(): AttachmentDao

    abstract fun authorDao(): AuthorDao

    abstract fun fileUploadInputDao(): FileUploadInputDao

    abstract fun mediaCommentDao(): MediaCommentDao

    abstract fun submissionCommentDao(): SubmissionCommentDao

    abstract fun pendingSubmissionCommentDao(): PendingSubmissionCommentDao

    abstract fun dashboardFileUploadDao(): DashboardFileUploadDao
}