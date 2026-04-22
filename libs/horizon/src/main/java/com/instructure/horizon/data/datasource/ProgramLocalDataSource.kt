/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.horizon.database.dao.HorizonDashboardProgramDao
import com.instructure.horizon.database.entity.HorizonDashboardProgramCourseRef
import com.instructure.horizon.database.entity.HorizonDashboardProgramEntity
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import java.util.Date
import javax.inject.Inject

class ProgramLocalDataSource @Inject constructor(
    private val programDao: HorizonDashboardProgramDao,
) {

    suspend fun getPrograms(): List<Program> {
        return programDao.getAll().map { programEntity ->
            val refs = programDao.getRefsForProgram(programEntity.programId)
            Program(
                id = programEntity.programId,
                name = programEntity.programName,
                description = programEntity.description,
                startDate = programEntity.startDateMs?.let { Date(it) },
                endDate = programEntity.endDateMs?.let { Date(it) },
                variant = ProgramVariantType.safeValueOf(programEntity.variant),
                courseCompletionCount = programEntity.courseCompletionCount,
                sortedRequirements = refs.sortedBy { it.sortOrder }.map { ref ->
                    ProgramRequirement(
                        id = ref.requirementId,
                        progressId = ref.progressId,
                        courseId = ref.courseId,
                        required = ref.required,
                        progress = ref.progress,
                        enrollmentStatus = ref.enrollmentStatus?.let {
                            ProgramProgressCourseEnrollmentStatus.safeValueOf(it)
                        },
                    )
                },
            )
        }
    }

    suspend fun savePrograms(programs: List<Program>, enrolledCourseIds: Set<Long>) {
        val programEntities = programs.map { program ->
            HorizonDashboardProgramEntity(
                programId = program.id,
                programName = program.name,
                description = program.description,
                startDateMs = program.startDate?.time,
                endDateMs = program.endDate?.time,
                variant = program.variant.rawValue,
                courseCompletionCount = program.courseCompletionCount,
            )
        }
        val refs = programs.flatMap { program ->
            program.sortedRequirements
                .filter { it.courseId in enrolledCourseIds }
                .mapIndexed { index, req ->
                    HorizonDashboardProgramCourseRef(
                        programId = program.id,
                        courseId = req.courseId,
                        requirementId = req.id,
                        progressId = req.progressId,
                        required = req.required,
                        progress = req.progress,
                        enrollmentStatus = req.enrollmentStatus?.rawValue,
                        sortOrder = index,
                    )
                }
        }
        programDao.replaceAll(programEntities, refs)
    }
}
