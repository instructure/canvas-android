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

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.managers.graphql.ModuleManager
import com.instructure.pandautils.room.offline.daos.CheckpointDao
import com.instructure.pandautils.room.offline.daos.CourseSettingsDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.facade.ModuleFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.modules.list.ModuleListRepository
import com.instructure.student.features.modules.list.datasource.ModuleListLocalDataSource
import com.instructure.student.features.modules.list.datasource.ModuleListNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class ModuleListModule {

    @Provides
    fun provideModuleListLocalDataSource(
        tabDao: TabDao,
        moduleFacade: ModuleFacade,
        courseSettingsDao: CourseSettingsDao,
        checkpointDao: CheckpointDao
    ): ModuleListLocalDataSource {
        return ModuleListLocalDataSource(tabDao, moduleFacade, courseSettingsDao, checkpointDao)
    }

    @Provides
    fun provideModuleListNetworkDataSource(
        moduleApi: ModuleAPI.ModuleInterface,
        tabApi: TabAPI.TabsInterface,
        courseApi: CourseAPI.CoursesInterface,
        moduleManager: ModuleManager
    ): ModuleListNetworkDataSource {
        return ModuleListNetworkDataSource(moduleApi, tabApi, courseApi, moduleManager)
    }

    @Provides
    fun provideModuleListRepository(
        moduleListLocalDataSource: ModuleListLocalDataSource,
        moduleListNetworkDataSource: ModuleListNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): ModuleListRepository {
        return ModuleListRepository(moduleListLocalDataSource, moduleListNetworkDataSource, networkStateProvider, featureFlagProvider)
    }

}