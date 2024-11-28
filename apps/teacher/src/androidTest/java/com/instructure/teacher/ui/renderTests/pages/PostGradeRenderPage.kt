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
package com.instructure.teacher.ui.renderTests.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.pages.BasePage
import com.instructure.teacher.R

class PostGradeRenderPage : BasePage(R.id.postGradePage) {

    val postPolicyStatusCount by OnViewWithId(R.id.postPolicyStatusCount)
    val postPolicyOnlyGradedRow by OnViewWithId(R.id.postPolicyOnlyGradedRow)
    val postPolicySectionToggleHolder by OnViewWithId(R.id.postPolicySectionToggleHolder)
    val postPolicyRecycler by OnViewWithId(R.id.postPolicyRecycler)
    val postGradeButton by OnViewWithId(R.id.postGradeButton)
    val postEmptyLayout by OnViewWithId(R.id.postEmptyLayout)

    fun assertPostedView(withSectionsOn: Boolean) {
        postEmptyLayout.assertNotDisplayed()

        postGradeButton.assertDisplayed()
        postPolicyStatusCount.assertDisplayed()
        postPolicyOnlyGradedRow.assertDisplayed()
        postPolicySectionToggleHolder.assertDisplayed()
        if (withSectionsOn) {
            postPolicyRecycler.assertDisplayed()
        } else {
            postPolicyRecycler.assertNotDisplayed()
        }
    }

    fun assertHiddenView(withSectionsOn: Boolean) {
        postEmptyLayout.assertNotDisplayed()
        postPolicyOnlyGradedRow.assertNotDisplayed()

        postGradeButton.assertDisplayed()
        postPolicyStatusCount.assertDisplayed()
        postPolicySectionToggleHolder.assertDisplayed()
        if (withSectionsOn) {
            postPolicyRecycler.assertDisplayed()
        } else {
            postPolicyRecycler.assertNotDisplayed()
        }
    }

    fun assertEmptyView() {
        postEmptyLayout.assertDisplayed()

        postPolicyStatusCount.assertNotDisplayed()
        postPolicyOnlyGradedRow.assertNotDisplayed()
        postPolicySectionToggleHolder.assertNotDisplayed()
        postPolicyRecycler.assertNotDisplayed()
        postGradeButton.assertNotDisplayed()
    }

}
