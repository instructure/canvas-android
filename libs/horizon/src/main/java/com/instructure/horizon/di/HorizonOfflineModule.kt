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
package com.instructure.horizon.di

import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.database.HorizonDatabase
import com.instructure.horizon.database.HorizonDatabaseProvider
import com.instructure.horizon.database.dao.HorizonDashboardEnrollmentDao
import com.instructure.horizon.database.dao.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.dao.HorizonDashboardProgramDao
import com.instructure.horizon.database.dao.HorizonLearnCollectionDao
import com.instructure.horizon.database.dao.HorizonLearnItemDao
import com.instructure.horizon.database.dao.HorizonLearnSavedItemDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class HorizonOfflineModule {

    @Provides
    fun provideHorizonDatabase(
        provider: HorizonDatabaseProvider,
        apiPrefs: ApiPrefs,
    ): HorizonDatabase {
        val userId = apiPrefs.user?.id ?: -1L
        return provider.getDatabase(userId)
    }

    @Provides
    fun provideHorizonDashboardEnrollmentDao(db: HorizonDatabase): HorizonDashboardEnrollmentDao {
        return db.dashboardEnrollmentDao()
    }

    @Provides
    fun provideHorizonDashboardProgramDao(db: HorizonDatabase): HorizonDashboardProgramDao {
        return db.dashboardProgramDao()
    }

    @Provides
    fun provideHorizonDashboardModuleItemDao(db: HorizonDatabase): HorizonDashboardModuleItemDao {
        return db.dashboardModuleItemDao()
    }

    @Provides
    fun provideHorizonSyncMetadataDao(db: HorizonDatabase): HorizonSyncMetadataDao {
        return db.syncMetadataDao()
    }

    @Provides
    fun provideHorizonLearnItemDao(db: HorizonDatabase): HorizonLearnItemDao {
        return db.learnItemDao()
    }

    @Provides
    fun provideHorizonLearnCollectionDao(db: HorizonDatabase): HorizonLearnCollectionDao {
        return db.learnCollectionDao()
    }

    @Provides
    fun provideHorizonLearnSavedItemDao(db: HorizonDatabase): HorizonLearnSavedItemDao {
        return db.learnSavedItemDao()
    }
}
