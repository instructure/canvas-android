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

package com.instructure.pandautils.room.appdatabase.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.entities.EnvironmentFeatureFlags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnvironmentFeatureFlagsDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var environmentFeatureFlagsDao: EnvironmentFeatureFlagsDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        environmentFeatureFlagsDao = db.environmentFeatureFlagsDao()
    }

    @After
    fun tearDoown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        val featureFlags = EnvironmentFeatureFlags(
            userId = 1,
            mapOf("feature_flag" to true)
        )
        val updated = featureFlags.copy(
            featureFlags = mapOf("feature_flag" to false)
        )

        environmentFeatureFlagsDao.insert(featureFlags)

        environmentFeatureFlagsDao.insert(updated)

        val result = environmentFeatureFlagsDao.findByUserId(1L)

        assertEquals(updated, result)
    }

    @Test
    fun testFindByUserId() = runTest {
        val featureFlags = EnvironmentFeatureFlags(
            userId = 1,
            mapOf("feature_flag" to true)
        )
        val featureFlags2 = EnvironmentFeatureFlags(
            userId = 2,
            mapOf("feature_flag" to false)
        )

        environmentFeatureFlagsDao.insert(featureFlags)
        environmentFeatureFlagsDao.insert(featureFlags2)

        val result = environmentFeatureFlagsDao.findByUserId(1L)

        assertEquals(featureFlags, result)
    }
}