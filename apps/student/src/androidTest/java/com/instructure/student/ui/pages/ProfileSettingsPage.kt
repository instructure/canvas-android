package com.instructure.student.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.student.R
import org.hamcrest.Matchers.not

class ProfileSettingsPage : BasePage(R.id.profile_settings_fragment) {
    private val editUsername by OnViewWithId(R.id.editUsername)
    private val editPhoto by OnViewWithId(R.id.editPhoto)
    private val createPandaAvatar by OnViewWithId(R.id.createPandaAvatar)

    fun changeUserNameTo(newUserName: String) {
        editUsername.click()

        onView(withId(R.id.textInput)).perform(clearText())
        onView(withId(R.id.textInput)).perform(typeText(newUserName))
        if(CanvasTest.isLandscapeDevice()) Espresso.pressBack()
        onView(containsTextCaseInsensitive("OK")).click()
    }

    fun assertSettingsDisabled() {
        editUsername.check(matches(not(isEnabled())))
        editPhoto.check(matches(not(isEnabled())))
        createPandaAvatar.check(matches(not(isEnabled())))
    }

    fun launchPandaAvatarCreator() {
        createPandaAvatar.click()
    }
}