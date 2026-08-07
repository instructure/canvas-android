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

package com.instructure.pandautils.features.lti

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LtiReturnLinkHelperTest {

    private val contextLink = "https://domain.com/courses/3"

    @Test
    fun `Course home url is a return link`() {
        assertTrue(isReturnLink("https://domain.com/courses/3"))
        assertTrue(isReturnLink("https://domain.com/courses/3/"))
        assertTrue(isReturnLink("https://domain.com/courses/3?foo=bar"))
        assertTrue(isReturnLink("https://domain.com/courses/3#anchor"))
    }

    @Test
    fun `Assignment list url is a return link for assignment ltis`() {
        assertTrue(isReturnLink("https://domain.com/courses/3/assignments"))
        assertTrue(isReturnLink("https://domain.com/courses/3/assignments/"))
        assertTrue(isReturnLink("https://domain.com/courses/3/assignments?module_item_id=8"))
    }

    @Test
    fun `Assignment list url is not a return link for other ltis`() {
        assertFalse(isReturnLink("https://domain.com/courses/3/assignments", isAssignmentLti = false))
    }

    @Test
    fun `Other urls are not return links`() {
        assertFalse(isReturnLink("https://domain.com/courses/3/assignments/6"))
        assertFalse(isReturnLink("https://domain.com/courses/3/quizzes"))
        assertFalse(isReturnLink("https://domain.com/courses/33/assignments"))
        assertFalse(isReturnLink("https://domain.com/courses/3/modules"))
    }

    @Test
    fun `Empty context link never matches`() {
        assertFalse(LtiReturnLinkHelper.isReturnLink("", "", true))
        assertFalse(LtiReturnLinkHelper.isReturnLink("/assignments", "", true))
    }

    private fun isReturnLink(url: String, isAssignmentLti: Boolean = true) =
        LtiReturnLinkHelper.isReturnLink(url, contextLink, isAssignmentLti)
}
