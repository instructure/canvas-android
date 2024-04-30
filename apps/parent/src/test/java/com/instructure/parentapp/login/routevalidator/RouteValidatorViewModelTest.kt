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

package com.instructure.parentapp.login.routevalidator

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.parentapp.features.login.routevalidator.RouteValidatorAction
import com.instructure.parentapp.features.login.routevalidator.RouteValidatorViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class RouteValidatorViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val oAuthApi: OAuthAPI.OAuthInterface = mockk(relaxed = true)

    private lateinit var viewModel: RouteValidatorViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `Load route with null url`() {
        createViewModel()
        viewModel.loadRoute(null)

        Assert.assertEquals(RouteValidatorAction.Finish, viewModel.events.value?.peekContent())
    }

    @Test
    fun `Load route with empty url`() {
        createViewModel()
        viewModel.loadRoute("")

        Assert.assertEquals(RouteValidatorAction.Finish, viewModel.events.value?.peekContent())
    }

    private fun createViewModel() {
        viewModel = RouteValidatorViewModel(context, apiPrefs, oAuthApi)
    }
}