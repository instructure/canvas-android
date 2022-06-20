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
 *
 */
package com.instructure.espresso

import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers
import androidx.viewpager.widget.ViewPager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import org.hamcrest.Matchers
import java.lang.ClassCastException

class RecyclerViewItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val itemCount = (view as? RecyclerView)?.adapter?.itemCount
                ?: throw ClassCastException("View of type ${view.javaClass.simpleName} must be a RecyclerView")
        ViewMatchers.assertThat(itemCount, Matchers.`is`(expectedCount))
    }
}

class RecyclerViewItemCountGreaterThanAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val itemCount = (view as? RecyclerView)?.adapter?.itemCount
            ?: throw ClassCastException("View of type ${view.javaClass.simpleName} must be a RecyclerView")
        ViewMatchers.assertThat(itemCount, Matchers.greaterThan(expectedCount))
    }
}

class ViewPagerItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val count = (view as? ViewPager)?.adapter?.count
                ?: throw ClassCastException("View of type ${view.javaClass.simpleName} must be a ViewPager")
        ViewMatchers.assertThat(count, Matchers.`is`(expectedCount))
    }
}
