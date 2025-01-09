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
package com.instructure.pandautils.di

import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.features.reminder.DateTimePicker
import com.instructure.pandautils.features.reminder.ReminderManager
import com.instructure.pandautils.features.reminder.ReminderRepository
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ViewModelComponent::class)
class ReminderModule {

    @Provides
    fun provideReminderManager(
        dateTimePicker: DateTimePicker,
        reminderRepository: ReminderRepository
    ): ReminderManager {
        return ReminderManager(dateTimePicker, reminderRepository)
    }

    @Provides
    fun provideDateTimePicker(): DateTimePicker {
        return DateTimePicker()
    }
}

@Module
@InstallIn(SingletonComponent::class)
class ReminderSingletonModule {

    @Provides
    fun provideReminderRepository(
        reminderDao: ReminderDao,
        alarmScheduler: AlarmScheduler,
    ): ReminderRepository {
        return ReminderRepository(reminderDao, alarmScheduler)
    }
}
