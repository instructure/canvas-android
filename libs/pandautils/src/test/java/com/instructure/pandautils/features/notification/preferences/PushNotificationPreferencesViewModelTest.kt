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

package com.instructure.pandautils.features.notification.preferences

import android.content.res.Resources
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.managers.NotificationPreferencesManager
import com.instructure.canvasapi2.models.CommunicationChannel
import com.instructure.canvasapi2.models.NotificationPreference
import com.instructure.canvasapi2.models.NotificationPreferenceResponse
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.notification.preferences.itemviewmodels.PushNotificationCategoryItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import org.junit.Test

@ExperimentalCoroutinesApi
class PushNotificationPreferencesViewModelTest {

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
            coEvery { await() } returns DataResult.Success(listOf(CommunicationChannel(id = 1, userId = 1, type = "push")))
        }

        setupStrings()
        notificationPreferenceUtils = NotificationPreferenceUtils(resources)
    }

    @Test
    fun `Notification categories map correctly`() {
        val notificationResponse = NotificationPreferenceResponse(
                notificationPreferences = listOf(
                        NotificationPreference(notification = "notification1", category = "due_date", frequency = "immediately"),
                        NotificationPreference(notification = "notification2", category = "conversation_message", frequency = "immediately"),
                        NotificationPreference(notification = "notification3", category = "discussion", frequency = "never"),
                        NotificationPreference(notification = "notification4", category = "announcement", frequency = "never")
                )
        )

        every { notificationPreferencesManager.getNotificationPreferencesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(notificationResponse)
        }

        val viewModel = createViewModel()

        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner) {}

        val data = viewModel.data.value

        assertEquals(3, data?.items?.size)

        //Course Activities
        val courseActivitiesHeader = data?.items?.get(0)
        assertEquals("Course Activities", courseActivitiesHeader?.data?.title)
        assertEquals(0, courseActivitiesHeader?.data?.position)
        assertEquals(2, courseActivitiesHeader?.itemViewModels?.size)

        //Due Date
        val courseActivitiesItems = courseActivitiesHeader?.itemViewModels as? List<PushNotificationCategoryItemViewModel>
        assertEquals(2, courseActivitiesItems?.size)
        assertEquals("Due Date", courseActivitiesItems?.get(0)?.data?.title)
        assertEquals("Get notified when an assignment due date changes.", courseActivitiesItems?.get(0)?.data?.description)
        assertEquals(1, courseActivitiesItems?.get(0)?.data?.position)
        assertEquals(true, courseActivitiesItems?.get(0)?.isChecked)

        //Announcement
        assertEquals("Announcement", courseActivitiesItems?.get(1)?.data?.title)
        assertEquals("Get notified when there is a new announcement in your course.", courseActivitiesItems?.get(1)?.data?.description)
        assertEquals(5, courseActivitiesItems?.get(1)?.data?.position)
        assertEquals(false, courseActivitiesItems?.get(1)?.isChecked)

        //Discussions
        val discussionsHeader = data?.items?.get(1)
        assertEquals("Discussions", discussionsHeader?.data?.title)
        assertEquals(1, discussionsHeader?.data?.position)
        assertEquals(1, discussionsHeader?.itemViewModels?.size)

        //Discussion
        val discussionItems = discussionsHeader?.itemViewModels as? List<PushNotificationCategoryItemViewModel>
        assertEquals(1, discussionItems?.size)
        assertEquals("Discussion", discussionItems?.get(0)?.data?.title)
        assertEquals("Get notified when there’s a new discussion topic in your course.", discussionItems?.get(0)?.data?.description)
        assertEquals(1, discussionItems?.get(0)?.data?.position)
        assertEquals(false, discussionItems?.get(0)?.isChecked)

        //Conversations
        val conversationsHeader = data?.items?.get(2)
        assertEquals("Conversations", conversationsHeader?.data?.title)
        assertEquals(2, conversationsHeader?.data?.position)
        assertEquals(1, conversationsHeader?.itemViewModels?.size)

        //Membership update
        val conversationsItems = conversationsHeader?.itemViewModels as? List<PushNotificationCategoryItemViewModel>
        assertEquals(1, conversationsItems?.size)
        assertEquals("Conversation Message", conversationsItems?.get(0)?.data?.title)
        assertEquals("Get notified when you have a new inbox message.", conversationsItems?.get(0)?.data?.description)
        assertEquals(2, conversationsItems?.get(0)?.data?.position)
        assertEquals(true, conversationsItems?.get(0)?.isChecked)
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
    fun `Turn off notification category`() {
        val notificationResponse = NotificationPreferenceResponse(
                notificationPreferences = listOf(
                        NotificationPreference(notification = "notification1", category = "due_date", frequency = "immediately")
                )
        )

        val updatedNotificationResponse = NotificationPreferenceResponse(
                notificationPreferences = listOf(
                        NotificationPreference(notification = "notification1", category = "due_date", frequency = "never")
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

        val itemViewModel = data?.items?.get(0)?.itemViewModels?.get(0) as? PushNotificationCategoryItemViewModel

        assertEquals(true, itemViewModel?.isChecked)
        itemViewModel?.onCheckedChanged(false)

        assertEquals(false, itemViewModel?.isChecked)
    }

    @Test
    fun `Turn on notification category`() {
        val notificationResponse = NotificationPreferenceResponse(
                notificationPreferences = listOf(
                        NotificationPreference(notification = "notification1", category = "due_date", frequency = "never")
                )
        )

        val updatedNotificationResponse = NotificationPreferenceResponse(
                notificationPreferences = listOf(
                        NotificationPreference(notification = "notification1", category = "due_date", frequency = "immediately")
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

        val itemViewModel = data?.items?.get(0)?.itemViewModels?.get(0) as? PushNotificationCategoryItemViewModel

        assertEquals(false, itemViewModel?.isChecked)
        itemViewModel?.onCheckedChanged(true)

        assertEquals(true, itemViewModel?.isChecked)
    }

    @Test
    fun `On error keep previous state and show snackbar`() {
        val notificationResponse = NotificationPreferenceResponse(
                notificationPreferences = listOf(
                        NotificationPreference(notification = "notification1", category = "due_date", frequency = "never")
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

        val itemViewModel = data?.items?.get(0)?.itemViewModels?.get(0) as? PushNotificationCategoryItemViewModel

        assertEquals(false, itemViewModel?.isChecked)
        itemViewModel?.onCheckedChanged(true)

        assertEquals(false, itemViewModel?.isChecked)
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

    @Test
    fun `Notification categories filtered correctly`() {
        val notificationResponse = NotificationPreferenceResponse(
            notificationPreferences = listOf(
                NotificationPreference(notification = "notification1", category = "announcement", frequency = "immediately"),
                NotificationPreference(notification = "notification2", category = "due_date", frequency = "immediately"),
                NotificationPreference(notification = "notification3", category = "course_content", frequency = "immediately"),
                NotificationPreference(notification = "notification4", category = "grading_policies", frequency = "immediately"),
                NotificationPreference(notification = "notification5", category = "grading", frequency = "immediately"),
                NotificationPreference(notification = "notification6", category = "calendar", frequency = "immediately"),
                NotificationPreference(notification = "notification7", category = "invitation", frequency = "immediately"),
                NotificationPreference(notification = "notification8", category = "registration", frequency = "immediately"),
                NotificationPreference(notification = "notification9", category = "discussion", frequency = "immediately"),
                NotificationPreference(notification = "notification10", category = "late_grading", frequency = "immediately"),
                NotificationPreference(notification = "notification11", category = "submission_comment", frequency = "immediately"),
                NotificationPreference(notification = "notification12", category = "summaries", frequency = "immediately"),
                NotificationPreference(notification = "notification13", category = "other", frequency = "immediately"),
                NotificationPreference(notification = "notification14", category = "reminder", frequency = "immediately"),
                NotificationPreference(notification = "notification15", category = "membership_update", frequency = "immediately"),
                NotificationPreference(notification = "notification16", category = "discussion_entry", frequency = "immediately"),
                NotificationPreference(notification = "notification17", category = "migration", frequency = "immediately"),
                NotificationPreference(notification = "notification18", category = "all_submissions", frequency = "immediately"),
                NotificationPreference(notification = "notification19", category = "conversation_message", frequency = "immediately"),
                NotificationPreference(notification = "notification20", category = "added_to_conversation", frequency = "immediately"),
                NotificationPreference(notification = "notification21", category = "alert", frequency = "immediately"),
                NotificationPreference(notification = "notification22", category = "student_appointment_signups", frequency = "immediately"),
                NotificationPreference(notification = "notification23", category = "appointment_cancelations", frequency = "immediately"),
                NotificationPreference(notification = "notification24", category = "appointment_availability", frequency = "immediately"),
                NotificationPreference(notification = "notification25", category = "appointment_signups", frequency = "immediately"),
                NotificationPreference(notification = "notification26", category = "files", frequency = "immediately"),
                NotificationPreference(notification = "notification27", category = "announcement_created_by_you", frequency = "immediately"),
                NotificationPreference(notification = "notification28", category = "conversation_created", frequency = "immediately"),
                NotificationPreference(notification = "notification29", category = "recording_ready", frequency = "immediately"),
                NotificationPreference(notification = "notification30", category = "blueprint", frequency = "immediately"),
                NotificationPreference(notification = "notification31", category = "content_link_error", frequency = "immediately"),
                NotificationPreference(notification = "notification32", category = "account_notification", frequency = "immediately"),
                NotificationPreference(notification = "notification33", category = "discussion_mention", frequency = "immediately"),
                NotificationPreference(notification = "notification34", category = "reported_reply", frequency = "immediately")
            )
        )

        every { notificationPreferencesManager.getNotificationPreferencesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(notificationResponse)
        }

        val viewModel = createViewModel()

        viewModel.data.observe(lifecycleTestOwner.lifecycleOwner) {}

        val expected = listOf(
            "notification2",
            "notification3",
            "notification1",
            "notification5",
            "notification7",
            "notification11",
            "notification9",
            "notification16",
            "notification19",
            "notification22",
            "notification23",
            "notification24",
            "notification6"
        )

        val actual = viewModel.data.value?.items?.flatMap { header ->
            header.itemViewModels.map {
                it.data.notification
            }
        }

        assertEquals(expected, actual)
    }

    private fun createViewModel(): PushNotificationPreferencesViewModel {
        return PushNotificationPreferencesViewModel(communicationChannelsManager, notificationPreferencesManager, apiPrefs, notificationPreferenceUtils, resources)
    }

    private fun setupStrings() {
        every { resources.getString(R.string.notification_pref_due_date) } returns "Due Date"
        every { resources.getString(R.string.notification_pref_discussion) } returns "Discussion"
        every { resources.getString(R.string.notification_pref_announcement) } returns "Announcement"
        every { resources.getString(R.string.notification_pref_conversation_message) } returns "Conversation Message"
        every { resources.getString(R.string.notification_desc_due_date) } returns "Get notified when an assignment due date changes."
        every { resources.getString(R.string.notification_desc_announcement) } returns "Get notified when there is a new announcement in your course."
        every { resources.getString(R.string.notification_desc_discussion) } returns "Get notified when there’s a new discussion topic in your course."
        every { resources.getString(R.string.notification_desc_conversation_message) } returns "Get notified when you have a new inbox message."
        every { resources.getString(R.string.notification_cat_course_activities) } returns "Course Activities"
        every { resources.getString(R.string.notification_cat_discussions) } returns "Discussions"
        every { resources.getString(R.string.notification_cat_conversations) } returns "Conversations"
        every { resources.getString(R.string.errorOccurred) } returns "An unexpected error occurred."
    }
}
