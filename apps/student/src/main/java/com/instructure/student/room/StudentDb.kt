/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.pandautils.room.common.Converters
import com.instructure.student.room.entities.CreateSubmissionEntity
import com.instructure.student.room.entities.CreateFileSubmissionEntity
import com.instructure.student.room.entities.CreatePendingSubmissionCommentEntity
import com.instructure.student.room.entities.CreateSubmissionCommentFileEntity
import com.instructure.student.room.entities.daos.CreateSubmissionDao
import com.instructure.student.room.entities.daos.CreateFileSubmissionDao
import com.instructure.student.room.entities.daos.CreatePendingSubmissionCommentDao
import com.instructure.student.room.entities.daos.CreateSubmissionCommentFileDao

@Database(
    version = 5,
    entities = [
        CreateSubmissionEntity::class,
        CreatePendingSubmissionCommentEntity::class,
        CreateFileSubmissionEntity::class,
        CreateSubmissionCommentFileEntity::class
    ],
)
@TypeConverters(Converters::class)
abstract class StudentDb : RoomDatabase() {

    abstract fun submissionDao(): CreateSubmissionDao

    abstract fun pendingSubmissionCommentDao(): CreatePendingSubmissionCommentDao

    abstract fun fileSubmissionDao(): CreateFileSubmissionDao

    abstract fun submissionCommentFileDao(): CreateSubmissionCommentFileDao
}