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
import com.instructure.canvasapi2.models.Grades
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

private fun assertGradesPopulated(description: String, grades: Grades) {
    assertNotNull("$description + html_url", grades.htmlUrl)
    assertNotNull("$description + html_url", grades.currentScore)
    assertNotNull("$description + html_url", grades.currentGrade)
    assertNotNull("$description + html_url", grades.finalScore)
    assertNotNull("$description + html_url", grades.finalGrade)
}

data class PactEnrollmentFieldInfo(
        val userId: Long? = null,
        val courseId: Long? = null,
        val includeTotalScoresFields: Boolean = false,
        val includeCurrentGradingPeriodScoresFields: Boolean = false,
        val isObserver: Boolean = false,

        // These are different between "full" enrollment data and enrollment data embedded in a Course
        val populateFully: Boolean = false // Add a number of extra fields that only appear when an enrollment is not nested
)

fun LambdaDslObject.populateEnrollmentFields(fieldInfo: PactEnrollmentFieldInfo = PactEnrollmentFieldInfo()) : LambdaDslObject {
    this
            .stringMatcher("type", enrollmentTypes, "StudentEnrollment") // May not be the same as "role", despite API docs
            .stringMatcher("role", enrollmentTypes, "StudentEnrollment")
            .stringType("enrollment_state")
            .booleanType("limit_privileges_to_course_section")

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

    if(fieldInfo.isObserver) {
        this.id("associated_user_id")
    }

    if(fieldInfo.populateFully) {
        this.id("id")
        this.id("course_section_id")
        if(fieldInfo.courseId != null) {
            this.id("course_id", fieldInfo.courseId)
        }
        else {
            this.id("course_id")
        }
        this.timestamp("last_activity_at", PACT_TIMESTAMP_FORMAT)
        this.`object`("grades") { grades ->
            grades.populateGradesObject()
        }
        this.`object`("user") {user ->
            user.populateUserFields()
        }
        if(fieldInfo.isObserver) {
            this.`object`("observed_user"){ user ->
                user.populateUserFields()
            }
        }
    }
    return this
}

fun assertEnrollmentPopulated(
        description: String,
        enrollment: Enrollment,
        fieldInfo: PactEnrollmentFieldInfo = PactEnrollmentFieldInfo()
)
{
    assertNotNull("$description + type", enrollment.type)
    assertNotNull("$description + role", enrollment.role)
    assertNotNull("$description + enrollmentState", enrollment.enrollmentState)
    assertNotNull("$description + userId", enrollment.userId)
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

    if(fieldInfo.courseId != null) {
        assertNotNull("$description + courseId", enrollment.courseId)
        assertEquals("$description + courseId", fieldInfo.courseId, enrollment.courseId)
    }

    if(fieldInfo.userId != null) {
        assertEquals("$description + userId", fieldInfo.userId, enrollment.userId)
    }

    if(fieldInfo.isObserver) {
        assertNotNull("$description + associatedUserId", enrollment.associatedUserId)
    }

    if(fieldInfo.populateFully) {
        assertNotNull("$description + id", enrollment.id)
        assertNotNull("$description + courseSectionId", enrollment.courseSectionId)
        assertNotNull("$description + courseId", enrollment.courseId)
        if(fieldInfo.courseId != null) {
            assertEquals("$description + courseId", fieldInfo.courseId, enrollment.courseId)
        }
        assertNotNull("$description + lastActivityAt", enrollment.lastActivityAt)
        assertNotNull("$description + grades", enrollment.grades)
        assertGradesPopulated("$description + grades", enrollment.grades!!)
        assertNotNull("$description + user", enrollment.user)
        assertUserPopulated("$description + user", enrollment.user!!)
        if(fieldInfo.isObserver) {
            assertNotNull("$description + observedUser", enrollment.observedUser)
            assertUserPopulated("$description + observedUser", enrollment.observedUser!!)
        }
    }
}