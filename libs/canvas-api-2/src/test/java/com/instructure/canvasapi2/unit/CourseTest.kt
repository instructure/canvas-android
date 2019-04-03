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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import org.junit.Assert.*
import org.junit.Test

class CourseTest {

    @Test
    fun isStudent_hasStudentEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Student)
        val enrollments = arrayListOf(enrollment)

        val course = Course(enrollments = enrollments)

        assertEquals(true, course.isStudent)
    }

    @Test
    fun isStudent_noStudentEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Teacher)
        val enrollments = arrayListOf(enrollment)

        val course = Course(enrollments = enrollments)

        assertEquals(false, course.isStudent)
    }

    @Test
    fun isStudent_noEnrollments() {
        val course = Course(enrollments = arrayListOf())

        assertEquals(false, course.isStudent)
    }

    @Test
    fun isStudent_nullEnrollments() {
        val course = Course(enrollments = null)

        assertEquals(false, course.isStudent)
    }

    @Test
    fun isTeacher_hasTeacherEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Teacher)
        val enrollments = arrayListOf(enrollment)

        val course = Course(enrollments = enrollments)

        assertEquals(true, course.isTeacher)
    }

    @Test
    fun isTeacher_noTeacherEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Student)
        val enrollments = arrayListOf(enrollment)

        val course = Course(enrollments = enrollments)

        assertEquals(false, course.isTeacher)
    }

    @Test
    fun isTeacher_noEnrollments() {
        val course = Course(enrollments = arrayListOf())

        assertEquals(false, course.isTeacher)
    }

    @Test
    fun isTeacher_nullEnrollments() {
        val course = Course(enrollments = null)

        assertEquals(false, course.isTeacher)
    }

    @Test
    fun isTA_hasTaEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Ta)
        val enrollments = arrayListOf(enrollment)

        val course = Course(enrollments = enrollments)

        assertEquals(true, course.isTA)
    }

    @Test
    fun isTA_noTaEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Student)
        val enrollments = arrayListOf(enrollment)

        val course = Course(enrollments = enrollments)

        assertEquals(false, course.isTA)
    }

    @Test
    fun isTA_noEnrollments() {
        val course = Course(enrollments = arrayListOf())

        assertEquals(false, course.isTA)
    }

    @Test
    fun isTA_nullEnrollments() {
        val course = Course(enrollments = null)

        assertEquals(false, course.isTA)
    }

    @Test
    fun isObserver_hasObserverEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Observer)
        val enrollments = arrayListOf(enrollment)

        val course = Course(enrollments = enrollments)

        assertEquals(true, course.isObserver)
    }

    @Test
    fun isObserver_noObserverEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Student)
        val enrollments = arrayListOf(enrollment)

        val course = Course(enrollments = enrollments)

        assertEquals(false, course.isObserver)
    }

    @Test
    fun isObserver_noEnrollments() {
        val course = Course(enrollments = arrayListOf())

        assertEquals(false, course.isObserver)
    }

    @Test
    fun isObserver_nullEnrollments() {
        val course = Course(enrollments = null)

        assertEquals(false, course.isObserver)
    }

    @Test
    fun licenseToAPIString_all() {
        for (license in Course.License.values()) {
            assertNotEquals("Expected valid API license string for Course.LICENSE." + license.name, "", license.apiString)
        }
    }

    @Test
    fun licenseToPrettyPrint_all() {
        for (license in Course.License.values()) {
            assertNotEquals("Expected valid pretty print string for Course.LICENSE." + license.name, "", license.prettyString)
        }
    }

    @Test
    fun getLicense_PRIVATE_COPYRIGHTED() {
        val course = Course(license = Course.License.PRIVATE_COPYRIGHTED)
        assertEquals(Course.License.PRIVATE_COPYRIGHTED, course.license)
    }

    @Test
    fun getLicense_CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE() {
        val course = Course(license = Course.License.CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE)
        assertEquals(Course.License.CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE, course.license)
    }

    @Test
    fun getLicense_CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE() {
        val course = Course(license = Course.License.CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE)
        assertEquals(Course.License.CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE, course.license)
    }

    @Test
    fun getLicense_CC_ATTRIBUTION_NON_COMMERCIAL() {
        val course = Course(license = Course.License.CC_ATTRIBUTION_NON_COMMERCIAL)
        assertEquals(Course.License.CC_ATTRIBUTION_NON_COMMERCIAL, course.license)
    }

    @Test
    fun getLicense_CC_ATTRIBUTION_NO_DERIVATIVE() {
        val course = Course(license = Course.License.CC_ATTRIBUTION_NO_DERIVATIVE)
        assertEquals(Course.License.CC_ATTRIBUTION_NO_DERIVATIVE, course.license)
    }

    @Test
    fun getLicense_CC_ATTRIBUTION_SHARE_ALIKE() {
        val course = Course(license = Course.License.CC_ATTRIBUTION_SHARE_ALIKE)
        assertEquals(Course.License.CC_ATTRIBUTION_SHARE_ALIKE, course.license)
    }

    @Test
    fun getLicense_CC_ATTRIBUTION() {
        val course = Course(license = Course.License.CC_ATTRIBUTION)
        assertEquals(Course.License.CC_ATTRIBUTION, course.license)
    }

    @Test
    fun getLicense_PUBLIC_DOMAIN() {
        val course = Course(license = Course.License.PUBLIC_DOMAIN)
        assertEquals(Course.License.PUBLIC_DOMAIN, course.license)
    }

    @Test
    fun getLicense_empty() {
        val course = Course()
        assertEquals(Course.License.PRIVATE_COPYRIGHTED, course.license)
    }

    @Test
    fun getLicense_all() {
        for (license in Course.License.values()) {
            val course = Course(license = license)
            assertEquals(license, course.license)
        }
    }

    @Test
    fun isCourseGradeLocked_hideFinal() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Student)
        val enrollments = arrayListOf(enrollment)
        val course = Course(hideFinalGrades = true, enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.isLocked)
    }

    @Test
    fun isCourseGradeLocked_hideAllGradingPeriods() {
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                currentGradingPeriodId = 0,
                multipleGradingPeriodsEnabled = true,
                totalsForAllGradingPeriodsOption = false)
        val enrollments = arrayListOf(enrollment)
        val course = Course(hasGradingPeriods = true, enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.isLocked)
    }

    @Test
    fun courseHasNoCurrentGrade() {
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                computedCurrentGrade = "",
                computedCurrentScore = null)
        val enrollments = arrayListOf(enrollment)
        val course = Course(enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.noCurrentGrade)
    }

    @Test
    fun courseHasNoFinalGrade() {
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                computedFinalGrade = "",
                computedFinalScore = null)
        val enrollments = arrayListOf(enrollment)
        val course = Course(enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.noFinalGrade)
    }

    @Test
    fun courseHasCurrentGrade() {
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                computedCurrentGrade = "A",
                computedCurrentScore = 95.0)
        val enrollments = arrayListOf(enrollment)
        val course = Course(enrollments = enrollments)

        assertFalse(course.getCourseGrade(false)!!.noCurrentGrade)
    }

    @Test
    fun courseHasFinalGrade() {
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                computedFinalGrade = "A",
                computedFinalScore = 95.0)
        val enrollments = arrayListOf(enrollment)
        val course = Course(enrollments = enrollments)

        assertFalse(course.getCourseGrade(false)!!.noFinalGrade)
    }

    @Test
    fun courseGrade_currentGradeMGP() {
        val currentGrade = "A"
        val finalGrade = "C"
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                currentGradingPeriodId = 27,
                multipleGradingPeriodsEnabled = true,
                currentPeriodComputedCurrentGrade = currentGrade,
                currentPeriodComputedFinalGrade = finalGrade
        )
        val enrollments = arrayListOf(enrollment)
        val course = Course(hasGradingPeriods = true, enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.currentGrade == currentGrade)
    }

    @Test
    fun courseGrade_currentScoreMGP() {
        val currentScore = 96.0
        val finalScore = 47.0
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                currentGradingPeriodId = 27,
                multipleGradingPeriodsEnabled = true,
                currentPeriodComputedCurrentScore = currentScore,
                currentPeriodComputedFinalScore = finalScore)
        val enrollments = arrayListOf(enrollment)
        val course = Course(hasGradingPeriods = true, enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.currentScore == currentScore)
    }

    @Test
    fun courseGrade_currentGradeNonMGP() {
        val currentGrade = "A"
        val finalGrade = "C"
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                currentGradingPeriodId = 27,
                computedCurrentGrade = currentGrade,
                computedFinalGrade = finalGrade)
        val enrollments = arrayListOf(enrollment)
        val course = Course(enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.currentGrade == currentGrade)
    }

    @Test
    fun courseGrade_currentScoreNonMGP() {
        val currentScore = 96.0
        val finalScore = 47.0
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                currentGradingPeriodId = 27,
                computedCurrentScore = currentScore,
                computedFinalScore = finalScore
        )
        val enrollments = arrayListOf(enrollment)
        val course = Course(enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.currentScore == currentScore)
    }

    @Test
    fun courseGrade_finalGradeMGP() {
        val currentGrade = "A"
        val finalGrade = "C"
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                currentGradingPeriodId = 27,
                multipleGradingPeriodsEnabled = true,
                currentPeriodComputedFinalGrade = finalGrade,
                currentPeriodComputedCurrentGrade = currentGrade)
        val enrollments = arrayListOf(enrollment)

        val course = Course(hasGradingPeriods = true, enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.finalGrade == finalGrade)
    }

    @Test
    fun courseGrade_finalScoreMGP() {
        val currentScore = 96.0
        val finalScore = 47.0
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                currentGradingPeriodId = 27,
                multipleGradingPeriodsEnabled = true,
                currentPeriodComputedFinalScore = finalScore,
                currentPeriodComputedCurrentScore = currentScore)

        val enrollments = arrayListOf(enrollment)
        val course = Course(hasGradingPeriods = true, enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.finalScore == finalScore)
    }

    @Test
    fun courseGrade_finalGradeNonMGP() {
        val currentGrade = "A"
        val finalGrade = "C"
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                computedFinalGrade = finalGrade,
                computedCurrentGrade = currentGrade
        )
        val enrollments = arrayListOf(enrollment)
        val course = Course(enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.finalGrade == finalGrade)
    }

    @Test
    fun courseGrade_finalScoreNonMGP() {
        val currentScore = 96.0
        val finalScore = 47.0
        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                computedFinalScore = finalScore,
                computedCurrentScore = currentScore
        )
        val enrollments = arrayListOf(enrollment)
        val course = Course(enrollments = enrollments)

        assertTrue(course.getCourseGrade(false)!!.finalScore == finalScore)
    }

}