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

import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Grades
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

// A regex enumerating all possible enrollment types
private val enrollmentTypes = "StudentEnrollment|TeacherEnrollment|TaEnrollment|DesignerEnrollment|ObserverEnrollment|student"

/**
 * Information about how to set up Enrollment user fields.
 * If [userId] is non-null, we will expect that value for the userId field; otherwise,
 * we will expect a generic value.
 * If [courseId] is non-null, we will expect that value for the courseId field; otherwise,
 * we will expect a generic value.  Note that the courseId field also requires
 * [populateFully] to be true in order to be populated.
 * If [includeTotalScoresFields] is true, the computed_current_score, computed_current_grade,
 * computed_final_score and computed_final_grade fields will be populated.
 * If [includeCurrentGradingPeriodScoresFields] is true, then a number of fields pertaining to
 * scores and other info from the current period will be populated.
 * If [isObserver] is true, then the associated_user_id and observed_user field will be
 * populated.  Note that the observed_user field also requires [populateFully] to be true
 * in order to be populated.
 * If [populateFully] is true, then a number of additional fields are populated.  Typically,
 * [populateFully] is true when directly retrieving Enrollment objects, and false for Enrollment
 * objects that are embedded in other retrieved objects.
 */
data class PactEnrollmentFieldConfig(
        val userId: Long? = null,
        val courseId: Long? = null,
        val includeTotalScoresFields: Boolean = false,
        val includeCurrentGradingPeriodScoresFields: Boolean = false,
        val includeGrades: Boolean = false,
        val isObserver: Boolean = false,
        val populateFully: Boolean = false
)

/**
 * Populate an Enrollment object in a Pact specification, based on PactEnrollmentFieldConfig settings.
 */
fun LambdaDslObject.populateEnrollmentFields(fieldConfig: PactEnrollmentFieldConfig = PactEnrollmentFieldConfig()) : LambdaDslObject {
    this
            .stringMatcher("role", enrollmentTypes, "StudentEnrollment")
            .stringMatcher("type", enrollmentTypes, "StudentEnrollment")
            .stringType("enrollment_state")
            .booleanType("limit_privileges_to_course_section")

    if(fieldConfig.includeTotalScoresFields) {
        this
                .numberType("computed_current_score")
                .numberType("computed_final_score")
                .stringType("computed_current_grade")
                .stringType("computed_final_grade")
    }

    if(fieldConfig.includeCurrentGradingPeriodScoresFields) {
        this
                .booleanType("multiple_grading_periods_enabled")
                .booleanType("totals_for_all_grading_periods_option")
                .numberType("current_period_computed_current_score", 100)
                .numberType("current_period_computed_final_score", 100)
                .stringType("current_period_computed_current_grade")
                .stringType("current_period_computed_final_grade")
                .id("current_grading_period_id")
                .stringType("current_grading_period_title")
    }

    if(fieldConfig.userId != null) {
        this.id("user_id", fieldConfig.userId)
    }
    else {
        this.id("user_id")
    }

    if(fieldConfig.includeGrades) {
        this.`object`("grades") { grades ->
            grades.populateGradesObject()
        }
    }

    if(fieldConfig.isObserver) {
        this.id("associated_user_id")
    }

    if(fieldConfig.populateFully) {
        this.id("id")
        this.id("course_section_id")
        if(fieldConfig.courseId != null) {
            this.id("course_id", fieldConfig.courseId)
        }
        else {
            this.id("course_id")
        }
        this.stringMatcher("last_activity_at", PACT_TIMESTAMP_REGEX, "2020-01-23T00:00:00Z")
        //this.timestamp("last_activity_at", PACT_TIMESTAMP_FORMAT)
        this.`object`("user") {user ->
            user.populateUserFields()
        }
        if(fieldConfig.isObserver) {
            this.`object`("observed_user"){ user ->
                user.populateUserFields()
            }
        }
    }
    return this
}

/**
 * Assert that an Enrollment object in a response has been populated correctly.
 */
fun assertEnrollmentPopulated(
        description: String,
        enrollment: Enrollment,
        fieldConfig: PactEnrollmentFieldConfig = PactEnrollmentFieldConfig()
)
{
    assertNotNull("$description + type", enrollment.type)
    assertNotNull("$description + role", enrollment.role)
    assertNotNull("$description + enrollmentState", enrollment.enrollmentState)
    assertNotNull("$description + userId", enrollment.userId)
    assertNotNull("$description + limitPrivilegesToCourseSection", enrollment.limitPrivilegesToCourseSection)

    if(fieldConfig.includeTotalScoresFields) {
        assertNotNull("$description + computedCurrentScore", enrollment.computedCurrentScore)
        assertNotNull("$description + computedFinalScore", enrollment.computedFinalScore)
        assertNotNull("$description + computedCurrentGrade", enrollment.computedCurrentGrade)
        assertNotNull("$description + computedFinalGrade", enrollment.computedFinalGrade)
    }

    if(fieldConfig.includeCurrentGradingPeriodScoresFields) {
        assertNotNull("$description + multipleGradingPeriodsEnabled", enrollment.multipleGradingPeriodsEnabled)
        assertNotNull("$description + totalsForAllGradingPeriodsOption", enrollment.totalsForAllGradingPeriodsOption)
        assertNotNull("$description + currentPeriodComputedCurrentGrade", enrollment.currentPeriodComputedCurrentGrade)
        assertNotNull("$description + currentPeriodComputedCurrentScore", enrollment.currentPeriodComputedCurrentScore)
        assertNotNull("$description + currentPeriodComputedFinalGrade", enrollment.currentPeriodComputedFinalGrade)
        assertNotNull("$description + currentPeriodComputedFinalScore", enrollment.currentPeriodComputedFinalScore)
        assertNotNull("$description + currentGradingPeriodId", enrollment.currentGradingPeriodId)
        assertNotNull("$description + currentGradingPeriodTitle", enrollment.currentGradingPeriodTitle)
    }

    if(fieldConfig.courseId != null) {
        assertNotNull("$description + courseId", enrollment.courseId)
        assertEquals("$description + courseId", fieldConfig.courseId, enrollment.courseId)
    }

    if(fieldConfig.userId != null) {
        assertEquals("$description + userId", fieldConfig.userId, enrollment.userId)
    }

    if(fieldConfig.isObserver) {
        assertNotNull("$description + associatedUserId", enrollment.associatedUserId)
    }

    if(fieldConfig.includeGrades) {
        assertNotNull("$description + grades", enrollment.grades)
        assertGradesPopulated("$description + grades", enrollment.grades!!)
    }

    if(fieldConfig.populateFully) {
        assertNotNull("$description + id", enrollment.id)
        assertNotNull("$description + courseSectionId", enrollment.courseSectionId)
        assertNotNull("$description + courseId", enrollment.courseId)
        if(fieldConfig.courseId != null) {
            assertEquals("$description + courseId", fieldConfig.courseId, enrollment.courseId)
        }
        assertNotNull("$description + lastActivityAt", enrollment.lastActivityAt)
        assertNotNull("$description + user", enrollment.user)
        assertUserPopulated("$description + user", enrollment.user!!)
        if(fieldConfig.isObserver) {
            assertNotNull("$description + observedUser", enrollment.observedUser)
            assertUserPopulated("$description + observedUser", enrollment.observedUser!!)
        }
    }
}