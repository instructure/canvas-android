/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.addstudent

import android.content.Context
import android.graphics.Color
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import com.instructure.testutils.collectForTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddStudentViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private lateinit var viewModel: AddStudentViewModel

    private val selectedStudentHolder: SelectedStudentHolder = mockk(relaxed = true)
    private val repository: AddStudentRepository = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val analytics: Analytics = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setup() {
        ContextKeeper.appContext = context

        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(Color.BLACK)
        every { selectedStudentHolder.selectedStudentState.value } returns mockk(relaxed = true)
        viewModel = AddStudentViewModel(selectedStudentHolder, repository, crashlytics, analytics)
    }

    @Test
    fun `pairStudent should emit PairStudentSuccess`() = runTest {
        coEvery { repository.pairStudent(any()) } returns DataResult.Success(Unit)

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.uiState.value.actionHandler(AddStudentAction.PairStudent("pairingCode"))

        events.addAll(viewModel.events.replayCache)

        assert(events.last() is AddStudentViewModelAction.PairStudentSuccess)

        verify { analytics.logEvent(AnalyticsEventConstants.ADD_STUDENT_SUCCESS) }
    }

    @Test
    fun `pairStudent should not emit PairStudentSuccess`() = runTest {
        coEvery { repository.pairStudent(any()) } returns DataResult.Fail()

        viewModel.uiState.value.actionHandler(AddStudentAction.PairStudent("pairingCode"))

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        assert(events.size == 0)

        assert(viewModel.uiState.value.isError)

        verify { analytics.logEvent(AnalyticsEventConstants.ADD_STUDENT_FAILURE) }
    }

    @Test
    fun `resetError should set isError to false`() = runTest {

        viewModel.uiState.value.actionHandler(AddStudentAction.ResetError)

        assert(viewModel.uiState.value.isError.not())
    }

    @Test
    fun `unpairStudent should emit UnpairStudentSuccess`() = runTest {
        coEvery { repository.unpairStudent(any()) } returns DataResult.Success(Unit)

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.uiState.value.actionHandler(AddStudentAction.UnpairStudent(1))

        events.addAll(viewModel.events.replayCache)

        assert(events.last() is AddStudentViewModelAction.UnpairStudentSuccess)
    }

    @Test
    fun `unpairStudent should emit UnpairStudentFailed on error`() = runTest {
        coEvery { repository.unpairStudent(any()) } throws Exception("Unpair failed")

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.uiState.value.actionHandler(AddStudentAction.UnpairStudent(1))

        events.addAll(viewModel.events.replayCache)

        assert(events.last() is AddStudentViewModelAction.UnpairStudentFailed)
    }
}
