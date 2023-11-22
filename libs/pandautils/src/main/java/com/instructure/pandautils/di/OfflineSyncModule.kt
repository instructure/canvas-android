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

package com.instructure.pandautils.di

import android.content.Context
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.FileSync
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class OfflineSyncModule {

    @Provides
    fun provideAggregateProgressObserver(
        @ApplicationContext context: Context,
        courseSyncProgressDao: CourseSyncProgressDao,
        fileSyncProgressDao: FileSyncProgressDao
    ): AggregateProgressObserver {
        return AggregateProgressObserver(context, courseSyncProgressDao, fileSyncProgressDao)
    }

    @Provides
    fun provideFileSync(
        @ApplicationContext context: Context,
        fileDownloadApi: FileDownloadAPI,
        localFileDao: LocalFileDao,
        fileFolderDao: FileFolderDao,
        firebaseCrashlytics: FirebaseCrashlytics,
        fileSyncProgressDao: FileSyncProgressDao,
        fileSyncSettingsDao: FileSyncSettingsDao,
        courseSyncProgressDao: CourseSyncProgressDao,
        fileFolderApi: FileFolderAPI.FilesFoldersInterface,
    ): FileSync {
        return FileSync(
            context,
            fileDownloadApi,
            localFileDao,
            fileFolderDao,
            firebaseCrashlytics,
            fileSyncProgressDao,
            fileSyncSettingsDao,
            courseSyncProgressDao,
            fileFolderApi
        )
    }
}