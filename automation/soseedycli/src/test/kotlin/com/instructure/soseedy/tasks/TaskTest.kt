//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


package com.instructure.soseedy.tasks

import com.instructure.dataseeding.model.EnrollmentTypes.OBSERVER_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.STUDENT_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.TA_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.TEACHER_ENROLLMENT
import com.instructure.soseedy.utils.InvalidEnrollmentException
import org.junit.Assert.*

import org.junit.Test

class TaskTest {

    @Test
    fun validRoles_student() {
        assertTrue(SeedCourses.validRoles.contains("student"))
    }

    @Test
    fun validRoles_teacher() {
        assertTrue(SeedCourses.validRoles.contains("teacher"))
    }

    @Test
    fun validRoles_ta() {
        assertTrue(SeedCourses.validRoles.contains("ta"))
    }

    @Test
    fun validRoles_observer() {
        assertTrue(SeedCourses.validRoles.contains("observer"))
    }

    @Test
    fun checkEnrollmentType_student() {
        assertEquals(STUDENT_ENROLLMENT, SeedCourses.checkEnrollmentType("student"))
    }

    @Test
    fun checkEnrollmentType_teacher() {
        assertEquals(TEACHER_ENROLLMENT, SeedCourses.checkEnrollmentType("teacher"))
    }

    @Test
    fun checkEnrollmentType_ta() {
        assertEquals(TA_ENROLLMENT, SeedCourses.checkEnrollmentType("ta"))
    }

    @Test
    fun checkEnrollmentType_observer() {
        assertEquals(OBSERVER_ENROLLMENT, SeedCourses.checkEnrollmentType("observer"))
    }

    @Test
    fun checkEnrollmentType_upperCase() {
        assertEquals(STUDENT_ENROLLMENT, SeedCourses.checkEnrollmentType("STUDENT"))
    }

    @Test(expected = InvalidEnrollmentException::class)
    fun checkEnrollmentType_throwsException() {
        SeedCourses.checkEnrollmentType("invalid")
    }

    @Test
    fun help() {
        assertFalse(SeedCourses.help)
        SeedCourses.help = true
        assertTrue(SeedCourses.help)
    }
}
