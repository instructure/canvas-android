package com.instructure.teacher.features.coursesettings

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.PageManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.teacher.presenters.CourseSettingsFragmentPresenter
import com.instructure.teacher.viewinterface.CourseSettingsFragmentView
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class CourseSettingsFragmentPresenterTest {

    private lateinit var presenter: CourseSettingsFragmentPresenter
    private lateinit var view: CourseSettingsFragmentView
    private lateinit var course: Course

    @Before
    fun setUp() {
        presenter = CourseSettingsFragmentPresenter()
        view = mockk(relaxed = true)
        course = Course(id = 123L, name = "Test Course")
        presenter.onViewAttached(view)

        mockkObject(PageManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `editCourseHomePageClicked should call getFrontPage with course`() {
        // Given
        every { PageManager.getFrontPage(any(), any(), any()) } just Runs

        // When
        presenter.editCourseHomePageClicked(course)

        // Then
        verify { PageManager.getFrontPage(course, true, any()) }
    }

    @Test
    fun `editCourseHomePageClicked should show dialog with hasFrontPage true when API returns success with page`() {
        // Given
        val page = Page(id = 1L, title = "Front Page")
        val response = mockk<Response<Page>>(relaxed = true) {
            every { isSuccessful } returns true
            every { body() } returns page
        }

        val callbackSlot = slot<StatusCallback<Page>>()
        every { PageManager.getFrontPage(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onResponse(response, LinkHeaders(), ApiType.API)
        }

        // When
        presenter.editCourseHomePageClicked(course)

        // Then
        verify { view.showEditCourseHomePageDialog(true) }
    }

    @Test
    fun `editCourseHomePageClicked should show dialog with hasFrontPage false when API returns success but null body`() {
        // Given
        val response = mockk<Response<Page>>(relaxed = true) {
            every { isSuccessful } returns true
            every { body() } returns null
        }

        val callbackSlot = slot<StatusCallback<Page>>()
        every { PageManager.getFrontPage(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onResponse(response, LinkHeaders(), ApiType.API)
        }

        // When
        presenter.editCourseHomePageClicked(course)

        // Then
        verify { view.showEditCourseHomePageDialog(false) }
    }

    @Test
    fun `editCourseHomePageClicked should show dialog with hasFrontPage false when API returns error response`() {
        // Given
        val response = mockk<Response<Page>>(relaxed = true) {
            every { isSuccessful } returns false
            every { body() } returns null
        }

        val callbackSlot = slot<StatusCallback<Page>>()
        every { PageManager.getFrontPage(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onResponse(response, LinkHeaders(), ApiType.API)
        }

        // When
        presenter.editCourseHomePageClicked(course)

        // Then
        verify { view.showEditCourseHomePageDialog(false) }
    }

    @Test
    fun `editCourseHomePageClicked should show dialog with hasFrontPage false when API call fails`() {
        // Given
        val callbackSlot = slot<StatusCallback<Page>>()
        every { PageManager.getFrontPage(any(), any(), capture(callbackSlot)) } answers {
            callbackSlot.captured.onFail(null, Throwable("404 Not Found"), null)
        }

        // When
        presenter.editCourseHomePageClicked(course)

        // Then
        verify { view.showEditCourseHomePageDialog(false) }
    }

    @Test
    fun `editCourseNameClicked should call showEditCourseNameDialog`() {
        // When
        presenter.editCourseNameClicked()

        // Then
        verify { view.showEditCourseNameDialog() }
    }
}