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



package com.instructure.dataseeding.soseedy

import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.SectionsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.EnrollmentApiModel
import com.instructure.dataseeding.model.EnrollmentTypes.STUDENT_ENROLLMENT
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Test

class EnrollmentsTest {
    private val course = CoursesApi.createCourse()
    private val section = SectionsApi.createSection(courseId = course.id)
    private val user = UserApi.createCanvasUser()

    @Test
    fun enrollUserInCourse() {
        val enrollment = EnrollmentsApi.enrollUserAsStudent(course.id,user.id)
        assertThat(enrollment, instanceOf(EnrollmentApiModel::class.java))
        assertEquals(course.id, enrollment.courseId)
        assertTrue(enrollment.sectionId >= 1)
        assertEquals(user.id, enrollment.userId)
        assertEquals(STUDENT_ENROLLMENT, enrollment.type)
        assertEquals(STUDENT_ENROLLMENT, enrollment.role)
    }

    @Test
    fun enrollUserInSection() {
        val enrollment = EnrollmentsApi.enrollUserInSectionAsStudent(section.id, user.id)
        assertThat(enrollment, instanceOf(EnrollmentApiModel::class.java))
        assertEquals(course.id, enrollment.courseId)
        assertEquals(section.id, enrollment.sectionId)
        assertEquals(user.id, enrollment.userId)
        assertEquals(STUDENT_ENROLLMENT, enrollment.type)
        assertEquals(STUDENT_ENROLLMENT, enrollment.role)
    }
}
