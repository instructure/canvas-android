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
import com.instructure.horizon.database.dao.HorizonDashboardEnrollmentDao
import com.instructure.horizon.database.dao.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.dao.HorizonDashboardProgramDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.HorizonDashboardEnrollmentEntity
import com.instructure.horizon.database.entity.HorizonDashboardModuleItemEntity
import com.instructure.horizon.database.entity.HorizonDashboardProgramCourseRef
import com.instructure.horizon.database.entity.HorizonDashboardProgramEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity

@TypeConverters(HorizonTypeConverters::class)
@Database(
    entities = [
        HorizonDashboardEnrollmentEntity::class,
        HorizonDashboardProgramEntity::class,
        HorizonDashboardProgramCourseRef::class,
        HorizonDashboardModuleItemEntity::class,
        HorizonSyncMetadataEntity::class,
    ],
    version = 2,
)
abstract class HorizonDatabase : RoomDatabase() {
    abstract fun dashboardEnrollmentDao(): HorizonDashboardEnrollmentDao
    abstract fun dashboardProgramDao(): HorizonDashboardProgramDao
    abstract fun dashboardModuleItemDao(): HorizonDashboardModuleItemDao
    abstract fun syncMetadataDao(): HorizonSyncMetadataDao
}
