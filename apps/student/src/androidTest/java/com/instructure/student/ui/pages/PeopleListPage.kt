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
 *
 */
package com.instructure.student.ui.pages

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.User
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.withAncestor
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class PeopleListPage: BasePage(R.id.peopleListPage) {
    private val toolbar by OnViewWithId(R.id.toolbar)

    fun assertPersonListed(person: CanvasUserApiModel, role: String? = null)
    {
        var matcher : Matcher<View>? = null
        if(role == null) {
            matcher = allOf(withText(person.name), withId(R.id.title))
        }
        else {
            matcher = allOf(
                    withText(person.name),
                    withId(R.id.title),
                    hasSibling(
                            allOf(
                                    withId(R.id.role),
                                    withText(role)
                            )

                    )
            )
        }
        scrollToMatch(matcher)
        onView(matcher).assertDisplayed()
    }

    fun assertPersonListed(person: User)
    {
        val matcher = allOf(withText(person.name), withId(R.id.title))
        scrollToMatch(matcher)
        onView(matcher).assertDisplayed()
    }

    fun selectPerson(person: CanvasUserApiModel)
    {
        val matcher = allOf(withText(person.name), withId(R.id.title))
        scrollToMatch(matcher)
        onView(matcher).click()
    }

    fun selectPerson(user: User)
    {
        val matcher = allOf(withText(user.name), withId(R.id.title))
        scrollToMatch(matcher)
        onView(matcher).click()
    }

    private fun scrollToMatch(matcher: Matcher<View>) {
        onView(allOf(withId(R.id.listView), isDisplayed(), withAncestor(R.id.peopleListPage)))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(matcher)))
    }

}