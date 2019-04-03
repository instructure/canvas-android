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
import com.instructure.dataseeding.api.GroupsApi
import com.instructure.dataseeding.api.UserApi
import com.instructure.dataseeding.model.ContextTypes.COURSE
import com.instructure.dataseeding.model.GroupApiModel
import com.instructure.dataseeding.model.GroupCategoryApiModel
import com.instructure.dataseeding.model.GroupMembershipApiModel
import com.instructure.dataseeding.model.WorkflowStates.ACCEPTED
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GroupsTest {
    private val course = CoursesApi.createCourse()
    private val teacher = UserApi.createCanvasUser()
    private val student = UserApi.createCanvasUser()

    @Before
    fun setUp() {
        EnrollmentsApi.enrollUserAsTeacher(course.id, teacher.id)
        EnrollmentsApi.enrollUserAsStudent(course.id, student.id)
    }

    @Test
    fun createCourseGroupCategory() {
        val category = GroupsApi.createCourseGroupCategory(
                courseId = course.id,
                teacherToken = teacher.token
        )
        assertThat(category, instanceOf(GroupCategoryApiModel::class.java))
        assertTrue(category.id >= 1)
        assertTrue(category.name.isNotEmpty())
        assertEquals(COURSE, category.contextType)
    }

    @Test
    fun createGroup() {
        val category = GroupsApi.createCourseGroupCategory(
                courseId = course.id,
                teacherToken = teacher.token
        )
        val group = GroupsApi.createGroup(
                groupCategoryId = category.id,
                teacherToken = teacher.token
        )
        assertThat(group, instanceOf(GroupApiModel::class.java))
        assertTrue(group.id >= 1)
        assertTrue(group.name.isNotEmpty())
        assertTrue(group.description.isNotEmpty())
        assertEquals(COURSE, group.contextType)
        assertEquals(course.id, group.courseId)
        assertEquals(category.id, group.groupCategoryId)
    }

    @Test
    fun createGroupMembership() {
        val category = GroupsApi.createCourseGroupCategory(
                courseId = course.id,
                teacherToken = teacher.token
        )
        val group = GroupsApi.createGroup(
                groupCategoryId = category.id,
                teacherToken = teacher.token
        )
        val membership = GroupsApi.createGroupMembership(
                groupId = group.id,
                userId = student.id,
                teacherToken = teacher.token
        )
        assertThat(membership, instanceOf(GroupMembershipApiModel::class.java))
        assertTrue(membership.id >= 1)
        assertEquals(group.id, membership.groupId)
        assertEquals(student.id, membership.userId)
        assertEquals(ACCEPTED, membership.workflowState)
    }
}
