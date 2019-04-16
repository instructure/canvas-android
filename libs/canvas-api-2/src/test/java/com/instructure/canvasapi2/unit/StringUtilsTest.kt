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
package com.instructure.canvasapi2.unit

import com.instructure.canvasapi2.utils.replaceFirstAfter
import org.junit.Assert
import org.junit.Test

class StringUtilsTest : Assert() {

    @Test
    fun `replaceFirstAfter replaces first occurrence after specified delimiter`() {
        val source = "The brown, quick brown fox jumps over the quick brown fox."
        val expected = "The brown, quick red fox jumps over the quick brown fox."
        val actual = source.replaceFirstAfter("quick", "brown", "red")
        assertEquals(expected, actual)
    }

    @Test
    fun `replaceFirstAfter replaces first occurrence if delimiter is not found`() {
        val source = "The brown, quick brown fox jumps over the quick brown fox."
        val expected = "The red, quick brown fox jumps over the quick brown fox."
        val actual = source.replaceFirstAfter("amazing", "brown", "red")
        assertEquals(expected, actual)
    }

    @Test
    fun `replaceFirstAfter returns unmodified string if oldValue is not found`() {
        val source = "The brown, quick brown fox jumps over the quick brown fox."
        val actual = source.replaceFirstAfter("quick", "amazing", "red")
        assertEquals(source, actual)
    }

}
