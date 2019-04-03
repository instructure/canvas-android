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


package com.instructure.soseedy.producers

import com.instructure.dataseeding.model.EnrollmentTypes.TEACHER_ENROLLMENT
import com.instructure.soseedy.Enrollment
import com.instructure.soseedy.producers.Pipelines.coursesPipeline
import com.instructure.soseedy.producers.Pipelines.usersPipeline
import kotlinx.coroutines.experimental.runBlocking
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test

class EnrollmentProducerTest {

    @Test
    fun produceEnrollment() = runBlocking {
        val user = usersPipeline.receive()
        val course = coursesPipeline.receive()
        val role = TEACHER_ENROLLMENT
        val enrollment = EnrollmentProducer.produceEnrollment(user, course, role).receive()
        assertThat(enrollment, instanceOf(Enrollment::class.java))
        assertEquals(user.id, enrollment.userId)
        assertEquals(course.id, enrollment.courseId)
        assertEquals(role, enrollment.role)
    }
}
