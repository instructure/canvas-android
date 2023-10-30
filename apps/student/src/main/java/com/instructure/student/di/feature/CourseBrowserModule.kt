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

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.room.offline.facade.PageFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.coursebrowser.CourseBrowserRepository
import com.instructure.student.features.coursebrowser.datasource.CourseBrowserLocalDataSource
import com.instructure.student.features.coursebrowser.datasource.CourseBrowserNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class CourseBrowserModule {

    @Provides
    fun provideCourseBrowserLocalDataSource(
        tabDao: TabDao,
        pageFacade: PageFacade,
        courseSyncSettingsDao: CourseSyncSettingsDao,
        fileSyncSettingsDao: FileSyncSettingsDao
    ): CourseBrowserLocalDataSource {
        return CourseBrowserLocalDataSource(
            tabDao,
            pageFacade,
            courseSyncSettingsDao,
            fileSyncSettingsDao
        )
    }

    @Provides
    fun provideCourseBrowserNetworkDataSource(
        tabApi: TabAPI.TabsInterface,
        pageApi: PageAPI.PagesInterface
    ): CourseBrowserNetworkDataSource {
        return CourseBrowserNetworkDataSource(tabApi, pageApi)
    }

    @Provides
    fun provideCourseBrowserRepository(
        networkDataSource: CourseBrowserNetworkDataSource,
        localDataSource: CourseBrowserLocalDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): CourseBrowserRepository {
        return CourseBrowserRepository(networkDataSource, localDataSource, networkStateProvider, featureFlagProvider)
    }
}
