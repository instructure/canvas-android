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

package com.instructure.student.test.util

import android.content.Context
import android.os.Bundle
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.student.features.discussion.details.DiscussionDetailsFragment
import com.instructure.student.features.files.details.FileDetailsFragment
import com.instructure.student.features.modules.progression.ModuleQuizDecider
import com.instructure.student.features.modules.progression.NotAvailableOfflineFragment
import com.instructure.student.features.modules.util.ModuleUtility
import com.instructure.student.features.pages.details.PageDetailsFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.util.Const
import io.mockk.mockk
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModuleUtilityTest : TestCase() {

    private val context = mockk<Context>(relaxed = true)

    @Test
    fun testGetFragment_file() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/assignments/123456789"
        val moduleItem = ModuleItem(
                id = 4567,
                type = "File",
                url = url
        )

        var moduleObject: ModuleObject? = ModuleObject(
                id = 1234
        )

        val course = Course()
        val expectedUrl = "courses/222/assignments/123456789"

        var expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putString(Const.FILE_URL, expectedUrl)
        expectedBundle.putInt(Const.FILE_ID, 0)
        expectedBundle.putLong(Const.ITEM_ID, moduleItem.id)
        expectedBundle.putParcelable(com.instructure.pandautils.utils.Const.MODULE_OBJECT, moduleObject)


        var parentFragment = callGetFragment(moduleItem, course, moduleObject)
        assertNotNull(parentFragment)
        assertEquals(FileDetailsFragment::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())

        // Test module object is null
        moduleObject = null
        expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putString(Const.FILE_URL, expectedUrl)
        expectedBundle.putInt(Const.FILE_ID, 0)
        parentFragment = callGetFragment(moduleItem, course, moduleObject)
        assertNotNull(parentFragment)
        assertEquals(FileDetailsFragment::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())

    }

    @Test
    fun testGetFragment_fileOfflineNotAvailable() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/assignments/123456789"
        val moduleItem = ModuleItem(
            id = 4567,
            type = "File",
            url = url
        )

        val moduleObject: ModuleObject = ModuleObject(
            id = 1234
        )

        val course = Course()

        val filDetailsFragment = callGetFragment(moduleItem, course, moduleObject, isOnline = false)
        assertNotNull(filDetailsFragment)
        assertEquals(NotAvailableOfflineFragment::class.java, filDetailsFragment!!.javaClass)
    }

    @Test
    fun testGetFragment_page() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/pages/hello-world"
        val moduleItem = ModuleItem(
            id = 4567,
            type = "Page",
            url = url,
            title = "hello-world"
        )

        val course = Course()
        val expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putString(PageDetailsFragment.PAGE_NAME, "hello-world")
        expectedBundle.putBoolean(PageDetailsFragment.NAVIGATED_FROM_MODULES, false)

        val parentFragment = callGetFragment(moduleItem, course, null)
        assertNotNull(parentFragment)
        assertEquals(PageDetailsFragment::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
    }

    @Test
    fun testGetFragment_assignment() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/assignments/123456789"
        val moduleItem = ModuleItem(
                id = 4567,
                type = "Assignment",
                url = url
        )

        val course = Course()
        val expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putLong(com.instructure.pandautils.utils.Const.COURSE_ID, course.id)
        expectedBundle.putLong(Const.ASSIGNMENT_ID, 123456789)

        val parentFragment = callGetFragment(moduleItem, course, null)
        assertNotNull(parentFragment)
        assertEquals(AssignmentDetailsFragment::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
    }

    @Test
    fun testGetFragment_assignment_offlineSynced() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/assignments/123456789"
        val moduleItem = ModuleItem(
            id = 4567,
            type = "Assignment",
            url = url
        )

        val course = Course()
        val expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putLong(com.instructure.pandautils.utils.Const.COURSE_ID, course.id)
        expectedBundle.putLong(Const.ASSIGNMENT_ID, 123456789)

        val parentFragment = callGetFragment(moduleItem, course, null, isOnline = false, tabs = setOf(Tab.ASSIGNMENTS_ID))
        assertNotNull(parentFragment)
        assertEquals(AssignmentDetailsFragment::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
    }

    @Test
    fun testGetFragment_assignment_offlineNotSynced() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/assignments/123456789"
        val moduleItem = ModuleItem(
            id = 4567,
            type = "Assignment",
            url = url
        )

        val course = Course()

        val fragment = callGetFragment(moduleItem, course, null, isOnline = false)
        assertNotNull(fragment)
        assertEquals(NotAvailableOfflineFragment::class.java, fragment!!.javaClass)
    }

    @Test
    fun testGetFragment_assignmentShardId() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/assignments/12345~6789"
        val moduleItem = ModuleItem(
                id = 4567,
                type = "Assignment",
                url = url
        )

        val course = Course()
        val expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putLong(com.instructure.pandautils.utils.Const.COURSE_ID, course.id)
        expectedBundle.putLong(Const.ASSIGNMENT_ID, 123450000000006789)

        val parentFragment = callGetFragment(moduleItem, course, null)
        assertNotNull(parentFragment)
        assertEquals(AssignmentDetailsFragment::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
    }

    @Test
    fun testGetFragment_assignmentSubmissionShardId() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/assignments/12345~6789/submissions"
        val moduleItem = ModuleItem(
                id = 4567,
                type = "Assignment",
                url = url
        )

        val course = Course()
        val expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putLong(com.instructure.pandautils.utils.Const.COURSE_ID, course.id)
        expectedBundle.putLong(Const.ASSIGNMENT_ID, 123450000000006789)

        val parentFragment = callGetFragment(moduleItem, course, null)
        assertNotNull(parentFragment)
        assertEquals(AssignmentDetailsFragment::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
    }

    @Test
    fun testGetFragment_externalurl_externaltool() {
        val url = "https://instructure.com"
        val moduleItem = ModuleItem(
                id = 4567,
                type = "ExternalUrl",
                title = "Hello",
                htmlUrl = url

        )

        val course = Course()
        val expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putString(Const.INTERNAL_URL, "https://instructure.com?display=borderless")
        expectedBundle.putString(Const.ACTION_BAR_TITLE, "Hello")
        expectedBundle.putBoolean(Const.AUTHENTICATE, true)
        expectedBundle.putBoolean(com.instructure.pandautils.utils.Const.IS_EXTERNAL_TOOL, true)
        expectedBundle.putBoolean(com.instructure.pandautils.utils.Const.IS_UNSUPPORTED_FEATURE, true)

        var parentFragment = callGetFragment(moduleItem, course, null)
        assertNotNull(parentFragment)
        assertEquals(InternalWebviewFragment::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
        // test external tool type

        val moduleItem2 = moduleItem.copy(type = "ExternalTool")
        parentFragment = callGetFragment(moduleItem2, course, null)
        assertNotNull(parentFragment)
        assertEquals(InternalWebviewFragment::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
    }

    @Test
    fun testGetFragment_externalTool_offline() {
        val url = "https://instructure.com"
        val moduleItem = ModuleItem(
            id = 4567,
            type = "ExternalUrl",
            title = "Hello",
            htmlUrl = url

        )

        val course = Course()

        val fragment = callGetFragment(moduleItem, course, null, isOnline = false)
        assertNotNull(fragment)
        assertEquals(NotAvailableOfflineFragment::class.java, fragment!!.javaClass)
    }

    @Test
    fun testGetFragment_subheader() {
        val moduleItem = ModuleItem(
                type = "SubHeader"
        )

        val course = Course()
        val parentFragment = callGetFragment(moduleItem, course, null)
        TestCase.assertNull(parentFragment)
    }

    @Test
    fun testGetFragment_quiz() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/quizzes/123456789"
        val htmlUrl = "https://mobile.canvas.net/courses/222/quizzes/123456789"
        val apiUrl = "courses/222/quizzes/123456789"

        val moduleItem = ModuleItem(
                id = 4567,
                type = "Quiz",
                url = url,
                htmlUrl = htmlUrl,
                contentId = 55
        )

        val course = Course()
        val expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putString(Const.URL, htmlUrl)
        expectedBundle.putString(Const.API_URL, apiUrl)
        expectedBundle.putLong(Const.ID, 55)

        val parentFragment = callGetFragment(moduleItem, course, null)
        assertNotNull(parentFragment)
        assertEquals(ModuleQuizDecider::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
    }

    @Test
    fun testGetFragment_discussion() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/discussion_topics/123456789"
        val moduleItem = ModuleItem(
                id = 4567,
                type = "Discussion",
                url = url
        )

        val course = Course()
        val expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putLong(DiscussionDetailsFragment.DISCUSSION_TOPIC_HEADER_ID, 123456789)
        val parentFragment = callGetFragment(moduleItem, course, null)
        assertNotNull(parentFragment)
        assertEquals(DiscussionDetailsWebViewFragment::class.java, parentFragment!!.javaClass)
        assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
    }

    @Test
    fun testGetFragment_discussion_offline() {
        val url = "https://mobile.canvas.net/api/v1/courses/222/discussion_topics/123456789"
        val moduleItem = ModuleItem(
            id = 4567,
            type = "Discussion",
            url = url
        )

        val course = Course()
        val fragment = callGetFragment(moduleItem, course, null, isOnline = false)
        assertNotNull(fragment)
        assertEquals(NotAvailableOfflineFragment::class.java, fragment!!.javaClass)
    }

    private fun callGetFragment(moduleItem: ModuleItem, course: Course, moduleObject: ModuleObject?, isOnline: Boolean = true, tabs: Set<String> = emptySet(), files: List<Long> = emptyList()): Fragment? {
        return ModuleUtility.getFragment(moduleItem, course, moduleObject, false, isOnline, tabs, files, context)
    }
}
