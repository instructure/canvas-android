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
package com.instructure.parentapp.features.alerts.settings

import com.instructure.canvasapi2.apis.ObserverApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.postmodels.CreateObserverThreshold
import com.instructure.canvasapi2.models.postmodels.CreateObserverThresholdWrapper

class AlertSettingsRepository(
    private val observerApi: ObserverApi
) {

    suspend fun loadAlertThresholds(userId: Long): List<AlertThreshold> {
        return observerApi.getObserverAlertThresholds(
            userId,
            RestParams(isForceReadFromNetwork = true)
        ).dataOrThrow
    }

    suspend fun createAlertThreshold(alertType: AlertType, userId: Long, threshold: String?) {
        observerApi.createObserverAlert(
            CreateObserverThresholdWrapper(
                CreateObserverThreshold(
                    alertType,
                    userId,
                    threshold
                )
            ), RestParams(isForceReadFromNetwork = true)
        ).dataOrThrow
    }

    suspend fun deleteAlertThreshold(thresholdId: Long) {
        observerApi.deleteObserverAlert(
            thresholdId,
            RestParams(isForceReadFromNetwork = true)
        ).dataOrThrow
    }
}