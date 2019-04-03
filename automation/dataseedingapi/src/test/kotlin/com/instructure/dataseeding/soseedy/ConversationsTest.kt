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

import com.instructure.dataseeding.api.ConversationsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.ConversationApiModel
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ConversationsTest {
    private val course = CoursesApi.createCourse()
    private val student = UserApi.createCanvasUser()
    private val teacher = UserApi.createCanvasUser()

    @Before
    fun setUp() {
        EnrollmentsApi.enrollUserAsStudent(course.id, student.id)
        EnrollmentsApi.enrollUserAsTeacher(course.id, teacher.id)
    }

    @Test
    fun createConversation() {
        val conversation = ConversationsApi.createConversation(
                token = student.token,
                recipients = listOf("course_" + course.id)
        )
        assertNotNull(conversation)
        assertTrue(conversation.size > 0)
        assertThat(conversation[0], instanceOf(ConversationApiModel::class.java))
        assertTrue(conversation[0].id >= 1)
        assertTrue(conversation[0].subject.isNotEmpty())
    }
}
