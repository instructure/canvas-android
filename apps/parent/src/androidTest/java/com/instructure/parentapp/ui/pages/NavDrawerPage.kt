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
package com.instructure.parentapp.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.parentapp.R

class NavDrawerPage : BasePage(R.id.navigationDrawer) {

    private val help by OnViewWithId(R.id.navigationDrawerItem_help)
    private val changeUser by OnViewWithId(R.id.navigationDrawerItem_changeUser)
    private val logout by OnViewWithId(R.id.navigationDrawerItem_logout)
    private val manageChildren by OnViewWithId(R.id.navigationDrawerItem_manageChildren)
    private val startMasquerading by OnViewWithId(R.id.navigationDrawerItem_startMasquerading, false)
    private val stopMasquerading by OnViewWithId(R.id.navigationDrawerItem_stopMasquerading, false)

    fun clickHelp() = help.click()

    fun clickChangeUser() = changeUser.click()

    fun clickLogout() = logout.click()

    fun clickManageChildren() = manageChildren.click()

    fun clickStartMasquerading() = startMasquerading.click()

    fun clickStopMasquerading() = stopMasquerading.click()

}
