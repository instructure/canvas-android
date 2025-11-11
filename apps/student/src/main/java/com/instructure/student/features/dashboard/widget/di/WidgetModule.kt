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

package com.instructure.student.features.dashboard.widget.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.instructure.student.features.dashboard.widget.db.WidgetConfigDao
import com.instructure.student.features.dashboard.widget.db.WidgetDatabase
import com.instructure.student.features.dashboard.widget.db.WidgetMetadataDao
import com.instructure.student.features.dashboard.widget.repository.WidgetMetadataRepository
import com.instructure.student.features.dashboard.widget.repository.WidgetMetadataRepositoryImpl
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
    fun provideWidgetDatabase(@ApplicationContext context: Context): WidgetDatabase {
        return Room.databaseBuilder(context, WidgetDatabase::class.java, "widget_database")
            .build()
    }

    @Provides
    @Singleton
    fun provideWidgetConfigDao(database: WidgetDatabase): WidgetConfigDao {
        return database.widgetConfigDao()
    }

    @Provides
    @Singleton
    fun provideWidgetMetadataDao(database: WidgetDatabase): WidgetMetadataDao {
        return database.widgetMetadataDao()
    }

    @Provides
    @Singleton
    fun provideWidgetMetadataRepository(dao: WidgetMetadataDao): WidgetMetadataRepository {
        return WidgetMetadataRepositoryImpl(dao)
    }
}