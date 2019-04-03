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

import com.instructure.dataseeding.api.SeedApi
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test

class GeneralTest {

    @Test
    fun seedData() {
        val teacherCount = 1
        val studentCount = 2
        val courseCount = 2
        val favoriteCourseCount = 1
        val announcementsCount = 1
        val discussionCount = 2
        val gradingPeriod = true
        val request = SeedApi.SeedDataRequest (
                teachers = teacherCount,
                students = studentCount,
                courses = courseCount,
                favoriteCourses = favoriteCourseCount,
                announcements = announcementsCount,
                discussions = discussionCount,
                gradingPeriods = gradingPeriod
        )
        val response = SeedApi.seedData(request)
        assertThat(response, instanceOf(SeedApi.SeededDataApiModel::class.java))
        assertEquals(courseCount, response.coursesList.size)
        assertEquals(teacherCount * courseCount, response.teachersList.size)
        assertEquals(studentCount * courseCount, response.studentsList.size)
        assertEquals(favoriteCourseCount, response.favoriteCoursesList.size)
        assertEquals((teacherCount + studentCount) * courseCount, response.enrollmentsList.size)
        assertEquals(announcementsCount, response.announcementsList.size)
        assertEquals(discussionCount, response.discussionsList.size)
    }

    @Test
    fun seedParentData() {
        val parentCount = 1
        val courseCount = 1
        val studentCount = 3
        val enrollmentCount = (courseCount * studentCount) * 2
        val request = SeedApi.SeedParentDataRequest(
                parents = parentCount,
                courses = courseCount,
                students = studentCount
        )
        val response = SeedApi.seedParentData(request)
        assertEquals(parentCount, response.parentsList.size)
        assertEquals(courseCount, response.coursesList.size)
        assertEquals(studentCount, response.studentsList.size)
        assertEquals(enrollmentCount, response.enrollmentsList.size)
    }

}
