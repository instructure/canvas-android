package com.instructure.teacher.ui.pages

import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withChild
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.teacher.R
import org.hamcrest.Matchers.allOf

/**
 * Represents the Modules Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the modules
 * page. It contains various assertion methods to verify the presence and visibility of module items
 * and icons. It also includes methods for refreshing the page and clicking on the collapse/expand icon.
 */
class ModulesPage : BasePage() {

    /**
     * Asserts that the empty view is displayed on the modules page.
     */
    fun assertEmptyView() {
        onView(allOf(withId(R.id.moduleListEmptyView), withAncestor(R.id.moduleList))).assertDisplayed()
    }

    /**
     * Asserts that the module is not published.
     */
    fun assertModuleNotPublished() {
        onView(withId(R.id.unpublishedIcon)).assertDisplayed()
        onView(withId(R.id.publishedIcon)).assertNotDisplayed()
    }

    /**
     * Asserts that the module is published.
     */
    fun assertModuleIsPublished() {
        onView(withId(R.id.unpublishedIcon)).assertNotDisplayed()
        onView(withId(R.id.publishedIcon)).assertDisplayed()
    }

    /**
     * Asserts that the module with the specified title is displayed.
     *
     * @param moduleTitle The title of the module.
     */
    fun assertModuleIsDisplayed(moduleTitle: String) {
        onView(allOf(withId(R.id.moduleName), withText(moduleTitle))).assertDisplayed()
    }

    /**
     * Refreshes the modules page.
     */
    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), withAncestor(R.id.moduleList))).swipeDown()
    }

    /**
     * Asserts that the module item with the specified title is displayed.
     *
     * @param itemTitle The title of the module item.
     */
    fun assertModuleItemIsDisplayed(itemTitle: String) {
        onView(allOf(withId(R.id.moduleItemTitle), withText(itemTitle))).assertDisplayed()
    }

    /**
     * Asserts that the module item with the specified name is published.
     *
     * @param moduleItemName The name of the module item.
     */
    fun assertModuleItemIsPublished(moduleItemName: String) {
        val siblingChildMatcher = withChild(withId(R.id.moduleItemTitle) + withText(moduleItemName))
        onView(withId(R.id.moduleItemPublishedIcon) + hasSibling(siblingChildMatcher)).assertDisplayed()
        onView(withId(R.id.moduleItemUnpublishedIcon) + hasSibling(siblingChildMatcher)).assertNotDisplayed()
    }

    /**
     * Asserts that the module item with the specified title is not published.
     *
     * @param moduleTitle The title of the module.
     * @param moduleItemName The name of the module item.
     */
    fun assertModuleItemNotPublished(moduleTitle: String, moduleItemName: String) {
        val siblingChildMatcher = withChild(withId(R.id.moduleItemTitle) + withText(moduleItemName))
        onView(withId(R.id.moduleItemUnpublishedIcon) + hasSibling(siblingChildMatcher)).assertDisplayed()
        onView(withId(R.id.moduleItemPublishedIcon) + hasSibling(siblingChildMatcher)).assertNotDisplayed()
    }

    /**
     * Clicks on the collapse/expand icon.
     */
    fun clickOnCollapseExpandIcon() {
        onView(withId(R.id.collapseIcon)).click()
    }

    /**
     * Asserts the item count in the module with the specified title.
     *
     * @param moduleTitle The title of the module.
     * @param expectedCount The expected item count in the module.
     */
    fun assertItemCountInModule(moduleTitle: String, expectedCount: Int) {
        onView(withId(R.id.recyclerView) + withDescendant(withId(R.id.moduleName) +
                withText(moduleTitle))).waitForCheck(RecyclerViewItemCountAssertion(expectedCount + 1)) // Have to increase by one because of the module title element itself.
    }
}