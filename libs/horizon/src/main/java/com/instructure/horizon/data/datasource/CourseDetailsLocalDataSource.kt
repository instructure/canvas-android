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
import com.instructure.horizon.database.dao.HorizonCourseDao
import com.instructure.horizon.database.dao.HorizonEntitySyncMetadataDao
import com.instructure.horizon.database.dao.HorizonProgramDao
import com.instructure.horizon.database.dao.HorizonSyncMetadataDao
import com.instructure.horizon.database.entity.EntitySyncType
import com.instructure.horizon.database.entity.HorizonCourseEntity
import com.instructure.horizon.database.entity.HorizonEntitySyncMetadataEntity
import com.instructure.horizon.database.entity.HorizonProgramCourseRef
import com.instructure.horizon.database.entity.HorizonProgramEntity
import com.instructure.horizon.database.entity.HorizonSyncMetadataEntity
import com.instructure.horizon.database.entity.SyncDataType
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import java.util.Date
import javax.inject.Inject

class CourseDetailsLocalDataSource @Inject constructor(
    private val courseDao: HorizonCourseDao,
    private val programDao: HorizonProgramDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
    private val entitySyncMetadataDao: HorizonEntitySyncMetadataDao,
) {

    suspend fun getCourse(courseId: Long): CourseWithProgress {
        val entity = courseDao.getByCourseId(courseId)
            ?: throw IllegalStateException("Course $courseId not found in cache")
        return entity.toCourseWithProgress()
    }

    suspend fun getProgramsForCourse(courseId: Long): List<Program> {
        val allPrograms = programDao.getAll()
        return allPrograms.mapNotNull { programEntity ->
            val refs = programDao.getRefsForProgram(programEntity.programId)
            val program = programEntity.toProgram(refs)
            if (program.sortedRequirements.any { it.courseId == courseId }) program else null
        }
    }

    suspend fun saveCourseDetails(course: CourseWithProgress, programs: List<Program>) {
        courseDao.insertIfAbsent(listOf(course.toDefaultEntity()))
        courseDao.updateCourseDetailsFields(
            courseId = course.courseId,
            name = course.courseName,
            progress = course.progress,
            imageUrl = course.courseImageUrl,
            courseSyllabus = course.courseSyllabus,
        )
        val programEntities = programs.map { it.toEntity() }
        val refs = programs.flatMap { program ->
            program.sortedRequirements.mapIndexed { index, req ->
                HorizonProgramCourseRef(
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
        programDao.insertAll(programEntities)
        programDao.insertAllRefs(refs)
        val now = System.currentTimeMillis()
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                dataType = SyncDataType.COURSE_DETAILS,
                lastSyncedAtMs = now,
            )
        )
        entitySyncMetadataDao.upsert(
            HorizonEntitySyncMetadataEntity(EntitySyncType.COURSE, course.courseId, now)
        )
    }

    private fun HorizonCourseEntity.toCourseWithProgress(): CourseWithProgress {
        return CourseWithProgress(
            courseId = courseId,
            courseName = name,
            courseImageUrl = imageUrl,
            courseSyllabus = courseSyllabus,
            progress = progress,
        )
    }

    private fun CourseWithProgress.toDefaultEntity(): HorizonCourseEntity {
        return HorizonCourseEntity(
            courseId = courseId,
            name = courseName,
            progress = progress,
            imageUrl = courseImageUrl,
            startAtMs = null,
            endAtMs = null,
            requirementCount = null,
            requirementCompletedCount = null,
            completedAtMs = null,
            grade = null,
            workflowState = null,
            lastActivityAtMs = null,
            enrolledAtMs = null,
            courseSyllabus = courseSyllabus,
            moduleItemsDurations = "",
        )
    }

    private fun Program.toEntity(): HorizonProgramEntity {
        return HorizonProgramEntity(
            programId = id,
            name = name,
            description = description,
            startDateMs = startDate?.time,
            endDateMs = endDate?.time,
            variant = variant.rawValue,
            estimatedDurationMinutes = null,
            courseCount = sortedRequirements.size,
            courseCompletionCount = courseCompletionCount,
            enrolledAtMs = null,
            completionPercentage = null,
            enrollmentStatus = null,
        )
    }

    private fun HorizonProgramEntity.toProgram(refs: List<HorizonProgramCourseRef>): Program {
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
            name = name,
            description = description,
            startDate = startDateMs?.let { Date(it) },
            endDate = endDateMs?.let { Date(it) },
            variant = runCatching { ProgramVariantType.valueOf(variant) }.getOrDefault(ProgramVariantType.LINEAR),
            courseCompletionCount = courseCompletionCount,
            sortedRequirements = requirements,
        )
    }
}
