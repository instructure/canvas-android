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

import com.instructure.canvasapi2.apis.ObserverAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Alert
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.AlertWorkflowState
import com.instructure.canvasapi2.utils.depaginate

class AlertsRepository(private val observerApi: ObserverAPI.ObserverInterface) {

    suspend fun getAlertsForStudent(studentId: Long, forceNetwork: Boolean): List<Alert> {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return observerApi.getObserverAlerts(studentId, restParams).depaginate {
            observerApi.getNextPageObserverAlerts(it, restParams)
        }.dataOrThrow
    }

    suspend fun getAlertThresholdForStudent(studentId: Long, forceNetwork: Boolean): List<AlertThreshold> {
        val restParams = RestParams(isForceReadFromNetwork = forceNetwork)
        return observerApi.getObserverAlertThresholds(studentId, restParams).dataOrNull ?: emptyList()
    }

    suspend fun updateAlertWorkflow(studentId: Long, workflowState: AlertWorkflowState): Alert {
        val restParams = RestParams(isForceReadFromNetwork = true)
        return observerApi.updateAlertWorkflow(studentId, workflowState, restParams).dataOrThrow
    }

}