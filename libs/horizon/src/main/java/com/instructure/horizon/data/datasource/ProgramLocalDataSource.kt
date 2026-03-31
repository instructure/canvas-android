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
import com.instructure.horizon.database.course.HorizonDashboardCourseDao
import com.instructure.horizon.database.program.HorizonDashboardProgramCourseRef
import com.instructure.horizon.database.program.HorizonDashboardProgramDao
import com.instructure.horizon.database.program.HorizonDashboardProgramEntity
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import javax.inject.Inject

class ProgramLocalDataSource @Inject constructor(
    private val programDao: HorizonDashboardProgramDao,
    private val courseDao: HorizonDashboardCourseDao,
) : ProgramDataSource {

    override suspend fun getPrograms(): List<Program> {
        return programDao.getAll().map { programEntity ->
            val refs = programDao.getRefsForProgram(programEntity.programId)
            Program(
                id = programEntity.programId,
                name = programEntity.programName,
                description = null,
                startDate = null,
                endDate = null,
                variant = ProgramVariantType.UNKNOWN__,
                sortedRequirements = refs.map { ref ->
                    ProgramRequirement(
                        id = "",
                        progressId = "",
                        courseId = ref.courseId,
                        required = false,
                        enrollmentStatus = ref.enrollmentStatus?.let {
                            ProgramProgressCourseEnrollmentStatus.safeValueOf(it)
                        },
                    )
                }
            )
        }
    }

    suspend fun savePrograms(programs: List<Program>) {
        val courseIds = courseDao.getAllCourseIds().toSet()
        val programEntities = programs.map { HorizonDashboardProgramEntity(it.id, it.name) }
        val refs = programs.flatMap { program ->
            program.sortedRequirements
                .filter { it.courseId in courseIds }
                .map { req ->
                    HorizonDashboardProgramCourseRef(
                        programId = program.id,
                        courseId = req.courseId,
                        enrollmentStatus = req.enrollmentStatus?.rawValue,
                    )
                }
        }
        programDao.deleteAllRefs()
        programDao.deleteAll()
        programDao.insertAll(programEntities)
        programDao.insertAllRefs(refs)
    }
}
