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
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetSkillsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetWidgetsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Skill
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.journey.GetWidgetDataQuery
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import java.util.Date

class FakeGetProgramsManager : GetProgramsManager {
    override suspend fun getPrograms(forceNetwork: Boolean): List<Program> {
        return getProgramsData()
    }

    override suspend fun getProgramById(programId: String, forceNetwork: Boolean): Program {
        return getProgramsData().first { it.id == programId }
    }

    override suspend fun enrollCourse(progressId: String): DataResult<Unit> {
        return if (getProgramsData().first().sortedRequirements.any { it.progressId == progressId }) {
            DataResult.Success(Unit)
        } else {
            DataResult.Fail()
        }
    }

    private fun getProgramsData(): List<Program> {
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

class FakeGetWidgetsManager : GetWidgetsManager {
    override suspend fun getTimeSpentWidgetData(courseId: Long?, forceNetwork: Boolean): GetWidgetDataQuery.WidgetData {
        return GetWidgetDataQuery.WidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                mapOf(
                    "date" to "2025-10-08",
                    "user_id" to 1.0,
                    "course_id" to 101.0,
                    "course_name" to "Test Course",
                    "minutes_per_day" to 600.0
                )
            )
        )
    }

    override suspend fun getLearningStatusWidgetData(
        courseId: Long?,
        forceNetwork: Boolean
    ): GetWidgetDataQuery.WidgetData {
        return GetWidgetDataQuery.WidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                mapOf("module_count_completed" to 5)
            )
        )
    }
}

class FakeGetSkillsManager: GetSkillsManager {
    override suspend fun getSkills(
        completedOnly: Boolean?,
        forceNetwork: Boolean
    ): List<Skill> {
        return listOf(
            Skill(
                id = "1",
                name = "Skill 1",
                proficiencyLevel = "beginner",
                createdAt = null,
                updatedAt = null
            ),
            Skill(
                id = "2",
                name = "Skill 2",
                proficiencyLevel = "expert",
                createdAt = null,
                updatedAt = null
            )
        )
    }

}