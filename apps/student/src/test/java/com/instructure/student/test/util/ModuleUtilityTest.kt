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

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.student.fragment.*
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.util.Const
import com.instructure.student.util.ModuleUtility
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModuleUtilityTest : TestCase() {

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
        expectedBundle.putLong(Const.ITEM_ID, moduleItem.id)
        expectedBundle.putParcelable(com.instructure.pandautils.utils.Const.MODULE_OBJECT, moduleObject)


        var parentFragment = callGetFragment(moduleItem, course, moduleObject)
        TestCase.assertNotNull(parentFragment)
        TestCase.assertEquals(FileDetailsFragment::class.java, parentFragment!!.javaClass)
        TestCase.assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())

        // Test module object is null
        moduleObject = null
        expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putString(Const.FILE_URL, expectedUrl)
        parentFragment = callGetFragment(moduleItem, course, moduleObject)
        TestCase.assertNotNull(parentFragment)
        TestCase.assertEquals(FileDetailsFragment::class.java, parentFragment!!.javaClass)
        TestCase.assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())

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

        val parentFragment = callGetFragment(moduleItem, course, null)
        TestCase.assertNotNull(parentFragment)
        TestCase.assertEquals(PageDetailsFragment::class.java, parentFragment!!.javaClass)
        TestCase.assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
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
        expectedBundle.putLong(Const.ASSIGNMENT_ID, 123456789)

        val parentFragment = callGetFragment(moduleItem, course, null)
        TestCase.assertNotNull(parentFragment)
        TestCase.assertEquals(AssignmentDetailsFragment::class.java, parentFragment!!.javaClass)
        TestCase.assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
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
        expectedBundle.putLong(Const.ASSIGNMENT_ID, 123450000000006789)

        val parentFragment = callGetFragment(moduleItem, course, null)
        TestCase.assertNotNull(parentFragment)
        TestCase.assertEquals(AssignmentDetailsFragment::class.java, parentFragment!!.javaClass)
        TestCase.assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
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
        expectedBundle.putLong(Const.ASSIGNMENT_ID, 123450000000006789)

        val parentFragment = callGetFragment(moduleItem, course, null)
        TestCase.assertNotNull(parentFragment)
        TestCase.assertEquals(AssignmentDetailsFragment::class.java, parentFragment!!.javaClass)
        TestCase.assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
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
        TestCase.assertNotNull(parentFragment)
        TestCase.assertEquals(InternalWebviewFragment::class.java, parentFragment!!.javaClass)
        TestCase.assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
        // test external tool type

        val moduleItem2 = moduleItem.copy(type = "ExternalTool")
        parentFragment = callGetFragment(moduleItem2, course, null)
        TestCase.assertNotNull(parentFragment)
        TestCase.assertEquals(InternalWebviewFragment::class.java, parentFragment!!.javaClass)
        TestCase.assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
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
                htmlUrl = htmlUrl
        )

        val course = Course()
        val expectedBundle = Bundle()
        expectedBundle.putParcelable(Const.CANVAS_CONTEXT, course)
        expectedBundle.putString(Const.URL, htmlUrl)
        expectedBundle.putString(Const.API_URL, apiUrl)

        val parentFragment = callGetFragment(moduleItem, course, null)
        TestCase.assertNotNull(parentFragment)
        TestCase.assertEquals(ModuleQuizDecider::class.java, parentFragment!!.javaClass)
        TestCase.assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
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
        expectedBundle.putString(DiscussionDetailsFragment.DISCUSSION_TITLE, null)
        expectedBundle.putBoolean(DiscussionDetailsFragment.GROUP_DISCUSSION, false)
        val parentFragment = callGetFragment(moduleItem, course, null)
        TestCase.assertNotNull(parentFragment)
        TestCase.assertEquals(DiscussionDetailsFragment::class.java, parentFragment!!.javaClass)
        TestCase.assertEquals(expectedBundle.toString(), parentFragment.arguments!!.toString())
    }

    private fun callGetFragment(moduleItem: ModuleItem, course: Course, moduleObject: ModuleObject?): Fragment? {
        return ModuleUtility.getFragment(moduleItem, course, moduleObject)
    }
}
