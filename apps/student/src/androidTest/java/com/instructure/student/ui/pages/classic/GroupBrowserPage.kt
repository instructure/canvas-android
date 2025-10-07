package com.instructure.student.ui.pages.classic

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.canvasapi2.models.Group
import com.instructure.dataseeding.model.GroupApiModel
import com.instructure.espresso.assertHasText
import com.instructure.student.R
import org.hamcrest.Matchers

class GroupBrowserPage : CourseBrowserPage() {

    fun assertTitleCorrect(group: Group) {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.courseBrowserTitle),
                ViewMatchers.isDisplayed()
            )
        ).assertHasText(group.name!!)
    }

    fun assertTitleCorrect(group: GroupApiModel) {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.courseBrowserTitle),
                ViewMatchers.isDisplayed()
            )
        ).assertHasText(group.name!!)
    }
}