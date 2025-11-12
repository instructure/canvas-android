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
package com.instructure.canvas.espresso


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class TestMetaData(
    val priority: Priority,
    val featureCategory: FeatureCategory,
    val testCategory: TestCategory,
    val secondaryFeature: SecondaryFeatureCategory = SecondaryFeatureCategory.NONE
)

enum class Priority {
    MANDATORY, IMPORTANT, COMMON, NICE_TO_HAVE, BUG_CASE
}

enum class FeatureCategory {
    ASSIGNMENTS, SUBMISSIONS, LOGIN, COURSE, DASHBOARD, GROUPS, SETTINGS, PAGES, DISCUSSIONS, MODULES, CALENDAR,
    INBOX, GRADES, FILES, EVENTS, PEOPLE, CONFERENCES, COLLABORATIONS, SYLLABUS, TODOS, QUIZZES, NOTIFICATIONS,
    ANNOTATIONS, ANNOUNCEMENTS, COMMENTS, BOOKMARKS, NONE, CANVAS_FOR_ELEMENTARY, SPEED_GRADER, SYNC_SETTINGS, SYNC_PROGRESS, OFFLINE_CONTENT, LEFT_SIDE_MENU,
    COURSE_LIST, MANAGE_STUDENTS, COURSE_BROWSER, ALERTS, ACCOUNT_CREATION, COURSE_DETAILS
}

enum class SecondaryFeatureCategory {
    NONE, LOGIN_K5,
    SUBMISSIONS_TEXT_ENTRY, SUBMISSIONS_ANNOTATIONS, SUBMISSIONS_ONLINE_URL, SUBMISSIONS_MULTIPLE_TYPE,
    ASSIGNMENT_COMMENTS, ASSIGNMENT_QUIZZES, ASSIGNMENT_DISCUSSIONS, HOMEROOM, K5_GRADES, IMPORTANT_DATES, RESOURCES, SCHEDULE,
    GROUPS_DASHBOARD, GROUPS_FILES, GROUPS_ANNOUNCEMENTS, GROUPS_DISCUSSIONS, GROUPS_PAGES, GROUPS_PEOPLE,
    EVENTS_DISCUSSIONS, EVENTS_QUIZZES, EVENTS_ASSIGNMENTS, EVENTS_NOTIFICATIONS, SETTINGS_EMAIL_NOTIFICATIONS,
    MODULES_ASSIGNMENTS, MODULES_DISCUSSIONS, MODULES_FILES, MODULES_PAGES, MODULES_QUIZZES, OFFLINE_MODE, ALL_COURSES, CHANGE_USER, ASSIGNMENT_REMINDER, ASSIGNMENT_DETAILS, SMART_SEARCH, ADD_STUDENT,
    SYLLABUS, SUMMARY, FRONT_PAGE, CANVAS_NETWORK, INBOX_SIGNATURE, ACCESS_TOKEN_EXPIRATION, SECTIONS, HELP_MENU, DISCUSSION_CHECKPOINTS,
    CALENDAR_EVENT_REMINDER, CALENDAR_TODO_REMINDER
}

enum class TestCategory {
    RENDER, INTERACTION, E2E
}


