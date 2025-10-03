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
package com.instructure.pandautils.features.notification.preferences

import android.content.res.Resources
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.managers.NotificationPreferencesFrequency
import com.instructure.canvasapi2.managers.NotificationPreferencesManager
import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.models.NotificationPreference
import com.instructure.canvasapi2.models.NotificationPreferenceResponse
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.EmailNotificationCategoryItemViewModel
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.PushNotificationCategoryItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import org.junit.Test

@ExperimentalCoroutinesApi
class EmailNotificationPreferencesViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val communicationChannelsManager: CommunicationChannelsManager = mockk(relaxed = true)
    private val notificationPreferencesManager: NotificationPreferencesManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private lateinit var notificationPreferenceUtils: NotificationPreferenceUtils

    @Before
    fun setUp() {

        every { apiPrefs.user } returns User(id = 1)

        every { communicationChannelsManager.getCommunicationChannelsAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(CommunicationChannel(id = 1, userId = 1, type = "email")))
        }

        setupStrings()
        notificationPreferenceUtils = NotificationPreferenceUtils(resources)
    }

    @Test
    fun `Notification categories map correctly`() {
        val notificationResponse = NotificationPreferenceResponse(
            notificationPreferences = listOf(
                NotificationPreference(notification = "notification1", category = "due_date", frequency = "immediately"),
                NotificationPreference(notification = "notification2", category = "membership_update", frequency = "daily"),
                NotificationPreference(notification = "notification3", category = "discussion", frequency = "weekly"),
                NotificationPreference(notification = "notification4", category = "announcement_created_by_you", frequency = "never")
            )
        )

        every { notificationPreferencesManager.getNotificationPreferencesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(notificationResponse)
        }

        val viewModel = createViewModel()

        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner) {}

        val data = viewModel.data.value

        assertEquals(3, viewModel.data.value?.items?.size)

        //Course Activities
        val courseActivitiesHeader = data?.items?.get(0)
        assertEquals("Course Activities", courseActivitiesHeader?.data?.title)
        assertEquals(0, courseActivitiesHeader?.data?.position)
        assertEquals(2, courseActivitiesHeader?.itemViewModels?.size)

        //Due Date
        val courseActivitiesItems = courseActivitiesHeader?.itemViewModels as? List<EmailNotificationCategoryItemViewModel>
        assertEquals(2, courseActivitiesItems?.size)
        assertEquals("Due Date", courseActivitiesItems?.get(0)?.data?.title)
        assertEquals("Get notified when an assignment due date changes.", courseActivitiesItems?.get(0)?.data?.description)
        assertEquals(1, courseActivitiesItems?.get(0)?.data?.position)
        assertEquals("Immediately", courseActivitiesItems?.get(0)?.frequency)

        //Announcement Created By You
        assertEquals("Announcement Created By You", courseActivitiesItems?.get(1)?.data?.title)
        assertEquals("Get notified when you create an announcement and when somebody replies to your announcement.", courseActivitiesItems?.get(1)?.data?.description)
        assertEquals(6, courseActivitiesItems?.get(1)?.data?.position)
        assertEquals("Never", courseActivitiesItems?.get(1)?.frequency)

        //Discussions
        val discussionsHeader = data?.items?.get(1)
        assertEquals("Discussions", discussionsHeader?.data?.title)
        assertEquals(1, discussionsHeader?.data?.position)
        assertEquals(1, discussionsHeader?.itemViewModels?.size)

        //Discussion
        val discussionItems = discussionsHeader?.itemViewModels as? List<EmailNotificationCategoryItemViewModel>
        assertEquals(1, discussionItems?.size)
        assertEquals("Discussion", discussionItems?.get(0)?.data?.title)
        assertEquals("Get notified when there’s a new discussion topic in your course.", discussionItems?.get(0)?.data?.description)
        assertEquals(1, discussionItems?.get(0)?.data?.position)
        assertEquals("Weekly", discussionItems?.get(0)?.frequency)

        //Groups
        val groupsHeader = data?.items?.get(2)
        assertEquals("Groups", groupsHeader?.data?.title)
        assertEquals(4, groupsHeader?.data?.position)
        assertEquals(1, groupsHeader?.itemViewModels?.size)

        //Membership update
        val groupsItems = groupsHeader?.itemViewModels as? List<EmailNotificationCategoryItemViewModel>
        assertEquals(1, groupsItems?.size)
        assertEquals("Membership Update", groupsItems?.get(0)?.data?.title)
        assertEquals("Admin only, pending enrollment activated. Get notified when a group enrollment is accepted or rejected.", groupsItems?.get(0)?.data?.description)
        assertEquals(1, groupsItems?.get(0)?.data?.position)
        assertEquals("Daily", groupsItems?.get(0)?.frequency)
    }

    @Test
    fun `Error when cannot fetch notification preferences`() {
        every { notificationPreferencesManager.getNotificationPreferencesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        val viewModel = createViewModel()

        viewModel.state.observe(lifecycleTestOwner.lifecycleOwner) {}

        assertEquals(ViewState.Error("An unexpected error occurred."), viewModel.state.value)
    }

    @Test
    fun `Error when user is null`() {
        every { apiPrefs.user } returns null

        val viewModel = createViewModel()

        viewModel.state.observe(lifecycleTestOwner.lifecycleOwner) {}

        assertEquals(ViewState.Error("An unexpected error occurred."), viewModel.state.value)
    }

    @Test
    fun `Error when cannot fetch notification channels`() {
        every { communicationChannelsManager.getCommunicationChannelsAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        val viewModel = createViewModel()

        viewModel.state.observe(lifecycleTestOwner.lifecycleOwner) {}

        assertEquals(ViewState.Error("An unexpected error occurred."), viewModel.state.value)
    }

    @Test
    fun `Empty state`() {
        val notificationResponse = NotificationPreferenceResponse(emptyList())

        every { notificationPreferencesManager.getNotificationPreferencesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(notificationResponse)
        }

        val viewModel = createViewModel()

        viewModel.state.observe(lifecycleTestOwner.lifecycleOwner) {}

        assertEquals(ViewState.Empty(emptyTitle = R.string.no_notifications_to_show, emptyImage = R.drawable.ic_panda_noalerts), viewModel.state.value)
    }

    @Test
    fun `Show frequency selection dialog when notification category is clicked`() {
        val notificationResponse = NotificationPreferenceResponse(
            notificationPreferences = listOf(
                NotificationPreference(notification = "notification1", category = "due_date", frequency = "immediately")
            )
        )

        every { notificationPreferencesManager.getNotificationPreferencesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(notificationResponse)
        }

        val viewModel = createViewModel()

        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner) {}

        val data = viewModel.data.value

        val itemViewModel = data?.items?.get(0)?.itemViewModels?.get(0) as? EmailNotificationCategoryItemViewModel
        itemViewModel?.onClick()

        val event = viewModel.events.value?.getContentIfNotHandled()
        val expectedEvent = NotificationPreferencesAction.ShowFrequencySelectionDialog("notification1", NotificationPreferencesFrequency.IMMEDIATELY)
        assertEquals(expectedEvent, event)
    }

    @Test
    fun `Change notification category frequency`() {
        val notificationResponse = NotificationPreferenceResponse(
            notificationPreferences = listOf(
                NotificationPreference(notification = "notification1", category = "due_date", frequency = "immediately")
            )
        )

        val updatedNotificationResponse = NotificationPreferenceResponse(
            notificationPreferences = listOf(
                NotificationPreference(notification = "notification1", category = "due_date", frequency = "daily")
            )
        )

        every { notificationPreferencesManager.getNotificationPreferencesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(notificationResponse)
        }

        every { notificationPreferencesManager.updatePreferenceCategoryAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(updatedNotificationResponse)
        }

        val viewModel = createViewModel()

        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner) {}

        val data = viewModel.data.value

        val itemViewModel = data?.items?.get(0)?.itemViewModels?.get(0) as? EmailNotificationCategoryItemViewModel

        assertEquals("Immediately", itemViewModel?.frequency)
        viewModel.updateFrequency("notification1", NotificationPreferencesFrequency.DAILY)
        verify { notificationPreferencesManager.updatePreferenceCategoryAsync("notification1", any(), "daily") }

        assertEquals("Daily", itemViewModel?.frequency)
    }

    @Test
    fun `Revert previous state when changing frequency has error`() {
        val notificationResponse = NotificationPreferenceResponse(
            notificationPreferences = listOf(
                NotificationPreference(notification = "notification1", category = "due_date", frequency = "weekly")
            )
        )

        every { notificationPreferencesManager.getNotificationPreferencesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(notificationResponse)
        }

        every { notificationPreferencesManager.updatePreferenceCategoryAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        val viewModel = createViewModel()

        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner) {}
        viewModel.events.observe(lifecycleTestOwner.lifecycleOwner) {}

        val data = viewModel.data.value

        val itemViewModel = data?.items?.get(0)?.itemViewModels?.get(0) as? EmailNotificationCategoryItemViewModel

        assertEquals("Weekly", itemViewModel?.frequency)
        viewModel.updateFrequency("notification1", NotificationPreferencesFrequency.DAILY)

        assertEquals("Weekly", itemViewModel?.frequency)
        val event = viewModel.events.value?.getContentIfNotHandled()
        assert(event is NotificationPreferencesAction.ShowSnackbar)
        assertEquals("An unexpected error occurred.", (event as NotificationPreferencesAction.ShowSnackbar).snackbar)
    }

    @Test
    fun `Refresh`() {
        var notificationResponse = NotificationPreferenceResponse(emptyList())

        every { notificationPreferencesManager.getNotificationPreferencesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(notificationResponse)
        }

        val viewModel = createViewModel()

        viewModel.state.observe(lifecycleTestOwner.lifecycleOwner) {}

        assertEquals(ViewState.Empty(emptyTitle = R.string.no_notifications_to_show, emptyImage = R.drawable.ic_panda_noalerts), viewModel.state.value)

        notificationResponse = NotificationPreferenceResponse(
            notificationPreferences = listOf(
                NotificationPreference(notification = "notification1", category = "due_date", frequency = "never")
            )
        )

        every { notificationPreferencesManager.getNotificationPreferencesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(notificationResponse)
        }

        viewModel.refresh()
        assertEquals(ViewState.Success, viewModel.state.value)
        assertEquals(1, viewModel.data.value?.items?.size)
    }

    private fun createViewModel(): EmailNotificationPreferencesViewModel {
        return EmailNotificationPreferencesViewModel(communicationChannelsManager, notificationPreferencesManager, apiPrefs, notificationPreferenceUtils, resources)
    }

    private fun setupStrings() {
        every { resources.getString(R.string.notification_pref_due_date) } returns "Due Date"
        every { resources.getString(R.string.notification_pref_discussion) } returns "Discussion"
        every { resources.getString(R.string.notification_pref_announcement_created_by_you) } returns "Announcement Created By You"
        every { resources.getString(R.string.notification_pref_membership_update) } returns "Membership Update"
        every { resources.getString(R.string.notification_desc_due_date) } returns "Get notified when an assignment due date changes."
        every { resources.getString(R.string.notification_desc_announcement_created_by_you) } returns "Get notified when you create an announcement and when somebody replies to your announcement."
        every { resources.getString(R.string.notification_desc_discussion) } returns "Get notified when there’s a new discussion topic in your course."
        every { resources.getString(R.string.notification_desc_membership_update) } returns "Admin only, pending enrollment activated. Get notified when a group enrollment is accepted or rejected."
        every { resources.getString(R.string.notification_cat_course_activities) } returns "Course Activities"
        every { resources.getString(R.string.notification_cat_discussions) } returns "Discussions"
        every { resources.getString(R.string.notification_cat_groups) } returns "Groups"
        every { resources.getString(R.string.errorOccurred) } returns "An unexpected error occurred."
        every { resources.getString(R.string.emailNotificationsImmediately) } returns "Immediately"
        every { resources.getString(R.string.emailNotificationsNever) } returns "Never"
        every { resources.getString(R.string.emailNotificationsDaily) } returns "Daily"
        every { resources.getString(R.string.emailNotificationsWeekly) } returns "Weekly"
    }
}
