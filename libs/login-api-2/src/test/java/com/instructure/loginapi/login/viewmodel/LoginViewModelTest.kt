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

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.apis.ExperienceAPI
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.ExperienceSummary
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val userManager: UserManager = mockk(relaxed = true)
    private val oauthManager: OAuthManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val lifecycleTestOwner = LifecycleTestOwner()
    private val experienceApi: ExperienceAPI = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        coEvery { experienceApi.getExperienceSummary(any()) } returns DataResult.Fail()

        every { apiPrefs.user } returns mockk()
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
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        assertEquals(LoginResultAction.Login(Experience.Academic(true)), loginStatus.value!!.getContentIfNotHandled()!!)
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
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        assertEquals(LoginResultAction.Login(Experience.Academic(false)), loginStatus.value!!.getContentIfNotHandled()!!)
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
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

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
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        assertEquals(LoginResultAction.Login(Experience.Academic(false)), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Send login event with career experience when career experience is enabled`() {
        // Given
        coEvery { apiPrefs.canvasCareerView } returns null
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns false
        coEvery { experienceApi.getExperienceSummary(any()) } returns DataResult.Success(ExperienceSummary(currentApp = ExperienceSummary.CAREER_LEARNER_EXPERIENCE))
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, true)
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        verify { apiPrefs.canvasCareerView = true }
        verify { crashlytics.setCustomKey(CRASHLYTICS_EXPERIENCE_KEY, CAREER_EXPERIENCE) }
        assertEquals(LoginResultAction.Login(Experience.Career), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Send login event with academic experience when the current app is academic`() {
        // Given
        coEvery { apiPrefs.canvasCareerView } returns null
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns false
        coEvery { experienceApi.getExperienceSummary(any()) } returns DataResult.Success(ExperienceSummary(currentApp = ExperienceSummary.ACADEMIC_EXPERIENCE))
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, true)
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        verify { apiPrefs.canvasCareerView = false }
        verify { crashlytics.setCustomKey(CRASHLYTICS_EXPERIENCE_KEY, ACADEMIC_EXPERIENCE) }
        assertEquals(LoginResultAction.Login(Experience.Academic(false)), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Send login event with academic experience and set canSwitchToCanvasCareer when the current app is academic but career is possible`() {
        // Given
        coEvery { apiPrefs.canvasCareerView } returns null
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns false
        coEvery { experienceApi.getExperienceSummary(any()) } returns DataResult.Success(
            ExperienceSummary(
                currentApp = ExperienceSummary.ACADEMIC_EXPERIENCE,
                availableApps = listOf(ExperienceSummary.ACADEMIC_EXPERIENCE, ExperienceSummary.CAREER_LEARNER_EXPERIENCE)
            )
        )
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, true)
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        verify { apiPrefs.canvasCareerView = false }
        verify { apiPrefs.canSwitchToCanvasCareer = true }
        assertEquals(LoginResultAction.Login(Experience.Academic(false)), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Send login event with career experience when its already set`() {
        // Given
        coEvery { apiPrefs.canvasCareerView } returns true
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns false
        coEvery { experienceApi.getExperienceSummary(any()) } returns DataResult.Success(ExperienceSummary(currentApp = ExperienceSummary.CAREER_LEARNER_EXPERIENCE))
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, true)
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        verify { crashlytics.setCustomKey(CRASHLYTICS_EXPERIENCE_KEY, CAREER_EXPERIENCE) }
        assertEquals(LoginResultAction.Login(Experience.Career), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `Send login event with academic experience when its already set to academic`() {
        // Given
        coEvery { apiPrefs.canvasCareerView } returns false
        coEvery { featureFlagProvider.getCanvasForElementaryFlag() } returns false
        coEvery { experienceApi.getExperienceSummary(any()) } returns DataResult.Success(ExperienceSummary(currentApp = ExperienceSummary.ACADEMIC_EXPERIENCE))
        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("", requiresTermsAcceptance = false))
        }
        every { userManager.getSelfAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = createViewModel()
        val loginStatus = viewModel.checkLogin(true, true)
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        verify { crashlytics.setCustomKey(CRASHLYTICS_EXPERIENCE_KEY, ACADEMIC_EXPERIENCE) }
        assertEquals(LoginResultAction.Login(Experience.Academic(false)), loginStatus.value!!.getContentIfNotHandled()!!)
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
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        verify { crashlytics.setCustomKey(CRASHLYTICS_EXPERIENCE_KEY, ELEMENTARY_EXPERIENCE) }
        assertEquals(LoginResultAction.Login(Experience.Academic(true)), loginStatus.value!!.getContentIfNotHandled()!!)
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
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        assertEquals(LoginResultAction.ShouldAcceptPolicy(Experience.Academic(false)), loginStatus.value!!.getContentIfNotHandled()!!)
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
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        verify { apiPrefs.checkTokenAfterOfflineLogin = true }
        assertEquals(LoginResultAction.Login(Experience.Academic(false)), loginStatus.value!!.getContentIfNotHandled()!!)
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
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        verify { apiPrefs.checkTokenAfterOfflineLogin = false }
        assertEquals(LoginResultAction.Login(Experience.Academic(false)), loginStatus.value!!.getContentIfNotHandled()!!)
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
        loginStatus.observe(lifecycleTestOwner.lifecycleOwner, {})

        // Then
        verify { apiPrefs.checkTokenAfterOfflineLogin = false }
        assertEquals(LoginResultAction.Login(Experience.Academic(false)), loginStatus.value!!.getContentIfNotHandled()!!)
    }

    private fun createViewModel(): LoginViewModel {
        return LoginViewModel(featureFlagProvider, userManager, oauthManager, apiPrefs, experienceApi, networkStateProvider, crashlytics)
    }

}
