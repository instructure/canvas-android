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

package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.pandautils.features.offline.offlinecontent.CourseFileSharedRepository
import com.instructure.pandautils.features.offline.offlinecontent.OfflineContentRepository
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class OfflineContentModule {

    @Provides
    fun provideOfflineContentRepository(
        coursesApi: CourseAPI.CoursesInterface,
        courseSyncSettingsDao: CourseSyncSettingsDao,
        fileSyncSettingsDao: FileSyncSettingsDao,
        courseFileSharedRepository: CourseFileSharedRepository,
        syncSettingsFacade: SyncSettingsFacade,
        localFileDao: LocalFileDao,
        fileSyncProgressDao: FileSyncProgressDao
    ): OfflineContentRepository {
        return OfflineContentRepository(
            coursesApi,
            courseSyncSettingsDao,
            fileSyncSettingsDao,
            courseFileSharedRepository,
            syncSettingsFacade,
            localFileDao,
            fileSyncProgressDao
        )
    }
}
