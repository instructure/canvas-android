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
 *
 */

package com.instructure.student.di.feature

import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.files.details.FileDetailsLocalDataSource
import com.instructure.student.features.files.details.FileDetailsNetworkDataSource
import com.instructure.student.features.files.details.FileDetailsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class FileDetailsModule {

    @Provides
    fun provideFileDetailsNetworkDataSource(moduleApi: ModuleAPI.ModuleInterface, fileFolderApi: FileFolderAPI.FilesFoldersInterface): FileDetailsNetworkDataSource {
        return FileDetailsNetworkDataSource(moduleApi, fileFolderApi)
    }

    @Provides
    fun provideFileDetailsLocalDataSource(fileFolderDao: FileFolderDao, localFileDao: LocalFileDao): FileDetailsLocalDataSource {
        return FileDetailsLocalDataSource(fileFolderDao, localFileDao)
    }

    @Provides
    fun provideFileDetailsRepository(
            localDataSource:FileDetailsLocalDataSource,
            networkDataSource: FileDetailsNetworkDataSource,
            networkStateProvider: NetworkStateProvider,
            featureFlagProvider: FeatureFlagProvider,
    ): FileDetailsRepository {
        return FileDetailsRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }

}