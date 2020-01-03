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
    val numEnrollments: Int = 0,
    val numSections: Int = 1,
    val hasTerm: Boolean = false,
    val needsGradingCount: Long? = null,
    val isFavorite: Boolean? = null,
    val courseId: Long? = null
)

fun LambdaDslObject.populateTermFields() : LambdaDslObject {
    this
            .id("id")
            .stringType("name")
            .timestamp("start_at", PACT_TIMESTAMP_FORMAT)
            .timestamp("end_at", PACT_TIMESTAMP_FORMAT)

    return this
}

fun LambdaDslObject.populateCourseFields(fieldInfo: PactCourseFieldInfo = PactCourseFieldInfo()) : LambdaDslObject {

    this
            .stringType("name")
            .stringType("original_name")
            .stringType("course_code")
            .timestamp("start_at", PACT_TIMESTAMP_FORMAT)
            .timestamp("end_at", PACT_TIMESTAMP_FORMAT)
            .stringType("syllabus_body")
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

    if(fieldInfo.isFavorite != null) {
        this.booleanValue("is_favorite", fieldInfo.isFavorite)
    }
    else {
        this.booleanType("is_favorite")
    }

    if(fieldInfo.courseId != null) {
        this.id("course_id", fieldInfo.courseId)
    }
    else {
        this.id("course_id")
    }

    if(fieldInfo.numEnrollments > 0)
    {
        this.array("enrollments") { enrollment ->
            repeat(fieldInfo.numEnrollments) { index ->
                enrollment.`object`() {
                    it.populateEnrollmentFields()
                }
            }
        }
    }

    if(fieldInfo.hasTerm) {
        this.`object`("term") {
            it.populateTermFields()
        }
    }

    if(fieldInfo.needsGradingCount != null) {
        this.numberValue("needs_grading_count", fieldInfo.needsGradingCount)
    }
    else {
        this.numberType("needs_grading_count")
    }

    if(fieldInfo.numSections > 0) {
        this.array("sections") { sections ->
            sections.`object`() { section ->
                section.populateSectionFields(PactSectionFieldInfo(courseId = fieldInfo.courseId))
            }
        }
    }

    return this
}

fun assertCoursePopulated(description: String, course: Course, fieldInfo: PactCourseFieldInfo = PactCourseFieldInfo()) {

    assertNotNull("$description + name", course.name)
    assertNotNull("$description + originalName", course.originalName)
    assertNotNull("$description + courseCode", course.courseCode)
    assertNotNull("$description + startAt", course.startAt)
    assertNotNull("$description + endAt", course.endAt)
    assertNotNull("$description + syllabusBody", course.syllabusBody)
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
    assertNotNull("$description + needsGradingCount", course.needsGradingCount)

    if(fieldInfo.numEnrollments > 0) {
        assertNotNull("$description + enrollments", course.enrollments)
        assertEquals("$description + enrollment count", fieldInfo.numEnrollments, course.enrollments!!.size)
    }

    if(fieldInfo.hasTerm) {
        assertNotNull("$description + term", course.term)
    }

    if(fieldInfo.numSections > 0) {
        assertNotNull("$description + sections", course.sections)
        assertEquals("$description + sections count", fieldInfo.numSections, course.sections.size)
    }
}