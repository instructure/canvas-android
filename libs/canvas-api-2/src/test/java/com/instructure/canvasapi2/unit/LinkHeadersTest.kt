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

package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.utils.LinkHeaders

import org.junit.Test

import org.junit.Assert.assertEquals

class LinkHeadersTest {

    @Test
    fun toString_TestPrev() {
        val linkHeaders = generateLinkHeaders()
        val testVal = linkHeaders.toString()
        val testValArray = testVal.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        assertEquals("PREV: prevUrl", testValArray[0])
    }

    @Test
    fun toString_TestNext() {
        val linkHeaders = generateLinkHeaders()
        val testVal = linkHeaders.toString()
        val testValArray = testVal.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        assertEquals("NEXT: nextUrl", testValArray[1])
    }

    @Test
    fun toString_TestLast() {
        val linkHeaders = generateLinkHeaders()
        val testVal = linkHeaders.toString()
        val testValArray = testVal.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        assertEquals("LAST: lastUrl", testValArray[2])
    }

    @Test
    fun toString_TestFirst() {
        val linkHeaders = generateLinkHeaders()
        val testVal = linkHeaders.toString()
        val testValArray = testVal.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        assertEquals("FIRST: firstUrl", testValArray[3])
    }

    private fun generateLinkHeaders(): LinkHeaders {
        return LinkHeaders("prevUrl", "nextUrl", "lastUrl", "firstUrl")
    }
}