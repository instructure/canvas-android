/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.widget.courses

import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.data.repository.user.UserRepository
import com.instructure.pandautils.domain.usecase.announcements.LoadCourseAnnouncementsUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadFavoriteCoursesParams
import com.instructure.pandautils.domain.usecase.courses.LoadFavoriteCoursesUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadGroupsParams
import com.instructure.pandautils.domain.usecase.courses.LoadGroupsUseCase
import com.instructure.pandautils.domain.usecase.offline.ObserveOfflineSyncUpdatesUseCase
import com.instructure.pandautils.features.dashboard.customize.WidgetSettingItem
import com.instructure.pandautils.features.dashboard.widget.SettingType
import com.instructure.pandautils.features.dashboard.widget.courses.model.GradeDisplay
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveGlobalConfigUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetConfigUseCase
import com.instructure.pandautils.room.offline.daos.CourseDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CoursesWidgetViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val loadFavoriteCoursesUseCase: LoadFavoriteCoursesUseCase = mockk()
    private val loadGroupsUseCase: LoadGroupsUseCase = mockk()
    private val loadCourseUseCase: LoadCourseUseCase = mockk()
    private val loadCourseAnnouncementsUseCase: LoadCourseAnnouncementsUseCase = mockk()
    private val sectionExpandedStateDataStore: SectionExpandedStateDataStore = mockk(relaxed = true)
    private val coursesWidgetBehavior: CoursesWidgetBehavior = mockk(relaxed = true)
    private val courseSyncSettingsDao: CourseSyncSettingsDao = mockk()
    private val courseDao: CourseDao = mockk()
    private val networkStateProvider: NetworkStateProvider = mockk()
    private val featureFlagProvider: FeatureFlagProvider = mockk()
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val localBroadcastManager: LocalBroadcastManager = mockk()
    private val observeWidgetConfigUseCase: ObserveWidgetConfigUseCase = mockk()
    private val observeOfflineSyncUpdatesUseCase: ObserveOfflineSyncUpdatesUseCase = mockk()
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val observeGlobalConfigUseCase: ObserveGlobalConfigUseCase = mockk(relaxed = true)

    private lateinit var viewModel: CoursesWidgetViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(any<Course>()) } returns ThemedColor(0xFF0000, 0xFF0000)
        every { ColorKeeper.getOrGenerateColor(any<Group>()) } returns ThemedColor(0x00FF00, 0x00FF00)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun setupDefaultMocks() {
        coEvery { loadFavoriteCoursesUseCase(any()) } returns emptyList()
        coEvery { loadGroupsUseCase(any()) } returns emptyList()
        coEvery { loadCourseAnnouncementsUseCase(any()) } returns emptyList()
        every { sectionExpandedStateDataStore.observeCoursesExpanded() } returns flowOf(true)
        every { sectionExpandedStateDataStore.observeGroupsExpanded() } returns flowOf(true)
        every { observeWidgetConfigUseCase(any<String>()) } returns flowOf(
            listOf(
                WidgetSettingItem(key = CoursesConfig.KEY_SHOW_GRADES, value = false, type = SettingType.BOOLEAN),
                WidgetSettingItem(key = CoursesConfig.KEY_SHOW_COLOR_OVERLAY, value = false, type = SettingType.BOOLEAN)
            )
        )
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        every { networkStateProvider.isOnline() } returns true
        every { localBroadcastManager.registerReceiver(any(), any()) } returns Unit
        every { localBroadcastManager.unregisterReceiver(any()) } returns Unit
        coEvery { courseSyncSettingsDao.findAll() } returns emptyList()
        coEvery { courseDao.findByIds(any()) } returns emptyList()
        every { observeOfflineSyncUpdatesUseCase(Unit) } returns flowOf()
        every { observeGlobalConfigUseCase(Unit) } returns flowOf(
            com.instructure.pandautils.features.dashboard.widget.GlobalConfig()
        )
    }

    @Test
    fun `init loads courses and groups successfully`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = true)
        )
        val groups = listOf(
            Group(id = 1, name = "Group 1", isFavorite = true)
        )
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses
        coEvery { loadGroupsUseCase(any()) } returns groups

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals(2, state.courses.size)
        assertEquals(1, state.groups.size)
    }

    @Test
    fun `init sets error state when loading fails`() {
        setupDefaultMocks()
        coEvery { loadFavoriteCoursesUseCase(any()) } throws RuntimeException("Network error")

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isError)
    }

    @Test
    fun `refresh reloads data with forceRefresh`() {
        setupDefaultMocks()
        coEvery { loadFavoriteCoursesUseCase(any()) } returns emptyList()
        coEvery { loadGroupsUseCase(any()) } returns emptyList()

        viewModel = createViewModel()
        viewModel.refresh()

        coVerify { loadFavoriteCoursesUseCase(LoadFavoriteCoursesParams(forceRefresh = true)) }
        coVerify { loadGroupsUseCase(LoadGroupsParams(forceRefresh = true)) }
    }

    @Test
    fun `toggleCoursesExpanded updates expanded state`() {
        setupDefaultMocks()

        viewModel = createViewModel()
        viewModel.uiState.value.onToggleCoursesExpanded()

        coVerify { sectionExpandedStateDataStore.setCoursesExpanded(false) }
    }

    @Test
    fun `toggleGroupsExpanded updates expanded state`() {
        setupDefaultMocks()

        viewModel = createViewModel()
        viewModel.uiState.value.onToggleGroupsExpanded()

        coVerify { sectionExpandedStateDataStore.setGroupsExpanded(false) }
    }

    @Test
    fun `observeExpandedStates updates ui state`() {
        setupDefaultMocks()
        every { sectionExpandedStateDataStore.observeCoursesExpanded() } returns flowOf(false)
        every { sectionExpandedStateDataStore.observeGroupsExpanded() } returns flowOf(false)

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isCoursesExpanded)
        assertFalse(state.isGroupsExpanded)
    }

    @Test
    fun `observeGradeVisibility updates showGrades in ui state`() {
        setupDefaultMocks()
        every { observeWidgetConfigUseCase(any<String>()) } returns flowOf(
            listOf(
                WidgetSettingItem(key = CoursesConfig.KEY_SHOW_GRADES, value = true, type = SettingType.BOOLEAN),
                WidgetSettingItem(key = CoursesConfig.KEY_SHOW_COLOR_OVERLAY, value = false, type = SettingType.BOOLEAN)
            )
        )

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.showGrades)
    }

    @Test
    fun `observeColorOverlay updates showColorOverlay in ui state`() {
        setupDefaultMocks()
        every { observeWidgetConfigUseCase(any<String>()) } returns flowOf(
            listOf(
                WidgetSettingItem(key = CoursesConfig.KEY_SHOW_GRADES, value = false, type = SettingType.BOOLEAN),
                WidgetSettingItem(key = CoursesConfig.KEY_SHOW_COLOR_OVERLAY, value = true, type = SettingType.BOOLEAN)
            )
        )

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.showColorOverlay)
    }

    @Test
    fun `courses are mapped to CourseCardItems correctly`() {
        setupDefaultMocks()
        val enrollment = Enrollment(
            type = Enrollment.EnrollmentType.Student,
            computedCurrentGrade = "A",
            computedCurrentScore = 95.0
        )
        val courses = listOf(
            Course(
                id = 1,
                name = "Test Course",
                courseCode = "TC101",
                imageUrl = "https://example.com/image.jpg",
                isFavorite = true,
                enrollments = mutableListOf(enrollment)
            )
        )
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.courses.size)
        val courseCard = state.courses[0]
        assertEquals(1L, courseCard.id)
        assertEquals("Test Course", courseCard.name)
        assertEquals("TC101", courseCard.courseCode)
        assertEquals("https://example.com/image.jpg", courseCard.imageUrl)
        assertTrue(courseCard.isClickable)
    }

    @Test
    fun `grade is mapped as Letter when currentGrade is available`() {
        setupDefaultMocks()
        val enrollment = Enrollment(
            type = Enrollment.EnrollmentType.Student,
            computedCurrentGrade = "B+",
            computedCurrentScore = 88.0
        )
        val course = Course(id = 1, name = "Course", isFavorite = true, enrollments = mutableListOf(enrollment))
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()

        val grade = viewModel.uiState.value.courses[0].grade
        assertTrue(grade is GradeDisplay.Letter)
        assertEquals("B+", (grade as GradeDisplay.Letter).grade)
    }

    @Test
    fun `grade is mapped as Percentage when only score is available`() {
        setupDefaultMocks()
        val enrollment = Enrollment(
            type = Enrollment.EnrollmentType.Student,
            computedCurrentScore = 75.0
        )
        val course = Course(id = 1, name = "Course", isFavorite = true, enrollments = mutableListOf(enrollment))
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()

        val grade = viewModel.uiState.value.courses[0].grade
        assertTrue(grade is GradeDisplay.Percentage)
        assertEquals("75%", (grade as GradeDisplay.Percentage).value)
    }

    @Test
    fun `grade is mapped as Locked when hideFinalGrades is true and no grade available`() {
        setupDefaultMocks()
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Student)
        val course = Course(
            id = 1,
            name = "Course",
            isFavorite = true,
            hideFinalGrades = true,
            enrollments = mutableListOf(enrollment)
        )
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()

        val grade = viewModel.uiState.value.courses[0].grade
        assertTrue(grade is GradeDisplay.Locked)
    }

    @Test
    fun `grade is mapped as Hidden when no enrollment`() {
        setupDefaultMocks()
        val course = Course(id = 1, name = "Course", isFavorite = true)
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()

        val grade = viewModel.uiState.value.courses[0].grade
        assertTrue(grade is GradeDisplay.Hidden)
    }

    @Test
    fun `courses are marked as not clickable when offline and not synced`() {
        setupDefaultMocks()
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { courseSyncSettingsDao.findAll() } returns emptyList()
        every { networkStateProvider.isOnline() } returns false

        val courses = listOf(Course(id = 1, name = "Course", isFavorite = true))
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.courses[0].isClickable)
    }

    @Test
    fun `courses are marked as clickable when offline but synced`() {
        setupDefaultMocks()
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { courseSyncSettingsDao.findAll() } returns listOf(
            CourseSyncSettingsEntity(courseId = 1, courseName = "Course", fullContentSync = true)
        )
        every { networkStateProvider.isOnline() } returns false

        val courses = listOf(Course(id = 1, name = "Course", isFavorite = true))
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses
        coEvery { courseDao.findByIds(setOf(1)) } returns listOf(
            CourseEntity(Course(id = 1, name = "Course"))
        )

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.courses[0].isClickable)
        assertTrue(state.courses[0].isSynced)
    }

    @Test
    fun `courses are always clickable when online`() {
        setupDefaultMocks()
        every { networkStateProvider.isOnline() } returns true

        val courses = listOf(Course(id = 1, name = "Course", isFavorite = true))
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.courses[0].isClickable)
    }

    @Test
    fun `groups are mapped to GroupCardItems correctly`() {
        setupDefaultMocks()
        val parentCourse = Course(id = 100, name = "Parent Course")
        val groups = listOf(
            Group(id = 1, name = "Study Group", courseId = 100, membersCount = 5, isFavorite = true)
        )
        coEvery { loadGroupsUseCase(any()) } returns groups
        coEvery { loadCourseUseCase(any()) } returns parentCourse

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.groups.size)
        val groupCard = state.groups[0]
        assertEquals(1L, groupCard.id)
        assertEquals("Study Group", groupCard.name)
        assertEquals("Parent Course", groupCard.parentCourseName)
        assertEquals(5, groupCard.memberCount)
    }

    @Test
    fun `groups without parent course have null parentCourseName`() {
        setupDefaultMocks()
        val groups = listOf(
            Group(id = 1, name = "Standalone Group", courseId = 0, isFavorite = true)
        )
        coEvery { loadGroupsUseCase(any()) } returns groups

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.groups.size)
        assertEquals(null, state.groups[0].parentCourseName)
    }

    @Test
    fun `onCourseClick delegates to behavior`() {
        setupDefaultMocks()
        val course = Course(id = 1, name = "Course", isFavorite = true)
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()
        val activity: FragmentActivity = mockk()
        viewModel.uiState.value.onCourseClick(activity, 1)

        verify { coursesWidgetBehavior.onCourseClick(activity, course) }
    }

    @Test
    fun `onGroupClick delegates to behavior`() {
        setupDefaultMocks()
        val group = Group(id = 1, name = "Group", isFavorite = true)
        coEvery { loadGroupsUseCase(any()) } returns listOf(group)

        viewModel = createViewModel()
        val activity: FragmentActivity = mockk()
        viewModel.uiState.value.onGroupClick(activity, 1)

        verify { coursesWidgetBehavior.onGroupClick(activity, group) }
    }

    @Test
    fun `onManageOfflineContent delegates to behavior`() {
        setupDefaultMocks()
        val course = Course(id = 1, name = "Course", isFavorite = true)
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()
        val activity: FragmentActivity = mockk()
        viewModel.uiState.value.onManageOfflineContent(activity, 1)

        verify { coursesWidgetBehavior.onManageOfflineContent(activity, course) }
    }

    @Test
    fun `onCustomizeCourse delegates to behavior`() {
        setupDefaultMocks()
        val course = Course(id = 1, name = "Course", isFavorite = true)
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()
        val activity: FragmentActivity = mockk()
        viewModel.uiState.value.onCustomizeCourse(activity, 1)

        verify { coursesWidgetBehavior.onCustomizeCourse(activity, course) }
    }

    @Test
    fun `onAllCourses delegates to behavior`() {
        setupDefaultMocks()

        viewModel = createViewModel()
        val activity: FragmentActivity = mockk()
        viewModel.uiState.value.onAllCourses(activity)

        verify { coursesWidgetBehavior.onAllCoursesClicked(activity) }
    }

    @Test
    fun `broadcast receiver is registered on init`() {
        setupDefaultMocks()

        viewModel = createViewModel()

        verify {
            localBroadcastManager.registerReceiver(any(), any())
        }
    }

    @Test
    fun `broadcast with COURSE_FAVORITES true triggers refresh`() {
        setupDefaultMocks()
        val intent: Intent = mockk(relaxed = true)
        val extras: Bundle = mockk()
        every { intent.extras } returns extras
        every { intent.getLongExtra(Const.COURSE_ID, -1L) } returns -1L
        every { extras.getBoolean(Const.COURSE_FAVORITES) } returns true

        viewModel = createViewModel()
        coEvery { loadFavoriteCoursesUseCase(LoadFavoriteCoursesParams(forceRefresh = true)) } returns emptyList()
        coEvery { loadGroupsUseCase(LoadGroupsParams(forceRefresh = true)) } returns emptyList()

        val receiverSlot = slot<BroadcastReceiver>()
        verify { localBroadcastManager.registerReceiver(capture(receiverSlot), any()) }

        receiverSlot.captured.onReceive(mockk(), intent)

        coVerify { loadFavoriteCoursesUseCase(LoadFavoriteCoursesParams(forceRefresh = true)) }
        coVerify { loadGroupsUseCase(LoadGroupsParams(forceRefresh = true)) }
    }

    @Test
    fun `broadcast without COURSE_FAVORITES does not trigger refresh`() {
        setupDefaultMocks()
        val intent: Intent = mockk(relaxed = true)
        val extras: Bundle = mockk()
        every { intent.extras } returns extras
        every { intent.getLongExtra(Const.COURSE_ID, -1L) } returns -1L
        every { extras.getBoolean(Const.COURSE_FAVORITES) } returns false

        viewModel = createViewModel()

        val receiverSlot = slot<BroadcastReceiver>()
        verify { localBroadcastManager.registerReceiver(capture(receiverSlot), any()) }

        receiverSlot.captured.onReceive(mockk(), intent)

        coVerify(exactly = 1) { loadFavoriteCoursesUseCase(LoadFavoriteCoursesParams(forceRefresh = false)) }
        coVerify(exactly = 1) { loadGroupsUseCase(LoadGroupsParams(forceRefresh = false)) }
    }

    @Test
    fun `exception during load is recorded to crashlytics`() {
        setupDefaultMocks()
        val exception = Exception("Test exception")
        coEvery { loadFavoriteCoursesUseCase(any()) } throws exception

        viewModel = createViewModel()

        verify { crashlytics.recordException(exception) }
        assertTrue(viewModel.uiState.value.isError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `init loads announcements for each course`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = true)
        )
        val announcements1 = listOf(
            mockk<com.instructure.canvasapi2.models.DiscussionTopicHeader>(relaxed = true)
        )
        val announcements2 = listOf(
            mockk<com.instructure.canvasapi2.models.DiscussionTopicHeader>(relaxed = true),
            mockk<com.instructure.canvasapi2.models.DiscussionTopicHeader>(relaxed = true)
        )

        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses
        coEvery { loadCourseAnnouncementsUseCase(any()) } returns emptyList()
        coEvery { loadCourseAnnouncementsUseCase(match { it.courseId == 1L }) } returns announcements1
        coEvery { loadCourseAnnouncementsUseCase(match { it.courseId == 2L }) } returns announcements2

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.courses[0].announcements.size)
        assertEquals(2, state.courses[1].announcements.size)
    }

    @Test
    fun `init handles announcement loading failure gracefully`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = true)
        )
        val exception = Exception("Failed to load announcements")

        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses
        coEvery { loadCourseAnnouncementsUseCase(match { it.courseId == 1L }) } throws exception
        coEvery { loadCourseAnnouncementsUseCase(match { it.courseId == 2L }) } returns emptyList()

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isError)
        assertEquals(0, state.courses[0].announcements.size)
        assertEquals(0, state.courses[1].announcements.size)
        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `onAnnouncementClick finds course and calls behavior with announcements`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true)
        )
        val announcements = listOf(
            mockk<com.instructure.canvasapi2.models.DiscussionTopicHeader>(relaxed = true)
        )
        val activity: FragmentActivity = mockk(relaxed = true)

        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses
        coEvery { loadCourseAnnouncementsUseCase(any()) } returns announcements

        viewModel = createViewModel()

        viewModel.uiState.value.onAnnouncementClick(activity, 1L)

        verify { coursesWidgetBehavior.onAnnouncementClick(activity, courses[0], announcements) }
    }

    @Test
    fun `onAnnouncementClick does nothing when course not found`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true)
        )
        val activity: FragmentActivity = mockk(relaxed = true)

        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        viewModel.uiState.value.onAnnouncementClick(activity, 999L)

        verify(exactly = 0) { coursesWidgetBehavior.onAnnouncementClick(any(), any(), any()) }
    }

    @Test
    fun `onGroupMessageClick finds group and calls behavior`() {
        setupDefaultMocks()
        val groups = listOf(
            Group(id = 1, name = "Group 1", isFavorite = true)
        )
        val activity: FragmentActivity = mockk(relaxed = true)

        coEvery { loadGroupsUseCase(any()) } returns groups

        viewModel = createViewModel()

        viewModel.uiState.value.onGroupMessageClick(activity, 1L)

        verify { coursesWidgetBehavior.onGroupMessageClick(activity, groups[0]) }
    }

    @Test
    fun `onGroupMessageClick does nothing when group not found`() {
        setupDefaultMocks()
        val groups = listOf(
            Group(id = 1, name = "Group 1", isFavorite = true)
        )
        val activity: FragmentActivity = mockk(relaxed = true)

        coEvery { loadGroupsUseCase(any()) } returns groups

        viewModel = createViewModel()

        viewModel.uiState.value.onGroupMessageClick(activity, 999L)

        verify(exactly = 0) { coursesWidgetBehavior.onGroupMessageClick(any(), any()) }
    }

    @Test
    fun `broadcast receiver with COURSE_ID reloads specific course`() {
        setupDefaultMocks()
        val initialCourses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = true)
        )
        val updatedCourse = Course(id = 1, name = "Updated Course 1", isFavorite = true)
        val announcements = listOf(
            mockk<com.instructure.canvasapi2.models.DiscussionTopicHeader>(relaxed = true)
        )

        coEvery { loadFavoriteCoursesUseCase(any()) } returns initialCourses
        coEvery { loadCourseUseCase(any()) } returns updatedCourse
        coEvery { loadCourseAnnouncementsUseCase(any()) } returns announcements

        viewModel = createViewModel()

        val receiverSlot = slot<BroadcastReceiver>()
        verify { localBroadcastManager.registerReceiver(capture(receiverSlot), any()) }

        val intent: Intent = mockk(relaxed = true)
        every { intent.getLongExtra(Const.COURSE_ID, -1L) } returns 1L

        receiverSlot.captured.onReceive(mockk(), intent)

        coVerify(exactly = 1) { loadCourseUseCase(match { it.courseId == 1L && it.forceNetwork }) }
        coVerify(exactly = 1) { loadCourseAnnouncementsUseCase(match { it.courseId == 1L && it.forceNetwork }) }

        val state = viewModel.uiState.value
        assertEquals("Updated Course 1", state.courses.find { it.id == 1L }?.name)
    }

    @Test
    fun `broadcast receiver with COURSE_FAVORITES triggers full refresh`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true)
        )

        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        val receiverSlot = slot<BroadcastReceiver>()
        verify { localBroadcastManager.registerReceiver(capture(receiverSlot), any()) }

        val intent: Intent = mockk(relaxed = true)
        val extras: Bundle = mockk()
        every { intent.extras } returns extras
        every { intent.getLongExtra(Const.COURSE_ID, -1L) } returns -1L
        every { extras.getBoolean(Const.COURSE_FAVORITES) } returns true

        receiverSlot.captured.onReceive(mockk(), intent)

        coVerify(atLeast = 2) { loadFavoriteCoursesUseCase(any()) }
    }

    @Test
    fun `reloadCourse handles announcement loading failure`() {
        setupDefaultMocks()
        val initialCourses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true)
        )
        val updatedCourse = Course(id = 1, name = "Updated Course 1", isFavorite = true)
        val exception = Exception("Failed to load announcements")

        coEvery { loadFavoriteCoursesUseCase(any()) } returns initialCourses
        coEvery { loadCourseUseCase(any()) } returns updatedCourse
        coEvery { loadCourseAnnouncementsUseCase(any()) } throws exception

        viewModel = createViewModel()

        val receiverSlot = slot<BroadcastReceiver>()
        verify { localBroadcastManager.registerReceiver(capture(receiverSlot), any()) }

        val intent: Intent = mockk(relaxed = true)
        every { intent.getLongExtra(Const.COURSE_ID, -1L) } returns 1L

        receiverSlot.captured.onReceive(mockk(), intent)

        verify { crashlytics.recordException(exception) }
        val state = viewModel.uiState.value
        assertEquals("Updated Course 1", state.courses.find { it.id == 1L }?.name)
        assertEquals(0, state.courses.find { it.id == 1L }?.announcements?.size)
    }

    @Test
    fun `reloadCourse handles course loading failure gracefully`() {
        setupDefaultMocks()
        val initialCourses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true)
        )
        val exception = Exception("Failed to load course")

        coEvery { loadFavoriteCoursesUseCase(any()) } returns initialCourses
        coEvery { loadCourseUseCase(any()) } throws exception

        viewModel = createViewModel()

        val receiverSlot = slot<BroadcastReceiver>()
        verify { localBroadcastManager.registerReceiver(capture(receiverSlot), any()) }

        val intent: Intent = mockk(relaxed = true)
        every { intent.getLongExtra(Const.COURSE_ID, -1L) } returns 1L

        receiverSlot.captured.onReceive(mockk(), intent)

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `onCourseMoved does nothing when fromIndex equals toIndex`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = true),
            Course(id = 3, name = "Course 3", isFavorite = true)
        )
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()
        val initialState = viewModel.uiState.value

        viewModel.uiState.value.onCourseMoved(1, 1)

        val finalState = viewModel.uiState.value
        assertEquals(initialState.courses, finalState.courses)
        coVerify(exactly = 0) { userRepository.updateDashboardPositions(any()) }
    }

    @Test
    fun `onCourseMoved updates UI state when moving course forward`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = true),
            Course(id = 3, name = "Course 3", isFavorite = true)
        )
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        viewModel.uiState.value.onCourseMoved(0, 2)

        val state = viewModel.uiState.value
        assertEquals(2L, state.courses[0].id)
        assertEquals(3L, state.courses[1].id)
        assertEquals(1L, state.courses[2].id)
    }

    @Test
    fun `onCourseMoved updates UI state when moving course backward`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = true),
            Course(id = 3, name = "Course 3", isFavorite = true)
        )
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        viewModel.uiState.value.onCourseMoved(2, 0)

        val state = viewModel.uiState.value
        assertEquals(3L, state.courses[0].id)
        assertEquals(1L, state.courses[1].id)
        assertEquals(2L, state.courses[2].id)
    }

    @Test
    fun `onCourseMoved calls updateDashboardPositions with correct positions`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = true),
            Course(id = 3, name = "Course 3", isFavorite = true)
        )
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses
        coEvery { userRepository.updateDashboardPositions(any()) } returns mockk()

        viewModel = createViewModel()

        viewModel.uiState.value.onCourseMoved(0, 2)

        coVerify {
            userRepository.updateDashboardPositions(
                match { positions ->
                    positions.positions["course_2"] == 0 &&
                    positions.positions["course_3"] == 1 &&
                    positions.positions["course_1"] == 2
                }
            )
        }
    }

    @Test
    fun `onCourseMoved records exception when updateDashboardPositions fails`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = true)
        )
        val exception = Exception("Failed to update positions")
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses
        coEvery { userRepository.updateDashboardPositions(any()) } throws exception

        viewModel = createViewModel()

        viewModel.uiState.value.onCourseMoved(0, 1)

        verify { crashlytics.recordException(exception) }
    }

    @Test
    fun `getSyncedCourseIds validates courses exist in courseDao`() {
        setupDefaultMocks()
        val syncSettings = listOf(
            CourseSyncSettingsEntity(courseId = 1, courseName = "Course 1", fullContentSync = true),
            CourseSyncSettingsEntity(courseId = 2, courseName = "Course 2", fullContentSync = true),
            CourseSyncSettingsEntity(courseId = 3, courseName = "Course 3", fullContentSync = true)
        )
        val syncedCourses = listOf(
            CourseEntity(Course(id = 1, name = "Course 1")),
            CourseEntity(Course(id = 2, name = "Course 2"))
            // Course 3 is not in courseDao (not synced yet)
        )
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { courseSyncSettingsDao.findAll() } returns syncSettings
        coEvery { courseDao.findByIds(setOf(1, 2, 3)) } returns syncedCourses
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(
            Course(id = 1, name = "Course 1"),
            Course(id = 2, name = "Course 2"),
            Course(id = 3, name = "Course 3")
        )

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        // Only courses 1 and 2 should be marked as synced (validated by courseDao)
        assertEquals(true, state.courses[0].isSynced)
        assertEquals(true, state.courses[1].isSynced)
        assertEquals(false, state.courses[2].isSynced)
    }

    @Test
    fun `observeOfflineSyncUpdates updates synced states when sync completes`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1"),
            Course(id = 2, name = "Course 2")
        )
        val syncUpdateFlow = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()

        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses
        every { observeOfflineSyncUpdatesUseCase(Unit) } returns syncUpdateFlow
        coEvery { courseSyncSettingsDao.findAll() } returns listOf(
            CourseSyncSettingsEntity(courseId = 1, courseName = "Course 1", fullContentSync = true)
        )
        coEvery { courseDao.findByIds(setOf(1)) } returns listOf(
            CourseEntity(Course(id = 1, name = "Course 1"))
        )

        viewModel = createViewModel()

        // Initially course 1 should be synced, course 2 not synced
        assertEquals(true, viewModel.uiState.value.courses[0].isSynced)
        assertEquals(false, viewModel.uiState.value.courses[1].isSynced)

        // Simulate sync completion for course 2
        coEvery { courseSyncSettingsDao.findAll() } returns listOf(
            CourseSyncSettingsEntity(courseId = 1, courseName = "Course 1", fullContentSync = true),
            CourseSyncSettingsEntity(courseId = 2, courseName = "Course 2", fullContentSync = true)
        )
        coEvery { courseDao.findByIds(setOf(1, 2)) } returns listOf(
            CourseEntity(Course(id = 1, name = "Course 1")),
            CourseEntity(Course(id = 2, name = "Course 2"))
        )

        runBlocking { syncUpdateFlow.emit(Unit) }

        // Both courses should now be synced
        assertEquals(true, viewModel.uiState.value.courses[0].isSynced)
        assertEquals(true, viewModel.uiState.value.courses[1].isSynced)
    }

    @Test
    fun `updateSyncedStates updates isClickable based on online status and sync state`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1"),
            Course(id = 2, name = "Course 2")
        )
        val syncUpdateFlow = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()

        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses
        every { observeOfflineSyncUpdatesUseCase(Unit) } returns syncUpdateFlow
        every { networkStateProvider.isOnline() } returns false
        coEvery { courseSyncSettingsDao.findAll() } returns listOf(
            CourseSyncSettingsEntity(courseId = 1, courseName = "Course 1", fullContentSync = true)
        )
        coEvery { courseDao.findByIds(setOf(1)) } returns listOf(
            CourseEntity(Course(id = 1, name = "Course 1"))
        )

        viewModel = createViewModel()

        // Offline: only synced course should be clickable
        assertEquals(true, viewModel.uiState.value.courses[0].isClickable)
        assertEquals(false, viewModel.uiState.value.courses[1].isClickable)

        // Go online
        every { networkStateProvider.isOnline() } returns true
        runBlocking { syncUpdateFlow.emit(Unit) }

        // Online: all courses should be clickable
        assertEquals(true, viewModel.uiState.value.courses[0].isClickable)
        assertEquals(true, viewModel.uiState.value.courses[1].isClickable)
    }

    @Test
    fun `offline sync updates are not observed when offline is disabled`() {
        setupDefaultMocks()
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        every { observeOfflineSyncUpdatesUseCase(Unit) } returns flowOf()

        viewModel = createViewModel()

        // Verify the use case still gets called but returns empty flow
        verify { observeOfflineSyncUpdatesUseCase(Unit) }
    }

    @Test
    fun `observeConfig updates color in ui state from global config`() {
        setupDefaultMocks()
        val testColor = 0xFF00FF00.toInt()
        val themedColor = ThemedColor(testColor, testColor)
        val globalConfig = com.instructure.pandautils.features.dashboard.widget.GlobalConfig(
            backgroundColor = testColor
        )
        every { observeGlobalConfigUseCase(Unit) } returns flowOf(globalConfig)
        every { ColorKeeper.createThemedColor(testColor) } returns themedColor

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(themedColor, state.color)
    }

    @Test
    fun `observeConfig sets default color when global config has default value`() {
        setupDefaultMocks()
        val defaultColor = com.instructure.pandautils.features.dashboard.widget.GlobalConfig.DEFAULT_COLOR
        val themedColor = ThemedColor(defaultColor, defaultColor)
        val globalConfig = com.instructure.pandautils.features.dashboard.widget.GlobalConfig()
        every { observeGlobalConfigUseCase(Unit) } returns flowOf(globalConfig)
        every { ColorKeeper.createThemedColor(defaultColor) } returns themedColor

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(themedColor, state.color)
    }

    @Test
    fun `observeConfig records exception when global config flow fails`() {
        setupDefaultMocks()
        val exception = Exception("Failed to load global config")
        every { observeGlobalConfigUseCase(Unit) } returns kotlinx.coroutines.flow.flow {
            throw exception
        }

        viewModel = createViewModel()

        verify { crashlytics.recordException(exception) }
    }

    private fun createViewModel(): CoursesWidgetViewModel {
        return CoursesWidgetViewModel(
            loadFavoriteCoursesUseCase = loadFavoriteCoursesUseCase,
            loadGroupsUseCase = loadGroupsUseCase,
            loadCourseUseCase = loadCourseUseCase,
            loadCourseAnnouncementsUseCase = loadCourseAnnouncementsUseCase,
            sectionExpandedStateDataStore = sectionExpandedStateDataStore,
            coursesWidgetBehavior = coursesWidgetBehavior,
            courseSyncSettingsDao = courseSyncSettingsDao,
            courseDao = courseDao,
            networkStateProvider = networkStateProvider,
            featureFlagProvider = featureFlagProvider,
            crashlytics = crashlytics,
            localBroadcastManager = localBroadcastManager,
            observeWidgetConfigUseCase = observeWidgetConfigUseCase,
            observeOfflineSyncUpdatesUseCase = observeOfflineSyncUpdatesUseCase,
            userRepository = userRepository,
            observeGlobalConfigUseCase = observeGlobalConfigUseCase
        )
    }
}