package com.instructure.teacher.features.speedgrader.commentlibrary/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.CommentLibraryManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.UserSettings
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.HighlightedTextData
import com.instructure.pandautils.utils.Normalizer
import com.instructure.teacher.utils.TeacherPrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CommentLibraryViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()

    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val commentLibraryManager: CommentLibraryManager = mockk(relaxed = true)
    private val userManager: UserManager = mockk(relaxed = true)
    private val teacherPrefs: TeacherPrefs = mockk(relaxed = true)
    private val firebaseCrashlytics: FirebaseCrashlytics = mockk(relaxed = true)

    private lateinit var viewModel: CommentLibraryViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkObject(Normalizer)

        every { Normalizer.normalize(any()) } answers { firstArg() }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        
        unmockkObject(Normalizer)
    }

    @Test
    fun `Comment library is not loaded when it's not enabled in settings`() {
        // Given
        every { userManager.getSelfSettings(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(UserSettings())
        }

        // When
        viewModel = createViewModel()

        // Then
        coVerify(inverse = true) { commentLibraryManager.getCommentLibraryItems(any()) }
    }

    @Test
    fun `Comment is loaded when it's enabled in settings`() {
        // Given
        every { userManager.getSelfSettings(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(UserSettings(commentLibrarySuggestions = true))
        }

        // When
        viewModel = createViewModel()

        // Then
        coVerify { commentLibraryManager.getCommentLibraryItems(any()) }
    }

    @Test
    fun `Comment is loaded when it's settings request fails but user has settings cached`() {
        // Given
        every { userManager.getSelfSettings(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { teacherPrefs.commentLibraryEnabled } returns true

        // When
        viewModel = createViewModel()

        // Then
        coVerify { commentLibraryManager.getCommentLibraryItems(any()) }
    }

    @Test
    fun `Suggestions is empty when comment library doesn't return any suggestion`() {
        // Given
        every { userManager.getSelfSettings(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { teacherPrefs.commentLibraryEnabled } returns true
        coEvery { commentLibraryManager.getCommentLibraryItems(any()) } returns emptyList()

        // When
        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner, Observer {})

        // Then
        assertTrue(viewModel.data.value!!.isEmpty())
    }

    @Test
    fun `Show all suggestions initially when comment library is loaded`() {
        // Given
        every { userManager.getSelfSettings(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { teacherPrefs.commentLibraryEnabled } returns true
        coEvery { commentLibraryManager.getCommentLibraryItems(any()) } returns
                listOf(
                    "Great", "Fantastic", "Super"
                )

        // When
        viewModel = createViewModel()
        viewModel.data.observe(lifecycleOwner, Observer {})

        // Then
        val suggestions = viewModel.data.value!!.suggestions
        assertEquals(3, suggestions.size)

        val expectedItem1 = HighlightedTextData("Great", 0, 0)
        assertEquals(expectedItem1, suggestions[0].commentItemData)

        val expectedItem2 = HighlightedTextData("Fantastic", 0, 0)
        assertEquals(expectedItem2, suggestions[1].commentItemData)

        val expectedItem3 = HighlightedTextData("Super", 0, 0)
        assertEquals(expectedItem3, suggestions[2].commentItemData)
    }

    @Test
    fun `Show only filtered suggestions, when comment library is filtered`() {
        // Given
        val submissionId = 1L
        every { userManager.getSelfSettings(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { teacherPrefs.commentLibraryEnabled } returns true
        coEvery { commentLibraryManager.getCommentLibraryItems(any()) } returns
                listOf(
                    "Great", "Fantastic", "Super", "Great job"
                )

        // When
        viewModel = createViewModel()
        viewModel.currentSubmissionId = submissionId
        viewModel.setCommentBySubmission(submissionId, "Gre")
        viewModel.data.observe(lifecycleOwner, Observer {})

        // Then
        val suggestions = viewModel.data.value!!.suggestions
        assertEquals(2, suggestions.size)

        val expectedItem1 = HighlightedTextData("Great", 0, 3)
        assertEquals(expectedItem1, suggestions[0].commentItemData)

        val expectedItem2 = HighlightedTextData("Great job", 0, 3)
        assertEquals(expectedItem2, suggestions[1].commentItemData)
    }

    @Test
    fun `Verify that filtering should ignore case`() {
        // Given
        val submissionId = 1L
        every { userManager.getSelfSettings(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { teacherPrefs.commentLibraryEnabled } returns true
        coEvery { commentLibraryManager.getCommentLibraryItems(any()) } returns
                listOf(
                    "Great", "Fantastic", "Super", "This is great", "gReAt", "GrEaT"
                )

        // When
        viewModel = createViewModel()
        viewModel.currentSubmissionId = submissionId
        viewModel.setCommentBySubmission(submissionId, "great")
        viewModel.data.observe(lifecycleOwner, Observer {})

        // Then
        val suggestions = viewModel.data.value!!.suggestions
        assertEquals(4, suggestions.size)

        val expectedItem1 = HighlightedTextData("Great", 0, 5)
        assertEquals(expectedItem1, suggestions[0].commentItemData)

        val expectedItem2 = HighlightedTextData("This is great", 8, 13)
        assertEquals(expectedItem2, suggestions[1].commentItemData)

        val expectedItem3 = HighlightedTextData("gReAt", 0, 5)
        assertEquals(expectedItem3, suggestions[2].commentItemData)

        val expectedItem4 = HighlightedTextData("GrEaT", 0, 5)
        assertEquals(expectedItem4, suggestions[3].commentItemData)
    }

    @Test
    fun `Typing comment for a submission updates the correct comment by submission id`() {
        // Given
        val submissionId = 1L
        every { userManager.getSelfSettings(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { teacherPrefs.commentLibraryEnabled } returns true
        coEvery { commentLibraryManager.getCommentLibraryItems(any()) } returns
                listOf(
                    "Great", "Fantastic", "Super", "Great job"
                )

        // When
        viewModel = createViewModel()
        viewModel.currentSubmissionId = submissionId
        viewModel.setCommentBySubmission(submissionId, "Gre")
        viewModel.data.observe(lifecycleOwner, Observer {})

        // Then
        assertEquals("Gre", viewModel.getCommentBySubmission(submissionId).value?.comment)
    }

    @Test
    fun `Selecting an item from the suggestions replaces the comment with the suggestion and closes comment libraray`() {
        // Given
        val submissionId = 1L
        every { userManager.getSelfSettings(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { teacherPrefs.commentLibraryEnabled } returns true
        coEvery { commentLibraryManager.getCommentLibraryItems(any()) } returns
                listOf(
                    "Great", "Fantastic", "Super", "Great job"
                )

        // When
        viewModel = createViewModel()
        viewModel.currentSubmissionId = submissionId
        viewModel.setCommentBySubmission(submissionId, "Gre")
        viewModel.data.observe(lifecycleOwner, Observer {})

        viewModel.data.value!!.suggestions[0].onClick()

        // Then
        assertEquals("Great", viewModel.getCommentBySubmission(submissionId).value?.comment)
        assertTrue(viewModel.getCommentBySubmission(submissionId).value?.selectedFromSuggestion!!)
        assertEquals(CommentLibraryAction.CommentLibraryClosed, viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Changing submission updates the filtered suggestions`() {
        // Given
        val submissionId = 1L
        val newSubmissionId = 2L
        every { userManager.getSelfSettings(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }
        every { teacherPrefs.commentLibraryEnabled } returns true
        coEvery { commentLibraryManager.getCommentLibraryItems(any()) } returns
                listOf(
                    "Great", "Fantastic", "Super"
                )

        // When
        viewModel = createViewModel()
        // We preset the comment for the new submission id, this can be a case when an other submission has a draft comment saved
        viewModel.setCommentBySubmission(newSubmissionId, "Fant")

        viewModel.currentSubmissionId = submissionId
        viewModel.setCommentBySubmission(submissionId, "Gre")
        viewModel.data.observe(lifecycleOwner, Observer {})

        val expectedItem = HighlightedTextData("Great", 0, 3)
        assertEquals(expectedItem, viewModel.data.value!!.suggestions[0].commentItemData)

        viewModel.currentSubmissionId = newSubmissionId // Change to the other submission

        // Then
        val newExpectedItem = HighlightedTextData("Fantastic", 0, 4)
        assertEquals(newExpectedItem, viewModel.data.value!!.suggestions[0].commentItemData)
    }

    private fun createViewModel(): CommentLibraryViewModel =
        CommentLibraryViewModel(apiPrefs, commentLibraryManager, userManager, teacherPrefs, firebaseCrashlytics)
}