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

package com.instructure.student.di

import android.content.Context
import androidx.room.Room
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.daos.*
import com.instructure.student.features.offline.NetworkStateProvider
import com.instructure.student.features.offline.SyncManager
import com.instructure.student.features.offline.db.OfflineDatabase
import com.instructure.student.features.offline.repository.coursebrowser.CourseBrowserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ActivityComponent::class)
class OfflineModule {

    @Provides
    fun provideOfflineDatabase(@ApplicationContext context: Context, apiPrefs: ApiPrefs): OfflineDatabase {
        return Room.databaseBuilder(context, OfflineDatabase::class.java, "offline-db-${apiPrefs.user?.id}").build()
    }

    @Provides
    fun provideCourseBrowserRepository(tabApi: TabAPI.TabsInterface, tabDao: TabDao, networkStateProvider: NetworkStateProvider): CourseBrowserRepository {
        return CourseBrowserRepository(tabApi, tabDao, networkStateProvider)
    }

    @Provides
    fun provideNetworkStateProvider(@ApplicationContext context: Context): NetworkStateProvider {
        return NetworkStateProvider(context)
    }

    @Provides
    fun provideSyncManager(
        courseManager: CourseManager,
        courseDao: CourseDao,
        enrollmentDao: EnrollmentDao,
        gradesDao: GradesDao,
        gradingPeriodDao: GradingPeriodDao,
        sectionDao: SectionDao,
        termDao: TermDao,
        userDao: UserDao,
        courseGradingPeriodDao: CourseGradingPeriodDao,
        tabDao: TabDao,
        userApi: UserAPI.UsersInterface
    ): SyncManager {
        return SyncManager(
            courseManager,
            courseDao,
            enrollmentDao,
            gradesDao,
            gradingPeriodDao,
            sectionDao,
            termDao,
            userDao,
            courseGradingPeriodDao,
            tabDao,
            userApi
        )
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
}