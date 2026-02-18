/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.navigation

import com.instructure.horizon.features.learn.LearnTab

sealed class LearnRoute {
    data object LearnScreen: LearnRoute() {
        const val selectedTabAttr = "selectedTab"
        const val selectedTabFromDetailsKey = "selectedTabFromDetails"
        private const val baseUrl = "learn_screen"
        const val route = "$baseUrl/{$selectedTabAttr}"
        fun route(selectedTab: LearnTab? = null): String {
            return if (selectedTab == null)
                "$baseUrl"
            else
                "$baseUrl/${selectedTab.stringValue}"
        }
    }

    data class LearnCourseDetailsScreen(val courseId: Long): LearnRoute() {
        companion object {
            const val courseIdAttr = "courseId"
            const val baseUrl = "courses"
            const val route = "$baseUrl/{$courseIdAttr}"
            fun route(courseId: Long) = "$baseUrl/$courseId"
        }
    }

    data class LearnProgramDetailsScreen(val courseId: Long): LearnRoute() {
        companion object {
            const val programIdAttr = "programId"
            const val baseUrl = "programs"
            const val route = "$baseUrl/{$programIdAttr}"
            fun route(programId: String) = "$baseUrl/$programId"
        }
    }

    data object LearnLearningLibraryDetailsScreen: LearnRoute() {
        const val collectionIdIdAttr = "collectionId"
        const val baseUrl = "learning_library"
        const val route = "${baseUrl}/{$collectionIdIdAttr}"
        fun route(collectionId: String) = "${baseUrl}/$collectionId"
    }

    data object LearnLearningLibraryBookmarkScreen: LearnRoute() {
        const val typeAttr = "type"
        const val baseUrl = "learning_library/bookmark"
        const val route = "$baseUrl/{$typeAttr}"
        fun route() = "$baseUrl/bookmark"
    }

    data object LearnLearningLibraryCompletedScreen: LearnRoute() {
        const val typeAttr = "type"
        const val baseUrl = "learning_library/completed"
        const val route = "$baseUrl/{$typeAttr}"
        fun route() = "$baseUrl/completed"
    }
}