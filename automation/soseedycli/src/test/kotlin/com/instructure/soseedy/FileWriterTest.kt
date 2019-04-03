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


package com.instructure.soseedy

import com.instructure.dataseeding.model.EnrollmentTypes.TEACHER_ENROLLMENT
import com.instructure.soseedy.utils.CommandLineTest
import com.instructure.soseedy.utils.FileWriter
import org.junit.Assert.assertTrue
import org.junit.Test

class FileWriterTest : CommandLineTest() {

    private val testUser = CanvasUser.newBuilder()
            .setId(3)
            .setLoginId("testLoginId")
            .setPassword("testPassword")
            .setToken("testToken")
            .build()

    private val testCourse = Course.newBuilder()
            .setId(7)
            .setName("testCourse")
            .setCourseCode("testCode")
            .build()

    private val testEnrollment = Enrollment.newBuilder()
            .setUserId(testUser.id)
            .setCourseId(testCourse.id)
            .setType(TEACHER_ENROLLMENT)
            .build()

    @Test
    fun writeUser() {
        FileWriter.write(testUser)
        val file = testCsvFile().readText()
        assertTrue(file.contains(testUser.loginId))
        assertTrue(file.contains(testUser.password))
        assertTrue(file.contains(testUser.token))
    }

    @Test
    fun writeUserCourseEnrollment() {
        FileWriter.write(listOf(testUser), listOf(testCourse), listOf(testEnrollment))
        val file = testCsvFile().readText()
        assertTrue(file.contains(testUser.loginId))
        assertTrue(file.contains(testUser.password))
        assertTrue(file.contains(testUser.token))
        assertTrue(file.contains(testCourse.id.toString()))
        assertTrue(file.contains(testCourse.name))
        assertTrue(file.contains(testCourse.courseCode))
        assertTrue(file.contains(testEnrollment.role))
    }
}
