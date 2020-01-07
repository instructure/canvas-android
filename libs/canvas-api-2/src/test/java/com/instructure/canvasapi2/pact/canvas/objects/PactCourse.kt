/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.pact.canvas.objects

import com.instructure.canvasapi2.models.Course
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

data class PactCourseFieldInfo (
    val courseId: Long, // Mandatory
    val numEnrollments: Int = 1,
    val isFavorite: Boolean = false,
    val includeCourseImage: Boolean = false,
    val includeCurrentGradingPeriodScores: Boolean = false,
    val includeNeedsGradingCount: Boolean = false,
    val includeObservedUsers: Boolean = false,
    val includeSections: Boolean = false,
    val includeSyllabusBody : Boolean = false,
    val includeTerm: Boolean = false,
    val includeTotalScores: Boolean = false
) {
    companion object {
        fun fromQueryString(courseId: Long, isFavorite: Boolean, query: String) : PactCourseFieldInfo {
            return PactCourseFieldInfo(
                    courseId = courseId,
                    isFavorite = isFavorite,
                    includeCourseImage = query.contains("=course_image"),
                    includeCurrentGradingPeriodScores = query.contains("=current_grading_period_scores"),
                    includeNeedsGradingCount = query.contains("=needs_grading_count"),
                    includeObservedUsers = query.contains("=observed_users"),
                    includeSections = query.contains("=sections"),
                    includeSyllabusBody = query.contains("=syllabus_body"),
                    includeTerm = query.contains("=term"),
                    includeTotalScores = query.contains("=total_scores")
            )
        }
    }
}

fun LambdaDslObject.populateTermFields() : LambdaDslObject {
    this
            .id("id")
            .stringType("name")
            .timestamp("start_at", PACT_TIMESTAMP_FORMAT)
            .timestamp("end_at", PACT_TIMESTAMP_FORMAT)

    return this
}

fun LambdaDslObject.populateCourseFields(fieldInfo: PactCourseFieldInfo = PactCourseFieldInfo(courseId = 1)) : LambdaDslObject {

    this
            .stringType("name")
            .id("id", fieldInfo.courseId)
            .stringType("original_name")
            .stringType("course_code")
            .booleanValue("is_favorite", fieldInfo.isFavorite)
            .timestamp("start_at", PACT_TIMESTAMP_FORMAT)
            .timestamp("end_at", PACT_TIMESTAMP_FORMAT)
            .booleanType("hide_final_grades")
            .booleanType("is_public")
            .stringMatcher("license", "private|cc_by_nc_nd|c_by_nc_sa|c_by_nc|cc_by_nd|cc_by_sa|cc_by|public_domain", "private")
            .booleanType("apply_assignment_group_weights")
            .booleanType("access_restricted_by_date")
            .stringType("image_download_url")
            .booleanType("has_weighted_grading_periods")
            .booleanType("has_grading_periods") // Optional?
            .stringMatcher("default_view","feed|wiki|modules|assignments|syllabus", "modules")
            .booleanType("restrict_enrollments_to_course_dates")
            .stringMatcher("workflow_state", "unpublished|available|completed|deleted", "available")

    //
    // Optional/configurable fields
    //

    if(fieldInfo.includeSyllabusBody) {
        this.stringType("syllabus_body")
    }

    if(fieldInfo.numEnrollments > 0)
    {
        val enrollmentFieldInfo = PactEnrollmentFieldInfo(
                includeTotalScoresFields = fieldInfo.includeTotalScores,
                includeCurrentGradingPeriodScoresFields = fieldInfo.includeCurrentGradingPeriodScores
                //TODO: UserId
        )
        this.array("enrollments") { enrollment ->
            repeat(fieldInfo.numEnrollments) { index ->
                enrollment.`object`() {
                    it.populateEnrollmentFields(enrollmentFieldInfo)
                }
            }
        }
    }

    if(fieldInfo.includeTerm) {
        this.`object`("term") {
            it.populateTermFields()
        }
    }

    if(fieldInfo.includeNeedsGradingCount) {
        this.numberType("needs_grading_count")
    }

    if(fieldInfo.includeSections) { // Assume one section
        this.array("sections") { sections ->
            sections.`object`() { section ->
                section.populateSectionFields(PactSectionFieldInfo(courseId = fieldInfo.courseId))
            }
        }
    }

    return this
}

fun assertCoursePopulated(description: String, course: Course, fieldInfo: PactCourseFieldInfo = PactCourseFieldInfo(courseId = 1)) {

    assertNotNull("$description + name", course.name)
    assertNotNull("$description + originalName", course.originalName)
    assertNotNull("$description + courseCode", course.courseCode)
    assertNotNull("$description + startAt", course.startAt)
    assertNotNull("$description + endAt", course.endAt)
    assertNotNull("$description + hideFinalGrades", course.hideFinalGrades)
    assertNotNull("$description + isPublic", course.isPublic)
    assertNotNull("$description + license", course.license)
    assertNotNull("$description + isApplyAssignmentGroupWeights", course.isApplyAssignmentGroupWeights)
    assertNotNull("$description + accessRestrictedByDate", course.accessRestrictedByDate)
    assertNotNull("$description + imageUrl", course.imageUrl)
    assertNotNull("$description + isWeightedGradingPeriods", course.isWeightedGradingPeriods)
    assertNotNull("$description + homePage", course.homePage)
    assertNotNull("$description + restrictEnrollmentsToCourseDate", course.restrictEnrollmentsToCourseDate)
    assertNotNull("$description + workflowState", course.workflowState)
    assertNotNull("$description + isFavorite", course.isFavorite)
    assertNotNull("$description + id", course.id)

    if(fieldInfo.includeSyllabusBody) {
        assertNotNull("$description + syllabusBody", course.syllabusBody)
    }

    if(fieldInfo.includeNeedsGradingCount) {
        assertNotNull("$description + needsGradingCount", course.needsGradingCount)
    }

    if(fieldInfo.numEnrollments > 0) {
        assertNotNull("$description + enrollments", course.enrollments)
        assertEquals("$description + enrollment count", fieldInfo.numEnrollments, course.enrollments!!.size)
    }

    if(fieldInfo.includeTerm) {
        assertNotNull("$description + term", course.term)
    }

    if(fieldInfo.includeSections) {
        assertNotNull("$description + sections", course.sections)
        assertEquals("$description + sections count", 1, course.sections.size)
    }
}