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

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.horizon.database.dao.HorizonDashboardProgramDao
import com.instructure.horizon.database.dao.HorizonLearnCourseDao
import com.instructure.horizon.database.entity.HorizonDashboardProgramCourseRef
import com.instructure.horizon.database.entity.HorizonDashboardProgramEntity
import com.instructure.horizon.database.entity.HorizonLearnCourseEntity
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import java.util.Date
import javax.inject.Inject

class ProgramDetailsLocalDataSource @Inject constructor(
    private val programDao: HorizonDashboardProgramDao,
    private val learnCourseDao: HorizonLearnCourseDao,
) {

    suspend fun getProgramDetails(programId: String): Program {
        val entity = programDao.getAll().find { it.programId == programId }
            ?: throw IllegalArgumentException("Program with id $programId not found in cache")
        val refs = programDao.getRefsForProgram(programId)
        return entity.toProgram(refs)
    }

    suspend fun getCoursesById(courseIds: List<Long>): List<CourseWithModuleItemDurations> {
        return learnCourseDao.getByCourseIds(courseIds).map { it.toCourseWithModuleItemDurations() }
    }

    suspend fun saveCourses(courses: List<CourseWithModuleItemDurations>) {
        learnCourseDao.insertAll(courses.map { it.toEntity() })
    }

    private fun HorizonDashboardProgramEntity.toProgram(refs: List<HorizonDashboardProgramCourseRef>): Program {
        val requirements = refs.sortedBy { it.sortOrder }.map { ref ->
            ProgramRequirement(
                id = ref.requirementId,
                progressId = ref.progressId,
                courseId = ref.courseId,
                required = ref.required,
                progress = ref.progress,
                enrollmentStatus = ref.enrollmentStatus?.let {
                    runCatching { ProgramProgressCourseEnrollmentStatus.valueOf(it) }.getOrNull()
                },
            )
        }
        return Program(
            id = programId,
            name = programName,
            description = description,
            startDate = startDateMs?.let { Date(it) },
            endDate = endDateMs?.let { Date(it) },
            variant = runCatching { ProgramVariantType.valueOf(variant) }.getOrDefault(ProgramVariantType.LINEAR),
            courseCompletionCount = courseCompletionCount,
            sortedRequirements = requirements,
        )
    }

    private fun HorizonLearnCourseEntity.toCourseWithModuleItemDurations(): CourseWithModuleItemDurations {
        return CourseWithModuleItemDurations(
            courseId = courseId,
            courseName = courseName,
            moduleItemsDuration = if (moduleItemsDurations.isEmpty()) emptyList() else moduleItemsDurations.split(","),
            startDate = startDateMs?.let { Date(it) },
            endDate = endDateMs?.let { Date(it) },
        )
    }

    private fun CourseWithModuleItemDurations.toEntity(): HorizonLearnCourseEntity {
        return HorizonLearnCourseEntity(
            courseId = courseId,
            courseName = courseName,
            progress = 0.0,
            courseSyllabus = null,
            startDateMs = startDate?.time,
            endDateMs = endDate?.time,
            moduleItemsDurations = moduleItemsDuration.joinToString(","),
        )
    }
}
