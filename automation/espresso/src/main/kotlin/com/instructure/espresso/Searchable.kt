package com.instructure.espresso

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForView

class Searchable(private val searchButtonId: Int? = null, private val queryInputId: Int? = null, private val clearButtonId: Int? = null, private val backButtonId: Int? = null) {

    fun clickOnSearchButton() {
        onView(withId(searchButtonId!!)).click()
    }

    fun typeToSearchBar(textToType: String) {
        onView(withId(queryInputId!!)).perform(ViewActions.replaceText(textToType))
    }

    fun clickOnClearSearchButton() {
        waitForView(withId(clearButtonId!!)).click()
    }

    fun pressSearchBackButton() {
        onView(withId(backButtonId!!)).click()
    }
}