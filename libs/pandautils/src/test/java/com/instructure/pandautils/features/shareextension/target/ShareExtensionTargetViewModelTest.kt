/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.pandautils.features.shareextension.target

import android.content.res.Resources
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.FileUploadType
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import org.junit.Test

@ExperimentalCoroutinesApi
class ShareExtensionTargetViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val courseManager: CourseManager = mockk(relaxed = true)
    private val assignmentManager: AssignmentManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    @Before
    fun setUp() {

        val courses = listOf(
                Course(id = 1L, name = "Course 1"),
                Course(id = 2L, name = "Course 2")
        )

        every { apiPrefs.user } returns User(name = "Test User")
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { assignmentManager.getAllAssignmentsAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(any()) } returns ThemedColor(0, 0)
        every { ColorKeeper.getOrGenerateColor(any()) } returns ThemedColor(0, 0)

        setupStrings()
    }

    fun tearDown() {
        unmockkObject(ColorKeeper)
    }

    @Test
    fun `Error when fetching courses`() {
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        val viewModel = createViewModel()

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        assertEquals(ShareExtensionTargetAction.ShowToast("An unexpected error occurred."), viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Assignments target selected`() {
        val viewModel = createViewModel()

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.assignmentTargetSelected()

        assertEquals(ShareExtensionTargetAction.AssignmentTargetSelected, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `My files target selected`() {
        val viewModel = createViewModel()

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.filesTargetSelected()

        assertEquals(ShareExtensionTargetAction.FilesTargetSelected, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Assignments without online_upload submission are filtered`() {
        every { assignmentManager.getAllAssignmentsAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(createAssignment(1L, "Assignment 1", 1L, submissionTypes = emptyList())))
        }

        val viewModel = createViewModel()

        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.assignmentTargetSelected()
        viewModel.onCourseSelected(0)

        assertEquals(ShareExtensionAssignmentViewData("This course has no assignments that allow file uploads."), viewModel.data.value?.assignments?.get(0)?.data)
    }

    @Test
    fun `Switching courses refreshes the assignments`() {
        every { assignmentManager.getAllAssignmentsAsync(1L, any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(
                    createAssignment(1L, "Assignment 1", 1L)
            ))
        }

        every { assignmentManager.getAllAssignmentsAsync(2L, any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(
                    createAssignment(2L, "Assignment 2", 2L)
            ))
        }

        val viewModel = createViewModel()

        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.assignmentTargetSelected()
        viewModel.onCourseSelected(0)
        assertEquals(ShareExtensionAssignmentViewData("Assignment 1"), viewModel.data.value?.assignments?.get(0)?.data)

        viewModel.onCourseSelected(1)
        assertEquals(ShareExtensionAssignmentViewData("Assignment 2"), viewModel.data.value?.assignments?.get(0)?.data)
    }

    @Test
    fun `No assignment selected error`() {
        every { assignmentManager.getAllAssignmentsAsync(1L, any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(
                    createAssignment(1L, "Assignment 1", 1L)
            ))
        }

        val viewModel = createViewModel()

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.assignmentTargetSelected()
        viewModel.onCourseSelected(1)
        viewModel.onAssignmentSelected(0)
        viewModel.validateDataAndMoveToFileUpload()

        assertEquals(ShareExtensionTargetAction.ShowToast("Please select an assignment"), viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `My files validate data`() {
        val viewModel = createViewModel()

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.filesTargetSelected()
        viewModel.validateDataAndMoveToFileUpload()

        assertEquals(ShareExtensionTargetAction.ShowFileUpload(FileUploadTargetData(null, null, FileUploadType.USER)), viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Assignment validate data`() {
        val assignment = createAssignment(1L, "Assignment 1", 1L)
        val course = Course(1L, "Course 1")
        every { assignmentManager.getAllAssignmentsAsync(1L, any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(
                    assignment
            ))
        }
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }
        val viewModel = createViewModel()

        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        viewModel.assignmentTargetSelected()
        viewModel.onCourseSelected(0)
        viewModel.onAssignmentSelected(0)
        viewModel.validateDataAndMoveToFileUpload()

        assertEquals(ShareExtensionTargetAction.ShowFileUpload(FileUploadTargetData(course, assignment, FileUploadType.ASSIGNMENT)), viewModel.events.value?.getContentIfNotHandled())
    }

    private fun createViewModel(): ShareExtensionTargetViewModel {
        return ShareExtensionTargetViewModel(courseManager, assignmentManager, resources, apiPrefs)
    }

    private fun createAssignment(id: Long, name: String, courseId: Long, allowedExtensions: List<String> = emptyList(), submissionTypes: List<String> = listOf("online_upload")): Assignment {
        return Assignment(id = id, name = name, courseId = courseId, allowedExtensions = allowedExtensions, submissionTypesRaw = submissionTypes)
    }

    private fun setupStrings() {
        every { resources.getString(R.string.errorOccurred) } returns "An unexpected error occurred."
        every { resources.getString(R.string.noAssignmentsWithFileUpload) } returns "This course has no assignments that allow file uploads."
        every { resources.getString(R.string.noAssignmentSelected) } returns "Please select an assignment"
        every { resources.getString(R.string.noCourseSelected) } returns "Please select a course"
    }
}
