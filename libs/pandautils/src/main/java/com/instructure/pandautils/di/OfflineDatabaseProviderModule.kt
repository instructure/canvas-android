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

package com.instructure.pandautils.di

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.room.offline.OfflineDatabaseProvider
import com.instructure.pandautils.utils.LogoutHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class OfflineDatabaseProviderModule {

    @Provides
    @Singleton
    fun provideOfflineDatabaseProvider(
        @ApplicationContext context: Context,
        logoutHelper: LogoutHelper,
        firebaseCrashlytics: FirebaseCrashlytics,
        alarmScheduler: AlarmScheduler
    ): DatabaseProvider {
        return OfflineDatabaseProvider(context, logoutHelper, firebaseCrashlytics, alarmScheduler)
    }
}