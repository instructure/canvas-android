/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.DashboardCardEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardCardDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var dashboardCardDao: DashboardCardDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        dashboardCardDao = db.dashboardCardDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindAllEntities() = runTest {
        val entities = listOf(
            DashboardCardEntity(DashboardCard(id = 1, shortName = "No more tests please")),
            DashboardCardEntity(DashboardCard(id = 2, shortName = "No more tests please 2")),
        )
        dashboardCardDao.insertAll(entities)

        val result = dashboardCardDao.findAll()

        Assert.assertEquals(entities, result)
    }

    @Test
    fun testUpdatingEntities() = runTest {
        val entities = listOf(
            DashboardCardEntity(DashboardCard(id = 1, shortName = "No more tests please")),
            DashboardCardEntity(DashboardCard(id = 2, shortName = "No more tests please 2")),
        )

        val newEntities = listOf(
            DashboardCardEntity(DashboardCard(id = 1, shortName = "No more tests please")),
            DashboardCardEntity(DashboardCard(id = 3, shortName = "No more tests please 3")),
        )
        dashboardCardDao.insertAll(entities)

        dashboardCardDao.updateEntities(newEntities)
        val result = dashboardCardDao.findAll()

        Assert.assertEquals(newEntities, result)
    }
}