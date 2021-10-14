/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.elementary.course

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.managers.TabManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ViewState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ElementaryCourseViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = TestCoroutineDispatcher()

    private val tabManager: TabManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)

    private lateinit var viewModel: ElementaryCourseViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkStatic("kotlinx.coroutines.AwaitKt")

        setupStrings()

        viewModel = ElementaryCourseViewModel(tabManager, resources)
    }

    @Test
    fun `Map tabs correctly`() {
        val tabs = listOf(
            Tab(tabId = "home", label = "Home", htmlUrl = "/home", isHidden = false, position = 1),
            Tab(tabId = "schedule", label = "Schedule", htmlUrl = "/schedule", isHidden = false, position = 2),
            Tab(tabId = "modules", label = "Modules", htmlUrl = "/modules", isHidden = false, position = 3),
            Tab(tabId = "grades", label = "Grades", htmlUrl = "/grades", isHidden = false, position = 4),
            Tab(tabId = "resources", label = "Resources", htmlUrl = "/resources", isHidden = false, position = 5)
        )

        val expectedData = ElementaryCourseViewData(
            listOf(
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_home), "Home", "/home"),
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_schedule), "Schedule", "/schedule"),
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_modules), "Modules", "/modules"),
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_grades), "Grades", "/grades"),
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_resources), "Resources", "/resources")
            )
        )

        every { tabManager.getTabsForElementaryAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(tabs)
        }

        viewModel.getData(CanvasContext.defaultCanvasContext())
        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(expectedData, viewModel.data.value)
    }

    @Test
    fun `Order tabs by position`() {
        val tabs = listOf(
            Tab(tabId = "modules", label = "Modules", htmlUrl = "/modules", isHidden = false, position = 3),
            Tab(tabId = "home", label = "Home", htmlUrl = "/home", isHidden = false, position = 1),
            Tab(tabId = "resources", label = "Resources", htmlUrl = "/resources", isHidden = false, position = 5),
            Tab(tabId = "grades", label = "Grades", htmlUrl = "/grades", isHidden = false, position = 4),
            Tab(tabId = "schedule", label = "Schedule", htmlUrl = "/schedule", isHidden = false, position = 2)
        )

        val expectedData = ElementaryCourseViewData(
            listOf(
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_home), "Home", "/home"),
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_schedule), "Schedule", "/schedule"),
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_modules), "Modules", "/modules"),
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_grades), "Grades", "/grades"),
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_resources), "Resources", "/resources")
            )
        )

        every { tabManager.getTabsForElementaryAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(tabs)
        }

        viewModel.getData(CanvasContext.defaultCanvasContext())
        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(expectedData, viewModel.data.value)
    }

    @Test
    fun `Hide hidden tabs`() {
        val tabs = listOf(
            Tab(tabId = "modules", label = "Modules", htmlUrl = "/modules", isHidden = true, position = 3),
            Tab(tabId = "home", label = "Home", htmlUrl = "/home", isHidden = false, position = 1),
            Tab(tabId = "resources", label = "Resources", htmlUrl = "/resources", isHidden = false, position = 5),
            Tab(tabId = "grades", label = "Grades", htmlUrl = "/grades", isHidden = true, position = 4),
            Tab(tabId = "schedule", label = "Schedule", htmlUrl = "/schedule", isHidden = false, position = 2)
        )

        val expectedData = ElementaryCourseViewData(
            listOf(
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_home), "Home", "/home"),
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_schedule), "Schedule", "/schedule"),
                ElementaryCourseTab(resources.getDrawable(R.drawable.ic_resources), "Resources", "/resources")
            )
        )

        every { tabManager.getTabsForElementaryAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(tabs)
        }

        viewModel.getData(CanvasContext.defaultCanvasContext())
        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(expectedData, viewModel.data.value)
    }

    @Test
    fun `Empty state`() {
        every { tabManager.getTabsForElementaryAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }


        viewModel.getData(CanvasContext.defaultCanvasContext())
        assertEquals(ViewState.Error("Uh oh! An error occurred while loading the course details."), viewModel.state.value)
    }

    private fun setupStrings() {
        every { resources.getString(R.string.error_loading_course_details) } returns "Uh oh! An error occurred while loading the course details."
    }

}