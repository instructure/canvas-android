package com.instructure.pandautils.di

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.room.appdatabase.daos.ReminderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AlarmSchedulerModule {

    @Provides
    fun provideAlarmScheduler(@ApplicationContext context: Context, reminderDao: ReminderDao, apiPrefs: ApiPrefs): AlarmScheduler {
        return AlarmScheduler(context, reminderDao, apiPrefs)
    }
}