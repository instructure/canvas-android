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
package com.instructure.horizon.offline.sync

import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class HorizonCourseSync @Inject constructor(
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao,
    private val fileSyncPlanDao: HorizonFileSyncPlanDao,
    private val enrollmentSyncer: EnrollmentSyncer,
    private val courseContentSyncer: CourseContentSyncer,
    private val assignmentSyncer: AssignmentSyncer,
    private val pageSyncer: PageSyncer,
    private val scoreSyncer: ScoreSyncer,
    private val fileSyncer: FileSyncer,
) {
    @Volatile
    var isStopped = false

    suspend fun syncCourses(plans: List<HorizonCourseSyncPlanEntity>) {
        try {
            enrollmentSyncer.sync()
        } catch (_: Exception) {
            // Global sync failure is non-fatal for individual courses
        }

        for (plan in plans) {
            if (isStopped) break
            syncCourse(plan)
        }
    }

    private suspend fun syncCourse(plan: HorizonCourseSyncPlanEntity) {
        courseSyncPlanDao.updateState(plan.courseId, HorizonProgressState.IN_PROGRESS)
        try {
            val result = courseContentSyncer.sync(plan.courseId)
            courseSyncPlanDao.updateModulesState(plan.courseId, HorizonProgressState.COMPLETED)
            if (isStopped) return

            val assignmentIds = extractAssignmentIds(result.moduleItems)
            val pageUrls = extractPageUrls(result.moduleItems)
            val allAdditionalFileIds = result.additionalFileIds.toMutableSet()
            val allExternalUrls = result.externalFileUrls.toMutableSet()

            coroutineScope {
                if (plan.syncAssignments && assignmentIds.isNotEmpty()) {
                    launch {
                        try {
                            val r = assignmentSyncer.syncAssignments(plan.courseId, assignmentIds)
                            allAdditionalFileIds += r.additionalFileIds
                            allExternalUrls += r.externalFileUrls
                            courseSyncPlanDao.updateAssignmentsState(plan.courseId, HorizonProgressState.COMPLETED)
                        } catch (_: Exception) {
                            courseSyncPlanDao.updateAssignmentsState(plan.courseId, HorizonProgressState.ERROR)
                        }
                    }
                } else if (plan.syncAssignments) {
                    courseSyncPlanDao.updateAssignmentsState(plan.courseId, HorizonProgressState.COMPLETED)
                }

                if (plan.syncPages && pageUrls.isNotEmpty()) {
                    launch {
                        try {
                            val r = pageSyncer.syncPages(plan.courseId, pageUrls)
                            allAdditionalFileIds += r.additionalFileIds
                            allExternalUrls += r.externalFileUrls
                            courseSyncPlanDao.updatePagesState(plan.courseId, HorizonProgressState.COMPLETED)
                        } catch (_: Exception) {
                            courseSyncPlanDao.updatePagesState(plan.courseId, HorizonProgressState.ERROR)
                        }
                    }
                } else if (plan.syncPages) {
                    courseSyncPlanDao.updatePagesState(plan.courseId, HorizonProgressState.COMPLETED)
                }

                if (plan.syncScores) {
                    launch {
                        try {
                            scoreSyncer.sync(plan.courseId)
                            courseSyncPlanDao.updateScoresState(plan.courseId, HorizonProgressState.COMPLETED)
                        } catch (_: Exception) {
                            courseSyncPlanDao.updateScoresState(plan.courseId, HorizonProgressState.ERROR)
                        }
                    }
                }
            }

            if (isStopped) return

            if (plan.syncFiles) {
                try {
                    val selectedFileIds = fileSyncPlanDao.findByCourseId(plan.courseId)
                        .filter { !it.isAdditionalFile }
                        .map { it.fileId }
                    fileSyncer.syncFiles(
                        courseId = plan.courseId,
                        selectedFileIds = selectedFileIds,
                        additionalFileIds = allAdditionalFileIds,
                        externalUrls = allExternalUrls,
                        isStopped = { isStopped },
                    )
                    courseSyncPlanDao.updateFilesState(plan.courseId, HorizonProgressState.COMPLETED)
                } catch (_: Exception) {
                    courseSyncPlanDao.updateFilesState(plan.courseId, HorizonProgressState.ERROR)
                }
            }

            courseSyncPlanDao.updateState(plan.courseId, HorizonProgressState.COMPLETED)
        } catch (_: Exception) {
            courseSyncPlanDao.updateState(plan.courseId, HorizonProgressState.ERROR)
        }
    }

    private fun extractAssignmentIds(modules: List<ModuleObject>): List<Long> {
        return modules.flatMap { it.items }
            .filter { it.type == ModuleItem.Type.Assignment.name }
            .map { it.contentId }
            .distinct()
    }

    private fun extractPageUrls(modules: List<ModuleObject>): List<String> {
        return modules.flatMap { it.items }
            .filter { it.type == ModuleItem.Type.Page.name }
            .mapNotNull { it.pageUrl }
            .distinct()
    }
}
