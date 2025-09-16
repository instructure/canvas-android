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
package com.instructure.canvas.espresso

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputLayout
import com.instructure.espresso.ActivityHelper
import com.instructure.pandautils.utils.ColorUtils
import junit.framework.AssertionFailedError
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

//
// This is a repo for useful custom matchers
//

/**
 * Matches if the view is a TextView and its text contains [textToMatch], case insensitive
 */
fun containsTextCaseInsensitive(textToMatch: String) : Matcher<View> {
    return object: BaseMatcher<View>() {
        override fun matches(item: Any?): Boolean {
            when(item) {
                is TextView -> {
                    return item.text.toString().contains(textToMatch, ignoreCase = true)
                }
            }
            return false
        }

        override fun describeTo(description: Description?) {
            description?.appendText("check to see if TextView contains \"$textToMatch\", case insensitive")
        }
    }
}

// Similar to containsTextCaseInsensitive(), but operates on a String rather than a TextView.
// Originally created to combat situations where a content description contains garbage characters.
fun stringContainsTextCaseInsensitive(textToMatch: String) : Matcher<String> {
    return object: BaseMatcher<String>() {
        override fun matches(item: Any?): Boolean {
            when(item) {
                is String -> {
                    return item.contains(textToMatch, ignoreCase = true)
                }
            }
            return false
        }

        override fun describeTo(description: Description?) {
            description?.appendText("check to see if String contains \"$textToMatch\", case insensitive")
        }
    }
}

/**
 * Returns true if the element with the given resource id is currently displayed, false otherwise.
 */
fun isElementDisplayed(resourceId: Int) : Boolean {
    try {
        onView(withId(resourceId)).check(matches(isDisplayed()))
        return true
    }
    catch(t: Throwable) {
        return false
    }
}

inline fun <reified T : View> typedViewCondition(crossinline onCheckCondition: (T) -> Boolean): Matcher<View> {
    return object : BaseMatcher<View>() {
        override fun matches(item: Any?): Boolean = (item as? T)?.let(onCheckCondition) ?: false
        override fun describeTo(description: Description?) {
            description?.appendText("matches view type '${T::class.java.simpleName}' and fulfills the given condition")
        }
    }
}

fun checked(checked: Boolean = true, index: Int = 0, getText: (String) -> Unit = {}): BoundedMatcher<View, RadioButton> {
    return object : BoundedMatcher<View, RadioButton>(RadioButton::class.java) {
        var currentIndex = 0
        override fun describeTo(description: Description) {
            description.appendText("selected radio button ")
        }

        override fun matchesSafely(item: RadioButton): Boolean {
            if (index == currentIndex++ && item.isChecked == checked) {
                getText(item.text.toString())
                return true
            }
            currentIndex--
            return false
        }
    }
}

/**
 * @param matchTitle True if matching with title, false if matching with subtitle
 */
fun matchToolbarText(matchText: Matcher<String>, matchTitle: Boolean = true): BoundedMatcher<View, Toolbar> {
    return object : BoundedMatcher<View, Toolbar>(Toolbar::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with toolbar ${if (matchTitle) "title" else "subtitle"} ")
            matchText.describeTo(description)
        }

        override fun matchesSafely(view: Toolbar): Boolean {
            return matchText.matches(if (matchTitle) view.title else view.subtitle)
        }
    }
}

fun has(expectedCount: Int, selector: Matcher<View>): ViewAssertion {
    return ViewAssertion { view, noViewFoundException ->
        val rootView = view

        val descendantViews = TreeIterables.breadthFirstViewTraversal(rootView)
        val selectedViews = ArrayList<View>()
        descendantViews.forEach {
            if (selector.matches(it)) {
                selectedViews.add(it)
            }
        }

        if (selectedViews.size != expectedCount) {
            val errorMessage = HumanReadables.getViewHierarchyErrorMessage(rootView,
                    selectedViews,
                    String.format("Found %d views instead of %d matching: %s", selectedViews.size, expectedCount, selector),
                    "****MATCHES****")
            throw AssertionFailedError(errorMessage);
        }
    }
}

fun hasTextInputLayoutErrorText(stringResId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {

        override fun matchesSafely(view: View): Boolean {
            if (view !is TextInputLayout) {
                return false
            }

            val error = view.error ?: return false

            val hint = error.toString()

            val actualErrorMsg = view.resources.getString(stringResId)

            return actualErrorMsg == hint
        }

        override fun describeTo(description: Description) {}
    }
}

fun withIndex(matcher: Matcher<View>, index: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        internal var currentIndex = 0

        override fun describeTo(description: Description) {
            description.appendText("with index: ")
            description.appendValue(index)
            matcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            return matcher.matches(view) && currentIndex++ == index
        }
    }
}

fun withRotation(rotation: Float): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun matchesSafely(item: View): Boolean {
            return item.rotation == rotation
        }

        override fun describeTo(description: Description) {
            description.appendText("with rotation: $rotation")
        }
    }
}

fun hasCheckedState(checkedState: Int) : Matcher<View> {
    return object : TypeSafeMatcher<View>() {
        override fun matchesSafely(item: View): Boolean {
            return item is MaterialCheckBox && item.checkedState == checkedState
        }

        override fun describeTo(description: Description?) {
            description?.appendText("has the proper checked state.")
        }
    }
}

// A matcher for views whose width is less than the specified amount (in dp),
// but whose height is at least the specified amount.
// This is used to suppress accessibility failures related to overflow menus
// in action bars being to narrow.
fun withOnlyWidthLessThan(dimInDp: Int) : BaseMatcher<AccessibilityViewCheckResult>
{
    val densityDpi = ActivityHelper.currentActivity().resources.displayMetrics.densityDpi
    val dim_f = dimInDp * (densityDpi.toDouble() / DisplayMetrics.DENSITY_DEFAULT.toDouble())
    val dim = dim_f.toInt()
    return object : BaseMatcher<AccessibilityViewCheckResult>() {
        override fun describeTo(description: Description?) {
            description?.appendText("checking whether width < $dim && height >= $dim")
        }

        override fun matches(item: Any): Boolean {
            when(item) {
                is AccessibilityViewCheckResult -> {
                    if (item.view == null) return false
                    return item.view!!.width < dim && item.view!!.height >= dim
                }
                else -> return false
            }
        }

    }
}
fun getViewChildCountWithoutId(viewMatcher: Matcher<View>): Int {
    var count = 0
    onView(viewMatcher).perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(RecyclerView::class.java)
        }

        override fun getDescription(): String {
            return "Count RecyclerView children without ID"
        }

        override fun perform(uiController: UiController?, view: View?) {
            if (view is RecyclerView) {
                val childCount = view.childCount
                for (i in 0 until childCount) {
                    val child = view.getChildAt(i)
                    if (child.id == View.NO_ID) {
                        count++
                    }
                }
            }
        }
    })
    return count
}


fun countConstraintLayoutsInRecyclerView(recyclerViewId: ViewInteraction): Int {
    var count = 0
    recyclerViewId.perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(RecyclerView::class.java)
        }

        override fun getDescription(): String {
            return "Counting ConstraintLayouts in RecyclerView"
        }

        override fun perform(uiController: UiController, view: View) {
            if (view is RecyclerView) {
                count = countConstraintLayoutsInViewGroup(view)
            }
        }
    })
    return count
}

private fun countConstraintLayoutsInViewGroup(viewGroup: ViewGroup): Int {
    var count = 0
    for (i in 0 until viewGroup.childCount) {
        val child = viewGroup.getChildAt(i)
        if (child is ConstraintLayout) {
            count++
        } else if (child is ViewGroup) {
            count += countConstraintLayoutsInViewGroup(child)
        }
    }
    return count
}

fun getHintText(matcher: Matcher<View>): String {
    var text = ""
    onView(matcher).perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> = isAssignableFrom(AppCompatEditText::class.java)

        override fun getDescription(): String = "getting hint from AppCompatEditText"

        override fun perform(uiController: UiController?, view: View?) {
            val editText = view as EditText
            text = editText.hint?.toString() ?: ""
        }
    })
    return text
}

fun getText(matcher: Matcher<View>): String {
    var text = ""

    onView(matcher).perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(TextView::class.java)
        }

        override fun getDescription(): String {
            return "Getting text from a TextView"
        }

        override fun perform(uiController: UiController?, view: View?) {
            val tv = view as TextView
            text = tv.text.toString()
        }
    })

    return text
}

object SwipeRefreshLayoutMatchers {
    fun isRefreshing(isRefreshing: Boolean): Matcher<View> {
        return object : BoundedMatcher<View, SwipeRefreshLayout>(SwipeRefreshLayout::class.java) {

            override fun describeTo(description: Description) {
                description.appendText(if (isRefreshing) "is refreshing" else "is not refreshing")
            }

            override fun matchesSafely(view: SwipeRefreshLayout): Boolean {
                return view.isRefreshing == isRefreshing
            }
        }
    }
}

object ViewSizeMatcher {
    fun hasWidth(pixels: Int): Matcher<View> = object : TypeSafeMatcher<View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has a width of ${pixels}px")
        }

        override fun matchesSafely(view: View): Boolean = view.width == pixels
    }

    fun hasHeight(pixels: Int): Matcher<View> = object : TypeSafeMatcher<View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has a height of ${pixels}px")
        }

        override fun matchesSafely(view: View): Boolean = view.height == pixels
    }

    fun hasMinWidth(pixels: Int): Matcher<View> = object : TypeSafeMatcher<View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has a minimum width of ${pixels}px")
        }

        override fun matchesSafely(view: View): Boolean = view.width >= pixels
    }

    fun hasMinHeight(pixels: Int): Matcher<View> = object : TypeSafeMatcher<View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has a minimum height of ${pixels}px")
        }

        override fun matchesSafely(view: View): Boolean = view.height >= pixels
    }
}

fun ViewInteraction.assertLineCount(lineCount: Int) {
    val matcher = object : TypeSafeMatcher<View>() {
        override fun matchesSafely(item: View): Boolean {
            return (item as TextView).lineCount == lineCount
        }

        override fun describeTo(description: Description) {
            description.appendText("isTextInLines")
        }
    }
    check(matches(matcher))
}


fun ViewInteraction.getView(): View {
    lateinit var matchingView: View
    perform(object : ViewAction {
        override fun getDescription() = "Get View reference"

        override fun getConstraints(): Matcher<View> {
            return isAssignableFrom(View::class.java)
        }

        override fun perform(uiController: UiController?, view: View) {
            matchingView = view
        }
    })
    return matchingView
}

fun ViewInteraction.assertCompletelyAbove(other: ViewInteraction) {
    val view1 = getView()
    val view2 = other.getView()
    val location1 = view1.locationOnScreen
    val location2 = view2.locationOnScreen
    val isAbove = location1[1] + view1.height <= location2[1]
    assertThat("completely above", isAbove, `is`(true))
}

fun ViewInteraction.assertCompletelyBelow(other: ViewInteraction) {
    val view1 = getView()
    val view2 = other.getView()
    val location1 = view1.locationOnScreen
    val location2 = view2.locationOnScreen
    val isAbove = location2[1] + view2.height <= location1[1]
    assertThat("completely below", isAbove, `is`(true))
}

val View.locationOnScreen get() = IntArray(2).apply { getLocationOnScreen(this) }


/**
 * Asserts that the TextView uses the specified font size in scaled pixels
 */
fun ViewInteraction.assertFontSizeSP(expectedSP: Float) {
    val matcher = object : TypeSafeMatcher<View>(View::class.java) {

        override fun matchesSafely(target: View): Boolean {
            if (target !is TextView) return false
            val actualSP = target.textSize / target.getResources().displayMetrics.scaledDensity
            return actualSP.compareTo(expectedSP) == 0
        }

        override fun describeTo(description: Description) {
            description.appendText("with fontSize: ${expectedSP}px")
        }
    }
    check(matches(matcher))
}

fun ViewInteraction.assertIsRefreshing(isRefreshing: Boolean) {
    val matcher = object : BoundedMatcher<View, SwipeRefreshLayout>(SwipeRefreshLayout::class.java) {

        override fun describeTo(description: Description) {
            description.appendText(if (isRefreshing) "is refreshing" else "is not refreshing")
        }

        override fun matchesSafely(view: SwipeRefreshLayout): Boolean {
            return view.isRefreshing == isRefreshing
        }
    }
    check(matches(matcher))
}

class IntentActionMatcher(private val intentType: String, private val dataMatcher: String) : TypeSafeMatcher<Intent>() {

    override fun describeTo(description: Description?) {
        description?.appendText("Intent Matcher")
    }

    override fun matchesSafely(item: Intent?): Boolean {
        return (intentType == item?.action) && (item?.dataString?.contains(dataMatcher) ?: false)
    }
}

// Adapted from https://medium.com/@dbottillo/android-ui-test-espresso-matcher-for-imageview-1a28c832626f
/**
 * Matches ImageView (or ImageButton) with the drawable associated with [resourceId].  If [resourceId] < 0, will
 * match against "no drawable" / "drawable is null".
 *
 * If the [color] param is non-null, then the drawable associated with [resourceId] will be colored
 * prior to matching.
 */
class ImageViewDrawableMatcher(val resourceId: Int, val color: Int? = null) : TypeSafeMatcher<View>(
    ImageView::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("with drawable from resource id: ")
        description.appendValue(resourceId)
    }

    override fun matchesSafely(target: View?): Boolean {
        if (target !is ImageView) {
            return false
        }
        val imageView = target
        if (resourceId < 0) {
            return imageView.drawable == null
        }
        val resources: Resources = target.getContext().getResources()
        val expectedDrawable: Drawable = resources.getDrawable(resourceId) ?: return false
        if(color != null) {
            ColorUtils.colorIt(color, expectedDrawable)
        }
        val bitmap: Bitmap = getBitmap(imageView.getDrawable())
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
