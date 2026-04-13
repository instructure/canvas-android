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
package com.instructure.horizon.espresso

import android.content.Context
import androidx.room.Room
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.database.HorizonDatabase
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
import com.instructure.horizon.database.dao.HorizonProgramDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.di.HorizonHtmlParserQualifier
import com.instructure.horizon.di.HorizonOfflineModule
import com.instructure.horizon.offline.HorizonHtmlParserFileSource
import com.instructure.pandautils.features.offline.sync.HtmlParser
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [HorizonOfflineModule::class])
object HorizonOfflineTestModule {

    @Provides
    @Singleton
    fun provideHorizonDatabase(@ApplicationContext context: Context): HorizonDatabase {
        return Room.inMemoryDatabaseBuilder(context, HorizonDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideHorizonDashboardEnrollmentDao(db: HorizonDatabase): HorizonDashboardEnrollmentDao = db.dashboardEnrollmentDao()

    @Provides
    fun provideHorizonProgramDao(db: HorizonDatabase): HorizonProgramDao = db.programDao()

    @Provides
    fun provideHorizonDashboardModuleItemDao(db: HorizonDatabase): HorizonDashboardModuleItemDao = db.dashboardModuleItemDao()

    @Provides
    fun provideHorizonSyncMetadataDao(db: HorizonDatabase): HorizonSyncMetadataDao = db.syncMetadataDao()

    @Provides
    fun provideHorizonLearnItemDao(db: HorizonDatabase): HorizonLearnItemDao = db.learnItemDao()

    @Provides
    fun provideHorizonLearnCollectionDao(db: HorizonDatabase): HorizonLearnCollectionDao = db.learnCollectionDao()

    @Provides
    fun provideHorizonLearnSavedItemDao(db: HorizonDatabase): HorizonLearnSavedItemDao = db.learnSavedItemDao()

    @Provides
    fun provideHorizonCourseDao(db: HorizonDatabase): HorizonCourseDao = db.courseDao()

    @Provides
    fun provideHorizonCourseModuleDao(db: HorizonDatabase): HorizonCourseModuleDao = db.courseModuleDao()

    @Provides
    fun provideHorizonCourseScoreDao(db: HorizonDatabase): HorizonCourseScoreDao = db.courseScoreDao()

    @Provides
    fun provideHorizonLocalFileDao(db: HorizonDatabase): HorizonLocalFileDao = db.localFileDao()

    @Provides
    fun provideHorizonFileFolderDao(db: HorizonDatabase): HorizonFileFolderDao = db.fileFolderDao()

    @Provides
    @HorizonHtmlParserQualifier
    fun provideHorizonHtmlParser(
        fileSource: HorizonHtmlParserFileSource,
        apiPrefs: ApiPrefs,
        @ApplicationContext context: Context,
    ): HtmlParser = HtmlParser(fileSource, apiPrefs, context)
}
