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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserTest {

    private lateinit var user1: User
    private lateinit var user2: User

    @Before
    fun setup() {
        user1 = User()
        user2 = User()
    }

    @Test
    fun equals_TestSelfTrue() {
        assertEquals(true, user1 == user1)
    }

    @Test
    fun equals_TestFalse2() {
        val course = Course()
        assertEquals(false, user1 as CanvasContext == course)
    }

    @Test
    fun equals_TestFalse3() {
        val u1 = user1.copy(id = 1234)
        val u2 = user2.copy(4321)

        assertEquals(false, u1 == u2)
    }

    @Test
    fun equals_TestTrueIdMatch() {
        val u1 = user1.copy(id = 1234)
        val u2 = user2.copy(id = 1234)

        assertEquals(true, u1 == u2)
    }

    @Test
    fun getEnrollmentsHash_Test1() {
        val enrollment1 = Enrollment(courseId = 1234, role = Enrollment.EnrollmentType.Student)
        val enrollment2 = Enrollment(courseId = 1234, role = Enrollment.EnrollmentType.Teacher)
        val enrollmentList = arrayListOf(enrollment1, enrollment2)

        val u1 = user1.copy(enrollments = enrollmentList)

        assertEquals("student", u1.enrollmentsHash["1234"]!![0]!!.apiTypeString)
    }

    @Test
    fun getEnrollmentsHash_Test2() {
        val enrollment1 = Enrollment(courseId = 1234, role = Enrollment.EnrollmentType.Student)
        val enrollment2 = Enrollment(courseId = 1234, role = Enrollment.EnrollmentType.Teacher)
        val enrollmentList = arrayListOf(enrollment1, enrollment2)

        val u1 = user1.copy(enrollments = enrollmentList)

        assertEquals("teacher", u1.enrollmentsHash["1234"]!![1]!!.apiTypeString)
    }
}
