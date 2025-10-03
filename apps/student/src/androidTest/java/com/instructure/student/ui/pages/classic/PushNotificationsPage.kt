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
package com.instructure.student.ui.pages.classic

import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.instructure.espresso.WaitForViewWithText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.scrollToItem
import com.instructure.espresso.swipeUp
import com.instructure.student.R
import org.hamcrest.Matchers.allOf

class PushNotificationsPage : BasePage() {

    private val courseActivitiesLabel by WaitForViewWithText(R.string.notification_cat_course_activities)
    private val discussionsLabel by WaitForViewWithText(R.string.notification_cat_discussions)
    private val conversationsLabel by WaitForViewWithText(R.string.notification_cat_conversations)
    private val schedulingLabel by WaitForViewWithText(R.string.notification_cat_scheduling)

    fun swipeUp() {
        onView(withId(R.id.swipeRefreshLayout) + ViewMatchers.withParent(withId(R.id.pushNotificationPreferencesFragment))).swipeUp()
    }

    fun assertToolbarTitle() {
       onView(withText(getStringFromResource(R.string.pushNotifications)) + withParent(R.id.toolbar)).assertDisplayed()
    }

    fun assertCourseActivitiesPushNotificationsDisplayed() {

        //Course Activities group label
        courseActivitiesLabel.scrollTo().assertDisplayed()

        //Due Date
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_due_date))).scrollTo().assertDisplayed()
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_due_date))).scrollTo().assertDisplayed()

        //Course Content
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_course_content))).scrollTo().assertDisplayed()
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_course_content))).scrollTo().assertDisplayed()

        //Announcement
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_announcement))).scrollTo().assertDisplayed()
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_announcement))).scrollTo().assertDisplayed()

        //Grading
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_grading))).scrollTo().assertDisplayed()
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_grading))).scrollTo().assertDisplayed()

        //Invitation
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_invitation))).scrollTo().assertDisplayed()
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_invitation))).scrollTo().assertDisplayed()

        //Submission Comment
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_submission_comment))).scrollTo().assertDisplayed()
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_submission_comment))).scrollTo().assertDisplayed()
    }

    fun assertDiscussionsPushNotificationsDisplayed() {

        //Discussion group label
        scrollToItem(
            com.instructure.pandautils.R.id.pushNotificationPreferencesFragment, getStringFromResource(R.string.notification_cat_discussions), allOf(
                isAssignableFrom(androidx.recyclerview.widget.RecyclerView::class.java),
                withAncestor(com.instructure.pandautils.R.id.pushNotificationPreferencesFragment),
                withParent(withId(com.instructure.pandautils.R.id.swipeRefreshLayout))), withText(getStringFromResource(R.string.notification_cat_discussions)) + withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
        )
        discussionsLabel.assertDisplayed()

        //Discussion
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_discussion))).assertDisplayed()
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_discussion))).assertDisplayed()

        swipeUp() //Need to swipe up the page for the other notifications as they

        //Discussion Post
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_discussion_post))).assertDisplayed()
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_discussion_post))).assertDisplayed()
    }

    fun assertConversationsPushNotificationsDisplayed() {

        //Conversations group label
        conversationsLabel.scrollTo().assertDisplayed()

        //Conversation Message
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_conversation_message))).scrollTo().assertDisplayed()
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_conversation_message))).scrollTo().assertDisplayed()
    }

    fun assertSchedulingPushNotificationsDisplayed() {

        //Scheduling group label
        schedulingLabel.assertDisplayed()

        //Student Appointment Signups
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_student_appointment_signups)))
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_student_appointment_signups)))

        //Appointment Cancellations
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_appointment_cancelations)))
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_appointment_cancelations)))

        //Appointment Availability
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_appointment_availability)))
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_appointment_availability)))

        //Calendar
        onView(withId(R.id.title) + withText(getStringFromResource(R.string.notification_pref_calendar)))
        onView(withId(R.id.description) + withText(getStringFromResource(R.string.notification_desc_calendar)))
    }

}
