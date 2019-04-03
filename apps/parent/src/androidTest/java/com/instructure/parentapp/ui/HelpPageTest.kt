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

import com.instructure.dataseeding.api.SeedApi
import com.instructure.espresso.ditto.Ditto
import com.instructure.parentapp.ui.utils.ParentTest
import com.instructure.parentapp.ui.utils.openNavDrawer
import com.instructure.parentapp.ui.utils.seedData
import com.instructure.parentapp.ui.utils.tokenLogin
import org.junit.Test

class HelpPageTest : ParentTest() {

    @Test
    @Ditto
    override fun displaysPageObjects() {
        getToHelpPage()
        helpPage.assertPageObjects()
    }

    private fun getToHelpPage(): SeedApi.SeededParentDataApiModel {
        val data = seedData(parents = 1, students = 1, courses = 1)
        tokenLogin(data.parentsList[0])
        viewStudentPage.openNavDrawer()
        navDrawerPage.clickHelp()
        return data
    }

}
