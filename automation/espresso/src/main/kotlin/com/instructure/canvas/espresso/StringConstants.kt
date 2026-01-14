/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

/**
 * Shared string constants for E2E and UI interaction tests across Student, Teacher, and Parent apps.
 * Please use this for strings that don't have corresponding string resources.
 */
object StringConstants {

    // Worker names
    const val SUBMISSION_WORKER_NAME = "SubmissionWorker"

    // Help Menu related
    object HelpMenu {
        // Common titles and subtitles across all apps
        const val SEARCH_GUIDES_TITLE = "Search the Canvas Guides"
        const val SEARCH_GUIDES_SUBTITLE = "Find answers to common questions"

        const val CUSTOM_LINK_TITLE = "CUSTOM LINK"
        const val CUSTOM_LINK_SUBTITLE = "This is a custom help link."

        const val REPORT_PROBLEM_TITLE = "Report a Problem"
        const val REPORT_PROBLEM_SUBTITLE = "If Canvas misbehaves, tell us about it"

        const val SUBMIT_FEATURE_TITLE = "Submit a Feature Idea"
        const val SUBMIT_FEATURE_SUBTITLE = "Have an idea to improve Canvas?"

        const val SHARE_LOVE_TITLE = "Share Your Love for the App"
        const val SHARE_LOVE_SUBTITLE = "Tell us about your favorite parts of the app"

        // Student-specific
        object Student {
            const val ASK_INSTRUCTOR_TITLE = "Ask Your Instructor a Question"
            const val ASK_INSTRUCTOR_SUBTITLE = "Questions are submitted to your instructor"
        }

        // Teacher-specific
        object Teacher {
            const val CONFERENCE_GUIDES_TITLE = "Conference Guides for Remote Classrooms"
            const val CONFERENCE_GUIDES_SUBTITLE = "Get help on how to use and configure conferences in canvas."

            const val ASK_COMMUNITY_TITLE = "Ask the Community"
            const val ASK_COMMUNITY_SUBTITLE = "Explore guides, updates, blogs, forums, and resources to help you find answers, collaborate with others, and keep learning."

            const val TRAINING_PORTAL_TITLE = "Training Services Portal"
            const val TRAINING_PORTAL_SUBTITLE = "Access Canvas training videos and courses"
        }
    }
}