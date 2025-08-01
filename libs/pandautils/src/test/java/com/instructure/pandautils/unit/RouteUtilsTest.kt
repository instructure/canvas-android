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

package com.instructure.pandautils.unit

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.RouteUtils
import io.mockk.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RouteUtilsTest : Assert() {

    lateinit var route: Route
    lateinit var user: User

    @Before
    fun setup() {
        user = User()
        route = Route()

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns user
        every { ApiPrefs.fullDomain } returns "https://domain.instructure.com"
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns a url with a file id`() {
        val fileId = "6"

        RouteUtils.retrieveFileUrl(route, fileId) { url, _, _ ->
            assertEquals("https://domain.instructure.com/files/$fileId/preview", url)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns a url with a course and a file id`() {
        val fileId = "6"
        val courseId = "12"
        route.paramsHash = hashMapOf(Pair(RouterParams.COURSE_ID, courseId))

        RouteUtils.retrieveFileUrl(route, fileId) { url, _, _ ->
            assertEquals("https://domain.instructure.com/courses/$courseId/files/$fileId/preview", url)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns a url with a verifier`() {
        val fileId = "6"
        val verifier = "thisisaverifier"
        route.queryParamsHash = hashMapOf(Pair(RouterParams.VERIFIER, verifier))

        RouteUtils.retrieveFileUrl(route, fileId) { url, _, _ ->
            assertEquals("https://domain.instructure.com/files/$fileId/preview?verifier=$verifier", url)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns that auth is NOT needed with a verifier`() {
        val fileId = "6"
        val verifier = "thisisaverifier"
        route.queryParamsHash = hashMapOf(Pair(RouterParams.VERIFIER, verifier))

        RouteUtils.retrieveFileUrl(route, fileId) { _, _, needsAuth ->
            assertFalse(needsAuth)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns that auth is needed without a verifier`() {
        val fileId = "6"

        RouteUtils.retrieveFileUrl(route, fileId) { _, _, needsAuth ->
            assertTrue(needsAuth)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns a course context when a course id is provided`() {
        val fileId = "6"
        val courseId = "12"
        route.paramsHash = hashMapOf(Pair(RouterParams.COURSE_ID, courseId))

        RouteUtils.retrieveFileUrl(route, fileId) { _, context, _ ->
            assertEquals(CanvasContext.getGenericContext(CanvasContext.Type.COURSE, 12), context)
        }
    }

    @Test
    @Throws(Exception::class)
    fun `retrieveFileUrl returns a user context when no course id is provided`() {
        val fileId = "6"

        RouteUtils.retrieveFileUrl(route, fileId) { _, context, _ ->
            assertEquals(CanvasContext.currentUserContext(user), context)
        }
    }

}
