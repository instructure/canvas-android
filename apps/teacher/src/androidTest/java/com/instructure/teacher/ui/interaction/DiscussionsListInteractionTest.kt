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
package com.instructure.teacher.ui.interaction

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockcanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.teacher.ui.utils.TeacherTest
import com.instructure.teacher.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class DiscussionsListInteractionTest : TeacherTest() {

    lateinit var course: Course

    @Test
    override fun displaysPageObjects() {
        getToDiscussionsListPage()
        discussionsListPage.assertPageObjects()
    }

    @Test
    fun assertHasDiscussion() {
        val discussion = getToDiscussionsListPage().courseDiscussionTopicHeaders[course.id]!![0]
        discussionsListPage.assertHasDiscussion(discussion)
    }

    @Test
    fun searchesDiscussions() {
        val discussions = getToDiscussionsListPage(discussionCount = 3).courseDiscussionTopicHeaders[course.id]!!
        val searchDiscussion = discussions[2]
        discussionsListPage.assertDiscussionCount(discussions.size)
        discussionsListPage.searchable.clickOnSearchButton()
        discussionsListPage.searchable.typeToSearchBar(searchDiscussion.title!!.take(searchDiscussion.title!!.length / 2))
        discussionsListPage.assertDiscussionCount(1)
        discussionsListPage.assertHasDiscussion(searchDiscussion)
    }

    private fun getToDiscussionsListPage(discussionCount: Int = 1): MockCanvas {
        val data = MockCanvas.init(
                teacherCount = 1,
                courseCount = 1,
                favoriteCourseCount = 1
        )
        val teacher = data.teachers[0]
        course = data.courses.values.first()

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission(send_messages_all = true, send_messages = true)
        )

        val discussionsTab = Tab(position = 2, label = "Discussions", visibility = "public", tabId = Tab.DISCUSSIONS_ID)
        data.courseTabs[course.id]!! += discussionsTab

        repeat(discussionCount) {data.addDiscussionTopicToCourse(course, teacher)}

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.openCourse(course)
        courseBrowserPage.openDiscussionsTab()
        return data
    }
}
