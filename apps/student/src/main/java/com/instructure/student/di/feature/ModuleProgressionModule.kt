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

import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.daos.QuizDao
import com.instructure.pandautils.room.offline.facade.ModuleFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.modules.progression.ModuleProgressionRepository
import com.instructure.student.features.modules.progression.datasource.ModuleProgressionLocalDataSource
import com.instructure.student.features.modules.progression.datasource.ModuleProgressionNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class ModuleProgressionModule {

    @Provides
    fun provideModuleProgressionLocalDataSource(moduleFacade: ModuleFacade, quizDao: QuizDao): ModuleProgressionLocalDataSource {
        return ModuleProgressionLocalDataSource(moduleFacade, quizDao)
    }

    @Provides
    fun provideModuleProgressionNetworkDataSource(moduleApi: ModuleAPI.ModuleInterface, quizApi: QuizAPI.QuizInterface): ModuleProgressionNetworkDataSource {
        return ModuleProgressionNetworkDataSource(moduleApi, quizApi)
    }

    @Provides
    fun provideModuleProgressionRepository(
        moduleProgressionLocalDataSource: ModuleProgressionLocalDataSource,
        moduleProgressionNetworkDataSource: ModuleProgressionNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider,
        courseSyncSettingsDao: CourseSyncSettingsDao,
        localFileDao: LocalFileDao
    ): ModuleProgressionRepository {
        return ModuleProgressionRepository(
            moduleProgressionLocalDataSource,
            moduleProgressionNetworkDataSource,
            networkStateProvider,
            featureFlagProvider,
            courseSyncSettingsDao,
            localFileDao
        )
    }
}