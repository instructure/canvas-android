/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.di

import com.instructure.pandautils.compose.composables.calendar.CalendarStateMapper
import com.instructure.pandautils.features.calendar.CalendarPrefs
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import org.threeten.bp.Clock
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
class CalendarModule {

    @Provides
    fun provideCalendarPrefs(): CalendarPrefs {
        return CalendarPrefs
    }

    @Provides
    fun provideCalendarStateMapper(clock: Clock): CalendarStateMapper {
        return CalendarStateMapper(clock)
    }
}

@Module
@InstallIn(SingletonComponent::class)
class CalendarSingletonModule {

    @Provides
    @Singleton
    fun provideCalendarSharedEvents(): CalendarSharedEvents {
        return CalendarSharedEvents()
    }
}