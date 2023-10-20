/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.loginapi.login.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FEATURE_FLAG_OFFLINE
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val userManager: UserManager = mockk(relaxed = true)
    private val oauthManager: OAuthManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `Do not check token if not needed`() {
        // Given
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns true
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(false, true)
        loginStatus.observe(lifecycleOwner, {})

        // Then
        assertEquals(LoginResultAction.Login(true), loginStatus.value!!.getContentIfNotHandled()!!)
        verify(exactly = 0) { userManager.getSelfAsync(any()) }
    }

    @Test
    fun `Do not check elementary if not needed`() {
        // Given
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(false, false)
        loginStatus.observe(lifecycleOwner, {})

        // Then
        assertEquals(LoginResultAction.Login(false), loginStatus.value!!.getContentIfNotHandled()!!)
        coVerify(exactly = 0) { featureFlagProvider.getCanvasForElementaryFlag() }
    }

    @Test
    fun `Send token not valid event if token check fails`() {
        // Given
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, true)
        loginStatus.observe(lifecycleOwner, {})

        // Then
        assertEquals(LoginResultAction.TokenNotValid, loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Send login event when terms acceptance is not needed`() {
        // Given
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns false
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, true)
        loginStatus.observe(lifecycleOwner, {})

        // Then
        assertEquals(LoginResultAction.Login(false), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Send login event with elementary when terms acceptance is not needed and elementary is turned on`() {
        // Given
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns true
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, true)
        loginStatus.observe(lifecycleOwner, {})

        // Then
        assertEquals(LoginResultAction.Login(true), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Send should accept event when terms acceptance is needed`() {
        // Given
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns false
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = true))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, true)
        loginStatus.observe(lifecycleOwner, {})

        // Then
        assertEquals(LoginResultAction.ShouldAcceptPolicy(false), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Set offline login if the user logs in offline and the feature flag is on`() {
        // Given
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        every { networkStateProvider.isOnline() } returns false
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, false)
        loginStatus.observe(lifecycleOwner, {})

        // Then
        verify { apiPrefs.checkTokenAfterOfflineLogin = true }
        assertEquals(LoginResultAction.Login(false), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Dont Set offline login if the user logs in offline and the feature flag is off`() {
        // Given
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        every { networkStateProvider.isOnline() } returns false
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, false)
        loginStatus.observe(lifecycleOwner, {})

        // Then
        verify { apiPrefs.checkTokenAfterOfflineLogin = false }
        assertEquals(LoginResultAction.Login(false), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Dont Set offline login if the user logs in online`() {
        // Given
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        every { networkStateProvider.isOnline() } returns true
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, false)
        loginStatus.observe(lifecycleOwner, {})

        // Then
        verify { apiPrefs.checkTokenAfterOfflineLogin = false }
        assertEquals(LoginResultAction.Login(false), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    private fun createViewModel(): LoginViewModel {
        return LoginViewModel(featureFlagProvider, userManager, oauthManager, apiPrefs, networkStateProvider)
    }

}