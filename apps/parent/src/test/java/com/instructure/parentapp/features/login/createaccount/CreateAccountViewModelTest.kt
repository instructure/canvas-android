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
 */
package com.instructure.parentapp.features.login.createaccount

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandares.R
import com.instructure.pandautils.utils.studentColor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        coEvery { context.getString(R.string.createAccErrorCreatingAccount) } returns "An error occurred creating your account. Please check your internet connection and try again."
        coEvery { context.getString(R.string.createAccErrorEmailAlreadyInUse) } returns "This email address is already in use."
        coEvery { context.getString(R.string.createAccEnterValidEmail) } returns "Please enter a valid email address"
        coEvery { context.getString(R.string.createAccErrorPairingCode) } returns "This pairing code is expired or already used. Please try again with a new one."
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
    fun `Validation fails when name input is empty`() = runTest {
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email.com"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("password"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)
        coVerify(exactly = 0) {
            repository.createObserverUser(
                any(), any(), any(), any(), any(), any()
            )
        }
        assertFalse(viewModel.uiState.value.nameError.isNullOrBlank())
    }

    @Test
    fun `Validation fails when email input is empty`() = runTest {
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateName("Test Name"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("password"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)
        coVerify(exactly = 0) {
            repository.createObserverUser(
                any(), any(), any(), any(), any(), any()
            )
        }
        assertFalse(viewModel.uiState.value.emailError.isNullOrBlank())
    }

    @Test
    fun `Validation fails when password input is empty`() = runTest {
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateName("Test Name"))
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email.com"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)
        coVerify(exactly = 0) {
            repository.createObserverUser(
                any(), any(), any(), any(), any(), any()
            )
        }
        assertFalse(viewModel.uiState.value.passwordError.isNullOrBlank())
    }

    @Test
    fun `Validation fails when email input is not valid`() = runTest {
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateName("Test Name"))
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("password"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)
        coVerify(exactly = 0) {
            repository.createObserverUser(
                any(), any(), any(), any(), any(), any()
            )
        }
        assertFalse(viewModel.uiState.value.emailError.isNullOrBlank())
    }

    @Test
    fun `Validation fails when password input is not valid`() = runTest {
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateName("Test Name"))
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email.com"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("pass"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)
        coVerify(exactly = 0) {
            repository.createObserverUser(
                any(), any(), any(), any(), any(), any()
            )
        }
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
            showErrorSnack = true,
            errorSnackMessage = "An error occurred creating your account. Please check your internet connection and try again."
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

    @Test
    fun `Email error shown when email is already in use`() = runTest {
        val errorBodyString = "{\n" +
                "  \"errors\": {\n" +
                "    \"user\": {\n" +
                "      \"pseudonyms\": [\n" +
                "        {\n" +
                "          \"attribute\": \"pseudonyms\",\n" +
                "          \"type\": \"invalid\",\n" +
                "          \"message\": \"invalid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"pseudonym\": {\n" +
                "      \"unique_id\": [\n" +
                "        {\n" +
                "          \"attribute\": \"unique_id\",\n" +
                "          \"type\": \"taken\",\n" +
                "          \"message\": \"ID already in use for this account and authentication provider\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"observee\": {},\n" +
                "    \"pairing_code\": {\n" +
                "      \"code\": [\n" +
                "        {\n" +
                "          \"attribute\": \"code\",\n" +
                "          \"type\": \"invalid\",\n" +
                "          \"message\": \"invalid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"recaptcha\": null\n" +
                "  }\n" +
                "}"
        coEvery {
            repository.createObserverUser(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail(errorBody = errorBodyString.toResponseBody())
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateName("Test Name"))
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email.com"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("password"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)

        val expectedUiState = CreateAccountUiState(
            termsOfService = termsTestResponse,
            name = "Test Name",
            email = "test@email.com",
            password = "password",
            emailError = "This email address is already in use."
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `Email error shown when email validation fails on backend`() = runTest {
        val errorBodyString = "{\n" +
                "  \"errors\": {\n" +
                "    \"user\": {\n" +
                "      \"pseudonyms\": [\n" +
                "        {\n" +
                "          \"attribute\": \"pseudonyms\",\n" +
                "          \"type\": \"invalid\",\n" +
                "          \"message\": \"invalid\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"pseudonym\": {\n" +
                "       \n" +
                "    },\n" +
                "    \"observee\": {},\n" +
                "    \"pairing_code\": {\n" +
                "       \n" +
                "    },\n" +
                "    \"recaptcha\": null\n" +
                "  }\n" +
                "}"
        coEvery {
            repository.createObserverUser(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail(errorBody = errorBodyString.toResponseBody())
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateName("Test Name"))
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email.com"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("password"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)

        val expectedUiState = CreateAccountUiState(
            termsOfService = termsTestResponse,
            name = "Test Name",
            email = "test@email.com",
            password = "password",
            emailError = "Please enter a valid email address"
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `Pairing error shown in snackbar when pairing code is invalid`() = runTest {
        val errorBodyString = "{\n" +
                "   \"errors\":{\n" +
                "      \"user\":{\n" +
                "         \n" +
                "      },\n" +
                "      \"pseudonym\":{\n" +
                "         \n" +
                "      },\n" +
                "      \"observee\":{\n" +
                "         \n" +
                "      },\n" +
                "      \"pairing_code\":{\n" +
                "         \"code\":[\n" +
                "            {\n" +
                "               \"attribute\":\"code\",\n" +
                "               \"type\":\"invalid\",\n" +
                "               \"message\":\"invalid\"\n" +
                "            }\n" +
                "         ]\n" +
                "      },\n" +
                "      \"recaptcha\":null\n" +
                "   }\n" +
                "}"
        coEvery {
            repository.createObserverUser(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns DataResult.Fail(errorBody = errorBodyString.toResponseBody())
        createViewModel()
        viewModel.handleAction(CreateAccountAction.UpdateName("Test Name"))
        viewModel.handleAction(CreateAccountAction.UpdateEmail("test@email.com"))
        viewModel.handleAction(CreateAccountAction.UpdatePassword("password"))
        viewModel.handleAction(CreateAccountAction.CreateAccountTapped)

        val expectedUiState = CreateAccountUiState(
            termsOfService = termsTestResponse,
            name = "Test Name",
            email = "test@email.com",
            password = "password",
            showErrorSnack = true,
            errorSnackMessage = "This pairing code is expired or already used. Please try again with a new one."
        )
        assertEquals(expectedUiState, viewModel.uiState.value)
    }

    @Test
    fun `Sign in should navigate to sign in`() = runTest {
        createViewModel()
        val events = mutableListOf<CreateAccountViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }
        viewModel.handleAction(CreateAccountAction.SignInTapped)

        assert(events.last() is CreateAccountViewModelAction.NavigateToSignIn)
    }

    @Test
    fun `Privacy should navigate to privacy`() = runTest {
        createViewModel()
        val events = mutableListOf<CreateAccountViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }
        viewModel.handleAction(CreateAccountAction.PrivacyTapped)

        assert(events.last() is CreateAccountViewModelAction.NavigateToPrivacyPolicy)
    }

    @Test
    fun `Not passive terms should navigate to html content`() = runTest {
        createViewModel()
        val events = mutableListOf<CreateAccountViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }
        viewModel.handleAction(CreateAccountAction.TosTapped)

        assertEquals(
            "Content",
            (events.last() as CreateAccountViewModelAction.NavigateToHtmlContent).html
        )
    }

    @Test
    fun `Passive terms should navigate to terms`() = runTest {
        val passiveTerms = TermsOfService(passive = true)
        coEvery { repository.getTermsOfService(any(), any()) } returns passiveTerms
        createViewModel()
        val events = mutableListOf<CreateAccountViewModelAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }
        viewModel.handleAction(CreateAccountAction.TosTapped)

        assert(events.last() is CreateAccountViewModelAction.NavigateToTermsOfService)
    }
}
