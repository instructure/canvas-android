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
package com.instructure.student.di.feature

import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.files.search.FileSearchLocalDataSource
import com.instructure.student.features.files.search.FileSearchNetworkDataSource
import com.instructure.student.features.files.search.FileSearchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class FileSearchModule {

    @Provides
    fun provideFileSearchLocalDataSource(
        fileFolderDao: FileFolderDao,
        localFileDao: LocalFileDao
    ): FileSearchLocalDataSource {
        return FileSearchLocalDataSource(fileFolderDao, localFileDao)
    }

    @Provides
    fun provideFileSearchNetworkDataSource(
        fileFolderApi: FileFolderAPI.FilesFoldersInterface
    ): FileSearchNetworkDataSource {
        return FileSearchNetworkDataSource(fileFolderApi)
    }

    @Provides
    fun provideFileSearchRepository(
        fileSearchLocalDataSource: FileSearchLocalDataSource,
        fileSearchNetworkDataSource: FileSearchNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): FileSearchRepository {
        return FileSearchRepository(
            fileSearchLocalDataSource,
            fileSearchNetworkDataSource,
            networkStateProvider,
            featureFlagProvider)
    }
}