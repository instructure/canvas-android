package com.emeritus.student.db.sqlColAdapters

/*
 * Copyright (C) 2019 - present Instructure, Inc.
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

import com.instructure.canvasapi2.models.CanvasContext

import org.junit.Assert.*
import org.junit.Test

class CanvasContextColAdapterTest {

    @Test
    fun `CourseContext decodes to correct CanvasContext type`() {
        val encodedCourse = "course_123"
        val expectedValue = CanvasContext.emptyCourseContext(123L)

        assertEquals(expectedValue, CanvasContextColAdapter().decode(encodedCourse))
    }

    @Test
    fun `Non course or group decodes to Unknown CanvasContext type`() {
        val encodedUnknown = "section_1234"
        val expectedValue = CanvasContext.defaultCanvasContext()
        val actualValue = CanvasContextColAdapter().decode(encodedUnknown)

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `Course context encodes correctly`() {
        val expectedValue = "course_1234"
        val decodedCourse = CanvasContext.emptyCourseContext(id = 1234L)
        val actualValue = CanvasContextColAdapter().encode(decodedCourse)

        assertEquals(expectedValue, actualValue)
    }

    @Test
    fun `Unknown context encodes correctly`() {
        val expectedValue = "unknown_-1"
        val decodedUnknown = CanvasContext.defaultCanvasContext()
        val actualValue = CanvasContextColAdapter().encode(decodedUnknown)

        assertEquals(expectedValue, actualValue)
    }
}