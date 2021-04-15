package com.instructure.teacher.ui.pages

import androidx.test.espresso.Espresso
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withId
import com.instructure.espresso.replaceText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R

class EditDiscussionsDetailsPage : BasePage() {

    fun editTitle(newTitle: String) {
        onView(withId(R.id.editDiscussionName)).replaceText(newTitle)
        Espresso.closeSoftKeyboard()
    }

    fun switchPublished() {
        onView(withId(R.id.publishSwitch)).scrollTo()
        onView(withId(R.id.publishSwitch)).click()
    }

    fun deleteDiscussion() {
        onView(withId(R.id.deleteText)).scrollTo()
        onView(withId(R.id.deleteText)).click()
        onView(withId(android.R.id.button1)).click()
    }

    fun clickSave() {
        onView(withId(R.id.menuSave)).click()
    }
}