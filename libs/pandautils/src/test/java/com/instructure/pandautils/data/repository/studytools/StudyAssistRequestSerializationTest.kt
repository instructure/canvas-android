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
 *
 */
package com.instructure.pandautils.data.repository.studytools

import com.google.gson.Gson
import com.instructure.canvasapi2.models.studyassist.StudyAssistRequest
import com.instructure.canvasapi2.models.studyassist.StudyAssistState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The chips call is distinguished from a tool run purely by the *absence* of `prompt`. Gson's
 * default configuration (the one the standard `RestBuilder` uses) omits nulls, and these tests fail
 * loudly if that ever changes to `serializeNulls`.
 */
class StudyAssistRequestSerializationTest {

    private val gson = Gson()

    @Test
    fun `a null prompt is omitted entirely rather than sent as null`() {
        val json = gson.toJson(StudyAssistRequest(state = state()))

        assertFalse(json.contains("prompt"))
    }

    @Test
    fun `a prompt is serialized when present`() {
        val json = gson.toJson(StudyAssistRequest(prompt = "Quiz me", state = state()))

        assertTrue(json.contains("\"prompt\":\"Quiz me\""))
    }

    @Test
    fun `state uses the server's camel case ID keys`() {
        val json = gson.toJson(StudyAssistRequest(state = state()))

        assertTrue(json.contains("\"courseID\":\"28713\""))
        assertTrue(json.contains("\"pageID\":\"html-parse-test\""))
    }

    @Test
    fun `caller app is snake case and defaults to canvas`() {
        val json = gson.toJson(StudyAssistRequest(state = state()))

        assertTrue(json.contains("\"caller_app\":\"canvas\""))
    }

    @Test
    fun `regenerate is always sent`() {
        assertTrue(gson.toJson(StudyAssistRequest(state = state())).contains("\"regenerate\":false"))
        assertTrue(
            gson.toJson(StudyAssistRequest(state = state(), regenerate = true))
                .contains("\"regenerate\":true")
        )
    }

    @Test
    fun `the chips body matches the shape the server expects`() {
        assertEquals(
            """{"state":{"courseID":"28713","pageID":"html-parse-test"},"regenerate":false,"caller_app":"canvas"}""",
            gson.toJson(StudyAssistRequest(state = state()))
        )
    }

    private fun state() = StudyAssistState(courseId = "28713", pageId = "html-parse-test")
}
