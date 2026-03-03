/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.widget.di

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.dashboard.widget.db.WidgetConfigDao
import com.instructure.pandautils.features.dashboard.widget.db.WidgetDatabase
import com.instructure.pandautils.features.dashboard.widget.db.WidgetDatabaseProvider
import com.instructure.pandautils.features.dashboard.widget.db.WidgetMetadataDao
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetMetadataRepository
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetMetadataRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WidgetModule {

    @Provides
    @Singleton
    fun provideWidgetDatabaseProvider(
        @ApplicationContext context: Context,
        firebaseCrashlytics: FirebaseCrashlytics
    ): WidgetDatabaseProvider {
        return WidgetDatabaseProvider(context, firebaseCrashlytics)
    }

    @Provides
    fun provideWidgetDatabase(
        widgetDatabaseProvider: WidgetDatabaseProvider,
        apiPrefs: ApiPrefs
    ): WidgetDatabase {
        val userId = if (apiPrefs.isMasquerading || apiPrefs.isMasqueradingFromQRCode) {
            apiPrefs.masqueradeId
        } else {
            apiPrefs.user?.id
        }
        return widgetDatabaseProvider.getDatabase(userId)
    }

    @Provides
    fun provideWidgetConfigDao(database: WidgetDatabase): WidgetConfigDao {
        return database.widgetConfigDao()
    }

    @Provides
    fun provideWidgetMetadataDao(database: WidgetDatabase): WidgetMetadataDao {
        return database.widgetMetadataDao()
    }

    @Provides
    fun provideWidgetMetadataRepository(dao: WidgetMetadataDao): WidgetMetadataRepository {
        return WidgetMetadataRepositoryImpl(dao)
    }

    @Provides
    fun provideWidgetConfigDataRepository(dao: WidgetConfigDao): WidgetConfigDataRepository {
        return WidgetConfigDataRepository(dao)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
}