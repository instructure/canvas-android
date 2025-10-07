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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvasapi2.managers.graphql.JourneyApiManager
import com.instructure.canvasapi2.managers.graphql.Program
import com.instructure.canvasapi2.managers.graphql.ProgramRequirement
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType

class FakeJourneyApiManager(): JourneyApiManager {
    override suspend fun getPrograms(forceNetwork: Boolean): List<Program> {
        return getPrograms()
    }

    override suspend fun getProgramById(programId: String, forceNetwork: Boolean): Program {
        return getPrograms().first { it.id == programId }
    }

    override suspend fun enrollCourse(progressId: String): DataResult<Unit> {
        return if (getPrograms().first().sortedRequirements.any { it.progressId == progressId }) {
            DataResult.Success(Unit)
        } else {
            DataResult.Fail()
        }
    }

    fun getPrograms(): List<Program> {
        val program1 = Program(
            id = "1",
            name = "Program 1",
            description = "Description for Program 1",
            sortedRequirements = listOf(
                ProgramRequirement(
                    id = "1",
                    progressId = "1",
                    progress = 50.0,
                    courseId = MockCanvas.data.courses.values.toList()[0].id,
                    required = true,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.ENROLLED,
                )
            ),
            startDate = null,
            endDate = null,
            variant = ProgramVariantType.LINEAR
        )
        val program2 = Program(
            id = "2",
            name = "Program 2",
            description = "Description for Program 2",
            sortedRequirements = emptyList(),
            startDate = null,
            endDate = null,
            variant = ProgramVariantType.NON_LINEAR
        )
        return listOf(program1, program2)
    }
}