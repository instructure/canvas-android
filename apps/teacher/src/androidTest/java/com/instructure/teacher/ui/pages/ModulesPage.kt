package com.instructure.teacher.ui.pages

import androidx.annotation.StringRes
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withChild
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.ViewAlphaAssertion
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasContentDescription
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withDescendant
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withParent
import com.instructure.espresso.pages.withText
import com.instructure.espresso.scrollTo
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

    fun assertModuleNotPublished(moduleTitle: String) {
        onView(withId(R.id.unpublishedIcon) + withParent(hasSibling(withId(R.id.moduleName) + withText(moduleTitle)))).assertDisplayed()
        onView(withId(R.id.publishedIcon) + withParent(hasSibling(withId(R.id.moduleName) + withText(moduleTitle)))).assertNotDisplayed()
    }

    /**
     * Asserts that the module is published.
     */
    fun assertModuleIsPublished() {
        onView(withId(R.id.unpublishedIcon)).assertNotDisplayed()
        onView(withId(R.id.publishedIcon)).assertDisplayed()
    }

    fun assertModuleIsPublished(moduleTitle: String) {
        onView(withId(R.id.unpublishedIcon) + withParent(hasSibling(withId(R.id.moduleName) + withText(moduleTitle)))).assertNotDisplayed()
        onView(withId(R.id.publishedIcon) + withParent(hasSibling(withId(R.id.moduleName) + withText(moduleTitle)))).assertDisplayed()
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
     * Click on the module item with the given title to open it's detailer.
     *
     * @param itemTitle The title of the module item.
     */
    fun clickOnModuleItem(itemTitle: String) {
        onView(allOf(withId(R.id.moduleItemTitle), withText(itemTitle))).click()
    }

    /**
     * Asserts that the module item with the specified name is published.
     *
     * @param moduleItemName The name of the module item.
     */
    fun assertModuleItemIsPublished(moduleItemName: String) {
        onView(withAncestor(withChild(withText(moduleItemName))) + withId(R.id.moduleItemStatusIcon)).assertHasContentDescription(
            R.string.a11y_published
        )
    }

    /**
     * Asserts that the module item with the specified title is not published.
     *
     * @param moduleItemName The name of the module item.
     */
    fun assertModuleItemNotPublished(moduleItemName: String) {
        onView(withAncestor(withChild(withText(moduleItemName))) + withId(R.id.moduleItemStatusIcon)).assertHasContentDescription(
            R.string.a11y_unpublished
        )
    }

    /**
     * Assert module status icon alpha value.
     *
     * @param moduleItemName The name of the module item.
     * @param expectedAlphaValue The expected alpha (float) value.
     */
    fun assertModuleStatusIconAlpha(moduleItemName: String, expectedAlphaValue: Float) {
        onView(withId(R.id.moduleItemStatusIcon) + withParent(withId(R.id.publishActions) + hasSibling(withId(R.id.moduleItemTitle) + withText(moduleItemName)))).check(ViewAlphaAssertion(expectedAlphaValue))
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
        onView(
            withId(R.id.recyclerView) + withDescendant(
                withId(R.id.moduleName) +
                        withText(moduleTitle)
            )
        ).waitForCheck(RecyclerViewItemCountAssertion(expectedCount + 1)) // Have to increase by one because of the module title element itself.
    }

    fun assertToolbarMenuItems() {
        onView(withText(R.string.publishAllModulesAndItems)).assertDisplayed()
        onView(withText(R.string.publishModulesOnly)).assertDisplayed()
        onView(withText(R.string.unpublishAllModulesAndItems)).assertDisplayed()
    }

    fun clickItemOverflow(itemName: String) {
        onView(withParent(withChild(withText(itemName))) + withId(R.id.publishActions)).scrollTo().click()
    }

    fun assertModuleMenuItems() {
        onView(withText(R.string.publishModuleAndItems)).assertDisplayed()
        onView(withText(R.string.publishModuleOnly)).assertDisplayed()
        onView(withText(R.string.unpublishModuleAndItems)).assertDisplayed()
    }

    fun assertOverflowItem(@StringRes title: Int) {
        onView(withText(title)).assertDisplayed()
    }

    fun assertFileEditDialogVisible() {
        onView(withText(R.string.edit_permissions)).assertDisplayed()
    }

    fun clickOnText(@StringRes title: Int) {
        onView(withText(title)).click()
    }

    fun assertSnackbarText(@StringRes snackbarText: Int) {
        onView(withId(com.google.android.material.R.id.snackbar_text) + withText(snackbarText)).assertDisplayed()
    }

    fun assertSnackbarContainsText(snackbarText: String) {
        onView(withId(com.google.android.material.R.id.snackbar_text) + containsTextCaseInsensitive(snackbarText)).assertDisplayed()
    }

    fun assertModuleItemHidden(moduleItemName: String) {
        onView(withAncestor(withChild(withText(moduleItemName))) + withId(R.id.moduleItemStatusIcon)).assertHasContentDescription(
            R.string.a11y_hidden
        )
    }

    fun assertModuleItemScheduled(moduleItemName: String) {
        onView(withAncestor(withChild(withText(moduleItemName))) + withId(R.id.moduleItemStatusIcon)).assertHasContentDescription(
            R.string.a11y_scheduled
        )
    }
}