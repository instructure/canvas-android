/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.ApiPrefs
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Tab(
        @SerializedName("id")
        val tabId: String = "", // TODO: null?
        val label: String? = null,
        val type: String = TYPE_INTERNAL,
        @SerializedName("html_url")
        val htmlUrl: String? = null, // Internal url
        @SerializedName("full_url")
        val externalUrl: String? = null, // External url
        val visibility: String = "none", // possible values are: public, members, admins, and none // TODO: enum?
        @SerializedName("hidden")
        val isHidden: Boolean = false, // only included when true
        val position: Int = 0,
        @SerializedName("url")
        val ltiUrl: String = "" // TODO: null?
) : CanvasModel<Tab>() {
    override val id get() = position.toLong()

    val isExternal: Boolean get() = type == TYPE_EXTERNAL

    // Domain strips off trailing slashes.
    val url: String
        get() {
            var tempHtmlUrl = htmlUrl
            if (!tempHtmlUrl!!.startsWith("/")) {
                tempHtmlUrl = "/$tempHtmlUrl"
            }

            return ApiPrefs.domain + tempHtmlUrl
        }

    override fun toString(): String {
        return if (this.tabId == null || this.label == null) {
            ""
        } else this.tabId + ":" + this.label
    }

    companion object {
        const val TYPE_EXTERNAL = "external"
        const val TYPE_INTERNAL = "internal"

        // id constants (these should never change in the API)
        const val SYLLABUS_ID = "syllabus"
        const val ASSIGNMENTS_ID = "assignments"
        const val DISCUSSIONS_ID = "discussions"
        const val PAGES_ID = "pages"
        const val FRONT_PAGE_ID = "front_page"
        const val PEOPLE_ID = "people"
        const val QUIZZES_ID = "quizzes"
        const val FILES_ID = "files"
        const val ANNOUNCEMENTS_ID = "announcements"
        const val MODULES_ID = "modules"
        const val GRADES_ID = "grades"
        const val COLLABORATIONS_ID = "collaborations"
        const val CONFERENCES_ID = "conferences"
        const val OUTCOMES_ID = "outcomes"
        const val NOTIFICATIONS_ID = "notifications"
        const val HOME_ID = "home"
        const val SETTINGS_ID = "settings"
        const val STUDENT_VIEW = "student_view" // This is an extra tab we're adding that isn't returned by the API
    }
}
