/*
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
package com.instructure.loginapi.login.features.acceptableusepolicy

import android.webkit.CookieManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private val TERMS_OF_SERVICE =
    "Gentlemen, a short view back to the past. Thirty years ago, Niki Lauda told us ‘take a monkey, place him into the cockpit and he is able to drive the car.’ " +
    "Thirty years later, Sebastian told us ‘I had to start my car like a computer, it’s very complicated.’ " +
    "And Nico Rosberg said that during the race – I don’t remember what race - he pressed the wrong button on the wheel. " +
    "Question for you both: is Formula One driving today too complicated with twenty and more buttons on the wheel, are you too much under effort, under pressure? " +
    "What are your wishes for the future concerning the technical programme during the race? " +
    "Less buttons, more? Or less and more communication with your engineers?"

@ExperimentalCoroutinesApi
class AcceptableUsePolicyViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()

    private val userManager: UserManager = mockk(relaxed = true)

    private val cookieManager: CookieManager = mockk(relaxed = true)

    private lateinit var viewModel: AcceptableUsePolicyViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        
    }

    @Test
    fun `Load initial data correctly`() {
        // Given
        every { userManager.getTermsOfServiceAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(TermsOfService(content = TERMS_OF_SERVICE))
        }

        // When
        viewModel = AcceptableUsePolicyViewModel(userManager, cookieManager)

        // Then
        val expectedData = AcceptableUsePolicyViewData(TERMS_OF_SERVICE, false, false)
        assertEquals(expectedData, viewModel.data.value)
    }

    @Test
    fun `Change accept switch`() {
        // Given
        every { userManager.getTermsOfServiceAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(TermsOfService(content = TERMS_OF_SERVICE))
        }

        // When
        viewModel = AcceptableUsePolicyViewModel(userManager, cookieManager)
        viewModel.checkedChanged(true)

        // Then
        val expectedData = AcceptableUsePolicyViewData(TERMS_OF_SERVICE, true, false)
        assertEquals(expectedData, viewModel.data.value)
    }

    @Test
    fun `Open policy when policy is already loaded`() {
        // Given
        every { userManager.getTermsOfServiceAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(TermsOfService(content = TERMS_OF_SERVICE))
        }

        // When
        viewModel = AcceptableUsePolicyViewModel(userManager, cookieManager)
        viewModel.openPolicy()

        // Then
        val expectedEvent = AcceptableUsePolicyAction.OpenPolicy(TERMS_OF_SERVICE)
        assertEquals(expectedEvent, viewModel.events.value!!.peekContent())
        verify(exactly = 1) { userManager.getTermsOfServiceAsync(any()) }
    }

    @Test
    fun `Load and open policy when policy is not loaded`() {
        // Given
        every { userManager.getTermsOfServiceAsync(any()) } returns mockk {
            coEvery { await() } returnsMany listOf(DataResult.Fail(), DataResult.Success(TermsOfService(content = TERMS_OF_SERVICE)))
        }

        // When
        viewModel = AcceptableUsePolicyViewModel(userManager, cookieManager)
        viewModel.openPolicy()

        // Then
        val expectedEvent = AcceptableUsePolicyAction.OpenPolicy(TERMS_OF_SERVICE)
        assertEquals(expectedEvent, viewModel.events.value!!.peekContent())
        verify(exactly = 2) { userManager.getTermsOfServiceAsync(any()) }
    }

    @Test
    fun `Show error when policy opening is failed`() {
        // Given
        every { userManager.getTermsOfServiceAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        viewModel = AcceptableUsePolicyViewModel(userManager, cookieManager)
        viewModel.openPolicy()

        // Then
        val expectedEvent = AcceptableUsePolicyAction.PolicyOpenFailed
        assertEquals(expectedEvent, viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Accept policy succesfully`() {
        // Given
        every { userManager.acceptUserTermsAsync() } returns mockk {
            coEvery { await() } returns DataResult.Success(User())
        }

        // When
        viewModel = AcceptableUsePolicyViewModel(userManager, cookieManager)
        viewModel.acceptPolicy()

        // Then
        val expectedEvent = AcceptableUsePolicyAction.PolicyAccepted
        assertEquals(expectedEvent, viewModel.events.value!!.peekContent())
    }

    @Test
    fun `Send error event when failed to accept policy`() {
        // Given
        every { userManager.acceptUserTermsAsync() } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        viewModel = AcceptableUsePolicyViewModel(userManager, cookieManager)
        viewModel.acceptPolicy()

        // Then
        val expectedEvent = AcceptableUsePolicyAction.AcceptFailure
        assertEquals(expectedEvent, viewModel.events.value!!.peekContent())
    }
}