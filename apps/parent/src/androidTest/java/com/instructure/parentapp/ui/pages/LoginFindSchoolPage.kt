/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.replaceText
import com.instructure.parentapp.R

@Suppress("unused")
class LoginFindSchoolPage: BasePage() {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val whatsYourSchoolNameTextView by OnViewWithId(R.id.whatsYourSchoolName)
    private val topDivider by OnViewWithId(R.id.topDivider)
    private val bottomDivider by OnViewWithId(R.id.bottomDivider)
    private val domainInputEditText by OnViewWithId(R.id.domainInput)
    private val findSchoolRecyclerView by OnViewWithId(R.id.findSchoolRecyclerView)
    private val toolbarNextMenuButton by OnViewWithId(R.id.next)

    fun clickToolbarNextMenuItem() {
        toolbarNextMenuButton.click()
    }

    fun enterDomain(domain: String) {
        domainInputEditText.replaceText(domain)
    }
}
