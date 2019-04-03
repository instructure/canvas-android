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
import com.instructure.dataseeding.api.LatePolicyApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.LatePolicy
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LatePolicyTest {
    private val course = CoursesApi.createCourse()
    private val teacher = UserApi.createCanvasUser()

    @Before
    fun setUp() {
        EnrollmentsApi.enrollUserAsTeacher(course.id, teacher.id)
    }

    @Test
    fun createLatePolicy() {
        val latePolicy = LatePolicy(
                missingSubmissionDeductionEnabled = true,
                missingSubmissionDeduction = 5,
                lateSubmissionDeductionEnabled = true,
                lateSubmissionDeduction = 10,
                lateSubmissionInterval = "day",
                lateSubmissionMinimumPercentEnabled = true,
                lateSubmissionMinimumPercent = 50
        )
        val response = LatePolicyApi.createLatePolicy(
                courseId = course.id,
                latePolicy = latePolicy,
                teacherToken = teacher.token
        )
        assertTrue(response.latePolicy.missingSubmissionDeductionEnabled)
        assertEquals(response.latePolicy.missingSubmissionDeduction, 5)
        assertTrue(response.latePolicy.lateSubmissionDeductionEnabled)
        assertEquals(response.latePolicy.lateSubmissionDeduction, 10)
        assertEquals(response.latePolicy.lateSubmissionInterval, "day")
        assertTrue(response.latePolicy.lateSubmissionMinimumPercentEnabled)
        assertEquals(response.latePolicy.lateSubmissionMinimumPercent,50)
    }
}
