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
package com.emeritus.student.ui

import com.emeritus.student.ui.utils.StudentTest
import com.emeritus.student.ui.utils.enterDomain
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class LoginSignInPageTest: StudentTest() {

    // Runs live; no MockCanvas
    @Test
    override fun displaysPageObjects() {
        loginLandingPage.clickFindMySchoolButton()
        enterDomain()
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.assertPageObjects()
    }
}
