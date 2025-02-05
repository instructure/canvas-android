package com.instructure.espresso

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.espresso.page.plus

class ModuleItemInteractions(private val moduleNameId: Int? = null, private val nextArrowId: Int? = null, private val previousArrowId: Int? = null) {

    /**
     * Assert module name displayed
     *
     * @param moduleName
     */
    fun assertModuleNameDisplayed(moduleName: String) {
        onView(moduleNameId?.let { withId(it) + ViewMatchers.withText(moduleName)}).assertDisplayed()
    }


    /**
     * Click on next arrow to navigate to the next module item's details
     *
     */
    fun clickOnNextArrow() {
        onView(nextArrowId?.let { withId(it) }).click()
    }

    /**
     * Click on previous arrow to navigate to the previous module item's details
     *
     */
    fun clickOnPreviousArrow() {
        onView(previousArrowId?.let { withId(it) }).click()
    }

    /**
     * Assert previous arrow not displayed (e.g. invisible)
     *
     */
    fun assertPreviousArrowNotDisplayed() {
        onView(previousArrowId?.let { withId(it) }).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.INVISIBLE)))
    }

    /**
     * Assert previous arrow displayed
     *
     */
    fun assertPreviousArrowDisplayed() {
        onView(previousArrowId?.let { withId(it) }).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE)))
    }

    /**
     * Assert next arrow displayed
     *
     */
    fun assertNextArrowDisplayed() {
        onView(nextArrowId?.let { withId(it) }).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.VISIBLE)))
    }

    /**
     * Assert next arrow not displayed (e.g. invisible)
     *
     */
    fun assertNextArrowNotDisplayed() {
        onView(nextArrowId?.let { withId(it) }).check(
            ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(
                    ViewMatchers.Visibility.INVISIBLE)))
    }
}