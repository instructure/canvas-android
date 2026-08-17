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
package com.instructure.canvasapi2.models

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteTypeTest {

    @Test
    fun `fromReactions maps Important to IMPORTANT`() {
        assertEquals(NoteType.IMPORTANT, NoteType.fromReactions(listOf("Important")))
    }

    @Test
    fun `fromReactions maps Confusing to CONFUSING`() {
        assertEquals(NoteType.CONFUSING, NoteType.fromReactions(listOf("Confusing")))
    }

    @Test
    fun `fromReactions is case-insensitive`() {
        assertEquals(NoteType.IMPORTANT, NoteType.fromReactions(listOf("important")))
    }

    @Test
    fun `fromReactions maps empty list to OTHER`() {
        assertEquals(NoteType.OTHER, NoteType.fromReactions(emptyList()))
    }

    @Test
    fun `fromReactions maps an unrecognized value to OTHER`() {
        assertEquals(NoteType.OTHER, NoteType.fromReactions(listOf("Helpful")))
    }

    @Test
    fun `fromReactions uses the first reaction when multiple are present`() {
        assertEquals(NoteType.CONFUSING, NoteType.fromReactions(listOf("Confusing", "Important")))
    }
}
