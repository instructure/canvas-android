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

import com.instructure.canvasapi2.models.Enrollment
import io.pactfoundation.consumer.dsl.LambdaDslObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

private val enrollmentTypes = "StudentEnrollment|TeacherEnrollment|TaEnrollment|DesignerEnrollment|ObserverEnrollment"

private fun LambdaDslObject.populateGradesObject() : LambdaDslObject {
    this
            .stringType("html_url")
            .numberType("current_score", "final_score")
            .stringType("current_grade")
            .stringType("final_grade")

    return this
}

data class PactEnrollmentFieldInfo(
        val userId: Long? = null,
        val includeTotalScoresFields: Boolean = false,
        val includeCurrentGradingPeriodScoresFields: Boolean = false
)

fun LambdaDslObject.populateEnrollmentFields(fieldInfo: PactEnrollmentFieldInfo = PactEnrollmentFieldInfo()) : LambdaDslObject {
    this
            .numberType("id", 0)
            .stringMatcher("type", enrollmentTypes, "StudentEnrollment")
            .stringMatcher("role", enrollmentTypes, "StudentEnrollment")
            .numberType("course_id")
            .numberType("course_section_id")
            .stringType("enrollment_state")
            .numberType("associated_user_id") // TODO: Can/should be null for non-observer?
            .timestamp("last_activity_at", PACT_TIMESTAMP_FORMAT)
            .booleanType("limit_privileges_to_course_section")
            .`object`("grades") { gradesObject ->
                gradesObject.populateGradesObject()
            }

    // TODO: Punting on these for now
//    @SerializedName("observed_user")
//    val observedUser: User? = null,
//    var user: User? = null

    if(fieldInfo.includeTotalScoresFields) {
        this
                .numberType("computed_current_score")
                .numberType("computed_final_score")
                .stringType("computed_current_grade")
                .stringType("computed_final_grade")
    }

    if(fieldInfo.includeCurrentGradingPeriodScoresFields) {
        this
                .booleanType("multiple_grading_periods_enabled")
                .booleanType("totals_for_all_grading_periods_option")
                .numberType("current_period_computed_current_score")
                .numberType("current_period_computed_final_score")
                .stringType("current_period_computed_current_grade")
                .stringType("current_period_computed_final_grade")
                .numberType("current_grading_period_id")
                .stringType("current_grading_period_title")
    }

    if(fieldInfo.userId != null) {
        this.id("user_id", fieldInfo.userId)
    }
    else {
        this.id("user_id")
    }

    return this
}

fun assertEnrollmentPopulated(
        description: String,
        enrollment: Enrollment,
        fieldInfo: PactEnrollmentFieldInfo = PactEnrollmentFieldInfo()
)
{
    assertNotNull("$description + id", enrollment.id)
    assertNotNull("$description + type", enrollment.type)
    assertNotNull("$description + role", enrollment.role)
    assertNotNull("$description + courseId", enrollment.courseId)
    assertNotNull("$description + courseSectionId", enrollment.courseSectionId)
    assertNotNull("$description + enrollmentState", enrollment.enrollmentState)
    assertNotNull("$description + userId", enrollment.userId)
    assertNotNull("$description + associatedUserId", enrollment.associatedUserId)
    assertNotNull("$description + lastActivityAt", enrollment.lastActivityAt)
    assertNotNull("$description + limitPrivilegesToCourseSection", enrollment.limitPrivilegesToCourseSection)

    if(fieldInfo.includeTotalScoresFields) {
        assertNotNull("$description + computedCurrentScore", enrollment.computedCurrentScore)
        assertNotNull("$description + computedFinalScore", enrollment.computedFinalScore)
        assertNotNull("$description + computedCurrentGrade", enrollment.computedCurrentGrade)
        assertNotNull("$description + computedFinalGrade", enrollment.computedFinalGrade)
    }

    if(fieldInfo.includeCurrentGradingPeriodScoresFields) {
        assertNotNull("$description + multipleGradingPeriodsEnabled", enrollment.multipleGradingPeriodsEnabled)
        assertNotNull("$description + totalsForAllGradingPeriodsOption", enrollment.totalsForAllGradingPeriodsOption)
        assertNotNull("$description + currentPeriodComputedCurrentGrade", enrollment.currentPeriodComputedCurrentGrade)
        assertNotNull("$description + currentPeriodComputedCurrentScore", enrollment.currentPeriodComputedCurrentScore)
        assertNotNull("$description + currentPeriodComputedFinalGrade", enrollment.currentPeriodComputedFinalGrade)
        assertNotNull("$description + currentPeriodComputedFinalScore", enrollment.currentPeriodComputedFinalScore)
        assertNotNull("$description + currentGradingPeriodId", enrollment.currentGradingPeriodId)
        assertNotNull("$description + currentGradingPeriodTitle", enrollment.currentGradingPeriodTitle)
    }

    if(fieldInfo.userId != null) {
        assertEquals("$description + userId", fieldInfo.userId, enrollment.userId)
    }
}