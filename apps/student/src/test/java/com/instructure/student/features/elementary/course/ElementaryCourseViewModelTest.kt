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
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.TabManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DashboardCard
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.student.features.elementary.course.ElementaryCourseAction
import com.instructure.student.features.elementary.course.ElementaryCourseTab
import com.instructure.student.features.elementary.course.ElementaryCourseViewData
import com.instructure.student.features.elementary.course.ElementaryCourseViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
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
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val oauthManager: OAuthManager = mockk(relaxed = true)
    private val courseManager: CourseManager = mockk(relaxed = true)

    private lateinit var viewModel: ElementaryCourseViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkStatic("kotlinx.coroutines.AwaitKt")

        every { apiPrefs.fullDomain } returns "https://mockk.instructure.com"

        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        every { courseManager.getDashboardCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(DashboardCard(id = 0, isK5Subject = true)))
        }
        setupStrings()

        viewModel = ElementaryCourseViewModel(tabManager, resources, apiPrefs, oauthManager, courseManager)

        mockkObject(ColorKeeper)
        every { ColorKeeper.darkTheme } returns false
        every { ColorKeeper.getOrGenerateColor(any()) } returns ThemedColor(0)
    }

    fun tearDown() {
        unmockkAll()
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
                ElementaryCourseTab(Tab.HOME_ID, resources.getDrawable(R.drawable.ic_home), "Home", "https://mockk.instructure.com/courses/0?embed=true#home"),
                ElementaryCourseTab(Tab.SCHEDULE_ID, resources.getDrawable(R.drawable.ic_schedule), "Schedule", "https://mockk.instructure.com/courses/0?embed=true#schedule"),
                ElementaryCourseTab(Tab.MODULES_ID, resources.getDrawable(R.drawable.ic_modules), "Modules", "https://mockk.instructure.com/courses/0?embed=true#modules"),
                ElementaryCourseTab(Tab.GRADES_ID, resources.getDrawable(R.drawable.ic_grades), "Grades", "https://mockk.instructure.com/courses/0?embed=true#grades"),
                ElementaryCourseTab(Tab.RESOURCES_ID, resources.getDrawable(R.drawable.ic_resources), "Resources", "https://mockk.instructure.com/courses/0?embed=true#resources")
            ),
            0
        )

        every { tabManager.getTabsForElementaryAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(tabs)
        }

        viewModel.getData(CanvasContext.emptyCourseContext(), "")
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
                ElementaryCourseTab(Tab.HOME_ID, resources.getDrawable(R.drawable.ic_home), "Home", "https://mockk.instructure.com/courses/0?embed=true#home"),
                ElementaryCourseTab(Tab.SCHEDULE_ID, resources.getDrawable(R.drawable.ic_schedule), "Schedule", "https://mockk.instructure.com/courses/0?embed=true#schedule"),
                ElementaryCourseTab(Tab.MODULES_ID, resources.getDrawable(R.drawable.ic_modules), "Modules", "https://mockk.instructure.com/courses/0?embed=true#modules"),
                ElementaryCourseTab(Tab.GRADES_ID, resources.getDrawable(R.drawable.ic_grades), "Grades", "https://mockk.instructure.com/courses/0?embed=true#grades"),
                ElementaryCourseTab(Tab.RESOURCES_ID, resources.getDrawable(R.drawable.ic_resources), "Resources", "https://mockk.instructure.com/courses/0?embed=true#resources")
            ),
            0
        )

        every { tabManager.getTabsForElementaryAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(tabs)
        }

        viewModel.getData(CanvasContext.emptyCourseContext(), "")
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
                ElementaryCourseTab(Tab.HOME_ID, resources.getDrawable(R.drawable.ic_home), "Home", "https://mockk.instructure.com/courses/0?embed=true#home"),
                ElementaryCourseTab(Tab.SCHEDULE_ID, resources.getDrawable(R.drawable.ic_schedule), "Schedule", "https://mockk.instructure.com/courses/0?embed=true#schedule"),
                ElementaryCourseTab(Tab.RESOURCES_ID, resources.getDrawable(R.drawable.ic_resources), "Resources", "https://mockk.instructure.com/courses/0?embed=true#resources")
            ),
            0
        )

        every { tabManager.getTabsForElementaryAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(tabs)
        }

        viewModel.getData(CanvasContext.emptyCourseContext(), "")
        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(expectedData, viewModel.data.value)
    }

    @Test
    fun `Empty state`() {
        every { tabManager.getTabsForElementaryAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        viewModel.getData(CanvasContext.emptyCourseContext(), "")
        assertEquals(ViewState.Error("Uh oh! An error occurred while loading the course details."), viewModel.state.value)
    }

    @Test
    fun `Redirect to grades if non-K5 course grades tab is opened`() {
        every { courseManager.getDashboardCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        viewModel.getData(CanvasContext.emptyCourseContext(), "grades")

        assertEquals(ElementaryCourseAction.RedirectToGrades, viewModel.events.value?.peekContent())
    }

    @Test
    fun `Redirect to modules if non-K5 course modules tab is opened`() {
        every { courseManager.getDashboardCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        viewModel.getData(CanvasContext.emptyCourseContext(), "modules")

        assertEquals(ElementaryCourseAction.RedirectToModules, viewModel.events.value?.peekContent())
    }

    @Test
    fun `Redirect to course browser page if non-K5 course is opened`() {
        every { courseManager.getDashboardCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        viewModel.getData(CanvasContext.emptyCourseContext(), "")

        assertEquals(ElementaryCourseAction.RedirectToCourseBrowserPage, viewModel.events.value?.peekContent())
    }

    @Test
    fun `Resources tab is visible instead of external tools`() {
        val tabs = listOf(
            Tab(tabId = "home", label = "Home", htmlUrl = "/home", isHidden = false, position = 1),
            Tab(tabId = "schedule", label = "Schedule", htmlUrl = "/schedule", isHidden = false, position = 2),
            Tab(tabId = "modules", label = "Modules", htmlUrl = "/modules", isHidden = false, position = 3),
            Tab(tabId = "grades", label = "Grades", htmlUrl = "/grades", isHidden = false, position = 4),
            Tab(tabId = "external_tool_google_drive", label = "Google Drive", htmlUrl = "/google_drive", isHidden = false, position = 5, type = Tab.TYPE_EXTERNAL),
            Tab(tabId = "external_tool_name_coach", label = "NameCoach", htmlUrl = "/name_coach", isHidden = false, position = 6, type = Tab.TYPE_EXTERNAL)
        )

        val expectedData = ElementaryCourseViewData(
            listOf(
                ElementaryCourseTab(Tab.HOME_ID, resources.getDrawable(R.drawable.ic_home), "Home", "https://mockk.instructure.com/courses/0?embed=true#home"),
                ElementaryCourseTab(Tab.SCHEDULE_ID, resources.getDrawable(R.drawable.ic_schedule), "Schedule", "https://mockk.instructure.com/courses/0?embed=true#schedule"),
                ElementaryCourseTab(Tab.MODULES_ID, resources.getDrawable(R.drawable.ic_modules), "Modules", "https://mockk.instructure.com/courses/0?embed=true#modules"),
                ElementaryCourseTab(Tab.GRADES_ID, resources.getDrawable(R.drawable.ic_grades), "Grades", "https://mockk.instructure.com/courses/0?embed=true#grades"),
                ElementaryCourseTab(Tab.RESOURCES_ID, resources.getDrawable(R.drawable.ic_resources), "Resources", "https://mockk.instructure.com/courses/0?embed=true#resources")
            ),
            0
        )

        every { tabManager.getTabsForElementaryAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(tabs)
        }

        viewModel.getData(CanvasContext.emptyCourseContext(), "")
        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(expectedData, viewModel.data.value)
    }

    private fun setupStrings() {
        every { resources.getString(R.string.error_loading_course_details) } returns "Uh oh! An error occurred while loading the course details."
        every { resources.getString(R.string.dashboardTabResources) } returns "Resources"
    }

}