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
 */

package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.NaturalOrderComparator
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Page(
        @SerializedName("page_id")
        override var id: Long = 0,
        var url: String? = null,
        var title: String? = null,
        @SerializedName("created_at")
        val createdAt: Date? = null,
        @SerializedName("updated_at")
        val updatedAt: Date? = null,
        @SerializedName("hide_from_students")
        val hideFromStudents: Boolean = false,
        var status: String? = null,
        var body: String? = null,
        @SerializedName("front_page")
        var frontPage: Boolean = false,
        @SerializedName("lock_info")
        var lockInfo: LockInfo? = null,
        @SerializedName("published")
        var published: Boolean = false,
        @SerializedName("editing_roles")
        var editingRoles: String? = null,
        @SerializedName("html_url")
        var htmlUrl: String? = null
) : CanvasModel<Page>(), Parcelable {
    override val comparisonDate get() = updatedAt
    override val comparisonString get() = title

    override fun compareTo(other: Page) = comparePages(this, other)

    private fun comparePages(page1: Page, page2: Page): Int {
        return when {
            page1.frontPage -> -1
            page2.frontPage -> 1
            else -> NaturalOrderComparator.compare(page1.title?.lowercase(Locale.getDefault()).orEmpty(), page2.title?.lowercase(Locale.getDefault()).orEmpty())
        }
    }

    companion object {
        const val FRONT_PAGE_NAME = "front-page"
        const val TEACHERS = "teachers"
        const val STUDENTS = "students"
        const val ANYONE = "public"
        const val GROUP_MEMBERS = "members"
    }
}
