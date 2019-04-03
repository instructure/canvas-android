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

import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Grades
import org.junit.Assert.assertEquals
import org.junit.Test

class EnrollmentTest {

    @Test
    fun isStudent_StudentEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Student)
        assertEquals(true, enrollment.isStudent)
    }

    @Test
    fun isTeacher_TeacherEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Teacher)
        assertEquals(true, enrollment.isTeacher)
    }

    @Test
    fun isObserver_ObserverEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Observer)
        assertEquals(true, enrollment.isObserver)
    }

    @Test
    fun isTa_TaEnrollment() {
        val enrollment = Enrollment(type = Enrollment.EnrollmentType.Ta)
        assertEquals(true, enrollment.isTA)
    }

    @Test
    fun getCurrentScore() {
        val grades = Grades()
        grades.currentScore = 95.0
        val enrollment = Enrollment(computedCurrentScore = 50.0, grades = grades)

        assertEquals(95.0, enrollment.currentScore!!, 0.001)
    }

    @Test
    fun getCurrentScore_NullGrades() {
        val enrollment = Enrollment(computedCurrentScore = 50.0, grades = null)

        assertEquals(50.0, enrollment.currentScore!!, 0.001)
    }

    @Test
    fun getFinalScore() {
        val grades = Grades()
        grades.finalScore = 95.0
        val enrollment = Enrollment(computedFinalScore = 50.0, grades = grades)

        assertEquals(95.0, enrollment.finalScore!!, 0.001)
    }

    @Test
    fun getFinalScore_NullGrades() {
        val enrollment = Enrollment(computedFinalScore = 50.0, grades = null)

        assertEquals(50.0, enrollment.finalScore!!, 0.001)
    }

    @Test
    fun getCurrentGrade() {
        val grades = Grades()
        grades.currentGrade = "B+"

        val enrollment = Enrollment(computedCurrentGrade= "C-", grades = grades)

        assertEquals("B+", enrollment.currentGrade)
    }

    @Test
    fun getCurrentGrade_NullGrades() {
        val enrollment = Enrollment(computedCurrentGrade = "C-", grades = null)

        assertEquals("C-", enrollment.currentGrade)
    }

    @Test
    fun getFinalGrade() {
        val grades = Grades()
        grades.finalGrade = "B+"
        val enrollment = Enrollment(computedFinalGrade = "C-", grades = grades)

        assertEquals("B+", enrollment.finalGrade)
    }

    @Test
    fun getFinalGrade_NullGrades() {
        val enrollment = Enrollment(computedFinalGrade = "C-", grades = null)

        assertEquals("C-", enrollment.finalGrade)
    }

}
