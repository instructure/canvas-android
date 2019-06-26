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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class LoginInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit // Not used for interaction tests

    @Stub
    @Test
    fun testLogin_canFindSchool() {
        // Should be able to search for and select a school in the "What's your school's name?" page
    }

}
