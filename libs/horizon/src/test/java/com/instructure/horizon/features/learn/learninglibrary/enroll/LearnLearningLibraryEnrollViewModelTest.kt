/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.learn.learninglibrary.enroll

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.LearnEvent
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.features.learn.navigation.LearnRoute
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class LearnLearningLibraryEnrollViewModelTest {
    private val resources: Resources = mockk(relaxed = true)
    private val repository: LearnLearningLibraryEnrollRepository = mockk(relaxed = true)
    private val eventHandler: LearnEventHandler = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testItemId = "item-123"
    private val testCourseId = "456"
    private val testSyllabus = "This is the course syllabus"
    private val testCollectionItem = createTestCollectionItem(testItemId, testCourseId, "Test Course")
    private val testCourse = CourseWithProgress(
        courseId = testCourseId.toLong(),
        courseName = "Test Course",
        courseImageUrl = null,
        courseSyllabus = testSyllabus,
        progress = 0.0
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { resources.getString(any()) } returns ""
        coEvery { repository.loadLearningLibraryItem(any()) } returns testCollectionItem
        coEvery { repository.loadCourseDetails(any()) } returns testCourse
        coEvery { repository.enrollLearningLibraryItem(any()) } returns testCollectionItem
        coEvery { eventHandler.postEvent(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state has pull to refresh disabled`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.state.value.loadingState.isPullToRefreshEnabled)
    }

    @Test
    fun `Initial state has no syllabus`() = runTest {
        val viewModel = getViewModel(itemId = null)

        assertNull(viewModel.state.value.syllabus)
    }

    @Test
    fun `Initial state has isEnrollLoading false`() = runTest {
        val viewModel = getViewModel(itemId = null)

        assertFalse(viewModel.state.value.isEnrollLoading)
    }

    @Test
    fun `Initial state has no navigateToCourseId`() = runTest {
        val viewModel = getViewModel(itemId = null)

        assertNull(viewModel.state.value.navigateToCourseId)
    }

    @Test
    fun `loadData is triggered on init when ID is in SavedStateHandle`() = runTest {
        getViewModel()

        coVerify { repository.loadLearningLibraryItem(testItemId) }
    }

    @Test
    fun `loadData is not triggered on init when no ID in SavedStateHandle`() = runTest {
        getViewModel(itemId = null)

        coVerify(exactly = 0) { repository.loadLearningLibraryItem(any()) }
    }

    @Test
    fun `loadData success sets syllabus from course details`() = runTest {
        val viewModel = getViewModel()

        assertEquals(testSyllabus, viewModel.state.value.syllabus)
    }

    @Test
    fun `loadData success clears loading state`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.state.value.loadingState.isLoading)
    }

    @Test
    fun `loadData calls loadCourseDetails with courseId from collection item`() = runTest {
        getViewModel()

        coVerify { repository.loadCourseDetails(testCourseId.toLong()) }
    }

    @Test
    fun `loadData error clears loading state`() = runTest {
        coEvery { repository.loadLearningLibraryItem(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        assertFalse(viewModel.state.value.loadingState.isLoading)
    }

    @Test
    fun `loadData error does not set error state`() = runTest {
        coEvery { repository.loadLearningLibraryItem(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        assertFalse(viewModel.state.value.loadingState.isError)
    }

    @Test
    fun `loadData can be called manually with a new item id`() = runTest {
        val viewModel = getViewModel(itemId = null)

        viewModel.loadData("other-item")

        coVerify { repository.loadLearningLibraryItem("other-item") }
    }

    @Test
    fun `onEnroll success sets navigateToCourseId`() = runTest {
        val viewModel = getViewModel()

        viewModel.state.value.onEnrollClicked()

        assertEquals(testCourseId.toLong(), viewModel.state.value.navigateToCourseId)
    }

    @Test
    fun `onEnroll success clears isEnrollLoading`() = runTest {
        val viewModel = getViewModel()

        viewModel.state.value.onEnrollClicked()

        assertFalse(viewModel.state.value.isEnrollLoading)
    }

    @Test
    fun `onEnroll success posts RefreshLearningLibraryList event`() = runTest {
        val viewModel = getViewModel()

        viewModel.state.value.onEnrollClicked()

        coVerify { eventHandler.postEvent(LearnEvent.RefreshLearningLibraryList) }
    }

    @Test
    fun `onEnroll calls repository with item id`() = runTest {
        val viewModel = getViewModel()

        viewModel.state.value.onEnrollClicked()

        coVerify { repository.enrollLearningLibraryItem(testItemId) }
    }

    @Test
    fun `onEnroll error sets error message`() = runTest {
        every { resources.getString(R.string.learnLearningLibraryEnrollDialogFailedToEnrollMessage) } returns "Failed to enroll"
        coEvery { repository.enrollLearningLibraryItem(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.state.value.onEnrollClicked()

        assertNotNull(viewModel.state.value.loadingState.errorMessage)
    }

    @Test
    fun `onEnroll error clears isEnrollLoading`() = runTest {
        coEvery { repository.enrollLearningLibraryItem(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.state.value.onEnrollClicked()

        assertFalse(viewModel.state.value.isEnrollLoading)
    }

    @Test
    fun `onEnroll error does not set navigateToCourseId`() = runTest {
        coEvery { repository.enrollLearningLibraryItem(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.state.value.onEnrollClicked()

        assertNull(viewModel.state.value.navigateToCourseId)
    }

    @Test
    fun `resetNavigateToCourseId clears navigateToCourseId`() = runTest {
        val viewModel = getViewModel()
        viewModel.state.value.onEnrollClicked()

        viewModel.state.value.resetNavigateToCourseId()

        assertNull(viewModel.state.value.navigateToCourseId)
    }

    private fun getViewModel(itemId: String? = testItemId): LearnLearningLibraryEnrollViewModel {
        val savedStateHandle = if (itemId != null) {
            SavedStateHandle(mapOf(LearnRoute.LearnLearningLibraryEnrollScreen.learningLibraryIdAttr to itemId))
        } else {
            SavedStateHandle()
        }
        return LearnLearningLibraryEnrollViewModel(savedStateHandle, resources, repository, eventHandler)
    }

    private fun createTestCollectionItem(
        id: String,
        courseId: String,
        courseName: String
    ): LearningLibraryCollectionItem = LearningLibraryCollectionItem(
        id = id,
        libraryId = "library1",
        itemType = CollectionItemType.COURSE,
        displayOrder = 1.0,
        canvasCourse = CanvasCourseInfo(
            courseId = courseId,
            canvasUrl = "https://example.com",
            courseName = courseName,
            courseImageUrl = "https://example.com/image.png",
            moduleCount = 5.0,
            moduleItemCount = 20.0,
            estimatedDurationMinutes = 120.0
        ),
        programId = null,
        programCourseId = null,
        createdAt = Date(),
        updatedAt = Date(),
        isBookmarked = false,
        completionPercentage = 0.0,
        isEnrolledInCanvas = false
    )
}