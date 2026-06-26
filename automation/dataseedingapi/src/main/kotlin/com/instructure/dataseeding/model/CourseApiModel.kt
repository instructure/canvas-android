/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class CourseApiModel(
        val id: Long,
        val name: String,
        @SerializedName("course_code")
        var courseCode: String,
        @SerializedName("homeroom_course")
        val homeroomCourse: Boolean,
        @SerializedName("account_id")
        val accountId: Long?,
        @SerializedName("syllabus_body")
        val syllabusBody: String? = null,
        @SerializedName("default_view")
        var homePage: String? = null,
        @SerializedName("is_favorite")
        var isFavorite: Boolean = false,
)

data class CreateCourse(
        val name: String,
        @SerializedName("course_code")
        val courseCode: String,
        @SerializedName("enrollment_term_id")
        val enrollmentTermId: Long? = null,
        val role: String = Role.TEACHER.name,
        @SerializedName("homeroom_course")
        val homeroomCourse: Boolean = false,
        @SerializedName("account_id")
        val accountId: Long? = null,
        @SerializedName("syllabus_body")
        val syllabusBody: String? = null,
        @SerializedName("settings")
        val settings: CourseSettings? = null
)

data class UpdateCourse(
        @SerializedName("syllabus_body")
        val syllabusBody: String? = null,
        @SerializedName("default_view")
        var homePage: String? = null,
        @SerializedName("syllabus_course_summary")
        var showSummary: Int? = 1
)

data class CourseSettings(
        @SerializedName("restrict_quantitative_data")
        var restrictQuantitativeData: Boolean = false,
)

data class CreateCourseWrapper(
        val course: CreateCourse,
        val offer: Boolean = true
)

data class UpdateCourseWrapper(
        val course: UpdateCourse,
        val offer: Boolean = true
)

enum class Role {
    TEACHER, STUDENT, TA
}