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
package com.instructure.parentapp.features.alerts.list

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.ObserverApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.AlertWorkflowState
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.utils.depaginate

class AlertsRepository(
    private val observerApi: ObserverApi,
    private val courseApi: CourseAPI.CoursesInterface
) {

    suspend fun getAlertsForStudent(studentId: Long, forceNetwork: Boolean): List<Alert> {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)
        val allAlerts = observerApi.getObserverAlerts(studentId, restParams).depaginate {
            observerApi.getNextPageObserverAlerts(it, restParams)
        }.dataOrThrow.sortedByDescending { it.actionDate }

        val coursesMap = mutableMapOf<Long, CourseSettings?>()
        val filteredAlerts = allAlerts.filter { alert ->
            if (!alert.isQuantitativeRestrictionApplies()) return@filter true

            alert.getCourseId()?.let { courseId ->
                val settings = coursesMap.getOrPut(courseId) {
                    courseApi.getCourseSettings(courseId, restParams).dataOrNull
                }
                settings?.restrictQuantitativeData?.not() ?: true
            } ?: true
        }

        return filteredAlerts
    }

    suspend fun getAlertThresholdForStudent(
        studentId: Long,
        forceNetwork: Boolean
    ): List<AlertThreshold> {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return observerApi.getObserverAlertThresholds(studentId, restParams).dataOrNull
            ?: emptyList()
    }

    suspend fun updateAlertWorkflow(alertId: Long, workflowState: AlertWorkflowState): Alert {
        val restParams = RestParams(isForceReadFromNetwork = true)
        return observerApi.updateAlertWorkflow(
            alertId,
            workflowState.name.lowercase(),
            restParams
        ).dataOrThrow
    }

    suspend fun getUnreadAlertCount(studentId: Long): Int {
        val alerts = try {
            getAlertsForStudent(studentId, true)
        } catch (e: Exception) {
            emptyList()
        }
        return alerts.count { it.workflowState == AlertWorkflowState.UNREAD }
    }

}