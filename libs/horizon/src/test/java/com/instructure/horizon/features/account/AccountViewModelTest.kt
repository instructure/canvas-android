/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.account

import android.content.Context
import com.instructure.canvasapi2.models.ExperienceSummary
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.utils.LogoutHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
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

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val repository: AccountRepository = mockk(relaxed = true)
    private val logoutHelper: LogoutHelper = mockk(relaxed = true)
    private val databaseProvider: DatabaseProvider = mockk(relaxed = true)
    private val alarmScheduler: AlarmScheduler = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testUser = User(
        id = 1L,
        name = "Test User",
        shortName = "TUser"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getUserDetails(any()) } returns testUser
        coEvery { repository.getExperiences(any()) } returns listOf()
        every { context.getString(any()) } returns "Test String"
        every { context.getString(any(), any()) } returns "Test String"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test data loads successfully on init`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.screenState.isLoading)
        coVerify { repository.getUserDetails(false) }
    }

    @Test
    fun `Test user name is set from repository`() = runTest {
        val viewModel = getViewModel()

        assertEquals(testUser.shortName, viewModel.uiState.value.userName)
    }

    @Test
    fun `Test user name falls back to name if shortName is null`() = runTest {
        val userWithoutShortName = testUser.copy(shortName = null)
        coEvery { repository.getUserDetails(any()) } returns userWithoutShortName

        val viewModel = getViewModel()

        assertEquals(testUser.name, viewModel.uiState.value.userName)
    }

    @Test
    fun `Test failed data load sets error state`() = runTest {
        coEvery { repository.getUserDetails(any()) } throws Exception("Network error")

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.screenState.isLoading)
        assertTrue(viewModel.uiState.value.screenState.isError)
    }

    @Test
    fun `Test experience switcher is shown when academic experience is available`() = runTest {
        coEvery { repository.getExperiences(any()) } returns listOf(ExperienceSummary.ACADEMIC_EXPERIENCE)

        val viewModel = getViewModel()

        val hasExperienceGroup = viewModel.uiState.value.accountGroups.any { group ->
            group.items.any { it.type is AccountItemType.SwitchExperience }
        }
        assertTrue(hasExperienceGroup)
    }

    @Test
    fun `Test experience switcher is hidden when academic experience is not available`() = runTest {
        coEvery { repository.getExperiences(any()) } returns listOf()

        val viewModel = getViewModel()

        val hasExperienceGroup = viewModel.uiState.value.accountGroups.any { group ->
            group.items.any { it.type is AccountItemType.SwitchExperience }
        }
        assertFalse(hasExperienceGroup)
    }

    @Test
    fun `Test account groups are initialized`() = runTest {
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.accountGroups.isNotEmpty())
    }

    @Test
    fun `Test settings group contains expected items`() = runTest {
        val viewModel = getViewModel()

        val settingsGroup = viewModel.uiState.value.accountGroups.firstOrNull { group ->
            group.items.any { it.type is AccountItemType.Open }
        }

        assertTrue(settingsGroup != null)
        assertTrue(settingsGroup!!.items.isNotEmpty())
    }

    @Test
    fun `Test logout group is present`() = runTest {
        val viewModel = getViewModel()

        val logoutGroup = viewModel.uiState.value.accountGroups.firstOrNull { group ->
            group.items.any { it.type is AccountItemType.LogOut }
        }

        assertTrue(logoutGroup != null)
    }

    @Test
    fun `Test update user name changes state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateUserName("New Name")

        assertEquals("New Name", viewModel.uiState.value.userName)
    }

    @Test
    fun `Test perform logout calls logout helper`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.performLogout()

        verify { logoutHelper.logout(databaseProvider, alarmScheduler) }
    }

    @Test
    fun `Test switch experience sets restart flag`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.switchExperience()

        assertTrue(viewModel.uiState.value.restartApp)
        verify { apiPrefs.canvasCareerView = false }
    }

    @Test
    fun `Test experiences are fetched`() = runTest {
        val viewModel = getViewModel()

        coVerify { repository.getExperiences(false) }
    }

    private fun getViewModel(): AccountViewModel {
        return AccountViewModel(
            context,
            repository,
            logoutHelper,
            databaseProvider,
            alarmScheduler,
            apiPrefs
        )
    }
}
