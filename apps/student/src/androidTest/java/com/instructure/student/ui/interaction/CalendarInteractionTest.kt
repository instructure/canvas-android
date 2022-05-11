/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 */
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.SecondaryFeatureCategory
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CalendarInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.EVENTS, TestCategory.INTERACTION, true)
    fun testMonthView_tappingADayDisplaysAllItemsForThatDay() {
        // Tapping a day in the calendar view should display all items for that day in the list view
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.EVENTS, TestCategory.INTERACTION, true)
    fun testMonthView_itemListIsScrollable() {
        // List of calendar items should be scrollable
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.EVENTS, TestCategory.INTERACTION, true)
    fun testMonthView_todayButtonGoesToCurrentDate() {
        // The 'today' button in the toolbar should select the current date in the calendar
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.EVENTS, TestCategory.INTERACTION, true)
    fun testMonthView_tappingDayWithWeekRangeShowsAllItemsForThatWeek() {
        // The Week range/selected date is displayed and shows all events for that week range
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.EVENTS, TestCategory.INTERACTION, true)
    fun testMonthView_monthRangeShowsAllItemsForThatMonth() {
        // The Month range/selected date is displayed and shows all events for that month range
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.EVENTS, TestCategory.INTERACTION, true)
    fun testMonthView_addEventUpdatesList() {
        // *key note, these are personal events only
        // The user should be able to add an event, success is measured by the new event showing up in the list
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.EVENTS, TestCategory.INTERACTION, true)
    fun testMonthView_deleteEventUpdatesList() {
        // *key note, these are personal events only
        // The user should be able to delete an event, success is measured by the deleted event being removed from the list
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.EVENTS, TestCategory.INTERACTION, true)
    fun testMonthView_pullToRefresh() {
        // The user should be able to ptr to refresh the screen
    }

    @Stub
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.EVENTS, TestCategory.INTERACTION, true, SecondaryFeatureCategory.EVENTS_ASSIGNMENTS)
    fun testMonthView_tappingAssignmentItemDisplaysDetails() {
        // Tapping Assignment item navigates user to assignment details
    }

    @Stub
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.EVENTS, TestCategory.INTERACTION, true, SecondaryFeatureCategory.EVENTS_DISCUSSIONS)
    fun testMonthView_tappingDiscussionItemDisplaysDetails() {
        // Tapping Discussion item navigates user to discussion details
    }

    @Stub
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.EVENTS, TestCategory.INTERACTION, true, SecondaryFeatureCategory.EVENTS_QUIZZES)
    fun testMonthView_tappingQuizItemDisplaysDetails() {
        // Tapping Quiz item navigates user to quiz details
    }

    @Stub
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.EVENTS, TestCategory.INTERACTION, true, SecondaryFeatureCategory.EVENTS_NOTIFICATIONS)
    fun testMonthView_tappingNotificationItemDisplaysDetails() {
        // Tapping Notification item navigates user to details
    }

}
