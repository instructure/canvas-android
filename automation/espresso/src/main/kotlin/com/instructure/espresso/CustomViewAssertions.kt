/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.instructure.espresso

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import instructure.rceditor.RCETextEditor
import junit.framework.AssertionFailedError
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class RecyclerViewItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val itemCount = (view as? RecyclerView)?.adapter?.itemCount
                ?: throw ClassCastException("View of type ${view.javaClass.simpleName} must be a RecyclerView")
        ViewMatchers.assertThat(itemCount, Matchers.`is`(expectedCount))
    }
}

class RecyclerViewItemCountGreaterThanAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val itemCount = (view as? RecyclerView)?.adapter?.itemCount
            ?: throw ClassCastException("View of type ${view.javaClass.simpleName} must be a RecyclerView")
        ViewMatchers.assertThat(itemCount, Matchers.greaterThan(expectedCount))
    }
}

class ViewPagerItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val count = (view as? ViewPager)?.adapter?.count
                ?: throw ClassCastException("View of type ${view.javaClass.simpleName} must be a ViewPager")
        ViewMatchers.assertThat(count, Matchers.`is`(expectedCount))
    }
}

class TextViewColorAssertion(private val colorHexCode: String) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val item = (view as? TextView)
            ?: throw ClassCastException("View of type ${view.javaClass.simpleName} must be a TextView")
        assertEquals(item.currentTextColor, Color.parseColor(colorHexCode))
    }
}

class NotificationBadgeAssertion(@IdRes private val menuItemId: Int, private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val bottomNavigationView = (view as? BottomNavigationView)
            ?: throw ClassCastException("View of type ${view.javaClass.simpleName} must be a BottomNavigationView")
        val badgeCount = bottomNavigationView.getBadge(menuItemId)?.number ?: -1
        assertEquals(badgeCount, expectedCount)
    }
}

class DoesNotExistAssertion(private val timeoutInSeconds: Long, private val pollIntervalInSeconds: Long = 1L) : ViewAssertion {
    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        var elapsedTime = 0L

        while (elapsedTime < timeoutInSeconds * 1000) {
            try {
                doesNotExist()
                return
            } catch (e: AssertionFailedError) {
                Thread.sleep(pollIntervalInSeconds * 1000)
                elapsedTime += (pollIntervalInSeconds * 1000)
            }
        }

        throw AssertionError("View still exists after $timeoutInSeconds seconds.")
    }
}

class ConstraintLayoutItemCountAssertionWithMatcher(private val matcher: Matcher<View>, private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        if (view !is ConstraintLayout) {
            throw ClassCastException("View of type ${view.javaClass.simpleName} must be a ConstraintLayout")
        }
        val count = (0 until view.childCount)
            .map { view.getChildAt(it) }.count { matcher.matches(it) }
        ViewMatchers.assertThat(count, Matchers.`is`(expectedCount))
    }
}

class ConstraintLayoutItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        if (view !is ConstraintLayout) {
            throw ClassCastException("View of type ${view.javaClass.simpleName} must be a ConstraintLayout")
        }
        val count = view.childCount
        ViewMatchers.assertThat(count, Matchers.`is`(expectedCount))
    }
}

class ViewAlphaAssertion(private val expectedAlpha: Float): ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        assertThat("View alpha should be $expectedAlpha", view.alpha, `is`(expectedAlpha))
    }
}

class RCETextEditorContentAssertion(private val expectedText: String) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val rceEditor = (view as? RCETextEditor)
            ?: throw ClassCastException("View of type ${view.javaClass.simpleName} must be an RCETextEditor")
        val actualContent = rceEditor.accessibilityContentDescription
        assertTrue(
            "Expected RCE content to contain '$expectedText', but was '$actualContent'",
            actualContent.contains(expectedText)
        )
    }
}

class RCETextEditorHtmlAssertion(private val expectedHtml: String) : ViewAssertion {
    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val rceEditor = (view as? RCETextEditor)
            ?: throw ClassCastException("View of type ${view.javaClass.simpleName} must be an RCETextEditor")
        val actualHtml = rceEditor.getHtml() ?: ""
        assertTrue(
            "Expected RCE HTML to contain '$expectedHtml', but was '$actualHtml'",
            actualHtml.contains(expectedHtml)
        )
    }
}
