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
package com.instructure.student.features.dashboard

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.models.DashboardPositions
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.room.offline.daos.DashboardCardDao
import com.instructure.pandautils.room.offline.entities.DashboardCardEntity
import com.instructure.pandautils.room.offline.facade.CourseFacade

class DashboardLocalDataSource(
    private val courseFacade: CourseFacade,
    private val dashboardCardDao: DashboardCardDao
) : DashboardDataSource {

    override suspend fun getCourses(forceNetwork: Boolean): List<Course> {
        return courseFacade.getAllCourses()
    }

    override suspend fun getGroups(forceNetwork: Boolean): List<Group> {
        return emptyList()
    }

    override suspend fun getDashboardCards(forceNetwork: Boolean): List<DashboardCard> {
        return dashboardCardDao.findAll().map { it.toApiModel() }
    }

    suspend fun saveDashboardCards(dashboardCards: List<DashboardCard>) {
        dashboardCardDao.updateEntities(dashboardCards.map { DashboardCardEntity(it) })
    }

    suspend fun updateDashboardCardsOrder(dashboardPositions: DashboardPositions) {
        val cards = dashboardCardDao.findAll()
        val coursesWithPosition = dashboardPositions.positions
            .map { Pair(CanvasContext.fromContextCode(it.key), it.value) }
            .filter { it.first is Course }
            .associate { Pair((it.first as Course).id, it.second) }

        // If somehow we end up with different items in the positions response than the stored dashboard cards we should return and not update the positions
        val cardIds = cards.map { it.id }.toSet()
        val positionUpdateIds = coursesWithPosition.keys
        if (cardIds != positionUpdateIds) return

        val newCards = cards.map {
            val newPosition = coursesWithPosition[it.id]
            if (newPosition != null) {
                it.copy(position = newPosition)
            } else {
                it
            }
        }

        dashboardCardDao.updateEntities(newCards)
    }
}