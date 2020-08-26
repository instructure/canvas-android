/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.espresso.actions

import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.util.HumanReadables
import androidx.viewpager.widget.ViewPager
import android.view.View
import org.hamcrest.Matcher

class SetViewPagerCurrentItemAction(private val pageNumber: Int) : ViewAction {

    override fun getDescription() = "set ViewPager current item to $pageNumber"

    override fun getConstraints(): Matcher<View> = ViewMatchers.isAssignableFrom(ViewPager::class.java)

    override fun perform(uiController: UiController, view: View?) {
        val pager = view as ViewPager

        val adapter = pager.adapter ?: throw PerformException.Builder()
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(view))
                .withCause(RuntimeException("ViewPager adapter cannot be null"))
                .build()

        if (pageNumber >= adapter.count) throw PerformException.Builder()
                .withActionDescription(this.description)
                .withViewDescription(HumanReadables.describe(view))
                .withCause(IndexOutOfBoundsException("Requested page $pageNumber in ViewPager of size ${adapter.count}"))
                .build()

        pager.setCurrentItem(pageNumber, false)

        uiController.loopMainThreadUntilIdle()
    }

}
