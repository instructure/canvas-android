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

package com.instructure.student.di

import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.pandautils.room.offline.facade.ConferenceFacade
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.mobius.conferences.conference_list.ConferenceListRepository
import com.instructure.student.mobius.conferences.conference_list.datasource.ConferenceListLocalDataSource
import com.instructure.student.mobius.conferences.conference_list.datasource.ConferenceListNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class ConferenceListModule {

    @Provides
    fun provideNetworkDataSource(
        conferencesApi: ConferencesApi.ConferencesInterface,
        oAuthApi: OAuthAPI.OAuthInterface
    ): ConferenceListNetworkDataSource {
        return ConferenceListNetworkDataSource(conferencesApi, oAuthApi)
    }

    @Provides
    fun provideLocalDataSource(
        conferenceFacade: ConferenceFacade
    ): ConferenceListLocalDataSource {
        return ConferenceListLocalDataSource(conferenceFacade)
    }

    @Provides
    fun provideRepository(
        localDataSource: ConferenceListLocalDataSource,
        networkDataSource: ConferenceListNetworkDataSource,
        networkStateProvider: NetworkStateProvider
    ): ConferenceListRepository {
        return ConferenceListRepository(localDataSource, networkDataSource, networkStateProvider)
    }
}
