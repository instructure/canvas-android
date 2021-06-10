/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.di

import android.app.NotificationManager
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.instructure.pandautils.update.UpdateManager
import com.instructure.pandautils.update.UpdatePrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UpdateModule {

    @Provides
    @Singleton
    fun provideUpdateManager(appUpdateManager: AppUpdateManager, notificationManager: NotificationManager, updatePrefs: UpdatePrefs): UpdateManager {
        return UpdateManager(appUpdateManager, notificationManager, updatePrefs)
    }

    @Provides
    @Singleton
    fun provideUpdatePrefs(): UpdatePrefs {
        return UpdatePrefs
    }

    @Provides
    @Singleton
    fun provideAppUpdateManager(@ApplicationContext context: Context): AppUpdateManager {
        return AppUpdateManagerFactory.create(context)
    }
}