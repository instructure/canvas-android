/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.speedgrader

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


class SpeedGraderSelectedAttemptHolderTest {

    private val selectedAttemptHolder = SpeedGraderSelectedAttemptHolder()

    @Test
    fun `Set selected attempt ID updates state correctly`() = runTest {
        val studentId = 1L
        val attemptId = 123L
        selectedAttemptHolder.setSelectedAttemptId(studentId, attemptId)

        val result = selectedAttemptHolder.selectedAttemptIds.first().values.first()
        assertEquals(attemptId, result)
    }

    @Test
    fun `Selected attempt ID flow returns correct value`() = runTest {
        val studentId = 1L
        val attemptId = 123L
        selectedAttemptHolder.setSelectedAttemptId(11, 11)
        selectedAttemptHolder.setSelectedAttemptId(studentId, attemptId)

        val result = selectedAttemptHolder.selectedAttemptIdFlowFor(studentId).first()
        assertEquals(attemptId, result)
    }
}
