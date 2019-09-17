/*
 * Copyright (C) 2017 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.teacher.unit

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.interactions.router.Route
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class RouteTest : Assert() {

    //region Matching
    @Test
    fun testRouteMatching() {
        val route = Route("/courses")
        Assert.assertFalse(route.apply("http://mobiledev.instructure.com/courses/953090/"))
        Assert.assertFalse(route.apply("http://mobiledev.instructure.com/courses/953090")) // no slash at the end
    }

    //endregion

    //region Params
    @Test
    fun testRouteNoParams() {
        val expectedParams = HashMap<String, String>()

        val route = Route("/courses")
        Assert.assertTrue(route.apply("http://mobiledev.instructure.com/courses/"))
        Assert.assertEquals(expectedParams, route.paramsHash)

        Assert.assertTrue(route.apply("http://mobiledev.instructure.com/courses/")) // no slash at the end
        Assert.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testRouteTwoCharParam() {
        val expectedParams = HashMap<String, String>()
        expectedParams["course_id"] = "833052"
        expectedParams["page_id"] = "page-3"

        val route = Route("/courses/:course_id/pages/:page_id")
        Assert.assertTrue(route.apply("https://mobiledev.instructure.com/courses/833052/pages/page-3/"))
        Assert.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testRouteOneIntParam() {
        val expectedParams = HashMap<String, String>()
        expectedParams["course_id"] = "953090"

        var route = Route("/(?:courses|groups)/:course_id")
        Assert.assertTrue(route.apply("http://mobiledev.instructure.com/courses/953090/"))
        Assert.assertEquals(expectedParams, route.paramsHash)

        route = Route("/courses/:course_id/") // Test with a optional slash at the end
        Assert.assertTrue(route.apply("http://mobiledev.instructure.com/courses/953090")) // no slash at the end
        Assert.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testRouteTwoIntParams() {
        val expectedParams = HashMap<String, String>()
        expectedParams["course_id"] = "833052"
        expectedParams["file_id"] = "39506637"

        val route = Route("/courses/:course_id/files/:file_id")
        Assert.assertTrue(route.apply("https://mobiledev.instructure.com/courses/833052/files/39506637/"))
        Assert.assertEquals(expectedParams, route.paramsHash)

        Assert.assertTrue(route.apply("https://mobiledev.instructure.com/courses/833052/files/39506637")) // no slash at the end
        Assert.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testRouteThreeIntParams() {
        val expectedParams = HashMap<String, String>()
        expectedParams["course_id"] = "953090"
        expectedParams["assignment_id"] = "2651861"
        expectedParams["submission_id"] = "3690827"

        val route = Route("/(?:courses|groups)/:course_id/assignments/:assignment_id/submissions/:submission_id")
        Assert.assertTrue(route.apply("http://mobiledev.instructure.com/courses/953090/assignments/2651861/submissions/3690827/"))
        Assert.assertEquals(expectedParams, route.paramsHash)

        Assert.assertTrue(route.apply("http://mobiledev.instructure.com/courses/953090/assignments/2651861/submissions/3690827")) // no slash at the end
        Assert.assertEquals(expectedParams, route.paramsHash)
    }

    //endregion

    //region Query Params
    @Test
    fun testRouteQueryParams() {
        val expectedParams = HashMap<String, String>()
        expectedParams["course_id"] = "836357"
        expectedParams["quiz_id"] = "990775"

        val route = Route("/courses/:course_id/quizzes/:quiz_id")
        Assert.assertTrue(route.apply("https://mobiledev.instructure.com/courses/836357/quizzes/990775?module_item_id=6723096/"))
        Assert.assertEquals(expectedParams, route.paramsHash)
        Assert.assertEquals("module_item_id=6723096/", route.getQueryString())

        Assert.assertTrue(route.apply("https://mobiledev.instructure.com/courses/836357/quizzes/990775?module_item_id=6723096")) // no slash at the end
        Assert.assertEquals(expectedParams, route.paramsHash)
        Assert.assertEquals("module_item_id=6723096", route.getQueryString())
    }

    @Test
    fun testRouteQueryParams_shardCourse() {
        val expectedParams = HashMap<String, String>()
        expectedParams["course_id"] = "123450000000004321"

        val route = Route("/courses/:course_id")
        Assert.assertTrue(route.apply("https://mobiledev.instructure.com/courses/12345~4321"))
        Assert.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testRouteQueryParams_shardCourseAssignment() {
        val expectedParams = HashMap<String, String>()
        expectedParams["course_id"] = "123450000000004321"
        expectedParams["assignment_id"] = "123450000000004321"

        val route = Route("/courses/:course_id/assignments/:assignment_id")
        Assert.assertTrue(route.apply("https://mobiledev.instructure.com/courses/12345~4321/assignments/12345~4321"))
        Assert.assertEquals(expectedParams, route.paramsHash)
    }

    @Test
    fun testRouteQueryParams_shardPageId() {
        val expectedParams = HashMap<String, String>()
        expectedParams["course_id"] = "1234"
        expectedParams["page_id"] = "12345~4321"

        val route = Route("/courses/:course_id/pages/:page_id")
        Assert.assertTrue(route.apply("https://mobiledev.instructure.com/courses/1234/pages/12345~4321"))
        Assert.assertEquals(expectedParams, route.paramsHash)
    }

    //endregion

    /*
     * Fragment Identifier
     */
    @Test
    fun testRouteFragmentIdentifierParams() {
        val expectedParams = HashMap<String, String>()
        expectedParams["course_id"] = "836357"
        expectedParams["quiz_id"] = "990775"

        val route = Route("/courses/:course_id/quizzes/:quiz_id")
        Assert.assertTrue(route.apply("https://mobiledev.instructure.com/courses/836357/quizzes/990775?module_item_id=6723096#Fragment Identifier"))
        Assert.assertEquals(expectedParams, route.paramsHash)
        Assert.assertEquals("module_item_id=6723096", route.getQueryString())
        Assert.assertEquals("Fragment Identifier", route.getFragmentIdentifier())
    }
}
