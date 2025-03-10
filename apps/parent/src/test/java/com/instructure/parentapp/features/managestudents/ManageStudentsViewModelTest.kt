/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.parentapp.features.managestudents

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.parentapp.R
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class ManageStudentsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val repository: ManageStudentsRepository = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = spyk()
    private val analytics: Analytics = mockk(relaxed = true)
    private val selectedStudentHolder: SelectedStudentHolder = mockk(relaxed = true)

    private lateinit var viewModel: ManageStudentViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        ContextKeeper.appContext = context
        mockkObject(ColorUtils)
        every { ColorUtils.correctContrastForText(any(), any()) } answers { firstArg() }
        every { ColorUtils.correctContrastForButtonBackground(any(), any(), any()) } answers { firstArg() }
        every { context.getColor(any()) } answers { firstArg() }
        every { colorKeeper.createThemedColor(any()) } answers { ThemedColor(firstArg()) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(ColorUtils)
    }

    @Test
    fun `Load students`() {
        val students = listOf(User(id = 1, shortName = "Student 1", pronouns = "He/Him"))
        val expectedState = ManageStudentsUiState(
            studentListItems = listOf(
                StudentItemUiState(1, null, "Student 1", "He/Him", ThemedColor(1))
            )
        )
        coEvery { repository.getStudents(any()) } returns students
        coEvery { colorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(1)

        createViewModel()

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Load students error`() {
        val expectedState = ManageStudentsUiState(isLoadError = true)
        coEvery { repository.getStudents(any()) } throws Exception()

        createViewModel()

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Load students empty`() {
        val expectedState = ManageStudentsUiState(isLoading = false, isLoadError = false, studentListItems = emptyList())
        coEvery { repository.getStudents(any()) } returns emptyList()

        createViewModel()

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Navigate to alert settings screen`() = runTest {
        coEvery { repository.getStudents(any()) } returns listOf(User(id = 1))
        createViewModel()

        val events = mutableListOf<ManageStudentsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(ManageStudentsAction.StudentTapped(1L))

        val expected = ManageStudentsViewModelAction.NavigateToAlertSettings(User(id = 1))
        Assert.assertEquals(expected, events.last())
    }

    @Test
    fun `Refresh reloads students`() {
        createViewModel()

        viewModel.handleAction(ManageStudentsAction.Refresh)

        coVerify { repository.getStudents(true) }
    }

    @Test
    fun `Show color picker dialog`() {
        val userColors = listOf(
            UserColor(
                colorRes = R.color.studentBlue,
                color = ThemedColor(R.color.studentBlue),
                contentDescriptionRes = R.string.studentColorContentDescriptionBlue
            ),
            UserColor(
                colorRes = R.color.studentPurple,
                color = ThemedColor(R.color.studentPurple),
                contentDescriptionRes = R.string.studentColorContentDescriptionPurple
            ),
            UserColor(
                colorRes = R.color.studentPink,
                color = ThemedColor(R.color.studentPink),
                contentDescriptionRes = R.string.studentColorContentDescriptionPink
            ),
            UserColor(
                colorRes = R.color.studentRed,
                color = ThemedColor(R.color.studentRed),
                contentDescriptionRes = R.string.studentColorContentDescriptionRed
            ),
            UserColor(
                colorRes = R.color.studentOrange,
                color = ThemedColor(R.color.studentOrange),
                contentDescriptionRes = R.string.studentColorContentDescriptionOrange
            ),
            UserColor(
                colorRes = R.color.studentGreen,
                color = ThemedColor(R.color.studentGreen),
                contentDescriptionRes = R.string.studentColorContentDescriptionGreen
            ),
        )

        createViewModel()

        val initialUserColor = userColors[2]
        viewModel.handleAction(ManageStudentsAction.ShowColorPickerDialog(1L, initialUserColor.color))

        val expected = ManageStudentsUiState(
            colorPickerDialogUiState = ColorPickerDialogUiState(
                showColorPickerDialog = true,
                studentId = 1L,
                initialUserColor = initialUserColor,
                userColors = userColors
            )
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Hide color picker dialog`() = runTest {
        every { colorKeeper.userColors } returns emptyList()

        createViewModel()

        viewModel.handleAction(ManageStudentsAction.ShowColorPickerDialog(1L, ThemedColor(1)))
        Assert.assertTrue(viewModel.uiState.value.colorPickerDialogUiState.showColorPickerDialog)

        viewModel.handleAction(ManageStudentsAction.HideColorPickerDialog)
        Assert.assertFalse(viewModel.uiState.value.colorPickerDialogUiState.showColorPickerDialog)
    }

    @Test
    fun `Save student color`() = runTest {
        val expectedUiState = ManageStudentsUiState(
            colorPickerDialogUiState = ColorPickerDialogUiState(),
            studentListItems = listOf(
                StudentItemUiState(1, null, "Student 1", null, ThemedColor(2))
            )
        )
        val selectedUserColor = UserColor(
            colorRes = R.color.studentBlue,
            color = ThemedColor(2),
            contentDescriptionRes = R.string.studentColorContentDescriptionBlue
        )

        every { colorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(1)
        coEvery { repository.getStudents(any()) } returns listOf(User(id = 1, shortName = "Student 1"))
        coEvery { repository.saveStudentColor(any(), any()) } returns "#000000"
        every { ContextCompat.getColor(context, any()) } answers { firstArg() }
        every { context.getString(R.string.manageStudentsColorSavedSuccessfully) } returns "Color saved successfully"

        createViewModel()

        val events = mutableListOf<ManageStudentsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(ManageStudentsAction.StudentColorChanged(1L, selectedUserColor))

        Assert.assertEquals(expectedUiState, viewModel.uiState.value)
        Assert.assertEquals(ManageStudentsViewModelAction.AccessibilityAnnouncement("Color saved successfully"), events.last())
    }

    @Test
    fun `Save student color error`() = runTest {
        val expectedUiState = ManageStudentsUiState(
            colorPickerDialogUiState = ColorPickerDialogUiState(isSavingColorError = true),
            studentListItems = listOf(
                StudentItemUiState(1, null, "Student 1", null, ThemedColor(1))
            )
        )
        val selectedUserColor = UserColor(
            colorRes = R.color.studentBlue,
            color = ThemedColor(1),
            contentDescriptionRes = R.string.studentColorContentDescriptionBlue
        )

        every { colorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(1)
        coEvery { repository.getStudents(any()) } returns listOf(User(id = 1, shortName = "Student 1"))
        every { ContextCompat.getColor(context, any()) } answers { firstArg() }
        coEvery { repository.saveStudentColor(any(), any()) } throws Exception()
        every { context.getString(R.string.errorSavingColor) } returns "Error saving color"

        createViewModel()

        val events = mutableListOf<ManageStudentsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(ManageStudentsAction.StudentColorChanged(1L, selectedUserColor))

        Assert.assertEquals(expectedUiState, viewModel.uiState.value)
        Assert.assertEquals(ManageStudentsViewModelAction.AccessibilityAnnouncement("Error saving color"), events.last())
    }

    @Test
    fun `Add student`() = runTest {
        coEvery { repository.getStudents(any()) } returns listOf(User(id = 1))

        createViewModel()

        val events = mutableListOf<ManageStudentsViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(ManageStudentsAction.AddStudent)

        Assert.assertEquals(ManageStudentsViewModelAction.AddStudent, events.last())
        verify { analytics.logEvent(AnalyticsEventConstants.ADD_STUDENT_MANAGE_STUDENTS) }
    }

    private fun createViewModel() {
        viewModel = ManageStudentViewModel(context, colorKeeper, repository, selectedStudentHolder, analytics)
    }
}
