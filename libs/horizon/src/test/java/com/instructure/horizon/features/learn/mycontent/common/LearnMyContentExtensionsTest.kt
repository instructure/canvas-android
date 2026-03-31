/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.mycontent.common

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.instructure.canvasapi2.models.journey.mycontent.CourseEnrollmentItem
import com.instructure.canvasapi2.models.journey.mycontent.ProgramEnrollmentItem
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.navigation.MainNavigationRoute
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

class LearnMyContentExtensionsTest {

    private val resources: Resources = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setup() {
        val sharedPrefs: SharedPreferences = mockk(relaxed = true)
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.getInt(any(), any()) } returns 0
        ContextKeeper.appContext = context

        every { resources.getString(R.string.learnMyContentProgramLabel) } returns "Program"
        every { resources.getString(R.string.learnMyContentCourseLabel) } returns "Course"
        every { resources.getString(R.string.learnMyContentStartLearning) } returns "Start learning"
        every { resources.getString(R.string.learnMyContentResumeLearning) } returns "Resume learning"
        every { resources.getString(R.string.learnMyContentDurationHrsMin, any(), any()) } answers {
            val arr = args[1] as Array<*>
            "${arr[0]}h ${arr[1]}m"
        }
        every { resources.getString(R.string.learnMyContentDurationHrs, any()) } answers {
            val arr = args[1] as Array<*>
            "${arr[0]}h"
        }
        every { resources.getString(R.string.learnMyContentDurationMin, any()) } answers {
            val arr = args[1] as Array<*>
            "${arr[0]}m"
        }
        every { resources.getString(R.string.programTag_DateRange, any(), any()) } answers {
            val arr = args[1] as Array<*>
            "${arr[0]} - ${arr[1]}"
        }
        every { resources.getQuantityString(R.plurals.learnMyContentProgramCourseCount, any(), any()) } answers {
            "${secondArg<Int>()} courses"
        }
    }

    // --- ProgramEnrollmentItem tests ---

    @Test
    fun `ProgramEnrollmentItem toCardState has no imageUrl`() = runTest {
        val state = createTestProgramItem().toCardState(resources) { null }

        assertNull(state.imageUrl)
    }

    @Test
    fun `ProgramEnrollmentItem toCardState sets correct name`() = runTest {
        val state = createTestProgramItem(name = "My Program").toCardState(resources) { null }

        assertEquals("My Program", state.name)
    }

    @Test
    fun `ProgramEnrollmentItem toCardState sets correct progress`() = runTest {
        val state = createTestProgramItem(completionPercentage = 75.0).toCardState(resources) { null }

        assertEquals(75.0, state.progress)
    }

    @Test
    fun `ProgramEnrollmentItem toCardState has no buttonState`() = runTest {
        val state = createTestProgramItem().toCardState(resources) { null }

        assertNull(state.buttonState)
    }

    @Test
    fun `ProgramEnrollmentItem toCardState route points to program details screen`() = runTest {
        val state = createTestProgramItem(id = "prog123").toCardState(resources) { null }

        assertEquals(LearnRoute.LearnProgramDetailsScreen.route("prog123"), state.route)
    }

    @Test
    fun `ProgramEnrollmentItem toCardState includes program type chip`() = runTest {
        val state = createTestProgramItem().toCardState(resources) { null }

        assertTrue(state.cardChips.any { it.label == "Program" })
    }

    @Test
    fun `ProgramEnrollmentItem toCardState includes course count chip`() = runTest {
        val state = createTestProgramItem(courseCount = 3).toCardState(resources) { null }

        assertTrue(state.cardChips.any { it.label == "3 courses" })
    }

    @Test
    fun `ProgramEnrollmentItem toCardState includes hours and minutes duration chip`() = runTest {
        val state = createTestProgramItem(estimatedDurationMinutes = 90).toCardState(resources) { null }

        assertTrue(state.cardChips.any { it.label == "1h 30m" })
    }

    @Test
    fun `ProgramEnrollmentItem toCardState includes hours-only duration chip`() = runTest {
        val state = createTestProgramItem(estimatedDurationMinutes = 120).toCardState(resources) { null }

        assertTrue(state.cardChips.any { it.label == "2h" })
    }

    @Test
    fun `ProgramEnrollmentItem toCardState includes minutes-only duration chip`() = runTest {
        val state = createTestProgramItem(estimatedDurationMinutes = 45).toCardState(resources) { null }

        assertTrue(state.cardChips.any { it.label == "45m" })
    }

    @Test
    fun `ProgramEnrollmentItem toCardState excludes duration chip when estimatedDurationMinutes is null`() = runTest {
        val state = createTestProgramItem(estimatedDurationMinutes = null).toCardState(resources) { null }

        assertTrue(state.cardChips.none { it.label.matches(Regex("\\d.*[mh]")) })
    }

    @Test
    fun `ProgramEnrollmentItem toCardState excludes duration chip when estimatedDurationMinutes is zero`() = runTest {
        val state = createTestProgramItem(estimatedDurationMinutes = 0).toCardState(resources) { null }

        assertTrue(state.cardChips.none { it.label.matches(Regex("\\d.*[mh]")) })
    }

    @Test
    fun `ProgramEnrollmentItem toCardState includes date range chip when both start and end dates present`() = runTest {
        val state = createTestProgramItem(startDate = Date(0), endDate = Date(86400000L)).toCardState(resources) { null }

        assertTrue(state.cardChips.any { it.iconRes == R.drawable.calendar_today })
    }

    @Test
    fun `ProgramEnrollmentItem toCardState excludes date range chip when startDate is null`() = runTest {
        val state = createTestProgramItem(startDate = null, endDate = Date()).toCardState(resources) { null }

        assertTrue(state.cardChips.none { it.iconRes == R.drawable.calendar_today })
    }

    @Test
    fun `ProgramEnrollmentItem toCardState excludes date range chip when endDate is null`() = runTest {
        val state = createTestProgramItem(startDate = Date(), endDate = null).toCardState(resources) { null }

        assertTrue(state.cardChips.none { it.iconRes == R.drawable.calendar_today })
    }

    // --- CourseEnrollmentItem tests ---

    @Test
    fun `CourseEnrollmentItem toCardState route points to course details screen`() = runTest {
        val state = createTestCourseItem(id = "42").toCardState(resources) { null }

        assertEquals(LearnRoute.LearnCourseDetailsScreen.route(42L), state.route)
    }

    @Test
    fun `CourseEnrollmentItem toCardState includes course type chip`() = runTest {
        val state = createTestCourseItem().toCardState(resources) { null }

        assertTrue(state.cardChips.any { it.label == "Course" })
    }

    @Test
    fun `CourseEnrollmentItem toCardState with null completionPercentage has Start learning button`() = runTest {
        val moduleRoute = MainNavigationRoute.ModuleItemSequence(courseId = 42L, moduleItemId = 1L)

        val state = createTestCourseItem(completionPercentage = null).toCardState(resources) { moduleRoute }

        assertEquals("Start learning", state.buttonState?.label)
    }

    @Test
    fun `CourseEnrollmentItem toCardState with zero completionPercentage has Start learning button`() = runTest {
        val moduleRoute = MainNavigationRoute.ModuleItemSequence(courseId = 42L, moduleItemId = 1L)

        val state = createTestCourseItem(completionPercentage = 0.0).toCardState(resources) { moduleRoute }

        assertEquals("Start learning", state.buttonState?.label)
    }

    @Test
    fun `CourseEnrollmentItem toCardState with partial progress has Resume learning button`() = runTest {
        val moduleRoute = MainNavigationRoute.ModuleItemSequence(courseId = 42L, moduleItemId = 1L)

        val state = createTestCourseItem(completionPercentage = 55.0).toCardState(resources) { moduleRoute }

        assertEquals("Resume learning", state.buttonState?.label)
    }

    @Test
    fun `CourseEnrollmentItem toCardState with 100 percent completion has no button`() = runTest {
        val state = createTestCourseItem(completionPercentage = 100.0).toCardState(resources) { null }

        assertNull(state.buttonState)
    }

    @Test
    fun `CourseEnrollmentItem toCardState button route is the returned ModuleItemSequence route`() = runTest {
        val moduleRoute = MainNavigationRoute.ModuleItemSequence(courseId = 123L, moduleItemId = 456L)

        val state = createTestCourseItem(id = "123", completionPercentage = 50.0).toCardState(resources) { moduleRoute }

        assertNotNull(state.buttonState)
        assertTrue(state.buttonState!!.route is MainNavigationRoute.ModuleItemSequence)
        val route = state.buttonState!!.route as MainNavigationRoute.ModuleItemSequence
        assertEquals(456L, route.moduleItemId)
    }

    @Test
    fun `CourseEnrollmentItem toCardState without moduleItem has no button despite incomplete progress`() = runTest {
        val state = createTestCourseItem(completionPercentage = 50.0).toCardState(resources) { null }

        assertNull(state.buttonState)
    }

    @Test
    fun `CourseEnrollmentItem toCardState sets imageUrl from item`() = runTest {
        val state = createTestCourseItem(imageUrl = "https://example.com/img.jpg").toCardState(resources) { null }

        assertEquals("https://example.com/img.jpg", state.imageUrl)
    }

    @Test
    fun `CourseEnrollmentItem toCardState includes date range chip when both dates present`() = runTest {
        val state = createTestCourseItem(startAt = Date(0), endAt = Date(86400000L)).toCardState(resources) { null }

        assertTrue(state.cardChips.any { it.iconRes == R.drawable.calendar_today })
    }

    @Test
    fun `CourseEnrollmentItem toCardState excludes date range chip when only startAt is set`() = runTest {
        val state = createTestCourseItem(startAt = Date(), endAt = null).toCardState(resources) { null }

        assertTrue(state.cardChips.none { it.iconRes == R.drawable.calendar_today })
    }

    private fun createTestProgramItem(
        id: String = "program1",
        name: String = "Test Program",
        completionPercentage: Double? = 50.0,
        startDate: Date? = null,
        endDate: Date? = null,
        estimatedDurationMinutes: Int? = null,
        courseCount: Int = 2,
    ) = ProgramEnrollmentItem(
        id = id,
        name = name,
        position = 1,
        enrolledAt = Date(),
        completionPercentage = completionPercentage,
        startDate = startDate,
        endDate = endDate,
        status = "active",
        description = null,
        variant = "standard",
        estimatedDurationMinutes = estimatedDurationMinutes,
        courseCount = courseCount,
    )

    private fun createTestCourseItem(
        id: String = "42",
        name: String = "Test Course",
        completionPercentage: Double? = 50.0,
        imageUrl: String? = null,
        startAt: Date? = null,
        endAt: Date? = null,
    ) = CourseEnrollmentItem(
        id = id,
        name = name,
        position = 1,
        enrolledAt = Date(),
        completionPercentage = completionPercentage,
        startAt = startAt,
        endAt = endAt,
        requirementCount = 10,
        requirementCompletedCount = 5,
        completedAt = null,
        grade = null,
        imageUrl = imageUrl,
        workflowState = "available",
        lastActivityAt = null,
    )
}
