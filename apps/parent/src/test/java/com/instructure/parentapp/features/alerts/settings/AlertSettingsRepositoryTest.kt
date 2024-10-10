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
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.ThresholdWorkflowState
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.lang.IllegalStateException

class AlertSettingsRepositoryTest {

    private lateinit var alertSettingsRepository: AlertSettingsRepository

    private val observerApi: ObserverApi = mockk(relaxed = true)

    @Before
    fun setup() {
        alertSettingsRepository = AlertSettingsRepository(observerApi)
    }

    @Test
    fun `loadAlertThresholds should return list of alert thresholds`() = runTest {
        val expected = listOf(
            AlertThreshold(
                id = 1,
                alertType = AlertType.ASSIGNMENT_MISSING,
                threshold = null,
                userId = 1L,
                workflowState = ThresholdWorkflowState.ACTIVE,
                observerId = 1L
            ),
            AlertThreshold(
                id = 2,
                alertType = AlertType.ASSIGNMENT_GRADE_LOW,
                threshold = "10",
                userId = 1L,
                workflowState = ThresholdWorkflowState.ACTIVE,
                observerId = 1L
            ),
            AlertThreshold(
                id = 3,
                alertType = AlertType.ASSIGNMENT_GRADE_HIGH,
                threshold = "90",
                userId = 1L,
                workflowState = ThresholdWorkflowState.ACTIVE,
                observerId = 1L
            )
        )
        coEvery { observerApi.getObserverAlertThresholds(any(), any()) } returns DataResult.Success(
            expected
        )

        val result = alertSettingsRepository.loadAlertThresholds(1L)

        assert(result == expected)
    }

    @Test(expected = IllegalStateException::class)
    fun `loadAlertThresholds should throw exception`() = runTest {
        coEvery { observerApi.getObserverAlertThresholds(any(), any()) } returns DataResult.Fail()

        alertSettingsRepository.loadAlertThresholds(1L)
    }

    @Test
    fun `createAlertThreshold should return success`() = runTest {
        coEvery { observerApi.createObserverAlert(any(), any()) } returns DataResult.Success(Unit)

        alertSettingsRepository.createAlertThreshold(AlertType.ASSIGNMENT_MISSING, 1L, null)
    }

    @Test(expected = IllegalStateException::class)
    fun `createAlertThreshold should throw exception`() = runTest {
        coEvery { observerApi.createObserverAlert(any(), any()) } returns DataResult.Fail()

        alertSettingsRepository.createAlertThreshold(AlertType.ASSIGNMENT_MISSING, 1L, null)
    }

    @Test
    fun `deleteAlertThreshold should return success`() = runTest {
        coEvery { observerApi.deleteObserverAlert(any(), any()) } returns DataResult.Success(Unit)

        alertSettingsRepository.deleteAlertThreshold(1L)
    }

    @Test(expected = IllegalStateException::class)
    fun `deleteAlertThreshold should throw exception`() = runTest {
        coEvery { observerApi.deleteObserverAlert(any(), any()) } returns DataResult.Fail()

        alertSettingsRepository.deleteAlertThreshold(1L)
    }
}