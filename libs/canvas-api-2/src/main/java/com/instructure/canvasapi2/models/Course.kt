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
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.isCreationPending
import com.instructure.canvasapi2.utils.isNullOrEmpty
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

@Parcelize
data class Course(
        override val id: Long = 0,
        override var name: String = "",
        @SerializedName("original_name")
        var originalName: String? = null,
        @SerializedName("course_code")
        val courseCode: String? = null,
        @SerializedName("start_at")
        val startAt: String? = null,
        @SerializedName("end_at")
        val endAt: String? = null,
        @SerializedName("syllabus_body")
        var syllabusBody: String? = null,
        @SerializedName("hide_final_grades")
        val hideFinalGrades: Boolean = false,
        @SerializedName("is_public")
        val isPublic: Boolean = false,
        val license: License? = License.PRIVATE_COPYRIGHTED,
        val term: Term? = null,
        var enrollments: MutableList<Enrollment>? = ArrayList(),
        @SerializedName("needs_grading_count")
        val needsGradingCount: Long = 0,
        @SerializedName("apply_assignment_group_weights")
        val isApplyAssignmentGroupWeights: Boolean = false,
        val currentScore: Double? = null, // Helper variable
        val finalScore: Double? = null, // Helper variable
        val currentGrade: String? = null, // Helper variable
        val finalGrade: String? = null, // Helper variable
        @SerializedName("is_favorite")
        var isFavorite: Boolean = false,
        @SerializedName("access_restricted_by_date")
        val accessRestrictedByDate: Boolean = false,
        @SerializedName("image_download_url")
        val imageUrl: String? = null,
        @SerializedName("banner_image_download_url")
        val bannerImageUrl: String? = null,
        @SerializedName("has_weighted_grading_periods")
        val isWeightedGradingPeriods: Boolean = false,
        @SerializedName("has_grading_periods")
        val hasGradingPeriods: Boolean = false,
        val sections: List<Section> = ArrayList(),
        @SerializedName("default_view")
        var homePage: HomePage? = null,
        @SerializedName("restrict_enrollments_to_course_dates")
        val restrictEnrollmentsToCourseDate: Boolean = false,
        @SerializedName("workflow_state")
        val workflowState: WorkflowState? = null,
        @SerializedName("homeroom_course")
        val homeroomCourse: Boolean = false,
        @SerializedName("course_color")
        val courseColor: String? = null,
        @SerializedName("grading_periods")
        val gradingPeriods: List<GradingPeriod>? = null,
        @SerializedName("tabs")
        val tabs: List<Tab>? = null,
        @SerializedName("settings")
        val settings: CourseSettings? = null,
        @SerializedName("grading_scheme")
        val gradingSchemeRaw: List<List<@RawValue Any>>? = null,
        @SerializedName("points_based_grading_scheme")
        val pointsBasedGradingScheme: Boolean = false,
        @SerializedName("scaling_factor")
        val scalingFactor: Double = 1.0,
) : CanvasContext(), Comparable<CanvasContext> {
    override val type: Type get() = Type.COURSE

    val startDate: Date? get() = startAt.toDate()
    val endDate: Date? get() = endAt.toDate()

    val isStudent: Boolean get() = enrollments?.any { it.isStudent } ?: false
    val isTeacher: Boolean get() = enrollments?.any { it.isTeacher } ?: false
    val isTA: Boolean get() = enrollments?.any { it.isTA } ?: false
    val isObserver: Boolean get() = enrollments?.any { it.isObserver } ?: false
    val isDesigner: Boolean get() = enrollments?.any { it.isDesigner } ?: false

    /**
     * If the general hide final setting is off, we still need to check MGP all grades setting
     * The grade is Locked if they have no active period and the totals for all is hidden
     * If we've made it this far, the course is definitely not Locked
     *
     * KEY NOTE: This is only accurate for general course grade viewing.
     * If you are trying to view a grade w/in a filter that allows users to select specific grading periods, use the
     * function [getCourseGradeForGradingPeriodSpecificEnrollment]
     *
     * The key here is that hideFinalGrades is true for all things, but isTotalsForAllGradingPeriodsEnabled is only
     * true for when the _ALL_ grading period option is selected.
     */
    private val isCourseGradeLocked: Boolean
        get() {
            if (hideFinalGrades) {
                return true
            } else {
                if (hasGradingPeriods) {
                    return !hasActiveGradingPeriod() && !isTotalsForAllGradingPeriodsEnabled
                }
            }
            return false
        }

    val isTotalsForAllGradingPeriodsEnabled: Boolean
        get() {
            enrollments?.forEach { enrollment ->
                if (enrollment.isStudent || enrollment.isObserver) {
                    return enrollment.multipleGradingPeriodsEnabled && enrollment.totalsForAllGradingPeriodsOption
                }
            }

            return false
        }

    val gradingScheme: List<GradingSchemeRow>
        get() {
            return gradingSchemeRaw?.map { row ->
                if (row.size < 2 || row[0] !is String || row[1] !is Double) {
                    null
                } else {
                    GradingSchemeRow(row[0] as String, row[1] as Double)
                }
            }?.filterNotNull()?.sortedByDescending { it.value } ?: emptyList()
        }

    /**
     * A helper method to get access to all course grade values in one place and how to display them
     *
     * See CourseGrade for documentation regarding its properties
     *
     * @param ignoreMGP - This flag will ignore current grading period scores. If the course has
     * grading periods and this flag is set to true, the all GradingPeriod grades
     * will be returned as they are treated the same as standard course totals.
     *
     * @return CourseGrade - Contains all course grade values, Locked status, and noGrade status (N/A). Will be null
     * if the user does not have either a student or observer enrollment in this course.
     */
    fun getCourseGrade(ignoreMGP: Boolean): CourseGrade? {
        enrollments?.forEach { enrollment ->
            if (enrollment.isStudent || enrollment.isObserver) {
                Logger.d("Logging for Grades E2E, enrollment for coures grade: $enrollment")
                return getCourseGradeFromEnrollment(enrollment, ignoreMGP)
            }
        }
        return null
    }

    fun getCourseGradeFromEnrollment(enrollment: Enrollment, ignoreMGP: Boolean): CourseGrade =
            // First we want to see if its Locked before we proceed
            if (hasActiveGradingPeriod() && !ignoreMGP) {
                // If they have an active grading period we show the current period values
                CourseGrade(
                        enrollment.currentPeriodComputedCurrentGrade(),
                        enrollment.currentPeriodComputedCurrentScore(),
                        enrollment.currentPeriodComputedFinalGrade(),
                        enrollment.currentPeriodComputedFinalScore(),
                        isCourseGradeLocked,
                        noCurrentGrade(enrollment.currentPeriodComputedCurrentGrade(), enrollment.currentPeriodComputedCurrentScore()),
                        noFinalGrade(enrollment.currentPeriodComputedFinalGrade(), enrollment.currentPeriodComputedFinalScore()))
            } else {
                // Otherwise, we show the computed overall values (All Grading Periods is covered by this case)
                CourseGrade(
                        enrollment.currentGrade,
                        enrollment.currentScore,
                        enrollment.finalGrade,
                        enrollment.finalScore,
                        isCourseGradeLocked,
                        noCurrentGrade(enrollment.currentGrade, enrollment.currentScore),
                        noFinalGrade(enrollment.finalGrade, enrollment.finalScore))
            }

    fun getCourseGradeForGradingPeriodSpecificEnrollment(enrollment: Enrollment): CourseGrade {
        return CourseGrade(
                enrollment.currentPeriodComputedCurrentGrade(),
                enrollment.currentPeriodComputedCurrentScore(),
                enrollment.currentPeriodComputedFinalGrade(),
                enrollment.currentPeriodComputedFinalScore(),
                hideFinalGrades, // We only care about hiding final grades in this case if the course wants them hidden
                noCurrentGrade(enrollment.currentPeriodComputedCurrentGrade(), enrollment.currentPeriodComputedCurrentScore()),
                noFinalGrade(enrollment.currentPeriodComputedFinalGrade(), enrollment.currentPeriodComputedFinalScore()))
    }

    private fun noFinalGrade(finalGrade: String?, finalScore: Double?): Boolean =
            finalScore == null && (finalGrade == null || finalGrade.contains("N/A") || finalGrade.isEmpty())

    private fun noCurrentGrade(currentGrade: String?, currentScore: Double?): Boolean =
            currentScore == null && (currentGrade == null || currentGrade.contains("N/A") || currentGrade.isEmpty())

    fun addEnrollment(enrollment: Enrollment) {
        if (enrollments.isNullOrEmpty()) {
            enrollments = arrayListOf()
        }
        enrollments!!.add(enrollment)
    }

    /**
     * This function helps us to determine if we should be defaulting to the All Grading Period view or not.
     */
    private fun hasActiveGradingPeriod(): Boolean {
        enrollments?.forEach { enrollment ->
            if (enrollment.isStudent || enrollment.isObserver) {
                return enrollment.multipleGradingPeriodsEnabled && enrollment.currentGradingPeriodId != 0L
            }
        }

        return false
    }

    private fun parentHasActiveGradingPeriod(): Boolean {
        return enrollments?.any { it.multipleGradingPeriodsEnabled && it.currentGradingPeriodId != 0L } ?: false
    }

    private fun parentIsTotalsForAllGradingPeriodsEnabled() = this.enrollments.orEmpty().any {
        (it.isStudent || it.isObserver) && it.multipleGradingPeriodsEnabled && it.totalsForAllGradingPeriodsOption
    }

    private fun parentIsCourseGradeLocked(forAllGradingPeriod: Boolean = true): Boolean {
        return if (hideFinalGrades) {
            true
        } else if (hasGradingPeriods) {
            forAllGradingPeriod && !parentHasActiveGradingPeriod() && !parentIsTotalsForAllGradingPeriodsEnabled()
        } else {
            false
        }
    }

    fun parentGetCourseGradeFromEnrollment(
        enrollment: Enrollment,
        lockedForAllGradingPeriod: Boolean = true
    ) = if (parentHasActiveGradingPeriod()) {
        CourseGrade(
            enrollment.currentPeriodComputedCurrentGrade(),
            enrollment.currentPeriodComputedCurrentScore(),
            enrollment.currentPeriodComputedFinalGrade(),
            enrollment.currentPeriodComputedFinalScore(),
            parentIsCourseGradeLocked(lockedForAllGradingPeriod),
            noCurrentGrade(enrollment.currentPeriodComputedCurrentGrade(), enrollment.currentPeriodComputedCurrentScore()),
            noFinalGrade(enrollment.currentPeriodComputedFinalGrade(), enrollment.currentPeriodComputedFinalScore())
        )
    } else {
        CourseGrade(
            enrollment.currentGrade,
            enrollment.currentScore,
            enrollment.finalGrade,
            enrollment.finalScore,
            parentIsCourseGradeLocked(lockedForAllGradingPeriod),
            noCurrentGrade(enrollment.currentGrade, enrollment.currentScore),
            noFinalGrade(enrollment.finalGrade, enrollment.finalScore)
        )
    }

    /**
     * Helper function to check if the course is within a valid date range for use
     *
     * Useful for setting content to read-only, such as submissions
     */
    fun isBetweenValidDateRange(): Boolean {
        val now = Date()
        if (accessRestrictedByDate) return false

        if (workflowState == WorkflowState.COMPLETED) return false

        return if (restrictEnrollmentsToCourseDate) {
            isWithinDates(startAt.toDate(), endAt.toDate(), now)
        } else {
            val isValidForTerm = isWithinDates(term?.startDate, term?.endDate, now)

            if (isValidForTerm) {
                // check the sections
                if (sections.isEmpty()) {
                    true
                } else {
                    // All we need is one valid section
                    sections.any { section -> !section.restrictEnrollmentsToSectionDates || isWithinDates(section.startAt.toDate(), section.endAt.toDate(), now) }
                }
            } else {
                false
            }
        }
    }

    private fun isWithinDates(startAt: Date?, endAt: Date?, now: Date): Boolean {
        // If the dates are null, we have to show it
        val isValidStartAt: Boolean = if (startAt == null) true else now.after(startAt)
        val isValidEndAt: Boolean = if (endAt == null) true else now.before(endAt)

        return isValidEndAt && isValidStartAt
    }

    fun isPastEnrolment(): Boolean {
        val now = Date()
        if (accessRestrictedByDate) return false
        if (workflowState == WorkflowState.COMPLETED) return true

        val overrideSections = sections.filter { it.restrictEnrollmentsToSectionDates }

        return if (overrideSections.isNotEmpty()) {
            overrideSections.any { section -> section.endDate?.before(now) ?: false }
        } else if (restrictEnrollmentsToCourseDate) {
            endDate?.before(now) ?: false
        } else {
            term?.endDate?.before(now) ?: false
        }
    }

    fun isFutureEnrolment(): Boolean {
        val now = Date()
        if (accessRestrictedByDate) return false
        if (workflowState == WorkflowState.COMPLETED) return false

        if (isCreationPending()) return true

        val overrideSections = sections.filter { it.restrictEnrollmentsToSectionDates }

        return if (overrideSections.isNotEmpty()) {
            overrideSections.any { section -> section.startDate?.after(now) ?: false }
        } else if (restrictEnrollmentsToCourseDate) {
            startDate?.after(now) ?: false
        } else {
            term?.startDate?.after(now) ?: false
        }
    }

    fun isCurrentEnrolment(): Boolean {
        val now = Date()
        if (accessRestrictedByDate) return false

        if (workflowState == WorkflowState.COMPLETED) return false

        val overrideSections = sections.filter { it.restrictEnrollmentsToSectionDates }

        return if (overrideSections.isNotEmpty()) {
            overrideSections.any { isWithinDates(it.startAt.toDate(), it.endAt.toDate(), now) }
        } else if (restrictEnrollmentsToCourseDate) {
            isWithinDates(startAt.toDate(), endAt.toDate(), now)
        } else {
            isWithinDates(term?.startDate, term?.endDate, now)
        }
    }

    /**
     * Get home page label returns the fragment identifier.
     *
     * @return
     */
    //notifications can't be hidden, so if for some reason we don't have the home page
    //send them to notifications instead
    //send them to notifications if we don't know what to do
    val homePageID: String
        get() =
            when (homePage) {
                HomePage.HOME_FEED -> Tab.NOTIFICATIONS_ID
                HomePage.HOME_SYLLABUS -> Tab.SYLLABUS_ID
                HomePage.HOME_WIKI -> Tab.FRONT_PAGE_ID
                HomePage.HOME_ASSIGNMENTS -> Tab.ASSIGNMENTS_ID
                HomePage.HOME_MODULES -> Tab.MODULES_ID
                null -> Tab.NOTIFICATIONS_ID
            }

    enum class HomePage(val apiString: String) {
        @SerializedName("feed")
        HOME_FEED("feed"),

        @SerializedName("wiki")
        HOME_WIKI("wiki"),

        @SerializedName("modules")
        HOME_MODULES("modules"),

        @SerializedName("assignments")
        HOME_ASSIGNMENTS("assignments"),

        @SerializedName("syllabus")
        HOME_SYLLABUS("syllabus")
    }

    enum class License(val apiString: String, val prettyString: String) {
        @SerializedName("private")
        PRIVATE_COPYRIGHTED("private", "Private (Copyrighted)"),

        @SerializedName("cc_by_nc_nd")
        CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE("cc_by_nc_nd", "CC Attribution Non-Commercial No Derivatives"),

        @SerializedName("c_by_nc_sa")
        CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE("c_by_nc_sa", "CC Attribution Non-Commercial Share Alike"),

        @SerializedName("c_by_nc")
        CC_ATTRIBUTION_NON_COMMERCIAL("cc_by_nc", "CC Attribution Non-Commercial"),

        @SerializedName("cc_by_nd")
        CC_ATTRIBUTION_NO_DERIVATIVE("cc_by_nd", "CC Attribution No Derivatives"),

        @SerializedName("cc_by_sa")
        CC_ATTRIBUTION_SHARE_ALIKE("cc_by_sa", "CC Attribution Share Alike"),

        @SerializedName("cc_by")
        CC_ATTRIBUTION("cc_by", "CC Attribution"),

        @SerializedName("public_domain")
        PUBLIC_DOMAIN("public_domain", "Public Domain")
    }

    enum class WorkflowState(val apiString: String) {
        @SerializedName("unpublished")
        UNPUBLISHED("unpublished"),

        @SerializedName("available")
        AVAILABLE("available"),

        @SerializedName("completed")
        COMPLETED("completed"),

        @SerializedName("deleted")
        DELETED("deleted")

    }
}
