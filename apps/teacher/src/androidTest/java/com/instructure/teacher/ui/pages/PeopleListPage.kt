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
 */    package com.instructure.teacher.ui.pages

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.replaceText
import com.instructure.teacher.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class PeopleListPage : BasePage(R.id.peopleListPage) {

    fun clickPerson(user: CanvasUserApiModel) {
        waitForViewWithText(user.name).click()
    }

    fun assertPersonListed(person: CanvasUserApiModel, role: String? = null)
    {
        var matcher : Matcher<View>? = null
        if(role == null) {
            matcher = Matchers.allOf(ViewMatchers.withText(person.name), withId(R.id.userName))
        }
        else {
            matcher = Matchers.allOf(
                ViewMatchers.withText(person.name),
                withId(R.id.userName),
                ViewMatchers.hasSibling(
                    Matchers.allOf(
                        withId(R.id.userRole),
                        ViewMatchers.withText(role)
                    )

                )
            )
        }
        scrollToMatch(matcher)
        Espresso.onView(matcher).assertDisplayed()
    }


    fun assertSearchResultCount(expectedCount: Int) {
        onView(withId(R.id.recyclerView) + withAncestor(R.id.swipeRefreshLayout)).check(matches(hasChildCount(expectedCount))) //because of the CircleImageView, it's always there
    }

    private fun scrollToMatch(matcher: Matcher<View>) {
        Espresso.onView(
            Matchers.allOf(
                withId(R.id.recyclerView),
                ViewMatchers.isDisplayed(),
                withAncestor(R.id.peopleListPage)
            )
        )
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    ViewMatchers.hasDescendant(
                        matcher
                    )
                ))
    }

    fun clickSearchButton() {
        onView(withId(R.id.search)).click()
    }

    fun typeSearchInput(searchText: String) {
        onView(withId(R.id.search_src_text)).replaceText(searchText)
    }

    fun clickResetSearchText() {
        waitForView(withId(R.id.search_close_btn)).click()
    }

    fun assertEmptyViewIsDisplayed() {
        waitForView(withText(R.string.no_items_to_display_short) + withId(R.id.title) + withAncestor(withId(R.id.emptyPandaView))).assertDisplayed()
    }

    fun assertPersonRole(personName: String, role: UserRole) {
        onView(withId(R.id.userRole) + withText(role.roleName) + hasSibling(withId(R.id.userName) + withText(personName))).assertDisplayed()
    }

    enum class UserRole(val roleName: String) {
        TEACHER("Teacher"),
        STUDENT("Student"),
        OBSERVER("Observer")
    }
}
