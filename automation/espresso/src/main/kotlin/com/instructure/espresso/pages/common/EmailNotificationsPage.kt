/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.espresso.pages.common

import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.instructure.espresso.WaitForViewWithText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.getStringFromResource
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.waitForView
import com.instructure.espresso.pages.waitForViewWithText
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withParent
import com.instructure.espresso.pages.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.scrollToItem
import com.instructure.espresso.swipeUp
import com.instructure.pandautils.R
import org.hamcrest.Matchers.allOf

class EmailNotificationsPage : BasePage() {

    private val courseActivitiesLabel by WaitForViewWithText(R.string.notification_cat_course_activities)
    private val discussionsLabel by WaitForViewWithText(R.string.notification_cat_discussions)
    private val conversationsLabel by WaitForViewWithText(R.string.notification_cat_conversations)
    private val schedulingLabel by WaitForViewWithText(R.string.notification_cat_scheduling)
    private val groupsLabel by WaitForViewWithText(R.string.notification_cat_groups)
    private val alertsLabel by WaitForViewWithText(R.string.notification_cat_alerts)
    private val conferencesLabel by WaitForViewWithText(R.string.notification_cat_conferences)

    fun swipeUp() {
        onView(withId(R.id.swipeRefreshLayout) + ViewMatchers.withParent(withId(R.id.pushNotificationPreferencesFragment))).swipeUp()
    }

    fun assertToolbarTitle() {
       onView(withText(getStringFromResource(R.string.emailNotifications)) + withParent(R.id.toolbar)).assertDisplayed()
    }

    fun assertCourseActivitiesEmailNotificationsDisplayed() {

        //Course Activities group label
        courseActivitiesLabel.scrollTo().assertDisplayed()

        //Due Date
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_due_date))).scrollTo().assertDisplayed()

        //Grading Policies
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_grading_policies))).scrollTo().assertDisplayed()

        //Course Content
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_course_content))).scrollTo().assertDisplayed()

        //Files
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_files))).scrollTo().assertDisplayed()

        //Announcement (+ Announcement Created By You)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_announcement))).scrollTo().assertDisplayed()
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_announcement_created_by_you))).scrollTo().assertDisplayed()

        //Grading
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_grading))).scrollTo().assertDisplayed()

        //Invitation
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_invitation))).scrollTo().assertDisplayed()

        //All Submissions
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_all_submissions))).scrollTo().assertDisplayed()

        //Late Grading
        scrollToNotification(R.string.notification_pref_late_grading)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_late_grading))).assertDisplayed()

        //Submission Comment
        scrollToNotification(R.string.notification_pref_submission_comment)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_submission_comment))).assertDisplayed()
    }

    fun assertDiscussionsEmailNotificationsDisplayed() {

        //Discussion group label
        scrollToNotification(R.string.notification_cat_discussions)
        discussionsLabel.assertDisplayed()

        //Discussion
        scrollToNotification(R.string.notification_pref_discussion)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_discussion))).assertDisplayed()

        //Discussion Post
        scrollToNotification(R.string.notification_pref_discussion_post)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_discussion_post))).assertDisplayed()
    }

    fun assertConversationsEmailNotificationsDisplayed() {

        //Conversations group label
        scrollToNotification(R.string.notification_cat_conversations)
        conversationsLabel.assertDisplayed()

        //Add To Conversation
        scrollToNotification(R.string.notification_pref_add_to_conversation)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_add_to_conversation))).assertDisplayed()

        //Conversation Message (+ Conversations Created By You)
        scrollToNotification(R.string.notification_pref_conversation_message)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_conversation_message))).assertDisplayed()
        scrollToNotification(R.string.notification_pref_conversations_created_by_you)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_conversations_created_by_you))).assertDisplayed()
    }

    fun assertSchedulingEmailNotificationsDisplayed() {

        //Scheduling group label
        scrollToNotification(R.string.notification_cat_scheduling)
        schedulingLabel.assertDisplayed()

        //Student Appointment Signups
        scrollToNotification(R.string.notification_pref_student_appointment_signups)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_student_appointment_signups))).assertDisplayed()

        //Appointment Signups
        scrollToNotification(R.string.notification_pref_appointment_signups)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_appointment_signups))).assertDisplayed()

        //Appointment Cancellations
        scrollToNotification(R.string.notification_pref_appointment_cancelations)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_appointment_cancelations))).assertDisplayed()

        //Appointment Availability
        scrollToNotification(R.string.notification_pref_appointment_availability)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_appointment_availability))).assertDisplayed()

        //Calendar
        scrollToNotification(R.string.notification_pref_calendar)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_calendar))).assertDisplayed()
    }

    fun assertGroupsEmailNotificationsDisplayed() {

        //Groups group label
        scrollToNotification(R.string.notification_cat_groups)
        groupsLabel.assertDisplayed()

        //Membership Update
        scrollToNotification(R.string.notification_pref_membership_update)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_membership_update))).assertDisplayed()
    }

    fun assertAlertsEmailNotificationsDisplayed() {

        //Groups group label
        scrollToNotification(R.string.notification_cat_alerts)
        alertsLabel.assertDisplayed()

        //Administrative Notifications
        scrollToNotification(R.string.notification_pref_admin)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_admin))).assertDisplayed()
    }

    fun assertConferencesEmailNotificationsDisplayed() {

        //Conferences group label
        scrollToNotification(R.string.notification_cat_conferences)
        conferencesLabel.assertDisplayed()

        //Recording Ready
        scrollToNotification(R.string.notification_pref_recording_ready)
        onView(withId(R.id.appThemeTitle) + withText(getStringFromResource(R.string.notification_pref_recording_ready))).assertDisplayed()
    }

    fun clickOnNotification(notificationText: String) {
        onView(withId(R.id.appThemeTitle) + withText(notificationText)).click()
    }

    fun selectFrequency(frequencyText: String) {
        waitForViewWithText(getStringFromResource(R.string.selectFrequency)).assertDisplayed()
        onView(withId(android.R.id.text1) + withText(frequencyText) + withParent(R.id.select_dialog_listview)).click()
    }

    fun assertNotificationFrequency(notificationText: String, frequencyText: String) {
        waitForView(withId(R.id.appThemeStatus) + withText(frequencyText) + ViewMatchers.hasSibling(
            withId(R.id.appThemeTitle) + withText(notificationText)
        )
        ).assertDisplayed()
    }

    private fun scrollToNotification(notificationStringResource: Int) {
        scrollToItem(R.id.pushNotificationPreferencesFragment, getStringFromResource(notificationStringResource), allOf(
            isAssignableFrom(androidx.recyclerview.widget.RecyclerView::class.java),
            withAncestor(R.id.pushNotificationPreferencesFragment),
            withParent(withId(R.id.swipeRefreshLayout))), withText(notificationStringResource) + withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
        )
    }
}
