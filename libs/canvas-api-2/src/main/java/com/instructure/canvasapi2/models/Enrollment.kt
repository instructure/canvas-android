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
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Enrollment(
        override val id: Long = 0,
        val role: EnrollmentType? = EnrollmentType.NoEnrollment, // The enrollment role, for course-level permissions - this field will match `type` if the enrollment role has not been customized
        val type: EnrollmentType? = EnrollmentType.NoEnrollment,
        // Only included when we get enrollments using the user's url: /users/self/enrollments
        @SerializedName("course_id")
        val courseId: Long = 0,
        @SerializedName("course_section_id")
        val courseSectionId: Long = 0,
        @SerializedName("enrollment_state")
        var enrollmentState: String? = null,
        @SerializedName("user_id")
        val userId: Long = 0,
        val grades: Grades? = null,
        // Only included when we get the enrollment with a course object
        @SerializedName("computed_current_score")
        val computedCurrentScore: Double? = null,
        @SerializedName("computed_final_score")
        val computedFinalScore: Double? = null,
        @SerializedName("computed_current_grade")
        val computedCurrentGrade: String? = null,
        @SerializedName("computed_final_grade")
        val computedFinalGrade: String? = null,
        @SerializedName("computed_current_letter_grade")
        val computedCurrentLetterGrade: String? = null,
        @SerializedName("multiple_grading_periods_enabled")
        val multipleGradingPeriodsEnabled: Boolean = false,
        @SerializedName("totals_for_all_grading_periods_option")
        val totalsForAllGradingPeriodsOption: Boolean = false,
        @SerializedName("current_period_computed_current_score")
        val currentPeriodComputedCurrentScore: Double? = null,
        @SerializedName("current_period_computed_final_score")
        val currentPeriodComputedFinalScore: Double? = null,
        @SerializedName("current_period_computed_current_grade")
        val currentPeriodComputedCurrentGrade: String? = null,
        @SerializedName("current_period_computed_final_grade")
        val currentPeriodComputedFinalGrade: String? = null,
        @SerializedName("current_grading_period_id")
        val currentGradingPeriodId: Long = 0,
        @SerializedName("current_grading_period_title")
        val currentGradingPeriodTitle: String? = null,
        // The unique id of the associated user. Will be null unless type is ObserverEnrollment.
        @SerializedName("associated_user_id")
        val associatedUserId: Long = 0,
        @SerializedName("last_activity_at")
        val lastActivityAt: Date? = null,
        @SerializedName("limit_privileges_to_course_section")
        val limitPrivilegesToCourseSection: Boolean = false,
        @SerializedName("observed_user")
        val observedUser: User? = null,
        var user: User? = null
) : CanvasModel<Enrollment>() {
    override val comparisonString get() = type?.apiRoleString

    enum class EnrollmentType(val apiTypeString: String, val apiRoleString: String) {
        @SerializedName(value = "StudentEnrollment", alternate = ["student"])
        Student("student", "StudentEnrollment"),

        @SerializedName(value = "TeacherEnrollment", alternate = ["teacher"])
        Teacher("teacher", "TeacherEnrollment"),

        @SerializedName(value = "TaEnrollment", alternate = ["ta"])
        Ta("ta", "TaEnrollment"),

        @SerializedName(value = "ObserverEnrollment", alternate = ["observer"])
        Observer("observer", "ObserverEnrollment"),

        @SerializedName(value = "DesignerEnrollment", alternate = ["designer"])
        Designer("designer", "DesignerEnrollment"),

        NoEnrollment("", "") // Used in the People Recycler Adapter to show those with no enrollment

        // NOTE: There is also a StudentViewEnrollment that allows Teachers to view the course as a student - we don't handle that right now, and we probably don't have to worry about it
    }

    val isStudent: Boolean get() = type == EnrollmentType.Student || role == EnrollmentType.Student
    val isTeacher: Boolean get() = type == EnrollmentType.Teacher || role == EnrollmentType.Teacher
    val isObserver: Boolean get() = type == EnrollmentType.Observer || role == EnrollmentType.Observer
    val isTA: Boolean get() = type == EnrollmentType.Ta || role == EnrollmentType.Ta
    val isDesigner: Boolean get() = type == EnrollmentType.Designer || role == EnrollmentType.Designer

    val currentScore: Double? get() = grades?.currentScore ?: computedCurrentScore
    val finalScore: Double? get() = grades?.finalScore ?: computedFinalScore
    val currentGrade: String? get() = grades?.currentGrade ?: computedCurrentGrade ?: computedCurrentLetterGrade
    val finalGrade: String? get() = grades?.finalGrade ?: computedFinalGrade

    fun currentPeriodComputedCurrentScore(): Double? = grades?.currentScore ?: currentPeriodComputedCurrentScore
    fun currentPeriodComputedCurrentGrade(): String? = grades?.currentGrade ?: currentPeriodComputedCurrentGrade
    fun currentPeriodComputedFinalScore(): Double? = grades?.finalScore ?: currentPeriodComputedFinalScore
    fun currentPeriodComputedFinalGrade(): String? = grades?.finalGrade ?: currentPeriodComputedFinalGrade
}

