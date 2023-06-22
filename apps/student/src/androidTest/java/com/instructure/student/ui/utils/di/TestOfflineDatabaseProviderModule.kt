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

package com.instructure.student.ui.utils.di

import android.content.Context
import androidx.room.Room
import com.instructure.pandautils.di.OfflineDatabaseProviderModule
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.offlineDatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [OfflineDatabaseProviderModule::class]
)
class TestOfflineDatabaseProviderModule {

    @Provides
    @Singleton
    fun provideOfflineDatabaseProvider(@ApplicationContext context: Context): DatabaseProvider {
        return FakeOfflineDatabaseProvider(context)
    }
}

class FakeOfflineDatabaseProvider(private val context: Context) : DatabaseProvider {

    private val dbMap = mutableMapOf<Long, OfflineDatabase>()

    override fun getDatabase(userId: Long?): OfflineDatabase {
        if (userId == null) return Room.databaseBuilder(context, OfflineDatabase::class.java, "test-offline-db")
            .addMigrations(*offlineDatabaseMigrations)
            .build()

        return dbMap.getOrPut(userId) {
            Room.databaseBuilder(context, OfflineDatabase::class.java, "offline-db-$userId")
                .addMigrations(*offlineDatabaseMigrations)
                .build()
        }
    }

    override fun clearDatabase(userId: Long) {}
}