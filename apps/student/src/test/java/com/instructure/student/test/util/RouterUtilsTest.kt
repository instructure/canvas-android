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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.interactions.router.RouterParams
import com.instructure.student.activity.BaseRouterActivity
import com.instructure.student.fragment.*
import com.instructure.student.mobius.syllabus.ui.SyllabusFragment
import com.instructure.student.router.RouteMatcher
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.*

@RunWith(AndroidJUnit4::class)
class RouterUtilsTest : TestCase() {

    @Test
    fun testCanRouteInternally_misc() {
        // Home
        TestCase.assertTrue(callCanRouteInternally("http://mobiledev.instructure.com"))

        //  Login
        TestCase.assertFalse(callCanRouteInternally("http://mobiledev.instructure.com/login"))
    }

    @Test
    fun testCanRouteInternally_notSupported() {
        TestCase.assertTrue(callCanRouteInternally("https://mobiledev.instructure.com/courses/833052/media_download?"))
    }

    @Test
    fun testCanRouteInternally_courseIdParseCorrect() {
        TestCase.assertEquals(833052L, BaseRouterActivity.parseCourseId("833052"))
    }

    @Test
    fun testCanRouteInternally_courseIdParseWrong() {
        // Written due to a crash found by Crashlytics
        // See: https://fabric.io/instructure/android/apps/com.instructure.candroid/issues/5a69f6858cb3c2fa63977be1?time=1509408000000%3A1517270399999
        TestCase.assertNull(BaseRouterActivity.parseCourseId("sis_course_id:833"))
    }

    @Test
    fun testCanRouteInternally() {
        // Since there is a catch all, anything with the correct domain returns true.
        TestCase.assertTrue(callCanRouteInternally("https://mobiledev.instructure.com/calendar2?include_contexts=course_833052#view_name=month&view_start=2015-03-19T06%3A00%3A00.000Z"))
        TestCase.assertTrue(callCanRouteInternally("https://mobiledev.instructure.com/courses/833052/calendar_events/921098"))

        TestCase.assertFalse(callCanRouteInternally("http://google.com/courses/54564/"))

    }

    private fun callCanRouteInternally(url: String): Boolean {
        return RouteMatcher.canRouteInternally(RuntimeEnvironment.application, url, "mobiledev.instructure.com", false)
    }

    private fun callGetInternalRoute(url: String): Route? {
        // String domain = APIHelper.getDomain(RuntimeEnvironment.application);
        return RouteMatcher.getInternalRoute(url, "mobiledev.instructure.com")
    }

    @Test
    fun testGetInternalRoute_supportedDomain() {
        var route = callGetInternalRoute("https://instructure.com")
        TestCase.assertNull(route)

        route = callGetInternalRoute("https://mobiledev.instructure.com")
        TestCase.assertNotNull(route)

        route = callGetInternalRoute("https://canvas.net")
        TestCase.assertNull(route)

        route = callGetInternalRoute("https://canvas.net/courses/12344")
        TestCase.assertNull(route)
    }

    @Test
    fun testGetInternalRoute_nonSupportedDomain() {
        var route = callGetInternalRoute("https://google.com")
        TestCase.assertNull(route)

        route = callGetInternalRoute("https://youtube.com")
        TestCase.assertNull(route)

        route = callGetInternalRoute("https://aFakeWebsite.com/courses/12344")
        TestCase.assertNull(route)
    }

    @Test
    fun testGetInternalRoute_calendar() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/calendar2?include_contexts=course_833052#view_name=month&view_start=2015-03-19T06%3A00%3A00.000Z")
        TestCase.assertNotNull(route)
        // TODO add test for calendar
        //assertEquals(CalendarEventFragment.class, route.getMasterCls());

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/calendar_events/921098")
        TestCase.assertNotNull(route)
    }

    @Test
    fun testGetInternalRoute_externalTools() {
        val route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/external_tools/131971")
        TestCase.assertNotNull(route)

    }

    @Test
    fun testGetInternalRoute_files() {

        // Courses
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/files/63383591/download?wrap=1")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(RouteContext.FILE, route!!.routeContext)

        var expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "833052"
        expectedParams[RouterParams.FILE_ID] = "63383591"
        TestCase.assertEquals(expectedParams, route.paramsHash)

        val expectedQueryParams = HashMap<String, String>()
        expectedQueryParams["wrap"] = "1"
        TestCase.assertEquals(expectedQueryParams, route.queryParamsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/files/63383591")
        TestCase.assertNotNull(route) // route is not supported
        TestCase.assertEquals(null, route!!.primaryClass)


        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/files/63383591/download?verifier=12344556")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(RouteContext.FILE, route!!.routeContext)

        // Files
        route = callGetInternalRoute("https://mobiledev.instructure.com/files/63383591/download?wrap=1")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(RouteContext.FILE, route!!.routeContext)

        expectedParams = HashMap()
        expectedParams[RouterParams.FILE_ID] = "63383591"
        TestCase.assertEquals(expectedParams, route.paramsHash)

        TestCase.assertEquals(expectedQueryParams, route.queryParamsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/files/63383591")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(FileListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/files/63383591/download?verifier=12344556")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(RouteContext.FILE, route!!.routeContext)
    }

    @Test
    fun testGetInternalRoute_conversation() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/conversations/")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(InboxFragment::class.java, route!!.primaryClass)

        // Detailed Conversation
        route = callGetInternalRoute("https://mobiledev.instructure.com/conversations/1078680")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(InboxConversationFragment::class.java, route!!.primaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.CONVERSATION_ID] = "1078680"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_modules() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/modules")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(ModuleListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/modules/48753")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(ModuleListFragment::class.java, route!!.primaryClass)

        // Discussion
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/24219/discussion_topics/1129998?module_item_id=12345")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(ModuleListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)

        val expectedQueryParams = HashMap<String, String>()
        expectedQueryParams[RouterParams.MODULE_ITEM_ID] = "12345"
        TestCase.assertEquals(expectedQueryParams, route.queryParamsHash)

        // Pages
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/24219/pages/1129998?module_item_id=12345")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(ModuleListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)
        TestCase.assertEquals(expectedQueryParams, route.queryParamsHash)

        // Quizzes
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/24219/quizzes/1129998?module_item_id=12345")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(ModuleListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)
        TestCase.assertEquals(expectedQueryParams, route.queryParamsHash)

        // Assignments
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/24219/assignments/1129998?module_item_id=12345")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(ModuleListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)
        TestCase.assertEquals(expectedQueryParams, route.queryParamsHash)

        // Files
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/24219/files/1129998?module_item_id=12345")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(ModuleListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)
        TestCase.assertEquals(expectedQueryParams, route.queryParamsHash)
    }

    @Test
    fun testGetInternalRoute_notifications() {
        val route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/notifications")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(NotificationListFragment::class.java, route!!.primaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_grades() {
        val route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/grades")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(GradesListFragment::class.java, route!!.primaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_users() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/users")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(PeopleListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/users/1234")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(PeopleListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(PeopleDetailsFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.USER_ID] = "1234"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_discussion() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/discussion_topics")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(DiscussionListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/discussion_topics/1234")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(DiscussionListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(DiscussionDetailsFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.MESSAGE_ID] = "1234"
        TestCase.assertEquals(expectedParams, route.paramsHash)

    }

    @Test
    fun testGetInternalRoute_pages() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/pages")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(PageListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/pages/hello")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(PageListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(PageDetailsFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.PAGE_ID] = "hello"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_announcements() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/announcements")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(AnnouncementListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/announcements/12345")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(AnnouncementListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(DiscussionDetailsFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.MESSAGE_ID] = "12345"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_quiz() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/quizzes")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(QuizListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/quizzes/12345")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(QuizListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(BasicQuizViewFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.QUIZ_ID] = "12345"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_syllabus() {
        val route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/assignments/syllabus")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(SyllabusFragment::class.java, route!!.primaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_assignments() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/assignments/")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(AssignmentListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/assignments/213445213445213445213445213445213445213445213445213445213445213445213445")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(AssignmentListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(AssignmentFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.ASSIGNMENT_ID] = "213445213445213445213445213445213445213445213445213445213445213445213445"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_submissions_rubric() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/assignments/12345/rubric")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(AssignmentListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(AssignmentFragment::class.java, route.secondaryClass)

        var expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.ASSIGNMENT_ID] = "12345"
        expectedParams[RouterParams.SLIDING_TAB_TYPE] = "rubric"
        TestCase.assertEquals(expectedParams, route.paramsHash)


        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/assignments/213445213445213445213445213445213445213445213445213445213445213445213445/submissions/1234")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(AssignmentListFragment::class.java, route!!.primaryClass)
        TestCase.assertEquals(AssignmentFragment::class.java, route.secondaryClass)

        expectedParams = HashMap()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.ASSIGNMENT_ID] = "213445213445213445213445213445213445213445213445213445213445213445213445"
        expectedParams[RouterParams.SLIDING_TAB_TYPE] = "submissions"
        expectedParams[RouterParams.SUBMISSION_ID] = "1234"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_settings() {
        val route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/settings/")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(CourseSettingsFragment::class.java, route!!.primaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_unsupported() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/collaborations/")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(UnsupportedTabFragment::class.java, route!!.primaryClass)
        //        assertEquals(Tab.COLLABORATIONS_ID, route.getTabId());
        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        TestCase.assertEquals(expectedParams, route.paramsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/collaborations/234") // not an actual url
        TestCase.assertNotNull(route)
        TestCase.assertEquals(UnsupportedTabFragment::class.java, route!!.primaryClass)
        //        assertEquals(Tab.COLLABORATIONS_ID, route.getTabId());
        TestCase.assertEquals(expectedParams, route.paramsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/conferences/")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(UnsupportedTabFragment::class.java, route!!.primaryClass)
        //        assertEquals(Tab.CONFERENCES_ID, route.getTabId());
        TestCase.assertEquals(expectedParams, route.paramsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/conferences/234") // not an actual url
        TestCase.assertNotNull(route)
        TestCase.assertEquals(UnsupportedTabFragment::class.java, route!!.primaryClass)
        //        assertEquals(Tab.CONFERENCES_ID, route.getTabId());
        TestCase.assertEquals(expectedParams, route.paramsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/outcomes/")
        TestCase.assertNotNull(route)
        TestCase.assertEquals(UnsupportedTabFragment::class.java, route!!.primaryClass)
        //        assertEquals(Tab.OUTCOMES_ID, route.getTabId());
        TestCase.assertEquals(expectedParams, route.paramsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/outcomes/234") // not an actual url
        TestCase.assertNotNull(route)
        TestCase.assertEquals(UnsupportedTabFragment::class.java, route!!.primaryClass)
        //        assertEquals(Tab.OUTCOMES_ID, route.getTabId());
        TestCase.assertEquals(expectedParams, route.paramsHash)
    }


    @Test
    fun testCreateBookmarkCourse() {
        ApiPrefs.domain = "mobiledev.instructure.com"
        val replacementParams = HashMap<String, String>()
        replacementParams[RouterParams.COURSE_ID] = "123"
        replacementParams[RouterParams.QUIZ_ID] = "456"
        val canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, 123, "")

        val queryParams = HashMap<String, String>()


        val url = RouteMatcher.generateUrl(canvasContext.type, QuizListFragment::class.java, BasicQuizViewFragment::class.java, replacementParams, queryParams)
        TestCase.assertEquals("https://mobiledev.instructure.com/courses/123/quizzes/456", url)
    }

    @Test
    fun testCreateBookmarkGroups() {
        ApiPrefs.domain = "mobiledev.instructure.com"
        val replacementParams = HashMap<String, String>()
        replacementParams[RouterParams.COURSE_ID] = "123"
        replacementParams[RouterParams.QUIZ_ID] = "456"
        val canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.GROUP, 123, "")

        val queryParams = HashMap<String, String>()

        val url = RouteMatcher.generateUrl(canvasContext.type, QuizListFragment::class.java, BasicQuizViewFragment::class.java, replacementParams, queryParams)
        TestCase.assertEquals("https://mobiledev.instructure.com/groups/123/quizzes/456", url)
    }
}
