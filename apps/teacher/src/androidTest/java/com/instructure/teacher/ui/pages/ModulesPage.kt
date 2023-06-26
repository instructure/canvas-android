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

class ModulesPage : BasePage() {

    fun assertEmptyView() {
        onView(allOf(withId(R.id.moduleListEmptyView), withAncestor(R.id.moduleList))).assertDisplayed()
    }

    fun assertModuleNotPublished() {
        onView(withId(R.id.unpublishedIcon)).assertDisplayed()
        onView(withId(R.id.publishedIcon)).assertNotDisplayed()
    }

    fun assertModuleIsPublished() {
        onView(withId(R.id.unpublishedIcon)).assertNotDisplayed()
        onView(withId(R.id.publishedIcon)).assertDisplayed()
    }

    fun assertModuleIsDisplayed(moduleTitle: String) {
        onView(allOf(withId(R.id.moduleName), withText(moduleTitle))).assertDisplayed()
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), withAncestor(R.id.moduleList))).swipeDown()
    }

    fun assertModuleItemIsDisplayed(itemTitle: String) {
        onView(allOf(withId(R.id.moduleItemTitle), withText(itemTitle))).assertDisplayed()
    }

    fun assertModuleItemIsPublished(moduleItemName: String) {
        val siblingChildMatcher = withChild(withId(R.id.moduleItemTitle) + withText(moduleItemName))
        onView(withId(R.id.moduleItemPublishedIcon) + hasSibling(siblingChildMatcher)).assertDisplayed()
        onView(withId(R.id.moduleItemUnpublishedIcon) + hasSibling(siblingChildMatcher)).assertNotDisplayed()
    }

    fun assertModuleItemNotPublished(moduleTitle: String, moduleItemName: String) {
        val siblingChildMatcher = withChild(withId(R.id.moduleItemTitle) + withText(moduleItemName))
        onView(withId(R.id.moduleItemUnpublishedIcon) + hasSibling(siblingChildMatcher)).assertDisplayed()
        onView(withId(R.id.moduleItemPublishedIcon) + hasSibling(siblingChildMatcher)).assertNotDisplayed()
    }

    fun clickOnCollapseExpandIcon() {
        onView(withId(R.id.collapseIcon)).click()
    }

    fun assertItemCountInModule(moduleTitle: String, expectedCount: Int) {
        onView(withId(R.id.recyclerView) + withDescendant(withId(R.id.moduleName) +
                withText(moduleTitle))).waitForCheck(RecyclerViewItemCountAssertion(expectedCount + 1)) // Have to increase by one because of the module title element itself.
    }
}