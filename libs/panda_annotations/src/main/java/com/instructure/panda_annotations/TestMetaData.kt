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
package com.instructure.panda_annotations


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class TestMetaData(
    val priority: Priority,
    val featureCategory: FeatureCategory,
    val testCategory: TestCategory,
    val stubbed: Boolean = false,
    val secondaryFeature: SecondaryFeatureCategory = SecondaryFeatureCategory.NONE
)

enum class Priority {
    MANDATORY, IMPORTANT, COMMON, NICE_TO_HAVE, BUG_CASE
}

enum class FeatureCategory {
    ASSIGNMENTS, SUBMISSIONS, LOGIN, COURSE, DASHBOARD, GROUPS, SETTINGS, PAGES, DISCUSSIONS, MODULES,
    INBOX, GRADES, FILES, EVENTS, PEOPLE, CONFERENCES, COLLABORATIONS, SYLLABUS, TODOS, QUIZZES, NOTIFICATIONS,
    ANNOTATIONS, ANNOUNCEMENTS, COMMENTS, BOOKMARKS, NONE, K5_DASHBOARD, SPEED_GRADER
}

enum class SecondaryFeatureCategory {
    NONE, LOGIN_K5, SUBMISSIONS_TEXT_ENTRY, SUBMISSIONS_ANNOTATIONS
}

enum class TestCategory {
    RENDER, INTERACTION, E2E
}


