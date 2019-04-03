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
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.parentapp.R

@Suppress("unused")
class LoginLandingPage : BasePage() {

    private val canvasLogoImageView by OnViewWithId(R.id.canvasLogo)
    private val findMySchoolButton by OnViewWithId(R.id.findMySchool)
    private val canvasNetworkTextView by OnViewWithId(R.id.canvasNetwork)
    private val previousLoginWrapper by OnViewWithId(R.id.previousLoginWrapper, autoAssert = false)
    private val previousLoginTitleText by  OnViewWithId(R.id.previousLoginTitleText, autoAssert = false)
    private val previousLoginDivider by  OnViewWithId(R.id.previousLoginDivider, autoAssert = false)
    private val previousLoginRecyclerView by  OnViewWithId(R.id.previousLoginRecyclerView, autoAssert = false)
    private val canvasNameTextView by OnViewWithId(R.id.canvasName, autoAssert = false)
    private val appDescriptionTypeTextView by OnViewWithId(R.id.appDescriptionType, autoAssert = false)

    fun clickFindMySchoolButton() {
        findMySchoolButton.click()
    }

    fun clickCanvasNetworkButton() {
        canvasNetworkTextView.click()
    }

    fun assertDisplaysCanvasName() {
        canvasNameTextView.assertDisplayed()
    }

    fun assertDisplaysAppDescriptionType() {
        appDescriptionTypeTextView.assertDisplayed()
    }
}
