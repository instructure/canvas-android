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
package com.instructure.pandautils.utils

import android.app.Activity
import android.content.res.ColorStateList
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.instructure.pandautils.R
import java.util.*

/**
 * https://gist.github.com/peterkuterna/f808854cb2f7b23e861d
 */
object ToolbarColorizeHelper {

    /**
     * Use this method to colorize toolbar icons to the desired target color
     * @param toolbar toolbar view being colored
     * @param toolbarIconsColor the target color of toolbar icons
     * @param activity reference to activity needed to register observers
     */
    fun colorizeToolbar(toolbar: Toolbar, toolbarIconsColor: Int, activity: Activity, disabledColor: Int? = null) {
        toolbar.children.forEach { v ->
            when (v) {
            // Step 1: Change the color of back button (or open drawer button).
                is ImageButton -> ColorUtils.tintIt(toolbarIconsColor, v.drawable) // Action Bar back button
                is TextView -> v.setTextColor(toolbarIconsColor)

            // Step 2: Change the color of any ActionMenuViews - icons that are not back button, nor text, nor overflow menu icon.
                is ActionMenuView -> v.children<ActionMenuItemView>().forEach { menuItemView ->
                    // Sets text color for ActionMenuItemView when icon is not present
                    if (disabledColor != null) {
                        val colorStateList = ViewStyler.generateColorStateList(intArrayOf(android.R.attr.state_enabled) to toolbarIconsColor,
                            intArrayOf() to disabledColor)
                        menuItemView.setTextColor(colorStateList)
                    } else {
                        menuItemView.setTextColor(toolbarIconsColor)
                    }
                    menuItemView.compoundDrawables.filterNotNull().forEach {
                        // Double-post the tinting process, otherwise vector drawables won't be tinted the first time they are decoded
                        ColorUtils.tintIt(toolbarIconsColor, it)
                    }
                }
            }

            // Step 3: Change the color of title and subtitle.
            toolbar.setTitleTextColor(toolbarIconsColor)
            toolbar.setSubtitleTextColor(toolbarIconsColor)

            // Step 4: Change the color of the Overflow Menu icon.
            setOverflowButtonColor(toolbarIconsColor, activity)

            // Step 5: When not using setSupportActionbar this method is used to color the overflow icon
            toolbar.overflowIcon = ColorUtils.tintIt(toolbarIconsColor, toolbar.overflowIcon!!)
        }
    }

    /**
     * It's important to set overflowDescription attribute in styles, so we can grab the reference
     * to the overflow icon. Check: res/values/styles.xml
     * @param toolbarIconsColor The color to color
     * @param activity A context with reference to a window object
     */
    private fun setOverflowButtonColor(toolbarIconsColor: Int, activity: Activity) {
        val overflowDescription = activity.getString(R.string.accessibility_overflow)
        val decorView = activity.window.decorView as ViewGroup
        val viewTreeObserver = decorView.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val outViews = ArrayList<View>()
                decorView.findViewsWithText(outViews, overflowDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
                if (outViews.isEmpty()) {
                    decorView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    return
                }
                if (outViews[0] is ImageView) {
                    val overflow = outViews[0] as ImageView
                    ColorUtils.tintIt(toolbarIconsColor, overflow.drawable)
                }
                decorView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

}
