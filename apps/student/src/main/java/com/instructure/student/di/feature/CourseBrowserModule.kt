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
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.room.offline.daos.TabDao
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.offline.coursebrowser.CourseBrowserLocalDataSource
import com.instructure.student.features.offline.coursebrowser.CourseBrowserNetworkDataSource
import com.instructure.student.features.offline.coursebrowser.CourseBrowserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class CourseBrowserModule {

    @Provides
    fun provideCourseBrowserLocalDataSource(tabDao: TabDao, pageDao: PageDao): CourseBrowserLocalDataSource {
        return CourseBrowserLocalDataSource(tabDao, pageDao)
    }

    @Provides
    fun provideCourseBrowserNetworkDataSource(tabApi: TabAPI.TabsInterface, pageApi: PageAPI.PagesInterface): CourseBrowserNetworkDataSource {
        return CourseBrowserNetworkDataSource(tabApi, pageApi)
    }

    @Provides
    fun provideCourseBrowserRepository(networkDataSource: CourseBrowserNetworkDataSource, localDataSource: CourseBrowserLocalDataSource, networkStateProvider: NetworkStateProvider): CourseBrowserRepository {
        return CourseBrowserRepository(networkDataSource, localDataSource, networkStateProvider)
    }


}