/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.di.feature

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.apis.UnreadCountAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.parentapp.features.dashboard.AlertCountUpdater
import com.instructure.parentapp.features.dashboard.AlertCountUpdaterImpl
import com.instructure.parentapp.features.dashboard.DashboardRepository
import com.instructure.parentapp.features.dashboard.InboxCountUpdater
import com.instructure.parentapp.features.dashboard.InboxCountUpdaterImpl
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import com.instructure.parentapp.features.dashboard.SelectedStudentHolderImpl
import com.instructure.parentapp.util.navigation.Navigation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
class DashboardModule {

    @Provides
    fun provideDashboardRepository(
        enrollmentApi: EnrollmentAPI.EnrollmentInterface,
        unreadCountsApi: UnreadCountAPI.UnreadCountsInterface,
        launchDefinitionsApi: LaunchDefinitionsAPI.LaunchDefinitionsInterface
    ): DashboardRepository {
        return DashboardRepository(enrollmentApi, unreadCountsApi, launchDefinitionsApi)
    }
}

@Module
@InstallIn(SingletonComponent::class)
class SelectedStudentHolderModule {

    @Provides
    fun provideNavigation(apiPrefs: ApiPrefs): Navigation {
        return Navigation(apiPrefs)
    }

    @Provides
    @Singleton
    fun provideSelectedStudentHolder(): SelectedStudentHolder {
        return SelectedStudentHolderImpl()
    }

    @Provides
    @Singleton
    fun provideInboxCountUpdater(): InboxCountUpdater {
        return InboxCountUpdaterImpl()
    }

    @Provides
    @Singleton
    fun provideAlertCountUpdater(): AlertCountUpdater {
        return AlertCountUpdaterImpl()
    }
}
