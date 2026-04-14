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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.instructure.horizon.database.dao.HorizonAssignmentCommentDao
import com.instructure.horizon.database.dao.HorizonAssignmentDetailsDao
import com.instructure.horizon.database.dao.HorizonSubmissionDao
import com.instructure.horizon.database.dao.HorizonCourseDao
import com.instructure.horizon.database.dao.HorizonCourseModuleDao
import com.instructure.horizon.database.dao.HorizonCourseScoreDao
import com.instructure.horizon.database.dao.HorizonDashboardEnrollmentDao
import com.instructure.horizon.database.dao.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.dao.HorizonFileFolderDao
import com.instructure.horizon.database.dao.HorizonLearnCollectionDao
import com.instructure.horizon.database.dao.HorizonLearnItemDao
import com.instructure.horizon.database.dao.HorizonLearnSavedItemDao
import com.instructure.horizon.database.dao.HorizonLocalFileDao
import com.instructure.horizon.database.dao.HorizonPageDao
import com.instructure.horizon.database.dao.HorizonProgramDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.HorizonAssignmentCommentAttachmentEntity
import com.instructure.horizon.database.entity.HorizonAssignmentCommentEntity
import com.instructure.horizon.database.entity.HorizonAssignmentDetailsEntity
import com.instructure.horizon.database.entity.HorizonSubmissionAttachmentEntity
import com.instructure.horizon.database.entity.HorizonSubmissionEntity
import com.instructure.horizon.database.entity.HorizonCourseAssignmentEntity
import com.instructure.horizon.database.entity.HorizonCourseAssignmentGroupEntity
import com.instructure.horizon.database.entity.HorizonCourseEntity
import com.instructure.horizon.database.entity.HorizonCourseGradeEntity
import com.instructure.horizon.database.entity.HorizonCourseModuleEntity
import com.instructure.horizon.database.entity.HorizonCourseModuleItemEntity
import com.instructure.horizon.database.entity.HorizonDashboardEnrollmentEntity
import com.instructure.horizon.database.entity.HorizonDashboardModuleItemEntity
import com.instructure.horizon.database.entity.HorizonFileFolderEntity
import com.instructure.horizon.database.entity.HorizonLearnCollectionEntity
import com.instructure.horizon.database.entity.HorizonLearnCollectionItemEntity
import com.instructure.horizon.database.entity.HorizonLearnItemEntity
import com.instructure.horizon.database.entity.HorizonLearnSavedItemEntity
import com.instructure.horizon.database.entity.HorizonLocalFileEntity
import com.instructure.horizon.database.entity.HorizonPageEntity
import com.instructure.horizon.database.entity.HorizonProgramCourseRef
import com.instructure.horizon.database.entity.HorizonProgramEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity

@TypeConverters(HorizonTypeConverters::class)
@Database(
    entities = [
        HorizonDashboardEnrollmentEntity::class,
        HorizonProgramEntity::class,
        HorizonProgramCourseRef::class,
        HorizonDashboardModuleItemEntity::class,
        HorizonSyncMetadataEntity::class,
        HorizonLearnItemEntity::class,
        HorizonLearnCollectionEntity::class,
        HorizonLearnCollectionItemEntity::class,
        HorizonLearnSavedItemEntity::class,
        HorizonCourseEntity::class,
        HorizonCourseModuleEntity::class,
        HorizonCourseModuleItemEntity::class,
        HorizonCourseAssignmentGroupEntity::class,
        HorizonCourseAssignmentEntity::class,
        HorizonCourseGradeEntity::class,
        HorizonLocalFileEntity::class,
        HorizonFileFolderEntity::class,
        HorizonPageEntity::class,
        HorizonAssignmentDetailsEntity::class,
        HorizonAssignmentCommentEntity::class,
        HorizonAssignmentCommentAttachmentEntity::class,
        HorizonSubmissionEntity::class,
        HorizonSubmissionAttachmentEntity::class,
    ],
    version = 13,
)
abstract class HorizonDatabase : RoomDatabase() {
    abstract fun dashboardEnrollmentDao(): HorizonDashboardEnrollmentDao
    abstract fun programDao(): HorizonProgramDao
    abstract fun dashboardModuleItemDao(): HorizonDashboardModuleItemDao
    abstract fun syncMetadataDao(): HorizonSyncMetadataDao
    abstract fun learnItemDao(): HorizonLearnItemDao
    abstract fun learnCollectionDao(): HorizonLearnCollectionDao
    abstract fun learnSavedItemDao(): HorizonLearnSavedItemDao
    abstract fun courseDao(): HorizonCourseDao
    abstract fun courseModuleDao(): HorizonCourseModuleDao
    abstract fun courseScoreDao(): HorizonCourseScoreDao
    abstract fun localFileDao(): HorizonLocalFileDao
    abstract fun fileFolderDao(): HorizonFileFolderDao
    abstract fun pageDao(): HorizonPageDao
    abstract fun assignmentDetailsDao(): HorizonAssignmentDetailsDao
    abstract fun assignmentCommentDao(): HorizonAssignmentCommentDao
    abstract fun submissionDao(): HorizonSubmissionDao
}
