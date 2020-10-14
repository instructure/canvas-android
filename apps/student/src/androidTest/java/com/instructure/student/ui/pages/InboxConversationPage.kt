/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.pages

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.MenuPopupWindow
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withHint
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.stringContainsTextCaseInsensitive
import com.instructure.canvas.espresso.explicitClick
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithContentDescription
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithHint
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.replaceText
import com.instructure.espresso.swipeUp
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.student.R
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher

class InboxConversationPage : BasePage(R.id.inboxConversationPage) {

    fun replyToMessage(message: String) {
        waitForViewWithText(R.string.reply).click()
        waitForViewWithHint(R.string.message).replaceText(message)
        onViewWithContentDescription("Send").perform(explicitClick())
        // Wait for reply to propagate, and for us to return to the email thread page
        waitForView(withId(R.id.starred)).assertDisplayed()
    }

    fun replyAllToMessage(replyMessage: String, expectedChipCount: Int) {
        onView(withId(R.id.messageOptions)).click()
        onView(withText("Reply All")).click()
        onView(withId(R.id.chipGroup)).check(matches(hasChildCount(expectedChipCount)))
        onView(withHint(R.string.message)).replaceText(replyMessage)
        onView(withContentDescription("Send")).perform(explicitClick())
        onView(allOf(withId(R.id.messageBody), withText(replyMessage))).assertDisplayed()
    }

    fun markUnread() {
        onView(withContentDescription(stringContainsTextCaseInsensitive("More options"))).click()
        onView(withText("Mark as Unread")).click()
    }

    fun archive() {
        onView(withContentDescription(stringContainsTextCaseInsensitive("More options"))).click()
        onView(withText("Archive")).click()
    }

    fun deleteConversation() {
        onView(withContentDescription(stringContainsTextCaseInsensitive("More options"))).click()
        onView(withText("Delete")).click()
        onView(allOf(isAssignableFrom(AppCompatButton::class.java), containsTextCaseInsensitive("DELETE")))
                .click() // Confirmation click
    }

    fun deleteMessage(messageBody: String) {
        val targetMatcher = allOf(
                withId(R.id.messageOptions),
                hasSibling(
                        allOf(
                                withId(R.id.messageBody),
                                withText(messageBody)
                        )
                )
        )

        onView(targetMatcher).click()
        // "Delete" might be off the page, esp. in landscape mode on small screens
        onView(isAssignableFrom(MenuPopupWindow.MenuDropDownListView::class.java)).swipeUp()
        onView(withText("Delete")).click()
        onView(allOf(isAssignableFrom(AppCompatButton::class.java), containsTextCaseInsensitive("DELETE")))
                .click() // Confirmation click
    }

    fun assertMessageDisplayed(message: String) {
        val itemMatcher = CoreMatchers.allOf(
                ViewMatchers.hasSibling(withId(R.id.attachmentContainer)),
                ViewMatchers.hasSibling(withId(R.id.headerDivider)),
                withId(R.id.messageBody),
                withText(message)
        )
        waitForView(itemMatcher).assertDisplayed()
    }

    fun assertMessageNotDisplayed(message: String) {
        onView(withText(message)).check(doesNotExist())
    }

    fun assertAttachmentDisplayed(displayName: String) {
        scrollRecyclerView(R.id.listView, withText(displayName))
        onViewWithText(displayName).check(matches(isDisplayingAtLeast(5)))
    }

    fun refresh() {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.swipeRefreshLayout), ViewMatchers.isDisplayingAtLeast(10)))
                .perform(withCustomConstraints(ViewActions.swipeDown(), ViewMatchers.isDisplayingAtLeast(10)))
    }

    fun toggleStarred() {
        onView(withId(R.id.starred)).click()
    }

    fun assertStarred() {
        onView(withId(R.id.starred)).check(matches(ImageButtonDrawableMatcher(R.drawable.vd_star_filled, ThemePrefs.brandColor)))
    }

    fun assertNotStarred() {
        onView(withId(R.id.starred)).check(matches(ImageButtonDrawableMatcher(R.drawable.vd_star, ThemePrefs.brandColor)))
    }

}

// Arrgghh... I tried to put this in the canvas_espresso CustomMatchers module, but that required
// pulling in pandautils (for ColorUtils) into canvas_espresso, and that caused some weird build issues,
// so I'm just going to stash this matcher here for now.

// Adapted from https://medium.com/@dbottillo/android-ui-test-espresso-matcher-for-imageview-1a28c832626f
/**
 * Matches ImageButton with the drawable associated with [resourceId].  If [resourceId] < 0, will
 * match against "no drawable" / "drawable is null".
 *
 * If the [color] param is non-null, then the drawable associated with [resourceId] will be colored
 * prior to matching.
 */
class ImageButtonDrawableMatcher(val resourceId: Int, val color: Int? = null) : TypeSafeMatcher<View>(ImageButton::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("with drawable from resource id: ")
        description.appendValue(resourceId)
        // TODO: Support resource name in description
//        if (resourceName != null) {
//            description.appendText("[");
//            description.appendText(resourceName);
//            description.appendText("]");
//        }
    }

    override fun matchesSafely(target: View?): Boolean {
        if (target !is ImageButton) {
            return false
        }
        val imageButton = target as ImageButton
        if (resourceId < 0) {
            return imageButton.drawable == null
        }
        val resources: Resources = target.getContext().getResources()
        val expectedDrawable: Drawable = resources.getDrawable(resourceId) ?: return false
        if(color != null) {
            ColorUtils.colorIt(color, expectedDrawable)
        }
        val bitmap: Bitmap = getBitmap(imageButton.getDrawable())
        val otherBitmap: Bitmap = getBitmap(expectedDrawable)
        return bitmap.sameAs(otherBitmap)
    }

    private fun getBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }
}
