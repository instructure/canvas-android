package com.instructure.student.ui.pages.classic

import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.stringContainsTextCaseInsensitive
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.scrollTo
import com.instructure.student.R
import org.hamcrest.Matchers.allOf

class PandaAvatarPage : BasePage(R.id.panda_create_layout) {

    fun selectChangeHead() {
        onView(withId(R.id.changeHead)).scrollTo().click()
    }

    fun selectChangeBody() {
        onView(withId(R.id.changeBody)).scrollTo().click()
    }

    fun selectChangeLegs() {
        onView(withId(R.id.changeLegs)).scrollTo().click()
    }

    fun choosePart(contentDescriptionStringId: Int) {
        onView(allOf(
                withParent(R.id.partsContainer),
                withContentDescription(contentDescriptionStringId)
        )).scrollTo().click()
    }

    fun clickBackButton() {
        onView(withId(R.id.backButton)).click()
    }

    fun setAsAvatar() {
        onView(withContentDescription(stringContainsTextCaseInsensitive("More options"))).click()
        onView(allOf(withId(R.id.title), withText(R.string.setAsAvatar))).click()
    }

    fun save() {
        onView(withContentDescription(stringContainsTextCaseInsensitive("More options"))).click()
        onView(allOf(withId(R.id.title), withText(R.string.save))).click()
    }

}