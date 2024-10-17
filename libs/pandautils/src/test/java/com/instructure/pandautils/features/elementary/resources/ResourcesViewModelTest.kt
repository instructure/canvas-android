package com.instructure.pandautils.features.elementary.resources/*
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

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.ExternalToolManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ContactInfoItemViewModel
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ImportantLinksItemViewModel
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.LtiApplicationItemViewModel
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ResourcesHeaderViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.HtmlContentFormatter
import io.mockk.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ResourcesViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()

    private val resources: Resources = mockk(relaxed = true)
    private val courseManager: CourseManager = mockk(relaxed = true)
    private val userManager: UserManager = mockk(relaxed = true)
    private val externalToolManager: ExternalToolManager = mockk(relaxed = true)
    private val oAuthManager: OAuthManager = mockk(relaxed = true)
    private val htmlContentFormatter: HtmlContentFormatter = mockk(relaxed = true)

    private lateinit var viewModel: ResourcesViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        coEvery { htmlContentFormatter.formatHtmlWithIframes(any()) } returnsArgument 0

        mockkStatic("kotlinx.coroutines.AwaitKt")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        
    }

    @Test
    fun `Show error state if fetching courses fails`() {
        // Given
        every { resources.getString(R.string.failedToLoadResources) } returns "Error"
        initMockData(courses = DataResult.Fail())

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Error)
        assertEquals("Error", (viewModel.state.value as ViewState.Error).errorMessage)
    }

    @Test
    fun `Show empty state if there are no courses`() {
        // Given
        initMockData()

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Empty)
        assertEquals(R.string.resourcesEmptyMessage, (viewModel.state.value as ViewState.Empty).emptyTitle)
    }

    @Test
    fun `Do not create important links when we have a course but syllabus is empty`() {
        // Given
        val course = Course(syllabusBody = "")
        initMockData(courses = DataResult.Success(listOf(course)))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Empty)
        assertEquals(R.string.resourcesEmptyMessage, (viewModel.state.value as ViewState.Empty).emptyTitle)
    }

    @Test
    fun `Create important links from homeroom course syllabus body without course name if only 1 homeroom course is present`() {
        // Given
        val course = Course(homeroomCourse = true, syllabusBody = "This link is really important: www.tamaskozmer.com")
        initMockData(courses = DataResult.Success(listOf(course)))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(1, viewModel.data.value!!.importantLinksItems.size)

        val expectedHtmlContent = "This link is really important: www.tamaskozmer.com"
        assertFalse(viewModel.data.value!!.isEmpty())
        val viewData = (viewModel.data.value!!.importantLinksItems[0] as ImportantLinksItemViewModel).data
        assertEquals(ImportantLinksViewData("", expectedHtmlContent), viewData)
    }

    @Test
    fun `Create important links from homeroom course syllabus body with course name if there are more than 1 homeroom courses`() {
        // Given
        val course = Course(name = "Course 1", homeroomCourse = true, syllabusBody = "This link is really important: www.tamaskozmer.com")
        val course2 = Course(name = "Course 2", homeroomCourse = true, syllabusBody = "Something really important")
        initMockData(courses = DataResult.Success(listOf(course, course2)))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(2, viewModel.data.value!!.importantLinksItems.size)
        assertFalse(viewModel.data.value!!.isEmpty())

        val viewData1 = (viewModel.data.value!!.importantLinksItems[0] as ImportantLinksItemViewModel).data
        val viewData2 = (viewModel.data.value!!.importantLinksItems[1] as ImportantLinksItemViewModel).data
        assertEquals(ImportantLinksViewData("Course 1", "This link is really important: www.tamaskozmer.com", true), viewData1)
        assertEquals(ImportantLinksViewData("Course 2", "Something really important"), viewData2)
    }

    @Test
    fun `Do not create important links from non-homeroom course syllabus body`() {
        // Given
        val course = Course(syllabusBody = "This is a syllabus, not important links")
        initMockData(courses = DataResult.Success(listOf(course)))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Empty)
        assertEquals(R.string.resourcesEmptyMessage, (viewModel.state.value as ViewState.Empty).emptyTitle)
    }

    @Test
    fun `Do not request lti tools if there are no non-homeroom courses`() {
        // Given
        val course = Course(homeroomCourse = true, syllabusBody = "This link is really important: www.tamaskozmer.com")
        initMockData(courses = DataResult.Success(listOf(course)))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        verify(exactly = 0) { externalToolManager.getExternalToolsForCoursesAsync(any(), any()) }
    }

    @Test
    fun `Do not create action items and headers if no external tools and staff info received`() {
        // Given
        val course = Course(id = 1, homeroomCourse = true, syllabusBody = "This link is really important: www.tamaskozmer.com")
        val course2 = Course(id = 2, homeroomCourse = false)
        initMockData(courses = DataResult.Success(listOf(course, course2)))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.data.value!!.actionItems.isEmpty())
    }

    @Test
    fun `Create lti tools items from course lti tools and remove duplicated items`() {
        // Given
        val course = Course(id = 1, name = "Course uno")
        val course2 = Course(id = 2, name = "Course due")

        val ltiTools = listOf(
            LTITool(id = 1, contextId = course.id, contextName = course.name, courseNavigation = CourseNavigation("Google Drive"), url = "google.com", iconUrl = "drive.png"),
            LTITool(id = 1, contextId = course2.id, contextName = course2.name, courseNavigation = CourseNavigation("Google Drive"), url = "google.com", iconUrl = "drive.png"),
            LTITool(id = 2, name = "New Quizzes", contextId = course.id, contextName = course.name, url = "new.quizzes.com", iconUrl = "newquizzes.png")
        )
        initMockData(courses = DataResult.Success(listOf(course, course2)), externalTools = DataResult.Success(ltiTools))
        every { resources.getString(R.string.studentApplications) } returns "Student Applications"
        every { resources.getDimension(R.dimen.ltiAppsBottomMargin) } returns 10f

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(3, viewModel.data.value!!.actionItems.size) // We have 3 items, 2 LTI tools with the header
        assertFalse(viewModel.data.value!!.isEmpty())

        val header = viewModel.data.value!!.actionItems[0] as ResourcesHeaderViewModel
        assertEquals(ResourcesHeaderViewData("Student Applications"), header.data)

        val ltiTool1 = viewModel.data.value!!.actionItems[1] as LtiApplicationItemViewModel
        assertEquals(0, ltiTool1.marginBottom)
        assertEquals(LtiApplicationViewData("Google Drive", "drive.png", "google.com"), ltiTool1.data)

        val ltiTool2 = viewModel.data.value!!.actionItems[2] as LtiApplicationItemViewModel
        assertEquals(10, ltiTool2.marginBottom)
        assertEquals(LtiApplicationViewData("New Quizzes", "newquizzes.png", "new.quizzes.com"), ltiTool2.data)
    }

    @Test
    fun `Lti app click opens Lti app dialog with the list of courses for that lti app`() {
        // Given
        val course = Course(id = 1, name = "Course uno")
        val course2 = Course(id = 2, name = "Course due")

        val ltiTools = listOf(
            LTITool(id = 1, contextId = course.id, contextName = course.name, courseNavigation = CourseNavigation("Google Drive"), url = "google.com", iconUrl = "drive.png"),
            LTITool(id = 1, contextId = course2.id, contextName = course2.name, courseNavigation = CourseNavigation("Google Drive"), url = "google.com", iconUrl = "drive.png"),
            LTITool(id = 2, name = "New Quizzes", contextId = course.id, contextName = course.name, url = "new.quizzes.com", iconUrl = "newquizzes.png")
        )
        initMockData(courses = DataResult.Success(listOf(course, course2)), externalTools = DataResult.Success(ltiTools))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        val ltiTool = viewModel.data.value!!.actionItems[1] as LtiApplicationItemViewModel
        ltiTool.onClick()

        // Then
        val event = viewModel.events.value!!.getContentIfNotHandled()!!
        assertTrue(event is ResourcesAction.OpenLtiApp)

        val expectedLtiToolList = listOf(
            LTITool(id = 1, contextId = 1, contextName = "Course uno", courseNavigation = CourseNavigation("Google Drive"), url = "google.com", iconUrl = "drive.png"),
            LTITool(id = 1, contextId = 2, contextName = "Course due", courseNavigation = CourseNavigation("Google Drive"), url = "google.com", iconUrl = "drive.png")
        )
        assertEquals(expectedLtiToolList, (event as ResourcesAction.OpenLtiApp).ltiTools)
    }

    @Test
    fun `Create staff info views with header and remove duplicates`() {
        // Given
        val course = Course(id = 1, homeroomCourse = true, syllabusBody = "This link is really important: www.tamaskozmer.com")
        val teachers1 = listOf(
            User(id = 1, shortName = "Tamas Kozmer", avatarUrl = "http://a.b", enrollments = listOf(Enrollment(role = Enrollment.EnrollmentType.Teacher))),
            User(id = 2, shortName = "Balint Bartok", avatarUrl = "http://b.c", enrollments = listOf(Enrollment(role = Enrollment.EnrollmentType.Ta)))
        )
        val teachers2 = listOf(User(id = 1, shortName = "Tamas Kozmer", avatarUrl = "http://a.b", enrollments = listOf(Enrollment(role = Enrollment.EnrollmentType.Teacher))))
        initMockData(courses = DataResult.Success(listOf(course)), teachers = listOf(DataResult.Success(teachers1), DataResult.Success(teachers2)))

        every { resources.getString(R.string.staffContactInfo) } returns "Staff Info"
        every { resources.getString(R.string.staffRoleTeacher) } returns "Teacher"
        every { resources.getString(R.string.staffRoleTeacherAssistant) } returns "Assistant"

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(3, viewModel.data.value!!.actionItems.size) // We have 3 items, 2 staff info items with the header
        assertFalse(viewModel.data.value!!.isEmpty())

        val header = viewModel.data.value!!.actionItems[0] as ResourcesHeaderViewModel
        assertEquals(ResourcesHeaderViewData("Staff Info", hasDivider = true), header.data)

        val contactInfo1 = viewModel.data.value!!.actionItems[1] as ContactInfoItemViewModel
        assertEquals(ContactInfoViewData("Tamas Kozmer", "Teacher", "http://a.b"), contactInfo1.data)

        val contactInfo2 = viewModel.data.value!!.actionItems[2] as ContactInfoItemViewModel
        assertEquals(ContactInfoViewData("Balint Bartok", "Assistant", "http://b.c"), contactInfo2.data)
    }

    @Test
    fun `Clicking contact info opens compose message`() {
        // Given
        val course = Course(id = 1, homeroomCourse = true, syllabusBody = "This link is really important: www.tamaskozmer.com")
        val teachers = listOf(User(id = 1, shortName = "Tamas Kozmer", avatarUrl = "http://a.b", enrollments = listOf(Enrollment(role = Enrollment.EnrollmentType.Teacher))))
        initMockData(courses = DataResult.Success(listOf(course)), teachers = listOf(DataResult.Success(teachers)))

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        val contactInfo = viewModel.data.value!!.actionItems[1] as ContactInfoItemViewModel
        contactInfo.onClick()

        // Then
        val event = viewModel.events.value!!.getContentIfNotHandled()!!
        assertTrue(event is ResourcesAction.OpenComposeMessage)
        assertEquals(ResourcesAction.OpenComposeMessage(teachers[0]), event)
    }

    @Test
    fun `Error after refresh should trigger refresh error event if data is already available`() {
        // Given
        val course = Course(id = 1, homeroomCourse = true, syllabusBody = "This link is really important: www.tamaskozmer.com")
        initMockData(courses = DataResult.Success(listOf(course)))
        every { courseManager.getCoursesWithSyllabusAsyncWithActiveEnrollmentAsync(any()) } returns mockk {
            coEvery { await() }.returnsMany(DataResult.Success(listOf(course)), DataResult.Fail())
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        viewModel.refresh()

        // Then
        assertEquals(ViewState.Error(), viewModel.state.value)
        assertEquals(ResourcesAction.ShowRefreshError, viewModel.events.value!!.getContentIfNotHandled()!!)
    }

    @Test
    fun `OnImportantLinksViewsReady should send event`() {
        // When
        viewModel = createViewModel()
        viewModel.events.observe(lifecycleOwner, {})
        viewModel.onImportantLinksViewsReady()

        // Then
        assertEquals(ResourcesAction.ImportantLinksViewsReady, viewModel.events.value!!.getContentIfNotHandled()!!)
    }

    private fun createViewModel(): ResourcesViewModel {
        return ResourcesViewModel(resources, courseManager, userManager, externalToolManager, oAuthManager, htmlContentFormatter)
    }

    private fun initMockData(
        courses: DataResult<List<Course>> = DataResult.Success(emptyList()),
        externalTools: DataResult<List<LTITool>> = DataResult.Success(emptyList()),
        teachers: List<DataResult<List<User>>> = listOf(DataResult.Success(emptyList()))
    ) {
        every { courseManager.getCoursesWithSyllabusAsyncWithActiveEnrollmentAsync(any()) } returns mockk {
            coEvery { await() } returns courses
        }
        every { externalToolManager.getExternalToolsForCoursesAsync(any(), any()) } returns mockk() {
            coEvery { await() } returns externalTools
        }

        val usersDeferred: Deferred<DataResult<List<User>>> = mockk()
        every { userManager.getTeacherListForCourseAsync(any(), any()) } returns usersDeferred
        val listOfUsersDeferred = courses.dataOrNull?.map { usersDeferred } ?: emptyList()
        coEvery { listOfUsersDeferred.awaitAll() } returns teachers
    }
}
