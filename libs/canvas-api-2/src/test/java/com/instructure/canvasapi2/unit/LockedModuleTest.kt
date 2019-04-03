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

import com.instructure.canvasapi2.models.LockedModule

import org.junit.Test

import org.junit.Assert.assertEquals

class LockedModuleTest {
    private val validLockedModule: LockedModule
        get() {
            return LockedModule(contextId = 1, name = "module", unlockAt = "2017-01-15T15:53:00+07:00")
        }

    @Test
    fun isLockedModuleValid_InvalidContextId() {
        val module = validLockedModule.copy(contextId = 0)
        assertEquals(false, LockedModule.isLockedModuleValid(module))
    }

    @Test
    fun isLockedModuleValid_InvalidName() {
        val module = validLockedModule.copy(name = null)
        assertEquals(false, LockedModule.isLockedModuleValid(module))
    }

    @Test
    fun isLockedModuleValid_InvalidUnlockDate() {
        val module = validLockedModule.copy(unlockAt = null)
        assertEquals(false, LockedModule.isLockedModuleValid(module))
    }

    @Test
    fun isLockedModuleValid_NullPrerequisites() {
        val module = validLockedModule.copy(prerequisites = null)
        assertEquals(false, LockedModule.isLockedModuleValid(module))
    }

    @Test
    fun isLockedModuleValid_ValidModule() {
        val module = validLockedModule
        assertEquals(true, LockedModule.isLockedModuleValid(module))
    }
}