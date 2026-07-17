/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.interactions.router

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BaseRouteMatcherTest {

    private val subject = object : BaseRouteMatcher() {}

    @Test
    fun `forwardLocationParam adds location from the route query params`() {
        val route = Route().apply { queryParamsHash = hashMapOf(RouterParams.LOCATION to "course_syllabus_1") }

        val result = Uri.parse(subject.forwardLocationParam("$DOMAIN/files/2/download?verifier=abc", route))

        assertEquals("course_syllabus_1", result.getQueryParameter("location"))
        // The verifier is left intact; the backend prefers the location tag when both are present.
        assertEquals("abc", result.getQueryParameter("verifier"))
    }

    @Test
    fun `forwardLocationParam reads location from the route uri when not in query params`() {
        val route = Route().apply { uri = Uri.parse("$DOMAIN/courses/1/files/2?location=course_1") }

        val result = Uri.parse(subject.forwardLocationParam("$DOMAIN/files/2/download?verifier=abc", route))

        assertEquals("course_1", result.getQueryParameter("location"))
    }

    @Test
    fun `forwardLocationParam leaves the url unchanged when the route has no location`() {
        val url = "$DOMAIN/files/2/download?verifier=abc"

        val result = subject.forwardLocationParam(url, Route())

        assertEquals(url, result)
    }

    @Test
    fun `forwardLocationParam does not duplicate an existing location`() {
        val route = Route().apply { queryParamsHash = hashMapOf(RouterParams.LOCATION to "course_2") }
        val url = "$DOMAIN/files/2/download?location=course_1"

        val result = Uri.parse(subject.forwardLocationParam(url, route))

        assertEquals(listOf("course_1"), result.getQueryParameters("location"))
    }

    @Test
    fun `forwardLocationParam ignores a blank location`() {
        val route = Route().apply { queryParamsHash = hashMapOf(RouterParams.LOCATION to "") }
        val url = "$DOMAIN/files/2/download?verifier=abc"

        val result = Uri.parse(subject.forwardLocationParam(url, route))

        assertNull(result.getQueryParameter("location"))
    }

    companion object {
        private const val DOMAIN = "https://mobileqa.instructure.com"
    }
}