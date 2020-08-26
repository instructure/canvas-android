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
 *
 */
package com.instructure.student.ui

import com.instructure.student.ui.utils.StudentTest
import com.instructure.espresso.filters.P1
import org.junit.Test

class LoginLandingPageTest: StudentTest() {

    // Runs live; no MockCanvas
    @Test
    @P1
    override fun displaysPageObjects() {
        loginLandingPage.assertPageObjects()
    }

}
