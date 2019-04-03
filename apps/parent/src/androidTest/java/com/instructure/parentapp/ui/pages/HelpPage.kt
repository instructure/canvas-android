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
@file:Suppress("unused")

package com.instructure.parentapp.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertHasChild
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.withText
import com.instructure.parentapp.R

class HelpPage : BasePage() {

    private val canvasGuides by OnViewWithId(R.id.searchGuides)
    private val reportProblem by OnViewWithId(R.id.reportProblem)
    private val requestFeature by OnViewWithId(R.id.requestFeature)
    private val shareLove by OnViewWithId(R.id.shareLove)
    private val legal by OnViewWithId(R.id.legal)

    override fun assertPageObjects() {
        super.assertPageObjects()

        canvasGuides.assertHasChild(withText(R.string.searchGuides))
        canvasGuides.assertHasChild(withText(R.string.searchGuidesDetails))

        reportProblem.assertHasChild(withText(R.string.reportProblem))
        reportProblem.assertHasChild(withText(R.string.reportProblemDetails))

        requestFeature.assertHasChild(withText(R.string.requestFeature))
        requestFeature.assertHasChild(withText(R.string.requestFeatureDetails))

        shareLove.assertHasChild(withText(R.string.shareYourLove))
        shareLove.assertHasChild(withText(R.string.shareYourLoveDetails))

        legal.assertHasChild(withText(R.string.legal))
        legal.assertHasChild(withText(R.string.legalDetails))
    }

    fun clickCanvasGuides() = canvasGuides.click()

    fun clickReportProblem() = reportProblem.click()

    fun clickRequestFeature() = requestFeature.click()

    fun clickShareLove() = shareLove.click()

    fun clickLegal() = legal.click()

}
