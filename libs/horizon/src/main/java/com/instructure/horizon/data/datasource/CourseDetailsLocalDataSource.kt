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

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.horizon.database.dao.HorizonDashboardProgramDao
import com.instructure.horizon.database.dao.HorizonLearnCourseDao
import com.instructure.horizon.database.entity.HorizonDashboardProgramCourseRef
import com.instructure.horizon.database.entity.HorizonDashboardProgramEntity
import com.instructure.horizon.database.entity.HorizonLearnCourseEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.SyncDataType
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import java.util.Date
import javax.inject.Inject

class CourseDetailsLocalDataSource @Inject constructor(
    private val learnCourseDao: HorizonLearnCourseDao,
    private val programDao: HorizonDashboardProgramDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {

    suspend fun getCourse(courseId: Long): CourseWithProgress {
        val entity = learnCourseDao.getByCourseId(courseId)
            ?: throw IllegalStateException("Course $courseId not found in cache")
        return entity.toCourseWithProgress()
    }

    suspend fun getProgramsForCourse(courseId: Long): List<Program> {
        val allPrograms = programDao.getAll()
        return allPrograms.mapNotNull { programEntity ->
            val refs = programDao.getRefsForProgram(programEntity.programId)
            val program = programEntity.toProgram(refs)
            if (program.sortedRequirements.firstOrNull()?.courseId == courseId) program else null
        }
    }

    suspend fun saveCourseDetails(course: CourseWithProgress, programs: List<Program>) {
        learnCourseDao.insertAll(listOf(course.toEntity()))
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = SyncDataType.COURSE_DETAILS,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }

    private fun HorizonLearnCourseEntity.toCourseWithProgress(): CourseWithProgress {
        return CourseWithProgress(
            courseId = courseId,
            courseName = courseName,
            courseImageUrl = null,
            courseSyllabus = courseSyllabus,
            progress = progress,
        )
    }

    private fun CourseWithProgress.toEntity(): HorizonLearnCourseEntity {
        return HorizonLearnCourseEntity(
            courseId = courseId,
            courseName = courseName,
            progress = progress,
            courseSyllabus = courseSyllabus,
            startDateMs = null,
            endDateMs = null,
            moduleItemsDurations = "",
        )
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
}
