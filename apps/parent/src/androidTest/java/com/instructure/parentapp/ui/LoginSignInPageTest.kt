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
package com.instructure.parentapp.ui

import com.instructure.dataseeding.util.CanvasRestAdapter
import com.instructure.parentapp.ui.utils.ParentTest
import com.instructure.parentapp.ui.utils.seedData
import org.junit.Test

class LoginSignInPageTest: ParentTest() {

    // No Ditto
    @Test
    override fun displaysPageObjects() {
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.enterDomain(CanvasRestAdapter.canvasDomain)
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.assertPageObjects()
    }

    // No Ditto
    @Test
    fun performsSlowLogin() {
        val data = seedData(parents = 1, courses = 1, students = 1)
        val parent = data.parentsList[0]
        loginLandingPage.clickFindMySchoolButton()
        loginFindSchoolPage.enterDomain(parent.domain)
        loginFindSchoolPage.clickToolbarNextMenuItem()
        loginSignInPage.loginAs(parent)
        viewStudentPage.assertPageObjects()
    }
}
