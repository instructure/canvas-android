package com.instructure.parentapp.features.login.createaccount/*
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
 */
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.studentColor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAccountViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private val repository: CreateAccountRepository = mockk(relaxed = true)

    private lateinit var viewModel: CreateAccountViewModel

    private val testDomain = "test_domain"
    private val accountId = "1"
    private val pairingCode = "AAA"

    private val termsTestResponse = TermsOfService(content = "Content")

    private fun createViewModel() {
        viewModel = CreateAccountViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            repository = repository
        )
    }

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        mockkStatic(User::studentColor)

        coEvery { savedStateHandle.get<String>(CreateAccountFragment.ACCOUNT_ID) } returns accountId
        coEvery { savedStateHandle.get<String>(CreateAccountFragment.PAIRING_CODE) } returns pairingCode
        coEvery { savedStateHandle.get<String>(CreateAccountFragment.DOMAIN) } returns testDomain
        coEvery { repository.getTermsOfService(any(), any()) } returns termsTestResponse
        coEvery { context.getString(any()) } returns "Some String"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Terms are loaded when page is opened`() = runTest {
        val expectedUiState = CreateAccountUiState(termsOfService = termsTestResponse)

        createViewModel()
        coVerify { repository.getTermsOfService(testDomain, accountId) }
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `User inputs updated`() = runTest {
        val expectedUiState = CreateAccountUiState(
            termsOfService = termsTestResponse,
            name = "Test Name",
            email = "test@email.com",
            password = "password"
        )

        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateName("Test Name"))
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email.com"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("password"))
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `Account created when user inputs are valid`() = runTest {
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateName("Test Name"))
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email.com"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("password"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)
        coVerify {
            repository.createObserverUser(
                domain = testDomain,
                accountId = accountId,
                pairingCode = pairingCode,
                fullName = "Test Name",
                email = "test@email.com",
                password = "password"
            )
        }
        assert(viewModel.uiState.value.nameError.isNullOrBlank())
        assert(viewModel.uiState.value.emailError.isNullOrBlank())
        assert(viewModel.uiState.value.passwordError.isNullOrBlank())
    }

    @Test
    fun `Validation fails when user inputs are empty`() = runTest {
        createViewModel()
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)
        coVerify(exactly = 0) {
            repository.createObserverUser(
                any(), any(), any(), any(), any(), any()
            )
        }
        assertFalse(viewModel.uiState.value.nameError.isNullOrBlank())
        assertFalse(viewModel.uiState.value.emailError.isNullOrBlank())
        assertFalse(viewModel.uiState.value.passwordError.isNullOrBlank())
    }

    @Test
    fun `Validation fails when user inputs are not valid`() = runTest {
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("pass"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)
        coVerify(exactly = 0) {
            repository.createObserverUser(
                any(), any(), any(), any(), any(), any()
            )
        }
        assertFalse(viewModel.uiState.value.nameError.isNullOrBlank())
        assertFalse(viewModel.uiState.value.emailError.isNullOrBlank())
        assertFalse(viewModel.uiState.value.passwordError.isNullOrBlank())
    }

    @Test
    fun `Snackbar shown when account creation fails`() = runTest {
        coEvery {
            repository.createObserverUser(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws Exception()
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateName("Test Name"))
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email.com"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("password"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)

        var expectedUiState = CreateAccountUiState(
            termsOfService = termsTestResponse,
            name = "Test Name",
            email = "test@email.com",
            password = "password",
            showErrorSnack = true
        )
        assertEquals(expectedUiState, viewModel.uiState.value)

        viewModel.handleAction(CreateAccountAction.SnackbarDismissed)

        expectedUiState = CreateAccountUiState(
            termsOfService = termsTestResponse,
            name = "Test Name",
            email = "test@email.com",
            password = "password",
            showErrorSnack = false
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }
}
