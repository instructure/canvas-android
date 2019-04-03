/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.annotations

import org.junit.Assert
import org.junit.Test

class AnnotationUtilsTest : Assert() {

    //region 0
    @Test
    fun testCalculateRotationOffset_0_0() {
        assertEquals(0, calculateRotationOffset(0, 0))
    }

    @Test
    fun testCalculateRotationOffset_0_90() {
        assertEquals(90, calculateRotationOffset(0, 90))
    }

    @Test
    fun testCalculateRotationOffset_0_180() {
        assertEquals(180, calculateRotationOffset(0, 180))
    }

    @Test
    fun testCalculateRotationOffset_0_270() {
        assertEquals(270, calculateRotationOffset(0, 270))
    }
    //endregion

    //region 90
    @Test
    fun testCalculateRotationOffset_90_0() {
        assertEquals(270, calculateRotationOffset(90, 0))
    }

    @Test
    fun testCalculateRotationOffset_90_90() {
        assertEquals(0, calculateRotationOffset(90, 90))
    }

    @Test
    fun testCalculateRotationOffset_90_180() {
        assertEquals(90, calculateRotationOffset(90, 180))
    }

    @Test
    fun testCalculateRotationOffset_90_270() {
        assertEquals(180, calculateRotationOffset(90, 270))
    }
    //endregion

    //region 180
    @Test
    fun testCalculateRotationOffset_180_0() {
        assertEquals(180, calculateRotationOffset(180, 0))
    }

    @Test
    fun testCalculateRotationOffset_180_90() {
        assertEquals(270, calculateRotationOffset(180, 90))
    }

    @Test
    fun testCalculateRotationOffset_180_180() {
        assertEquals(0, calculateRotationOffset(180, 180))
    }

    @Test
    fun testCalculateRotationOffset_180_270() {
        assertEquals(90, calculateRotationOffset(180, 270))
    }
    //endregion

    //region 270
    @Test
    fun testCalculateRotationOffset_270_0() {
        assertEquals(90, calculateRotationOffset(270, 0))
    }

    @Test
    fun testCalculateRotationOffset_270_90() {
        assertEquals(180, calculateRotationOffset(270, 90))
    }

    @Test
    fun testCalculateRotationOffset_270_180() {
        assertEquals(270, calculateRotationOffset(270, 180))
    }

    @Test
    fun testCalculateRotationOffset_270_270() {
        assertEquals(0, calculateRotationOffset(270, 270))
    }
    //endregion
}