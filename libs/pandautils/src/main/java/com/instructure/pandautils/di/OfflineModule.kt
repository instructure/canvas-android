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
 */

package com.instructure.pandautils.di

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.OfflineDatabaseProvider
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class OfflineModule {

    @Provides
    @Singleton
    fun provideOfflineDatabaseProvider(@ApplicationContext context: Context): OfflineDatabaseProvider {
        return OfflineDatabaseProvider(context)
    }

    @Provides
    fun provideOfflineDatabase(offlineDatabaseProvider: OfflineDatabaseProvider, apiPrefs: ApiPrefs): OfflineDatabase {
        return offlineDatabaseProvider.getDatabase(apiPrefs.user?.id)
    }

    @Provides
    fun provideNetworkStateProvider(@ApplicationContext context: Context): NetworkStateProvider {
        return NetworkStateProvider(context)
    }

    @Provides
    fun provideCourseDao(appDatabase: OfflineDatabase): CourseDao {
        return appDatabase.courseDao()
    }

    @Provides
    fun provideEnrollmentDao(appDatabase: OfflineDatabase): EnrollmentDao {
        return appDatabase.enrollmentDao()
    }

    @Provides
    fun provideGradesDao(appDatabase: OfflineDatabase): GradesDao {
        return appDatabase.gradesDao()
    }

    @Provides
    fun provideGradingPeriodDao(appDatabase: OfflineDatabase): GradingPeriodDao {
        return appDatabase.gradingPeriodDao()
    }

    @Provides
    fun provideSectionDao(appDatabase: OfflineDatabase): SectionDao {
        return appDatabase.sectionDao()
    }

    @Provides
    fun provideTermDao(appDatabase: OfflineDatabase): TermDao {
        return appDatabase.termDao()
    }

    @Provides
    fun provideUserCalendarDao(appDatabase: OfflineDatabase): UserCalendarDao {
        return appDatabase.userCalendarDao()
    }

    @Provides
    fun provideUserDao(appDatabase: OfflineDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    fun provideCourseGradingPeriodDao(appDatabase: OfflineDatabase): CourseGradingPeriodDao {
        return appDatabase.courseGradingPeriodDao()
    }

    @Provides
    fun provideTabDao(appDatabase: OfflineDatabase): TabDao {
        return appDatabase.tabDao()
    }

    @Provides
    fun provideCourseSyncSettingsDao(appDatabase: OfflineDatabase): CourseSyncSettingsDao {
        return appDatabase.courseSyncSettingsDao()
    }

    @Provides
    fun providePageDao(appDatabase: OfflineDatabase): PageDao {
        return appDatabase.pageDao()
    }
}