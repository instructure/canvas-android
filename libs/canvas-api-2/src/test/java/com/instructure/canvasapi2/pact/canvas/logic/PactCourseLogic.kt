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
package com.instructure.canvasapi2.pact.canvas.logic

import com.instructure.canvasapi2.models.Course
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/**
 * Information about how to set up Course object fields.
 * You must specify a [courseId].
 * The [numEnrollments] setting will determine the number of embedded Enrollment objects in the Course object.
 * If [isFavorite] is true, this course is marked as a favorite course.
 * The includeXxx values correspond directly to include[]=xxx showing up in the request query.
 */
data class PactCourseFieldConfig (
    val courseId: Long, // Mandatory
    val isFavorite: Boolean? = null,
    val numEnrollments: Int = 1,
    val includeCourseImage: Boolean = false,
    val includeCurrentGradingPeriodScores: Boolean = false,
    val includeNeedsGradingCount: Boolean = false,
    val includeObservedUsers: Boolean = false,
    val includeSections: Boolean = false,
    val includeSyllabusBody : Boolean = false,
    val includeTerm: Boolean = false,
    val includeTotalScores: Boolean = false,
    val includePermissions: Boolean = false,
    val includeFavorites: Boolean = false
) {
    companion object {
        /***
         * Construct a PactCourseFieldConfig object based on the query string being passed with the request.
         */
        fun fromQueryString(courseId: Long, isFavorite: Boolean? = null, query: String) : PactCourseFieldConfig {
            val result = PactCourseFieldConfig(
                    courseId = courseId,
                    isFavorite = isFavorite,
                    //includeCourseImage = query.contains("=course_image"),
                    includeCurrentGradingPeriodScores = query.contains("=current_grading_period_scores"),
                    includeNeedsGradingCount = query.contains("=needs_grading_count"),
                    includeObservedUsers = query.contains("=observed_users"),
                    includeSections = query.contains("=sections"),
                    includeSyllabusBody = query.contains("=syllabus_body"),
                    includeTerm = query.contains("=term"),
                    includeTotalScores = query.contains("=total_scores"),
                    includePermissions = query.contains("=permissions"),
                    includeFavorites = query.contains("=favorites")
            )

            return result
        }
    }
}


/**
 * Populate a Course object in a Pact specification, based on PactCourseFieldConfig settings.
 *
 * Note that access_restricted_by_date, needs_grading_count and image_download_url
 * are apparently not pass back by the API, documentation to the contrary.
 */
fun LambdaDslObject.populateCourseFields(fieldConfig: PactCourseFieldConfig = PactCourseFieldConfig(courseId = 1)) : LambdaDslObject {

    this
            .stringType("name")
            .id("id", fieldConfig.courseId)
            .stringType("course_code")
            .stringMatcher("start_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .stringMatcher("end_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
            .booleanType("hide_final_grades")
            .booleanType("is_public")
            .stringMatcher("license", "private|cc_by_nc_nd|c_by_nc_sa|c_by_nc|cc_by_nd|cc_by_sa|cc_by|public_domain", "private")
            .booleanType("apply_assignment_group_weights")
            //.booleanType("access_restricted_by_date")
            //.stringType("image_download_url")
            .stringMatcher("default_view","feed|wiki|modules|assignments|syllabus", "modules")
            .booleanType("restrict_enrollments_to_course_dates")
            .stringMatcher("workflow_state", "unpublished|available|completed|deleted", "available")

    //
    // Optional/configurable fields
    //

    if(fieldConfig.includeFavorites) {
        if(fieldConfig.isFavorite != null) {
            this.booleanValue("is_favorite", fieldConfig.isFavorite)
        }
        else {
            this.booleanType("is_favorite")
        }
    }

    if(fieldConfig.includePermissions) {
        this.`object`("permissions") { permissions ->
            permissions.booleanType("create_discussion_topic")
            permissions.booleanType("create_announcement")
        }

    }
    if(fieldConfig.includeSyllabusBody) {
        this.stringType("syllabus_body")
    }

    if(fieldConfig.includeCurrentGradingPeriodScores) {
        this
                .booleanType("has_weighted_grading_periods")
                .booleanType("has_grading_periods")
    }

    if(fieldConfig.numEnrollments > 0)
    {
        val enrollmentFieldInfo = PactEnrollmentFieldConfig(
                includeTotalScoresFields = fieldConfig.includeTotalScores,
                includeCurrentGradingPeriodScoresFields = fieldConfig.includeCurrentGradingPeriodScores,
                courseId = fieldConfig.courseId
                //TODO: UserId
        )
        this.array("enrollments") { enrollment ->
            repeat(fieldConfig.numEnrollments) { index ->
                enrollment.`object`() {
                    it.populateEnrollmentFields(enrollmentFieldInfo)
                }
            }
        }
    }

    if(fieldConfig.includeTerm) {
        this.`object`("term") {
            it.populateTermFields()
        }
    }

    if(fieldConfig.includeNeedsGradingCount) {
        //this.numberType("needs_grading_count")
    }

    if(fieldConfig.includeSections) { // Assume one section
        this.array("sections") { sections ->
            sections.`object`() { section ->
                section.populateSectionFields()
            }
        }
    }

    return this
}

/**
 * Assert that a course object in a response has been properly populated, based on
 * PactCourseFieldConfig settings.
 */
fun assertCoursePopulated(description: String, course: Course, fieldConfig: PactCourseFieldConfig = PactCourseFieldConfig(courseId = 1)) {

    assertNotNull("$description + name", course.name)
    assertNotNull("$description + courseCode", course.courseCode)
    assertNotNull("$description + startAt", course.startAt)
    assertNotNull("$description + endAt", course.endAt)
    assertNotNull("$description + hideFinalGrades", course.hideFinalGrades)
    assertNotNull("$description + isPublic", course.isPublic)
    assertNotNull("$description + license", course.license)
    assertNotNull("$description + isApplyAssignmentGroupWeights", course.isApplyAssignmentGroupWeights)
    //assertNotNull("$description + accessRestrictedByDate", course.accessRestrictedByDate)
    //assertNotNull("$description + imageUrl", course.imageUrl)
    assertNotNull("$description + homePage", course.homePage)
    assertNotNull("$description + restrictEnrollmentsToCourseDate", course.restrictEnrollmentsToCourseDate)
    assertNotNull("$description + workflowState", course.workflowState)
    assertNotNull("$description + id", course.id)

    if(fieldConfig.includeFavorites) {
        assertNotNull("$description + isFavorite", course.isFavorite)
        if(fieldConfig.isFavorite != null) {
            assertEquals("$description + isFavorite", fieldConfig.isFavorite, course.isFavorite)
        }
    }

    if(fieldConfig.includeCurrentGradingPeriodScores) {
        assertNotNull("$description + hasGradingPeriods", course.hasGradingPeriods)
        assertNotNull("$description + isWeightedGradingPeriods", course.isWeightedGradingPeriods)
    }

    if(fieldConfig.includePermissions) {
        assertNotNull("$description + permissions", course.permissions)
        assertNotNull("$description + permissions.canCreateAnnouncement", course.permissions!!.canCreateAnnouncement)
        assertNotNull("$description + permissions.canCreateDiscussionTopic", course.permissions!!.canCreateDiscussionTopic)
    }

    if(fieldConfig.includeSyllabusBody) {
        assertNotNull("$description + syllabusBody", course.syllabusBody)
    }

    if(fieldConfig.includeNeedsGradingCount) {
        //assertNotNull("$description + needsGradingCount", course.needsGradingCount)
    }

    if(fieldConfig.numEnrollments > 0) {
        assertNotNull("$description + enrollments", course.enrollments)
        assertEquals("$description + enrollment count", fieldConfig.numEnrollments, course.enrollments!!.size)
    }

    if(fieldConfig.includeTerm) {
        assertNotNull("$description + term", course.term)
        assertTermPopulated("$description + term", course.term!!)
    }

    if(fieldConfig.includeSections) {
        assertNotNull("$description + sections", course.sections)
        assertEquals("$description + sections count", 1, course.sections.size)
        assertSectionPopulated("$description + section",course.sections[0])
    }
}