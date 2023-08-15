package com.instructure.espresso

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForView

class Searchable(private val searchButtonId: Int? = null, private val queryInputId: Int? = null, private val clearButtonId: Int? = null, private val backButtonId: Int? = null) {

    fun clickOnSearchButton() {
        onView(searchButtonId?.let { withId(it) }).click()
    }

    fun typeToSearchBar(textToType: String) {
        onView(queryInputId?.let { withId(it) }).perform(ViewActions.replaceText(textToType))
    }

    fun clickOnClearSearchButton() {
        waitForView(clearButtonId?.let { withId(it) }).click()
    }

    fun pressSearchBackButton() {
        onView(backButtonId?.let { withId(it) }).click()
    }
}