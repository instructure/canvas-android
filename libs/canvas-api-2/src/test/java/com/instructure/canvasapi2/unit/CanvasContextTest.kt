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
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.models.User

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals

class CanvasContextTest {

    //region typeIs
    @Test
    fun typeIsGroup_TestFalse() {
        val course = Course()

        assert(!CanvasContext.Type.isGroup(course))
    }

    @Test
    fun typeIsGroup_TestTrue() {
        val group = Group()

        assert(CanvasContext.Type.isGroup(group))
    }

    @Test
    fun typeIsCourse_TestFalse() {
        val group = Group()

        assert(!CanvasContext.Type.isCourse(group))
    }

    @Test
    fun typeIsCourse_TestTrue() {
        val course = Course()

        assert(CanvasContext.Type.isCourse(course))
    }

    @Test
    fun typeIsUser_TestFalse() {
        val course = Course()

        assert(!CanvasContext.Type.isUser(course))
    }

    @Test
    fun typeIsUser_TestTrue() {
        val user = User()

        assert(CanvasContext.Type.isUser(user))
    }

    @Test
    fun typeIsSection_TestFalse() {
        val course = Course()

        assert(!CanvasContext.Type.isSection(course))
    }

    @Test
    fun typeIsSection_TestTrue() {
        val section = Section()

        assert(CanvasContext.Type.isSection(section))
    }
    //endregion

    @Test
    fun canCreateDiscussions_TestTrue() {
        val course = Course()
        val canvasContextPermission = CanvasContextPermission(canCreateDiscussionTopic = true)
        course.permissions = canvasContextPermission

        assert(course.canCreateDiscussion())
    }

    @Test
    fun canCreateDiscussions_TestFalse() {
        val course = Course()
        val canvasContextPermission = CanvasContextPermission(canCreateDiscussionTopic = false)
        course.permissions = canvasContextPermission

        assert(!course.canCreateDiscussion())
    }

    //region equalsTest
    @Test
    fun equals_TestNull() {
        val course1 = Course()
        val course2: Course? = null

        assertNotEquals(course1, course2)
    }

    @Test
    fun equals_TestFalse() {
        val course = Course()
        val group = Group()

        assertNotEquals(course, group)
    }

    @Test
    fun equals_TestTrue() {
        val course1 = Course()
        val course2 = Course()

        assertEquals(course1, course2)
    }
    //endregion

    @Test
    fun getSecondaryName_TestCourse() {
        val courseCode = "Hodor"
        val course = Course(courseCode = courseCode)

        assertEquals(courseCode, course.secondaryName)
    }

    @Test
    fun getSecondaryName_TestGroup() {
        val name = "Hodor"
        val group = Group(name = name)

        assertEquals(name, group.secondaryName)
    }

    //region toAPIString
    @Test
    fun toAPIString_TestGroup() {
        val group = Group(id = 1234)

        assertEquals("/groups/1234", group.toAPIString())
    }

    @Test
    fun toAPIString_TestCourse() {
        val course = Course(id = 1234)

        assertEquals("/courses/1234", course.toAPIString())
    }

    @Test
    fun toAPIString_TestSection() {
        val section = Section(id = 1234)

        assertEquals("/sections/1234", section.toAPIString())
    }

    @Test
    fun toAPIString_TestUsers() {
        val user = User(id = 1234)

        assertEquals("/users/1234", user.toAPIString())
    }

    @Test
    fun toAPIString_TestSelf() {
        val user = User(id = 0)

        assertEquals("/users/self", user.toAPIString())
    }
    //endregion

    //region getContextId
    @Test
    fun getContextId_TestCourse() {
        val course = Course(id = 1234)

        assertEquals("course_1234", course.contextId)
    }

    @Test
    fun getContextId_TestGroup() {
        val group = Group(id = 1234)

        assertEquals("group_1234", group.contextId)
    }

    @Test
    fun getContextId_TestUser() {
        val user = User(id = 1234)

        assertEquals("user_1234", user.contextId)
    }
    //endregion

    //region fromContextCode
    @Test
    fun fromContextCode_TestNull1() {
        assertEquals(null, CanvasContext.fromContextCode(""))
    }

    @Test
    fun fromContextCode_TestNull2() {
        assertEquals(null, CanvasContext.fromContextCode("gr"))
    }

    @Test
    fun fromContextCode_TestCourse() {
        val course = Course(id = 1234)

        assertEquals(course, CanvasContext.fromContextCode(course.contextId))
    }

    @Test
    fun fromContextCode_TestGroup() {
        val group = Group(id = 1234)

        assertEquals(group, CanvasContext.fromContextCode(group.contextId))
    }

    @Test
    fun fromContextCode_TestUser() {
        val user = User(id = 1234)

        assertEquals(user, CanvasContext.fromContextCode(user.contextId))
    }
    //endregion

    @Test
    fun getApiContext_TestCourses() {
        val course = Course()

        assertEquals("courses", CanvasContext.getApiContext(course))
    }

    @Test
    fun getApiContext_TestGroups() {
        val group = Group()

        assertEquals("groups", CanvasContext.getApiContext(group))
    }

    //region getHomePageID
    @Test
    fun getHomePageID_TestNull() {
        val course = Course(homePage = null)

        assertEquals(Tab.NOTIFICATIONS_ID, course.homePageID)
    }

    @Test
    fun getHomePageID_TestHomeFeed() {
        val course = Course(homePage = Course.HomePage.HOME_FEED)

        assertEquals(Tab.NOTIFICATIONS_ID, course.homePageID)
    }

    @Test
    fun getHomePageID_TestHomeSyllabus() {
        val course = Course(homePage = Course.HomePage.HOME_SYLLABUS)

        assertEquals(Tab.SYLLABUS_ID, course.homePageID)
    }

    @Test
    fun getHomePageID_TestHomeWiki() {
        val course = Course(homePage = Course.HomePage.HOME_WIKI)

        assertEquals(Tab.FRONT_PAGE_ID, course.homePageID)
    }

    @Test
    fun getHomePageID_TestHomeAssignment() {
        val course = Course(homePage = Course.HomePage.HOME_ASSIGNMENTS)

        assertEquals(Tab.ASSIGNMENTS_ID, course.homePageID)
    }

    @Test
    fun getHomePageID_TestHomeModules() {
        val course = Course(homePage = Course.HomePage.HOME_MODULES)

        assertEquals(Tab.MODULES_ID, course.homePageID)
    }
    //endregion

    //region getGenericContext
    @Test
    fun getGenericContext_TestUser() {
        val id = 1234L
        val name = "hodor"
        val user = User(id = id, name = name)

        assertEquals(user, CanvasContext.getGenericContext(CanvasContext.Type.USER, id, name))
    }

    @Test
    fun getGenericContext_TestCourse() {
        val id = 1234L
        val name = "hodor"
        val course = Course(id = id, name = name)

        assertEquals(course, CanvasContext.getGenericContext(CanvasContext.Type.COURSE, id, name))
    }

    @Test
    fun getGenericContext_TestGroup() {
        val id = 1234L
        val name = "hodor"
        val group = Group(id = id, name = name)

        assertEquals(group, CanvasContext.getGenericContext(CanvasContext.Type.GROUP, id, name))
    }

    @Test
    fun getGenericContext_TestSection() {
        val id: Long = 1234
        val name = "hodor"
        val section = Section(id = id, name = name)

        assertEquals(section, CanvasContext.getGenericContext(CanvasContext.Type.SECTION, id, name))
    }

    @Test
    fun makeContextId_TestCourse() {
        val id: Long = 1234
        val type = CanvasContext.Type.COURSE
        assertEquals("course_1234", CanvasContext.makeContextId(type, id))
    }

    @Test
    fun makeContextId_TestGroup() {
        val id: Long = 1234
        val type = CanvasContext.Type.GROUP
        assertEquals("group_1234", CanvasContext.makeContextId(type, id))
    }

    @Test
    fun makeContextId_TestUser() {
        val id: Long = 1234
        val type = CanvasContext.Type.USER
        assertEquals("user_1234", CanvasContext.makeContextId(type, id))
    }
}