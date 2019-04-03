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
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.dataseeding.model.FavoriteApiModel
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CoursesTest {
    private val course = CoursesApi.createCourse()
    private val student = UserApi.createCanvasUser()

    @Before
    fun setUp() {
        EnrollmentsApi.enrollUserAsStudent(course.id, student.id)
    }

    @Test
    fun createCourse() {
        val course = CoursesApi.createCourse()
        assertThat(course, instanceOf(CourseApiModel::class.java))
        assertTrue(course.id >= 1)
        assertTrue(course.name.isNotEmpty())
        //assertFalse(course.favorite) -- Not found in CourseApiModel
        assertTrue(course.courseCode.isNotEmpty())
    }

    @Test
    fun addFavoriteCourse() {
        val favorite = CoursesApi.addCourseToFavorites( course.id, student.token)
        assertThat(favorite, instanceOf(FavoriteApiModel::class.java))
        assertEquals(course.id, favorite.contextId)
    }
}
