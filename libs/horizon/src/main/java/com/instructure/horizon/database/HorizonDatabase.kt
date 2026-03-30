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
import com.instructure.horizon.database.course.HorizonDashboardCourseDao
import com.instructure.horizon.database.course.HorizonDashboardCourseEntity
import com.instructure.horizon.database.moduleitem.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.moduleitem.HorizonDashboardModuleItemEntity
import com.instructure.horizon.database.program.HorizonDashboardProgramCourseRef
import com.instructure.horizon.database.program.HorizonDashboardProgramDao
import com.instructure.horizon.database.program.HorizonDashboardProgramEntity
import com.instructure.horizon.database.sync.HorizonSyncMetadataDao
import com.instructure.horizon.database.sync.HorizonSyncMetadataEntity

@Database(
    entities = [
        HorizonDashboardCourseEntity::class,
        HorizonDashboardProgramEntity::class,
        HorizonDashboardProgramCourseRef::class,
        HorizonDashboardModuleItemEntity::class,
        HorizonSyncMetadataEntity::class,
    ],
    version = 1,
)
abstract class HorizonDatabase : RoomDatabase() {
    abstract fun dashboardCourseDao(): HorizonDashboardCourseDao
    abstract fun dashboardProgramDao(): HorizonDashboardProgramDao
    abstract fun dashboardModuleItemDao(): HorizonDashboardModuleItemDao
    abstract fun syncMetadataDao(): HorizonSyncMetadataDao
}
