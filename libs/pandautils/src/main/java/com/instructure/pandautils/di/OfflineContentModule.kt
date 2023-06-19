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
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.pandautils.features.offline.offlinecontent.OfflineContentRepository
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
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
        filesFoldersInterface: FileFolderAPI.FilesFoldersInterface,
        courseSyncSettingsDao: CourseSyncSettingsDao,
        fileSyncSettingsDao: FileSyncSettingsDao
    ): OfflineContentRepository {
        return OfflineContentRepository(coursesApi, filesFoldersInterface, courseSyncSettingsDao, fileSyncSettingsDao)
    }
}
