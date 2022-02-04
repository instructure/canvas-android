package com.instructure.teacher.ui.pages

import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.*
import com.instructure.espresso.swipeDown
import org.hamcrest.Matchers.allOf
import com.instructure.teacher.R

class ModulesPage : BasePage() {

    fun assertEmptyView() {
        onView(allOf(withId(R.id.moduleListEmptyView), withAncestor(R.id.moduleList))).assertDisplayed()
    }

    fun assertModuleIsUnpublished() {
        onView(withId(R.id.unpublishedIcon)).assertDisplayed()
    }

    fun assertModuleIsPublished() {
        onView(withId(R.id.unpublishedIcon)).assertNotDisplayed()
        onView(withId(R.id.publishedIcon)).assertDisplayed()
    }

    fun assertModuleIsPresent(moduleTitle: String) {
        onView(allOf(withId(R.id.moduleName), withText(moduleTitle))).assertDisplayed()
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), withAncestor(R.id.moduleList))).swipeDown()
    }

    fun assertModuleItemIsPresent(itemTitle: String) {
        onView(allOf(withId(R.id.moduleItemTitle), withText(itemTitle))).assertDisplayed()
    }
}