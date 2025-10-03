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

package com.instructure.parentapp.login.routevalidator

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.OAuthTokenResponse
import com.instructure.canvasapi2.models.TokenUser
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.loginapi.login.util.QRLogin
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.parentapp.R
import com.instructure.parentapp.features.login.routevalidator.RouteValidatorAction
import com.instructure.parentapp.features.login.routevalidator.RouteValidatorViewModel
import com.instructure.parentapp.util.ParentLogoutTask
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import com.instructure.testutils.collectForTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RouteValidatorViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val context: Context = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val oAuthApi: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private val qrLogin: QRLogin = mockk(relaxed = true)
    private val analytics: Analytics = mockk(relaxed = true)
    private val alarmScheduler: AlarmScheduler = mockk(relaxed = true)
    private val mockUri = mockk<Uri>(relaxed = true) {
        every { host } returns "mobiledev.instructure.com"
    }

    private lateinit var viewModel: RouteValidatorViewModel

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockUri
        ContextKeeper.appContext = context
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } just runs
        mockkConstructor(ParentLogoutTask::class)
        every { anyConstructed<ParentLogoutTask>().execute() } just runs
    }

    @Test
    fun `Load route with null url`() = runTest {
        createViewModel()
        viewModel.loadRoute(null)

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        Assert.assertEquals(RouteValidatorAction.Finish, events.last())
    }

    @Test
    fun `Load route with empty url`() = runTest {
        createViewModel()
        viewModel.loadRoute("")

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        Assert.assertEquals(RouteValidatorAction.Finish, events.last())
    }

    @Test
    fun `Load route with QR login url when user is already signed in`() = runTest {
        every { apiPrefs.getValidToken() } returns "token"
        every { qrLogin.verifySSOLoginUri(any()) } returns true

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.loadRoute("https://mobiledev.instructure.com/qrlogin")

        Assert.assertEquals(RouteValidatorAction.Finish, events.last())
    }

    @Test
    fun `Load route with QR login url`() = runTest {
        every { apiPrefs.getValidToken() } returns ""
        every { qrLogin.verifySSOLoginUri(any()) } returns true
        coEvery { qrLogin.performSSOLogin(any(), any(), any()) } returns OAuthTokenResponse()
        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns DataResult.Success(AuthenticatedSession("sessionUrl"))

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.loadRoute("https://mobiledev.instructure.com/qrlogin")

        Assert.assertEquals(RouteValidatorAction.LoadWebViewUrl("sessionUrl"), events.last())
        delay(800)
        Assert.assertEquals(RouteValidatorAction.StartMainActivity(), events.last())
    }

    @Test
    fun `Load route with masquerade QR login url`() = runTest {
        every { apiPrefs.getValidToken() } returns ""
        every { qrLogin.verifySSOLoginUri(any()) } returns true
        coEvery { qrLogin.performSSOLogin(any(), any(), any()) } returns OAuthTokenResponse(
            realUser = TokenUser(1, "", ""),
            user = TokenUser(1, "", "")
        )
        coEvery { oAuthApi.getAuthenticatedSession(any(), any()) } returns DataResult.Success(AuthenticatedSession("sessionUrl"))

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.loadRoute("https://mobiledev.instructure.com/qrlogin")

        Assert.assertEquals(RouteValidatorAction.LoadWebViewUrl("sessionUrl"), events.last())
        delay(800)
        Assert.assertEquals(RouteValidatorAction.StartMainActivity(1), events.last())
    }

    @Test
    fun `Load route with QR login url when error happens`() = runTest {
        every { apiPrefs.getValidToken() } returns ""
        every { qrLogin.verifySSOLoginUri(any()) } returns true
        coEvery { qrLogin.performSSOLogin(any(), any(), any()) } throws Exception()

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.loadRoute("https://mobiledev.instructure.com/qrlogin")

        verify { apiPrefs.clearAllData() }
        Assert.assertEquals(RouteValidatorAction.Finish, events.last())
    }

    @Test
    fun `Load route when user is not logged in`() = runTest {
        every { apiPrefs.getValidToken() } returns ""

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.loadRoute("https://mobiledev.instructure.com/courses")

        delay(800)
        Assert.assertEquals(
            RouteValidatorAction.StartSignInActivity(AccountDomain("mobiledev.instructure.com")),
            events.last()
        )
    }

    @Test
    fun `Load route when user is not logged in and no host found in url`() = runTest {
        every { apiPrefs.getValidToken() } returns ""
        every { mockUri.host } returns ""

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.loadRoute("https://mobiledev.instructure.com/courses")

        delay(800)
        Assert.assertEquals(RouteValidatorAction.StartLoginActivity, events.last())
    }

    @Test
    fun `Load route when user is logged in`() = runTest {
        every { apiPrefs.domain } returns "mobiledev.instructure.com"
        every { apiPrefs.getValidToken() } returns "token"

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.loadRoute("https://mobiledev.instructure.com/courses")

        delay(800)
        Assert.assertEquals(RouteValidatorAction.StartMainActivity(data = mockUri), events.last())
    }

    @Test
    fun `Load route when user is logged in but the domain is different`() = runTest {
        every { context.getString(R.string.differentDomainFromLink) } returns "Different domain message"
        every { apiPrefs.domain } returns "mobiledev.instructure.com"
        every { apiPrefs.getValidToken() } returns "token"
        every { mockUri.host } returns "different.instructure.com"

        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.loadRoute("https://mobiledev.instructure.com/courses")

        delay(800)
        Assert.assertEquals(RouteValidatorAction.StartMainActivity(message = "Different domain message"), events.last())
    }

    private fun createViewModel() {
        viewModel = RouteValidatorViewModel(context, apiPrefs, oAuthApi, qrLogin, analytics, alarmScheduler)
    }
}
