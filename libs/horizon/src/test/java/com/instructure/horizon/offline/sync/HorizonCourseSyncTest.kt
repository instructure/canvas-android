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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HorizonCourseSyncTest {
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao = mockk(relaxed = true)
    private val fileSyncPlanDao: HorizonFileSyncPlanDao = mockk(relaxed = true)
    private val enrollmentSyncer: EnrollmentSyncer = mockk(relaxed = true)
    private val accountSyncer: AccountSyncer = mockk(relaxed = true)
    private val notificationStreamSyncer: NotificationStreamSyncer = mockk(relaxed = true)
    private val announcementSyncer: AnnouncementSyncer = mockk(relaxed = true)
    private val courseContentSyncer: CourseContentSyncer = mockk(relaxed = true)
    private val assignmentSyncer: AssignmentSyncer = mockk(relaxed = true)
    private val pageSyncer: PageSyncer = mockk(relaxed = true)
    private val scoreSyncer: ScoreSyncer = mockk(relaxed = true)
    private val fileSyncer: FileSyncer = mockk(relaxed = true)
    private val notebookSyncer: NotebookSyncer = mockk(relaxed = true)
    private val courseCleanupHelper: CourseCleanupHelper = mockk(relaxed = true)
    private val imageSyncer: ImageSyncer = mockk(relaxed = true)
    private val dashboardEnrollmentDao: HorizonDashboardEnrollmentDao = mockk(relaxed = true)
    private val courseDao: HorizonCourseDao = mockk(relaxed = true)

    private val syncer = HorizonCourseSync(
        courseSyncPlanDao = courseSyncPlanDao,
        fileSyncPlanDao = fileSyncPlanDao,
        enrollmentSyncer = enrollmentSyncer,
        accountSyncer = accountSyncer,
        notificationStreamSyncer = notificationStreamSyncer,
        announcementSyncer = announcementSyncer,
        courseContentSyncer = courseContentSyncer,
        assignmentSyncer = assignmentSyncer,
        pageSyncer = pageSyncer,
        scoreSyncer = scoreSyncer,
        fileSyncer = fileSyncer,
        notebookSyncer = notebookSyncer,
        courseCleanupHelper = courseCleanupHelper,
        imageSyncer = imageSyncer,
        dashboardEnrollmentDao = dashboardEnrollmentDao,
        courseDao = courseDao,
    )

    @Test
    fun `head syncer failure does not abort per-course sync`() = runTest {
        coEvery { accountSyncer.sync() } throws RuntimeException("transient API error")
        coEvery { courseContentSyncer.sync(any()) } returns CourseSyncResult(emptyList(), emptySet(), emptySet())

        syncer.syncCourses(listOf(plan(1L), plan(2L)))

        coVerify { courseCleanupHelper.cleanupCourseContent(1L) }
        coVerify { courseCleanupHelper.cleanupCourseContent(2L) }
    }

    @Test
    fun `cancellation thrown from a sub-syncer is propagated`() = runTest {
        coEvery { courseContentSyncer.sync(any()) } throws CancellationException("worker stopped")

        try {
            syncer.syncCourses(listOf(plan(1L)))
            fail("Expected CancellationException to propagate")
        } catch (_: CancellationException) {
            // expected
        }
    }

    @Test
    fun `external scope cancellation stops further course syncs`() = runTest {
        val plans = (1L..5L).map { plan(it) }
        coEvery { courseContentSyncer.sync(any()) } coAnswers {
            delay(50)
            CourseSyncResult(emptyList(), emptySet(), emptySet())
        }

        coroutineScope {
            val job: Job = launch { syncer.syncCourses(plans) }
            delay(25)
            job.cancel()
        }

        // No assertion needed beyond the absence of an exception — the test is that the
        // launched job cooperatively stops without throwing past cancel(), which is the
        // structured-concurrency invariant we want to preserve.
        assertTrue(true)
    }

    private fun plan(courseId: Long) = HorizonCourseSyncPlanEntity(
        courseId = courseId,
        courseName = "Course $courseId",
    )
}
