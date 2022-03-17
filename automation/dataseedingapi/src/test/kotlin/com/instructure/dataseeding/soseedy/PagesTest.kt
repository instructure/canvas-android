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
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.PageApiModel
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Locale

class PagesTest {
    private val course = CoursesApi.createCourse()
    private val teacher = UserApi.createCanvasUser()

    @Before
    fun setUp() {
        EnrollmentsApi.enrollUserAsTeacher(course.id, teacher.id)
    }

    @Test
    fun createCoursePage() {
        val page = PagesApi.createCoursePage(
                courseId = course.id,
                published = false,
                frontPage = false,
                token = teacher.token
        )
        assertThat(page, instanceOf(PageApiModel::class.java))
        assertTrue(page.title.isNotEmpty())
        assertEquals(page.title.replace(" ", "-").replace("'","").lowercase(Locale.getDefault()), page.url);
        assertTrue(page.body.isNotEmpty())
        assertFalse(page.published)
        assertFalse(page.frontPage)
    }
}
