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

package com.instructure.pandautils.features.dashboard.notifications

import android.content.Context
import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.AccountNotificationManager
import com.instructure.canvasapi2.managers.ConferenceManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.AnnouncementItemViewModel
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.ConferenceItemViewModel
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.InvitationItemViewModel
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.UploadItemViewModel
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.AggregateProgressViewData
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.models.ConferenceDashboardBlacklist
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.entities.DashboardFileUploadEntity
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.StudioMediaProgressDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.util.UUID

@ExperimentalCoroutinesApi
class DashboardNotificationsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()

    private val resources: Resources = mockk(relaxed = true)
    private val courseManager: CourseManager = mockk(relaxed = true)
    private val groupManager: GroupManager = mockk(relaxed = true)
    private val enrollmentManager: EnrollmentManager = mockk(relaxed = true)
    private val conferenceManager: ConferenceManager = mockk(relaxed = true)
    private val accountNotificationManager: AccountNotificationManager = mockk(relaxed = true)
    private val oauthManager: OAuthManager = mockk(relaxed = true)
    private val conferenceDashboardBlacklist: ConferenceDashboardBlacklist = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val fileUploadInputDao: FileUploadInputDao = mockk(relaxed = true)
    private val fileUploadUtilsHelper: FileUploadUtilsHelper = mockk(relaxed = true)
    private val dashboardFileUploadDao: DashboardFileUploadDao = mockk(relaxed = true)
    private val aggregateProgressObserver: AggregateProgressObserver = mockk(relaxed = true)
    private val courseSyncProgressDao: CourseSyncProgressDao = mockk(relaxed = true)
    private val fileSyncProgressDao: FileSyncProgressDao = mockk(relaxed = true)
    private val studioMediaProgressDao: StudioMediaProgressDao = mockk(relaxed = true)

    private lateinit var uploadsLiveData: MutableLiveData<List<DashboardFileUploadEntity>>
    private lateinit var progressLiveData: MutableLiveData<AggregateProgressViewData>

    private lateinit var viewModel: DashboardNotificationsViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkStatic("kotlinx.coroutines.AwaitKt")

        ContextKeeper.appContext = Mockito.mock(Context::class.java)

        setupResources()

        every { conferenceDashboardBlacklist.conferenceDashboardBlacklist } returns emptySet()

        every { enrollmentManager.getSelfEnrollmentsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { conferenceManager.getLiveConferencesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { accountNotificationManager.getAllAccountNotificationsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { apiPrefs.user } returns User(id = 1)

        uploadsLiveData = MutableLiveData(emptyList())
        every { dashboardFileUploadDao.getAllForUser(1) } returns uploadsLiveData

        progressLiveData = MutableLiveData()
        every { aggregateProgressObserver.progressData } returns progressLiveData

        viewModel = DashboardNotificationsViewModel(
            resources,
            courseManager,
            groupManager,
            enrollmentManager,
            conferenceManager,
            accountNotificationManager,
            oauthManager,
            conferenceDashboardBlacklist,
            apiPrefs,
            workManager,
            dashboardFileUploadDao,
            fileUploadInputDao,
            fileUploadUtilsHelper,
            aggregateProgressObserver,
            courseSyncProgressDao,
            fileSyncProgressDao,
            studioMediaProgressDao
        )

        viewModel.data.observe(lifecycleOwner, {})
        viewModel.events.observe(lifecycleOwner, {})
    }

    @After
    fun tearDown() {}

    private fun setupResources() {
        every { resources.getString(R.string.courseInviteTitle) } returns "You have been invited"
        every { resources.getString(R.string.errorOccurred) } returns "An unexpected error occurred."
    }

    @Test
    fun `Announcements map correctly`() {

        val accountNotifications = listOf(
            AccountNotification(1, "AC1", "AC1", icon = AccountNotification.ACCOUNT_NOTIFICATION_ERROR),
            AccountNotification(2, "AC2", "AC2", icon = AccountNotification.ACCOUNT_NOTIFICATION_CALENDAR),
            AccountNotification(3, "AC3", "AC3", icon = AccountNotification.ACCOUNT_NOTIFICATION_QUESTION),
            AccountNotification(4, "AC4", "AC4", icon = AccountNotification.ACCOUNT_NOTIFICATION_WARNING)
        )

        val expectedItems = listOf(
            AnnouncementViewData(
                1,
                "AC1",
                "AC1",
                color = R.color.backgroundDanger,
                icon = R.drawable.ic_warning
            ),
            AnnouncementViewData(
                2,
                "AC2",
                "AC2",
                color = R.color.backgroundInfo,
                icon = R.drawable.ic_calendar
            ),
            AnnouncementViewData(
                3,
                "AC3",
                "AC3",
                color = R.color.backgroundInfo,
                icon = R.drawable.ic_question_mark
            ),
            AnnouncementViewData(
                4,
                "AC4",
                "AC4",
                color = R.color.backgroundWarning,
                icon = R.drawable.ic_warning
            ),
        )

        every { accountNotificationManager.getAllAccountNotificationsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(accountNotifications)
        }

        viewModel.loadData(true)

        val items = viewModel.data.value?.items

        assert(items != null)
        items?.forEachIndexed { index, itemViewModel ->
            assert(itemViewModel is AnnouncementItemViewModel)
            assertEquals(expectedItems[index], (itemViewModel as AnnouncementItemViewModel).data)
        }
    }

    @Test
    fun `Open announcement`() {
        val accountNotifications = listOf(
            AccountNotification(1, "AC1 subject", "AC1 message", icon = AccountNotification.ACCOUNT_NOTIFICATION_ERROR)
        )

        val expectedData = DashboardNotificationsActions.OpenAnnouncement("AC1 subject", "AC1 message")

        every { accountNotificationManager.getAllAccountNotificationsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(accountNotifications)
        }

        viewModel.loadData(true)

        val itemViewModel = viewModel.data.value?.items?.get(0)
        assert(itemViewModel is AnnouncementItemViewModel)
        val announcementItemViewModel = itemViewModel as AnnouncementItemViewModel

        announcementItemViewModel.open()

        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is DashboardNotificationsActions.OpenAnnouncement)
        val openAnnouncementAction = event as DashboardNotificationsActions.OpenAnnouncement

        assertEquals(expectedData, openAnnouncementAction)
    }

    @Test
    fun `Invitations map correctly`() {
        val courses = listOf(
            Course(id = 1, name = "Invited course"),
            Course(id = 2, name = "Invited course with section", sections = listOf(Section(id = 1, name = "Section")))
        )
        val enrolments = listOf(
            Enrollment(id = 1, courseId = 1, enrollmentState = EnrollmentAPI.STATE_INVITED),
            Enrollment(id = 2, courseId = 2, enrollmentState = EnrollmentAPI.STATE_INVITED, courseSectionId = 1)
        )

        val expectedData = listOf(
            InvitationViewData(
                title = "You have been invited",
                description = "Invited course",
                enrollmentId = 1,
                courseId = 1
            ),
            InvitationViewData(
                title = "You have been invited",
                description = "Invited course with section, Section",
                enrollmentId = 2,
                courseId = 2
            )
        )

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { enrollmentManager.getSelfEnrollmentsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(enrolments)
        }

        viewModel.loadData(true)

        viewModel.data.value?.items?.forEachIndexed { index, itemViewModel ->
            assert(itemViewModel is InvitationItemViewModel)
            assertEquals(expectedData[index], (itemViewModel as InvitationItemViewModel).data)
        }
    }

    @Test
    fun `Accept invitation`() {
        val course = Course(id = 1, name = "Invited course")

        val enrolment = Enrollment(id = 1, courseId = 1, enrollmentState = EnrollmentAPI.STATE_INVITED)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        every { enrollmentManager.getSelfEnrollmentsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(enrolment))
        }

        every { enrollmentManager.handleInviteAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Unit)
        }

        viewModel.loadData(true)

        val itemViewModel = viewModel.data.value?.items?.get(0)
        assert(itemViewModel is InvitationItemViewModel)

        (itemViewModel as InvitationItemViewModel).handleInvitation(true)

        val updatedItemViewModel = viewModel.data.value?.items?.get(0)
        assert(updatedItemViewModel is InvitationItemViewModel)
        assert((updatedItemViewModel as InvitationItemViewModel).accepted == true)
    }

    @Test
    fun `Decline invitation`() {
        val course = Course(id = 1, name = "Invited course")

        val enrolment = Enrollment(id = 1, courseId = 1, enrollmentState = EnrollmentAPI.STATE_INVITED)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        every { enrollmentManager.getSelfEnrollmentsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(enrolment))
        }

        every { enrollmentManager.handleInviteAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Unit)
        }

        viewModel.loadData(true)

        val itemViewModel = viewModel.data.value?.items?.get(0)
        assert(itemViewModel is InvitationItemViewModel)

        (itemViewModel as InvitationItemViewModel).handleInvitation(false)

        val updatedItemViewModel = viewModel.data.value?.items?.get(0)
        assert(updatedItemViewModel is InvitationItemViewModel)
        assert((updatedItemViewModel as InvitationItemViewModel).accepted == false)
    }

    @Test
    fun `Handle invitation error`() {
        val course = Course(id = 1, name = "Invited course")

        val enrolment = Enrollment(id = 1, courseId = 1, enrollmentState = EnrollmentAPI.STATE_INVITED)

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        every { enrollmentManager.getSelfEnrollmentsAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(enrolment))
        }

        every { enrollmentManager.handleInviteAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        viewModel.loadData(true)

        val itemViewModel = viewModel.data.value?.items?.get(0)
        assert(itemViewModel is InvitationItemViewModel)

        (itemViewModel as InvitationItemViewModel).handleInvitation(true)

        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is DashboardNotificationsActions.ShowToast)
        val showToastAction = event as DashboardNotificationsActions.ShowToast

        assertEquals("An unexpected error occurred.", showToastAction.toast)
    }

    @Test
    fun `Dismissed conference is not visible`() {
        val conference = Conference(id = 1, title = "Conference")

        every { conferenceManager.getLiveConferencesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(conference))
        }

        every { conferenceDashboardBlacklist.conferenceDashboardBlacklist } returns setOf("1")

        viewModel.loadData(true)

        assert(viewModel.data.value?.items?.isEmpty() == true)
    }

    @Test
    fun `Open conference`() {
        val conference = Conference(id = 1, title = "Conference", joinUrl = "https://notAuthenticatedSession.com")

        every { conferenceManager.getLiveConferencesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(conference))
        }

        every { oauthManager.getAuthenticatedSessionAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(AuthenticatedSession("https://authenticatedSession.com"))
        }

        viewModel.loadData(true)

        val itemViewModel = viewModel.data.value?.items?.get(0)

        assert(itemViewModel is ConferenceItemViewModel)
        val conferenceItemViewModel = (itemViewModel as ConferenceItemViewModel)
        conferenceItemViewModel.handleJoin()
        assert(conferenceItemViewModel.isJoining)

        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is DashboardNotificationsActions.LaunchConference)
        val launchConferenceAction = event as DashboardNotificationsActions.LaunchConference

        assertEquals("https://authenticatedSession.com", launchConferenceAction.url)
    }

    @Test
    fun `Conferences map correctly`() {

        val courses = listOf(Course(id = 1, name = "Invited course"))

        val conferences = listOf(
            Conference(
                id = 1,
                title = "Conference",
                joinUrl = "https://notAuthenticatedSession.com",
                contextId = 1,
                contextType = "course"
            ),
        )

        val expectedData = listOf(
            ConferenceViewData(subtitle = "Invited course", conference = conferences[0]),
        )

        every { conferenceManager.getLiveConferencesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(conferences)
        }

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        viewModel.loadData(true)

        viewModel.data.value?.items?.forEachIndexed { index, itemViewModel ->
            assert(itemViewModel is ConferenceItemViewModel)
            assertEquals(expectedData[index], (itemViewModel as ConferenceItemViewModel).data)
        }
    }

    @Test
    fun `Upload map correctly`() {
        val workerId = UUID.randomUUID()
        val title = "Title"
        val subTitle = "SubTitle"

        val workerId2 = UUID.randomUUID()
        val title2 = "Title"
        val subTitle2 = "SubTitle"

        val workerId3 = UUID.randomUUID()
        val title3 = "Title"
        val subTitle3 = "SubTitle"

        uploadsLiveData.value = listOf(
            DashboardFileUploadEntity(workerId.toString(), 1, title, subTitle, null, null, null, null),
            DashboardFileUploadEntity(workerId2.toString(), 1, title2, subTitle2, null, null, null, null),
            DashboardFileUploadEntity(workerId3.toString(), 1, title3, subTitle3, null, null, null, null)
        )

        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(
            WorkInfo(
                workerId,
                WorkInfo.State.RUNNING,
                emptySet(),
                Data.EMPTY,
                Data.Builder()
                    .putString(FileUploadWorker.PROGRESS_DATA_TITLE, title)
                    .putString(FileUploadWorker.PROGRESS_DATA_ASSIGNMENT_NAME, subTitle)
                    .putLong(FileUploadWorker.PROGRESS_DATA_FULL_SIZE, 1L)
                    .putLong(FileUploadWorker.PROGRESS_DATA_UPLOADED_SIZE, 0L)
                    .build(),
                1,
                1
            )
        )

        every { workManager.getWorkInfoByIdFlow(workerId2) } returns flowOf(
            WorkInfo(
                workerId2,
                WorkInfo.State.SUCCEEDED,
                emptySet(),
                Data.EMPTY,
                Data.Builder()
                    .putString(FileUploadWorker.PROGRESS_DATA_TITLE, title2)
                    .putString(FileUploadWorker.PROGRESS_DATA_ASSIGNMENT_NAME, subTitle2)
                    .putLong(FileUploadWorker.PROGRESS_DATA_FULL_SIZE, 1L)
                    .putLong(FileUploadWorker.PROGRESS_DATA_UPLOADED_SIZE, 0L)
                    .build(),
                1,
                1
            )
        )

        every { workManager.getWorkInfoByIdFlow(workerId3) } returns flowOf(
            WorkInfo(
                workerId3,
                WorkInfo.State.FAILED,
                emptySet(),
                Data.EMPTY,
                Data.Builder()
                    .putString(FileUploadWorker.PROGRESS_DATA_TITLE, title3)
                    .putString(FileUploadWorker.PROGRESS_DATA_ASSIGNMENT_NAME, subTitle3)
                    .putLong(FileUploadWorker.PROGRESS_DATA_FULL_SIZE, 1L)
                    .putLong(FileUploadWorker.PROGRESS_DATA_UPLOADED_SIZE, 0L)
                    .build(),
                1,
                1
            )
        )

        val expectedData = listOf(
            UploadViewData(title, subTitle, R.drawable.ic_upload, R.color.backgroundInfo, true),
            UploadViewData(title2, subTitle2, R.drawable.ic_check_white_24dp, R.color.backgroundSuccess, false),
            UploadViewData(title3, subTitle3, R.drawable.ic_exclamation_mark, R.color.backgroundDanger, false),
        )

        viewModel.loadData()

        assertEquals(3, viewModel.data.value?.uploadItems?.size)
        assertEquals(0, viewModel.data.value?.uploadItems?.get(0)?.progress)
        assertEquals(expectedData[0], viewModel.data.value?.uploadItems?.get(0)?.data)
        assertEquals(expectedData[1], viewModel.data.value?.uploadItems?.get(1)?.data)
        assertEquals(expectedData[2], viewModel.data.value?.uploadItems?.get(2)?.data)
    }

    @Test
    fun `Upload notification shows up and updates when it's finished`() {
        val workerId = UUID.randomUUID()
        val title = "Title"
        val title2 = "Title"
        val subTitle = "SubTitle"

        val expectedRunning = UploadViewData(title, subTitle, R.drawable.ic_upload, R.color.backgroundInfo, true)
        val expectedFinished =
            UploadViewData(title2, subTitle, R.drawable.ic_check_white_24dp, R.color.backgroundSuccess, false)

        uploadsLiveData.value = listOf(
            DashboardFileUploadEntity(workerId.toString(), 1, title, subTitle, null, null, null, null)
        )

        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(
            WorkInfo(
                workerId,
                WorkInfo.State.RUNNING,
                emptySet(),
                Data.EMPTY,
                Data.EMPTY,
                1,
                1
            )
        )

        viewModel.loadData()

        assertEquals(1, viewModel.data.value?.uploadItems?.size)
        assertEquals(expectedRunning, viewModel.data.value?.uploadItems?.first()?.data)

        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(
            WorkInfo(
                workerId,
                WorkInfo.State.SUCCEEDED,
                emptySet(),
                Data.EMPTY,
                Data.EMPTY,
                1,
                1
            )
        )

        uploadsLiveData.value = listOf(
            DashboardFileUploadEntity(workerId.toString(), 1, title2, subTitle, null, null, null, null)
        )

        assertEquals(1, viewModel.data.value?.uploadItems?.size)
        assertEquals(expectedFinished, viewModel.data.value?.uploadItems?.first()?.data)
    }

    @Test
    fun `Open progress dialog`() {
        val workerId = UUID.randomUUID()

        uploadsLiveData.value = listOf(
            DashboardFileUploadEntity(workerId.toString(), 1, "", "", null, null, null, null)
        )

        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(
            WorkInfo(
                workerId,
                WorkInfo.State.RUNNING,
                emptySet(),
                Data.EMPTY,
                Data.EMPTY,
                1,
                1
            )
        )

        viewModel.loadData()

        val itemViewModel = viewModel.data.value?.uploadItems?.first()
        assert(itemViewModel is UploadItemViewModel)

        itemViewModel as UploadItemViewModel
        itemViewModel.open(workerId)

        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is DashboardNotificationsActions.OpenProgressDialog)

        event as DashboardNotificationsActions.OpenProgressDialog
        assertEquals(workerId, event.uuid)
    }

    @Test
    fun `Success notification click opens submission details if it has the data`() {
        val workerId = UUID.randomUUID()

        uploadsLiveData.value = listOf(
            DashboardFileUploadEntity(workerId.toString(), 1, "", "", 1, 2, 3, null)
        )

        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(
            WorkInfo(
                workerId,
                WorkInfo.State.SUCCEEDED,
                emptySet(),
                Data.EMPTY,
                Data.EMPTY,
                1,
                1
            )
        )

        every { courseManager.getCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(id = 1))
        }

        viewModel.loadData()

        val itemViewModel = viewModel.data.value?.uploadItems?.first()

        itemViewModel as UploadItemViewModel
        itemViewModel.open(workerId)

        val event = viewModel.events.value?.getContentIfNotHandled()

        assert(event is DashboardNotificationsActions.NavigateToSubmissionDetails)

        event as DashboardNotificationsActions.NavigateToSubmissionDetails
        assertEquals(1, event.canvasContext.id)
        assertEquals(2, event.assignmentId)
        assertEquals(3, event.attemptId)
    }

    @Test
    fun `Success notification click opens my files if it has the data`() {
        val workerId = UUID.randomUUID()

        uploadsLiveData.value = listOf(
            DashboardFileUploadEntity(workerId.toString(), 1, "", "", null, null, null, 0)
        )

        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(
            WorkInfo(
                workerId,
                WorkInfo.State.SUCCEEDED,
                emptySet(),
                Data.EMPTY,
                Data.EMPTY,
                1,
                1
            )
        )

        viewModel.loadData()

        val itemViewModel = viewModel.data.value?.uploadItems?.first()

        itemViewModel as UploadItemViewModel
        itemViewModel.open(workerId)

        val event = viewModel.events.value?.getContentIfNotHandled()

        assert(event is DashboardNotificationsActions.NavigateToMyFiles)

        event as DashboardNotificationsActions.NavigateToMyFiles
        assertEquals(0, event.folderId)
    }

    @Test
    fun `Add progress notification`() {
        viewModel.loadData()

        assertNull(viewModel.data.value?.syncProgressItems)

        progressLiveData.postValue(
            AggregateProgressViewData(
                title = "Course 1",
                progressState = ProgressState.IN_PROGRESS,
                progress = 0,
                totalSize = "0 bytes"
            )
        )

        assertNotNull(viewModel.data.value?.syncProgressItems)
    }

    @Test
    fun `Remove progress notification on complete`() {
        viewModel.loadData()

        progressLiveData.postValue(
            AggregateProgressViewData(
                title = "Course 1",
                progressState = ProgressState.COMPLETED,
                progress = 0,
                totalSize = "0 bytes"
            )
        )

        assertNull(viewModel.data.value?.syncProgressItems)
    }

    @Test
    fun `Update progress notification`() {
        var progressData = AggregateProgressViewData(
            title = "Course 1",
            progressState = ProgressState.IN_PROGRESS,
            progress = 0,
            totalSize = "0 bytes"
        )

        viewModel.loadData()

        progressLiveData.postValue(
            progressData
        )

        assertNotNull(viewModel.data.value?.syncProgressItems)

        progressData = progressData.copy(progress = 50)

        progressLiveData.postValue(
            progressData
        )

        verify {
            viewModel.data.value?.syncProgressItems?.update(progressData)
        }
    }

    @Test
    fun `Open sync progress`() {
        val progressData = AggregateProgressViewData(
            title = "Course 1",
            progressState = ProgressState.IN_PROGRESS,
            progress = 0,
            totalSize = "0 bytes"
        )

        viewModel.loadData()

        progressLiveData.postValue(
            progressData
        )

        viewModel.data.value?.syncProgressItems?.onClick?.invoke()

        assertEquals(DashboardNotificationsActions.OpenSyncProgress, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Dismiss progress notification`() {
        val progressData = AggregateProgressViewData(
            title = "Course 1",
            progressState = ProgressState.ERROR,
            progress = 0,
            totalSize = "0 bytes"
        )

        viewModel.loadData()

        progressLiveData.postValue(
            progressData
        )

        viewModel.data.value?.syncProgressItems?.onDismiss?.invoke()

        coVerify {
            courseSyncProgressDao.deleteAll()
            fileSyncProgressDao.deleteAll()
            studioMediaProgressDao.deleteAll()
        }
    }

    @Test
    fun `Clear progress notification`() {
        val progressData = AggregateProgressViewData(
            title = "Course 1",
            progressState = ProgressState.ERROR,
            progress = 0,
            totalSize = "0 bytes"
        )

        viewModel.loadData()

        progressLiveData.postValue(
            progressData
        )

        assertNotNull(viewModel.data.value?.syncProgressItems)

        progressLiveData.postValue(null)

        assertNull(viewModel.data.value?.syncProgressItems)
    }
}
