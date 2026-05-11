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

import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryModuleInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.horizon.data.datasource.LearnLearningLibraryLocalDataSource
import com.instructure.horizon.data.datasource.LearnLearningLibraryNetworkDataSource
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonGlobalSyncPlanDao
import com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity
import com.instructure.horizon.database.entity.HorizonGlobalSyncPlanEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class LearningLibrarySyncerTest {
    private val networkDataSource: LearnLearningLibraryNetworkDataSource = mockk(relaxed = true)
    private val localDataSource: LearnLearningLibraryLocalDataSource = mockk(relaxed = true)
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao = mockk(relaxed = true)
    private val globalSyncPlanDao: HorizonGlobalSyncPlanDao = mockk(relaxed = true)
    private val courseContentSyncer: CourseContentSyncer = mockk(relaxed = true)
    private val assignmentSyncer: AssignmentSyncer = mockk(relaxed = true)
    private val pageSyncer: PageSyncer = mockk(relaxed = true)
    private val fileSyncer: FileSyncer = mockk(relaxed = true)
    private val imageSyncer: ImageSyncer = mockk(relaxed = true)

    private fun syncer() = LearningLibrarySyncer(
        networkDataSource,
        localDataSource,
        courseSyncPlanDao,
        globalSyncPlanDao,
        courseContentSyncer,
        assignmentSyncer,
        pageSyncer,
        fileSyncer,
        imageSyncer,
    )

    private fun page(items: List<LearningLibraryCollectionItem>, hasNext: Boolean = false, nextCursor: String? = null) =
        LearningLibraryCollectionItemsResponse(
            items = items,
            pageInfo = LearningLibraryPageInfo(nextCursor, null, hasNext, false, null, null),
        )

    private fun pageItem(
        id: String,
        type: CollectionItemType,
        courseId: String? = "10",
        isEnrolled: Boolean? = true,
        resourceId: String? = null,
        imageUrl: String? = null,
    ): LearningLibraryCollectionItem = LearningLibraryCollectionItem(
        id = id,
        libraryId = "lib",
        itemType = type,
        displayOrder = 0.0,
        canvasCourse = courseId?.let {
            CanvasCourseInfo(
                courseId = it,
                canvasUrl = "https://canvas.test/courses/$it",
                courseName = "course-$it",
                courseImageUrl = imageUrl,
                moduleCount = 0.0,
                moduleItemCount = 0.0,
                estimatedDurationMinutes = null,
            )
        },
        moduleInfo = resourceId?.let {
            LearningLibraryModuleInfo(
                moduleId = "m",
                moduleItemId = "mi",
                moduleItemType = null,
                resourceId = it,
            )
        },
        programId = null,
        programCourseId = null,
        createdAt = Date(),
        updatedAt = Date(),
        isBookmarked = false,
        completionPercentage = null,
        isEnrolledInCanvas = isEnrolled,
        canvasEnrollmentId = null,
    )

    @Test
    fun `sync depaginates browse items and saves to local store`() = runTest {
        coEvery { networkDataSource.getEnrolledLearningLibraries(any(), any()) } returns emptyList()
        coEvery { networkDataSource.getLearningLibraryRecommendations(any()) } returns emptyList()
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = null, limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = false, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(listOf(pageItem("a", CollectionItemType.PAGE, courseId = null)), hasNext = true, nextCursor = "c1")
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = "c1", limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = false, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(listOf(pageItem("b", CollectionItemType.PAGE, courseId = null)))
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = null, limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = true, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(emptyList())

        val saved = slot<List<LearningLibraryCollectionItem>>()
        coEvery { localDataSource.saveBrowseItems(capture(saved)) } returns Unit

        syncer().sync()

        assertEquals(listOf("a", "b"), saved.captured.map { it.id })
    }

    @Test
    fun `sync syncs course content only for non-enrollable items in unplanned courses`() = runTest {
        val items = listOf(
            pageItem("p1", CollectionItemType.PAGE, courseId = "10", resourceId = "intro"),
            pageItem("a1", CollectionItemType.ASSIGNMENT, courseId = "10", resourceId = "55"),
            pageItem("c-enrolled", CollectionItemType.COURSE, courseId = "10", isEnrolled = true),
            pageItem("c-locked", CollectionItemType.COURSE, courseId = "999", isEnrolled = false),
            pageItem("p2", CollectionItemType.PAGE, courseId = "20", resourceId = "second"),
        )
        coEvery { networkDataSource.getEnrolledLearningLibraries(any(), any()) } returns emptyList()
        coEvery { networkDataSource.getLearningLibraryRecommendations(any()) } returns emptyList()
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = null, limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = false, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(items)
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = null, limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = true, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(emptyList())
        coEvery { courseSyncPlanDao.findAll() } returns listOf(
            HorizonCourseSyncPlanEntity(courseId = 20L, courseName = "planned"),
        )

        syncer().sync()

        // Course 10 has non-enrollable items → sync. Course 20 already planned → skip. Course 999 is enrollable-only → skip.
        coVerify(exactly = 1) { courseContentSyncer.sync(10L) }
        coVerify(exactly = 0) { courseContentSyncer.sync(20L) }
        coVerify(exactly = 0) { courseContentSyncer.sync(999L) }
        coVerify(exactly = 1) { pageSyncer.syncPages(10L, listOf("intro")) }
        coVerify(exactly = 1) { assignmentSyncer.syncAssignments(10L, listOf(55L)) }
    }

    @Test
    fun `sync transitions library state through IN_PROGRESS and COMPLETED`() = runTest {
        coEvery { networkDataSource.getEnrolledLearningLibraries(any(), any()) } returns emptyList()
        coEvery { networkDataSource.getLearningLibraryRecommendations(any()) } returns emptyList()
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = null, limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = false, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(emptyList())
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = null, limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = true, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(emptyList())
        coEvery { globalSyncPlanDao.getPlanOnce() } returnsMany listOf(
            HorizonGlobalSyncPlanEntity(),
            HorizonGlobalSyncPlanEntity(learningLibraryState = HorizonProgressState.IN_PROGRESS),
        )

        syncer().sync()

        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            globalSyncPlanDao.upsert(match { it.learningLibraryState == HorizonProgressState.IN_PROGRESS })
            globalSyncPlanDao.upsert(match { it.learningLibraryState == HorizonProgressState.COMPLETED })
        }
    }

    @Test
    fun `sync marks state ERROR when a step fails`() = runTest {
        coEvery { networkDataSource.getEnrolledLearningLibraries(any(), any()) } throws RuntimeException("boom")
        coEvery { networkDataSource.getLearningLibraryRecommendations(any()) } returns emptyList()
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = null, limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = false, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(emptyList())
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = null, limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = true, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(emptyList())
        coEvery { globalSyncPlanDao.getPlanOnce() } returns HorizonGlobalSyncPlanEntity()

        syncer().sync()

        coVerify { globalSyncPlanDao.upsert(match { it.learningLibraryState == HorizonProgressState.ERROR }) }
    }

    @Test
    fun `sync forwards image URLs from items collections and recommendations`() = runTest {
        coEvery { networkDataSource.getEnrolledLearningLibraries(any(), any()) } returns listOf(
            EnrolledLearningLibraryCollection(
                id = "col",
                name = "x",
                publicName = null,
                description = null,
                createdAt = Date(),
                updatedAt = Date(),
                totalItemCount = 1,
                items = listOf(pageItem("col-i", CollectionItemType.PAGE, courseId = "30", imageUrl = "https://img/collection.png")),
            )
        )
        coEvery { networkDataSource.getLearningLibraryRecommendations(any()) } returns emptyList()
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = null, limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = false, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(listOf(pageItem("b-i", CollectionItemType.PAGE, courseId = "40", imageUrl = "https://img/browse.png")))
        coEvery {
            networkDataSource.getLearningLibraryItems(
                cursor = null, limit = any(), searchQuery = null, typeFilter = null,
                bookmarkedOnly = true, completedOnly = false, sortBy = null, forceRefresh = true,
            )
        } returns page(emptyList())

        val captured = slot<Set<String>>()
        coEvery { imageSyncer.syncImages(capture(captured)) } returns Unit

        syncer().sync()

        assertTrue(captured.captured.contains("https://img/collection.png"))
        assertTrue(captured.captured.contains("https://img/browse.png"))
    }
}
