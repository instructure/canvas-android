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

import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.assignments.list.AssignmentListFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.inbox.details.InboxDetailsFragment
import com.instructure.pandautils.features.inbox.list.InboxFragment
import com.instructure.student.activity.BaseRouterActivity
import com.instructure.student.features.discussion.list.DiscussionListFragment
import com.instructure.student.features.grades.GradesListFragment
import com.instructure.student.features.modules.list.ModuleListFragment
import com.instructure.student.features.modules.progression.CourseModuleProgressionFragment
import com.instructure.student.features.pages.list.PageListFragment
import com.instructure.student.features.people.details.PeopleDetailsFragment
import com.instructure.student.features.people.list.PeopleListFragment
import com.instructure.student.features.quiz.list.QuizListFragment
import com.instructure.student.fragment.AnnouncementListFragment
import com.instructure.student.fragment.CourseSettingsFragment
import com.instructure.student.fragment.NotificationListFragment
import com.instructure.student.fragment.UnsupportedTabFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsFragment
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListRepositoryFragment
import com.instructure.student.mobius.syllabus.ui.SyllabusRepositoryFragment
import com.instructure.student.router.RouteMatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RouterUtilsTest : TestCase() {

    private val activity: FragmentActivity = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkObject(RemoteConfigUtils)
        every { RemoteConfigUtils.getString(any()) } returns "false"
    }

    @Test
    fun testCanRouteInternally_misc() {
        // Home
        assertTrue(callCanRouteInternally("http://mobiledev.instructure.com"))

        //  Login
        assertFalse(callCanRouteInternally("http://mobiledev.instructure.com/login"))
    }

    @Test
    fun testCanRouteInternally_notSupported() {
        assertTrue(callCanRouteInternally("https://mobiledev.instructure.com/courses/833052/media_download?"))
    }

    @Test
    fun testCanRouteInternally_courseIdParseCorrect() {
        assertEquals(833052L, BaseRouterActivity.parseCourseId("833052"))
    }

    @Test
    fun testCanRouteInternally_courseIdParseWrong() {
        // Written due to a crash found by Crashlytics
        // See: https://fabric.io/instructure/android/apps/com.instructure.candroid/issues/5a69f6858cb3c2fa63977be1?time=1509408000000%3A1517270399999
        assertNull(BaseRouterActivity.parseCourseId("sis_course_id:833"))
    }

    @Test
    fun testCanRouteInternally() {
        // Since there is a catch all, anything with the correct domain returns true.
        assertTrue(callCanRouteInternally("https://mobiledev.instructure.com/calendar2?include_contexts=course_833052#view_name=month&view_start=2015-03-19T06%3A00%3A00.000Z"))
        assertTrue(callCanRouteInternally("https://mobiledev.instructure.com/courses/833052/calendar_events/921098"))

        assertFalse(callCanRouteInternally("http://google.com/courses/54564/"))

    }

    private fun callCanRouteInternally(url: String): Boolean {
        return RouteMatcher.canRouteInternally(activity, url, "mobiledev.instructure.com", false)
    }

    private fun callGetInternalRoute(url: String): Route? {
        // String domain = APIHelper.getDomain(RuntimeEnvironment.application);
        return RouteMatcher.getInternalRoute(url, "mobiledev.instructure.com")
    }

    @Test
    fun testGetInternalRoute_supportedDomain() {
        var route = callGetInternalRoute("https://instructure.com")
        assertNull(route)

        route = callGetInternalRoute("https://mobiledev.instructure.com")
        assertNotNull(route)

        route = callGetInternalRoute("https://canvas.net")
        assertNull(route)

        route = callGetInternalRoute("https://canvas.net/courses/12344")
        assertNull(route)
    }

    @Test
    fun testGetInternalRoute_nonSupportedDomain() {
        var route = callGetInternalRoute("https://google.com")
        assertNull(route)

        route = callGetInternalRoute("https://youtube.com")
        assertNull(route)

        route = callGetInternalRoute("https://aFakeWebsite.com/courses/12344")
        assertNull(route)
    }

    @Test
    fun testGetInternalRoute_calendar() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/calendar2?include_contexts=course_833052#view_name=month&view_start=2015-03-19T06%3A00%3A00.000Z")
        assertNotNull(route)
        // TODO add test for calendar
        //assertEquals(CalendarEventFragment.class, route.getMasterCls());

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/calendar_events/921098")
        assertNotNull(route)
    }

    @Test
    fun testGetInternalRoute_externalTools() {
        val route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/external_tools/131971")
        assertNotNull(route)

    }

    @Test
    fun testGetInternalRoute_files() {

        // Courses
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/files/63383591/download?wrap=1")
        assertNotNull(route)
        assertEquals(RouteContext.FILE, route!!.routeContext)

        var expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "833052"
        expectedParams[RouterParams.FILE_ID] = "63383591"
        assertEquals(expectedParams, route.paramsHash)

        val expectedQueryParams = HashMap<String, String>()
        expectedQueryParams["wrap"] = "1"
        assertEquals(expectedQueryParams, route.queryParamsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/files/63383591")
        assertNotNull(route) // route is not supported
        assertEquals(null, route!!.primaryClass)


        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/files/63383591/download?verifier=12344556")
        assertNotNull(route)
        assertEquals(RouteContext.FILE, route!!.routeContext)

        // Files
        route = callGetInternalRoute("https://mobiledev.instructure.com/files/63383591/download?wrap=1")
        assertNotNull(route)
        assertEquals(RouteContext.FILE, route!!.routeContext)

        expectedParams = HashMap()
        expectedParams[RouterParams.FILE_ID] = "63383591"
        assertEquals(expectedParams, route.paramsHash)

        assertEquals(expectedQueryParams, route.queryParamsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/files/63383591")
        assertNotNull(route)
        assertEquals(RouteContext.FILE, route!!.routeContext)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/833052/files/63383591/download?verifier=12344556")
        assertNotNull(route)
        assertEquals(RouteContext.FILE, route!!.routeContext)
    }

    @Test
    fun testGetInternalRoute_conversation() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/conversations/")
        assertNotNull(route)
        assertEquals(InboxFragment::class.java, route!!.primaryClass)

        // Detailed Conversation
        route = callGetInternalRoute("https://mobiledev.instructure.com/conversations/1078680")
        assertNotNull(route)
        assertEquals(InboxDetailsFragment::class.java, route!!.primaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.CONVERSATION_ID] = "1078680"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_modules() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/modules")
        assertNotNull(route)
        assertEquals(ModuleListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/modules/48753")
        assertNotNull(route)
        assertEquals(ModuleListFragment::class.java, route!!.primaryClass)

        // Discussion
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/24219/discussion_topics/1129998?module_item_id=12345")
        assertNotNull(route)
        assertEquals(ModuleListFragment::class.java, route!!.primaryClass)
        assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)

        val expectedQueryParams = HashMap<String, String>()
        expectedQueryParams[RouterParams.MODULE_ITEM_ID] = "12345"
        assertEquals(expectedQueryParams, route.queryParamsHash)

        // Pages
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/24219/pages/1129998?module_item_id=12345")
        assertNotNull(route)
        assertEquals(ModuleListFragment::class.java, route!!.primaryClass)
        assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)
        assertEquals(expectedQueryParams, route.queryParamsHash)

        // Quizzes
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/24219/quizzes/1129998?module_item_id=12345")
        assertNotNull(route)
        assertEquals(ModuleListFragment::class.java, route!!.primaryClass)
        assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)
        assertEquals(expectedQueryParams, route.queryParamsHash)

        // Assignments
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/24219/assignments/1129998?module_item_id=12345")
        assertNotNull(route)
        assertEquals(ModuleListFragment::class.java, route!!.primaryClass)
        assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)
        assertEquals(expectedQueryParams, route.queryParamsHash)

        // Files
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/24219/files/1129998?module_item_id=12345")
        assertNotNull(route)
        assertEquals(ModuleListFragment::class.java, route!!.primaryClass)
        assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)
        assertEquals(expectedQueryParams, route.queryParamsHash)
    }

    @Test
    fun testGetInternalRoute_notifications() {
        val route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/notifications")
        assertNotNull(route)
        assertEquals(NotificationListFragment::class.java, route!!.primaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_grades() {
        val route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/grades")
        assertNotNull(route)
        assertEquals(GradesListFragment::class.java, route!!.primaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_users() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/users")
        assertNotNull(route)
        assertEquals(PeopleListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/users/1234")
        assertNotNull(route)
        assertEquals(PeopleListFragment::class.java, route!!.primaryClass)
        assertEquals(PeopleDetailsFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.USER_ID] = "1234"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_discussion() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/discussion_topics")
        assertNotNull(route)
        assertEquals(DiscussionListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/discussion_topics/1234")
        assertNotNull(route)
        assertEquals(DiscussionListFragment::class.java, route!!.primaryClass)
        assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.MESSAGE_ID] = "1234"
        assertEquals(expectedParams, route.paramsHash)

    }

    @Test
    fun testGetInternalRoute_pages() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/pages")
        assertNotNull(route)
        assertEquals(PageListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/pages/hello")
        assertNotNull(route)
        assertEquals(PageListFragment::class.java, route!!.primaryClass)
        assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.PAGE_ID] = "hello"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_announcements() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/announcements")
        assertNotNull(route)
        assertEquals(AnnouncementListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/announcements/12345")
        assertNotNull(route)
        assertEquals(AnnouncementListFragment::class.java, route!!.primaryClass)
        assertEquals(DiscussionRouterFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.MESSAGE_ID] = "12345"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_quiz() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/quizzes")
        assertNotNull(route)
        assertEquals(QuizListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/quizzes/12345")
        assertNotNull(route)
        assertEquals(QuizListFragment::class.java, route!!.primaryClass)
        assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.QUIZ_ID] = "12345"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_syllabus() {
        val route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/assignments/syllabus")
        assertNotNull(route)
        assertEquals(SyllabusRepositoryFragment::class.java, route!!.primaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_assignments() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/assignments/")
        assertNotNull(route)
        assertEquals(AssignmentListFragment::class.java, route!!.primaryClass)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/assignments/213445213445213445213445213445213445213445213445213445213445213445213445")
        assertNotNull(route)
        assertEquals(AssignmentListFragment::class.java, route!!.primaryClass)
        assertEquals(CourseModuleProgressionFragment::class.java, route.secondaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.ASSIGNMENT_ID] = "213445213445213445213445213445213445213445213445213445213445213445213445"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_submissions_rubric() {
        // TODO: This test needs to change when we remove the assignment feature flag
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/assignments/12345/rubric")
        assertNotNull(route)
        assertEquals(AssignmentDetailsFragment::class.java, route!!.primaryClass)
        assertEquals(SubmissionDetailsFragment::class.java, route.secondaryClass)

        var expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.ASSIGNMENT_ID] = "12345"
        expectedParams[RouterParams.SLIDING_TAB_TYPE] = "rubric"
        assertEquals(expectedParams, route.paramsHash)


        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/assignments/213445213445213445213445213445213445213445213445213445213445213445213445/submissions/1234")
        assertNotNull(route)
        assertEquals(AssignmentDetailsFragment::class.java, route!!.primaryClass)
        assertEquals(null, route.secondaryClass)

        expectedParams = HashMap()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        expectedParams[RouterParams.ASSIGNMENT_ID] = "213445213445213445213445213445213445213445213445213445213445213445213445"
        expectedParams[RouterParams.SLIDING_TAB_TYPE] = "submissions"
        expectedParams[RouterParams.SUBMISSION_ID] = "1234"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_settings() {
        val route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/settings/")
        assertNotNull(route)
        assertEquals(CourseSettingsFragment::class.java, route!!.primaryClass)

        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_unsupported() {
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/collaborations/")
        assertNotNull(route)
        assertEquals(UnsupportedTabFragment::class.java, route!!.primaryClass)
        //        assertEquals(Tab.COLLABORATIONS_ID, route.getTabId());
        val expectedParams = HashMap<String, String>()
        expectedParams[RouterParams.COURSE_ID] = "836357"
        assertEquals(expectedParams, route.paramsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/collaborations/234") // not an actual url
        assertNotNull(route)
        assertEquals(UnsupportedTabFragment::class.java, route!!.primaryClass)
        //        assertEquals(Tab.COLLABORATIONS_ID, route.getTabId());
        assertEquals(expectedParams, route.paramsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/outcomes/")
        assertNotNull(route)
        assertEquals(UnsupportedTabFragment::class.java, route!!.primaryClass)
        //        assertEquals(Tab.OUTCOMES_ID, route.getTabId());
        assertEquals(expectedParams, route.paramsHash)

        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/836357/outcomes/234") // not an actual url
        assertNotNull(route)
        assertEquals(UnsupportedTabFragment::class.java, route!!.primaryClass)
        //        assertEquals(Tab.OUTCOMES_ID, route.getTabId());
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testGetInternalRoute_conferences() {
        val courseId = "836357"
        var route = callGetInternalRoute("https://mobiledev.instructure.com/courses/$courseId/conferences/")
        val expectedParams = hashMapOf(RouterParams.COURSE_ID to courseId)
        assertNotNull(route)
        assertEquals(ConferenceListRepositoryFragment::class.java, route!!.primaryClass)
        assertEquals(expectedParams, route.paramsHash)

        // There is currently no API endpoint for specific conferences, so we must route to the conference list
        route = callGetInternalRoute("https://mobiledev.instructure.com/courses/$courseId/conferences/234") // not an actual url
        assertNotNull(route)
        assertEquals(ConferenceListRepositoryFragment::class.java, route!!.primaryClass)
        assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testCreateBookmarkCourse() {
        ApiPrefs.domain = "mobiledev.instructure.com"
        val replacementParams = HashMap<String, String>()
        replacementParams[RouterParams.COURSE_ID] = "123"
        replacementParams[RouterParams.QUIZ_ID] = "456"
        val canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, 123, "")

        val queryParams = HashMap<String, String>()


        val url = RouteMatcher.generateUrl(canvasContext.type, QuizListFragment::class.java, CourseModuleProgressionFragment::class.java, replacementParams, queryParams)
        assertEquals("https://mobiledev.instructure.com/courses/123/quizzes/456", url)
    }

    @Test
    fun testCreateBookmarkGroups() {
        ApiPrefs.domain = "mobiledev.instructure.com"
        val replacementParams = HashMap<String, String>()
        replacementParams[RouterParams.COURSE_ID] = "123"
        replacementParams[RouterParams.QUIZ_ID] = "456"
        val canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.GROUP, 123, "")

        val queryParams = HashMap<String, String>()

        val url = RouteMatcher.generateUrl(canvasContext.type, QuizListFragment::class.java, CourseModuleProgressionFragment::class.java, replacementParams, queryParams)
        assertEquals("https://mobiledev.instructure.com/groups/123/quizzes/456", url)
    }
}
