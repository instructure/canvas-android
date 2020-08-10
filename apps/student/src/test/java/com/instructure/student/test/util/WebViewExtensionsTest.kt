/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.test.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.utils.handleBlankTarget
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebViewExtensionsTest : TestCase() {

    @Test
    fun testhandleBlankTarget_singleLink() {
        assertEquals(singleLinkHtmlResult, handleBlankTarget(singleLinkHtml))
    }

    @Test
    fun testhandleBlankTarget_multiLink() {
        assertEquals(multiLinkHtmlResult, handleBlankTarget(multiLinkHtml))
    }

    val singleLinkHtml = """<p><a href="https://google.com" target="_blank"><img id="137435114"
                                                     src="https://mobiledev.instructure.com/courses/1567973/files/137435114/preview?verifier=qv8ph8TIWOtZD3VnNoQa5W1jsULkAoTKcCO8qJhx"
                                                     alt="rce-b8f4b126-375b-46dc-98b9-936e648f96b0.jpeg" width="187"
                                                     height="249"
                                                     data-api-endpoint="https://mobiledev.instructure.com/api/v1/courses/1567973/files/137435114"
                                                     data-api-returntype="File"></a></p>"""
    val singleLinkHtmlResult = """<p><a href="https://google.com"><img id="137435114"
                                                     src="https://mobiledev.instructure.com/courses/1567973/files/137435114/preview?verifier=qv8ph8TIWOtZD3VnNoQa5W1jsULkAoTKcCO8qJhx"
                                                     alt="rce-b8f4b126-375b-46dc-98b9-936e648f96b0.jpeg" width="187"
                                                     height="249"
                                                     data-api-endpoint="https://mobiledev.instructure.com/api/v1/courses/1567973/files/137435114"
                                                     data-api-returntype="File"></a></p>"""

    val multiLinkHtml = """<p><a href="https://google.com" target="_blank"><img id="137435114"
                                                     src="https://mobiledev.instructure.com/courses/1567973/files/137435114/preview?verifier=qv8ph8TIWOtZD3VnNoQa5W1jsULkAoTKcCO8qJhx"
                                                     alt="rce-b8f4b126-375b-46dc-98b9-936e648f96b0.jpeg" width="187"
                                                     height="249"
                                                     data-api-endpoint="https://mobiledev.instructure.com/api/v1/courses/1567973/files/137435114"
                                                     data-api-returntype="File"></a></p>
<p> </p>
<p> </p>
<p><a href="https://amazon.com" target="_blank"><img id="137433343"
                                                     src="https://mobiledev.instructure.com/courses/1567973/files/137433343/preview?verifier=fv4CwENUj9FsLzSyqbCLX0iHi0xJwyKcHBw0qm7Z"
                                                     alt="Mario.jpeg"
                                                     data-api-endpoint="https://mobiledev.instructure.com/api/v1/courses/1567973/files/137433343"
                                                     data-api-returntype="File"></a></p>"""

    val multiLinkHtmlResult = """<p><a href="https://google.com"><img id="137435114"
                                                     src="https://mobiledev.instructure.com/courses/1567973/files/137435114/preview?verifier=qv8ph8TIWOtZD3VnNoQa5W1jsULkAoTKcCO8qJhx"
                                                     alt="rce-b8f4b126-375b-46dc-98b9-936e648f96b0.jpeg" width="187"
                                                     height="249"
                                                     data-api-endpoint="https://mobiledev.instructure.com/api/v1/courses/1567973/files/137435114"
                                                     data-api-returntype="File"></a></p>
<p> </p>
<p> </p>
<p><a href="https://amazon.com"><img id="137433343"
                                                     src="https://mobiledev.instructure.com/courses/1567973/files/137433343/preview?verifier=fv4CwENUj9FsLzSyqbCLX0iHi0xJwyKcHBw0qm7Z"
                                                     alt="Mario.jpeg"
                                                     data-api-endpoint="https://mobiledev.instructure.com/api/v1/courses/1567973/files/137433343"
                                                     data-api-returntype="File"></a></p>"""
}