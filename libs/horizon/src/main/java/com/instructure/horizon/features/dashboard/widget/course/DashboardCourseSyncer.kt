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
package com.instructure.horizon.features.dashboard.widget.course

import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.database.course.HorizonDashboardCourseDao
import com.instructure.horizon.database.course.HorizonDashboardCourseEntity
import com.instructure.horizon.database.moduleitem.HorizonDashboardModuleItemDao
import com.instructure.horizon.database.moduleitem.HorizonDashboardModuleItemEntity
import com.instructure.horizon.database.program.HorizonDashboardProgramCourseRef
import com.instructure.horizon.database.program.HorizonDashboardProgramDao
import com.instructure.horizon.database.program.HorizonDashboardProgramEntity
import com.instructure.horizon.database.sync.HorizonSyncMetadataDao
import com.instructure.horizon.database.sync.HorizonSyncMetadataEntity
import com.instructure.horizon.model.LearningObjectType
import com.instructure.horizon.offline.SyncPolicy
import javax.inject.Inject

class DashboardCourseSyncer @Inject constructor(
    private val courseDao: HorizonDashboardCourseDao,
    private val programDao: HorizonDashboardProgramDao,
    private val moduleItemDao: HorizonDashboardModuleItemDao,
    private val syncMetadataDao: HorizonSyncMetadataDao,
) {

    suspend fun syncCourses(enrollments: List<GetCoursesQuery.Enrollment>, policy: SyncPolicy) {
        if (policy == SyncPolicy.SKIP_IF_PRESENT && courseDao.count() > 0) return
        val entities = enrollments.mapNotNull { enrollment ->
            val course = enrollment.course ?: return@mapNotNull null
            val completionPercentage = course.usersConnection?.nodes
                ?.firstOrNull()?.courseProgression?.requirements?.completionPercentage ?: 0.0
            HorizonDashboardCourseEntity(
                enrollmentId = enrollment.id?.toLongOrNull() ?: return@mapNotNull null,
                courseId = course.id.toLongOrNull() ?: return@mapNotNull null,
                courseName = course.name,
                courseImageUrl = course.image_download_url,
                completionPercentage = completionPercentage,
                enrollmentState = enrollment.state.rawValue,
            )
        }
        courseDao.replaceAll(entities)
        if (policy == SyncPolicy.ALWAYS_REPLACE) updateLastSync()
    }

    suspend fun syncPrograms(programs: List<Program>, policy: SyncPolicy) {
        if (policy == SyncPolicy.SKIP_IF_PRESENT) return
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

    suspend fun syncModuleItem(courseId: Long, modules: List<ModuleObject>, policy: SyncPolicy) {
        if (policy == SyncPolicy.SKIP_IF_PRESENT) return
        val firstItem = modules.flatMap { it.items }.firstOrNull() ?: return
        val entity = HorizonDashboardModuleItemEntity(
            moduleItemId = firstItem.id,
            courseId = courseId,
            moduleItemTitle = firstItem.title.orEmpty(),
            moduleItemType = if (firstItem.quizLti) LearningObjectType.ASSESSMENT.name
                             else LearningObjectType.fromApiString(firstItem.type.orEmpty()).name,
            dueDateMs = firstItem.moduleDetails?.dueDate?.time,
            estimatedDuration = firstItem.estimatedDuration,
            isQuizLti = firstItem.quizLti,
        )
        moduleItemDao.insertAll(listOf(entity))
    }

    suspend fun getLastSyncedAt(): Long? {
        return syncMetadataDao.getLastSyncedAt(HorizonSyncMetadataEntity.KEY_DASHBOARD_COURSES)
    }

    private suspend fun updateLastSync() {
        syncMetadataDao.upsert(
            HorizonSyncMetadataEntity(
                key = HorizonSyncMetadataEntity.KEY_DASHBOARD_COURSES,
                lastSyncedAtMs = System.currentTimeMillis(),
            )
        )
    }
}
