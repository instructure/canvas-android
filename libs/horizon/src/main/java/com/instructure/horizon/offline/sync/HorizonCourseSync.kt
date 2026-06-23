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
import com.instructure.horizon.database.dao.HorizonCourseDao
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonDashboardEnrollmentDao
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext
import javax.inject.Inject

class HorizonCourseSync @Inject constructor(
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao,
    private val fileSyncPlanDao: HorizonFileSyncPlanDao,
    private val enrollmentSyncer: EnrollmentSyncer,
    private val accountSyncer: AccountSyncer,
    private val notificationStreamSyncer: NotificationStreamSyncer,
    private val announcementSyncer: AnnouncementSyncer,
    private val courseContentSyncer: CourseContentSyncer,
    private val assignmentSyncer: AssignmentSyncer,
    private val pageSyncer: PageSyncer,
    private val scoreSyncer: ScoreSyncer,
    private val fileSyncer: FileSyncer,
    private val notebookSyncer: NotebookSyncer,
    private val courseCleanupHelper: CourseCleanupHelper,
    private val imageSyncer: ImageSyncer,
    private val dashboardEnrollmentDao: HorizonDashboardEnrollmentDao,
    private val courseDao: HorizonCourseDao,
) {
    suspend fun syncCourses(plans: List<HorizonCourseSyncPlanEntity>) {
        runHeadSync { accountSyncer.sync() }
        runHeadSync { enrollmentSyncer.sync() }
        runHeadSync { syncEnrollmentImages() }
        runHeadSync { notificationStreamSyncer.sync() }
        runHeadSync { announcementSyncer.sync() }

        for (plan in plans) {
            coroutineContext.ensureActive()
            syncCourse(plan)
        }
    }

    private suspend fun runHeadSync(block: suspend () -> Unit) {
        coroutineContext.ensureActive()
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Head syncer failure is non-fatal — per-course sync continues
        }
    }

    private suspend fun syncCourse(plan: HorizonCourseSyncPlanEntity) {
        courseSyncPlanDao.updateState(plan.courseId, HorizonProgressState.IN_PROGRESS)
        try {
            courseCleanupHelper.cleanupCourseContent(plan.courseId)

            val result = courseContentSyncer.sync(plan.courseId)
            courseSyncPlanDao.updateModulesState(plan.courseId, HorizonProgressState.COMPLETED)
            coroutineContext.ensureActive()

            val assignmentIds = extractAssignmentIds(result.moduleItems)
            val pageUrls = extractPageUrls(result.moduleItems)
            val fileIdsMutex = Mutex()
            val allAdditionalFileIds = result.additionalFileIds.toMutableSet()
            val allExternalUrls = result.externalFileUrls.toMutableSet()

            coroutineScope {
                if (plan.syncAssignments && assignmentIds.isNotEmpty()) {
                    launchSubSync({ courseSyncPlanDao.updateAssignmentsState(plan.courseId, it) }) {
                        val r = assignmentSyncer.syncAssignments(plan.courseId, assignmentIds)
                        fileIdsMutex.withLock {
                            allAdditionalFileIds += r.additionalFileIds
                            allExternalUrls += r.externalFileUrls
                        }
                    }
                } else if (plan.syncAssignments) {
                    courseSyncPlanDao.updateAssignmentsState(plan.courseId, HorizonProgressState.COMPLETED)
                }

                if (plan.syncPages && pageUrls.isNotEmpty()) {
                    launchSubSync({ courseSyncPlanDao.updatePagesState(plan.courseId, it) }) {
                        val r = pageSyncer.syncPages(plan.courseId, pageUrls)
                        fileIdsMutex.withLock {
                            allAdditionalFileIds += r.additionalFileIds
                            allExternalUrls += r.externalFileUrls
                        }
                    }
                } else if (plan.syncPages) {
                    courseSyncPlanDao.updatePagesState(plan.courseId, HorizonProgressState.COMPLETED)
                }

                if (plan.syncScores) {
                    launchSubSync({ courseSyncPlanDao.updateScoresState(plan.courseId, it) }) {
                        scoreSyncer.sync(plan.courseId)
                    }
                }

                if (plan.syncNotes) {
                    launchSubSync({ courseSyncPlanDao.updateNotesState(plan.courseId, it) }) {
                        notebookSyncer.syncNotes(plan.courseId)
                    }
                }
            }

            coroutineContext.ensureActive()

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
                    )
                    val fileStates = fileSyncPlanDao.findByCourseId(plan.courseId).map { it.state }
                    val anyErrored = fileStates.any { it == HorizonProgressState.ERROR }
                    val anyCompleted = fileStates.any { it == HorizonProgressState.COMPLETED }
                    val filesState = when {
                        anyErrored && anyCompleted -> HorizonProgressState.PARTIAL
                        anyErrored -> HorizonProgressState.ERROR
                        else -> HorizonProgressState.COMPLETED
                    }
                    courseSyncPlanDao.updateFilesState(plan.courseId, filesState)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    courseSyncPlanDao.updateFilesState(plan.courseId, HorizonProgressState.ERROR)
                }
            }

            courseSyncPlanDao.updateImagesState(plan.courseId, HorizonProgressState.IN_PROGRESS)
            syncCourseImages(plan.courseId)
            courseSyncPlanDao.updateImagesState(plan.courseId, HorizonProgressState.COMPLETED)

            courseSyncPlanDao.updateState(plan.courseId, resolveCourseFinalState(plan))
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            courseSyncPlanDao.updateState(plan.courseId, HorizonProgressState.ERROR)
        }
    }

    private suspend fun resolveCourseFinalState(plan: HorizonCourseSyncPlanEntity): HorizonProgressState {
        val current = courseSyncPlanDao.findByCourseId(plan.courseId) ?: return HorizonProgressState.COMPLETED
        val components = buildList {
            add(current.modulesState)
            if (plan.syncAssignments) add(current.assignmentsState)
            if (plan.syncPages) add(current.pagesState)
            if (plan.syncScores) add(current.scoresState)
            if (plan.syncNotes) add(current.notesState)
            if (plan.syncFiles) add(current.filesState)
            add(current.imagesState)
        }
        val anyErrored = components.any { it == HorizonProgressState.ERROR }
        val anyPartial = components.any { it == HorizonProgressState.PARTIAL }
        val anyCompleted = components.any { it == HorizonProgressState.COMPLETED }
        return when {
            anyPartial -> HorizonProgressState.PARTIAL
            anyErrored && anyCompleted -> HorizonProgressState.PARTIAL
            anyErrored -> HorizonProgressState.ERROR
            else -> HorizonProgressState.COMPLETED
        }
    }

    private fun CoroutineScope.launchSubSync(
        updateState: suspend (HorizonProgressState) -> Unit,
        block: suspend () -> Unit,
    ) = launch {
        try {
            block()
            updateState(HorizonProgressState.COMPLETED)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            updateState(HorizonProgressState.ERROR)
        }
    }

    private suspend fun syncEnrollmentImages() {
        try {
            val imageUrls = dashboardEnrollmentDao.getAll()
                .mapNotNull { it.courseImageUrl }
                .toSet()
            imageSyncer.syncImages(imageUrls)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Image sync failure is non-fatal
        }
    }

    private suspend fun syncCourseImages(courseId: Long) {
        try {
            val imageUrls = mutableSetOf<String>()
            courseDao.getByCourseId(courseId)?.imageUrl?.let { imageUrls.add(it) }
            imageSyncer.syncImages(imageUrls, courseId = courseId)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Image sync failure is non-fatal
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
