/*
 * Original source code obtained from:
 *      https://github.com/aosp-mirror/platform_frameworks_support/blob/237c8946756af4b0fe9d0fa3965593e247d53698/appcompat/src/main/java/androidx/appcompat/widget/TooltipPopup.java
 *
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.PixelFormat
import android.graphics.Rect
import android.text.Layout
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.instructure.pandautils.utils.DP
import com.instructure.student.R
import kotlinx.android.synthetic.main.view_rubric_tooltip.view.*

internal class SubmissionRubricTooltipPopup(private val context: Context) {

    private val tooltipOffset = context.DP(5).toInt()

    private val tooltipView = RubricTooltipView(context)

    private val layoutParams = WindowManager.LayoutParams()

    private val isShowing: Boolean get() = tooltipView.parent != null

    init {
        layoutParams.title = javaClass.simpleName
        layoutParams.packageName = context.packageName
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.format = PixelFormat.TRANSLUCENT
        layoutParams.windowAnimations = R.style.RubricTooltipWindowAnimation
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
    }

    /** Shows this tooltip and returns a suggested show duration based on the visible text length */
    @SuppressLint("ClickableViewAccessibility")
    fun show(anchorView: View, tooltipText: CharSequence): Long {
        if (isShowing) hide()
        tooltipView.setText(tooltipText)
        tooltipView.setOnTouchListener { _, event ->
            // Hide when the user touches outside the tooltip bounds
            if (event.action == MotionEvent.ACTION_OUTSIDE) hide()
            false
        }
        computePosition(anchorView, layoutParams)
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).addView(tooltipView, layoutParams)
        // Return the suggested duration, 3 seconds plus 20ms for every visible character
        return 3000L + tooltipView.tooltipTextView.layout.visibleTextLength * 20L
    }

    /** Hides this tooltip */
    fun hide() {
        if (!isShowing) return
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(tooltipView)
    }

    private fun computePosition(anchorView: View, outParams: WindowManager.LayoutParams) {
        outParams.token = anchorView.applicationWindowToken

        val offsetX = anchorView.width / 2

        val offsetBelow = anchorView.height  // Place below the view in most cases.

        outParams.gravity = Gravity.START or Gravity.TOP

        val displayFrame = Rect()
        val appView = getAppRootView(anchorView)
        appView.getWindowVisibleDisplayFrame(displayFrame)
        if (displayFrame.left < 0 && displayFrame.top < 0) {
            // No meaningful display frame, the anchor view is probably in a subpanel
            // (such as a popup window). Use the screen frame as a reasonable approximation.
            val res = context.resources
            val statusBarHeight: Int
            val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
            statusBarHeight = if (resourceId != 0) res.getDimensionPixelSize(resourceId) else 0
            val metrics = res.displayMetrics
            displayFrame.set(0, statusBarHeight, metrics.widthPixels, metrics.heightPixels)
        }

        val appPos = IntArray(2)
        appView.getLocationOnScreen(appPos)

        val anchorPos = IntArray(2)
        anchorView.getLocationOnScreen(anchorPos)
        anchorPos[0] -= appPos[0]
        anchorPos[1] -= appPos[1]
        // mTmpAnchorPos is now relative to the main app window.

        outParams.x = 0
        val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        tooltipView.measure(spec, spec)
        val tooltipHeight = tooltipView.measuredHeight

        outParams.x = anchorPos[0] + offsetX - tooltipView.measuredWidth / 2
        outParams.x = outParams.x.coerceIn(0, appView.width - tooltipView.measuredWidth)

        val yAbove = anchorPos[1] - tooltipOffset - tooltipHeight
        val yBelow = anchorPos[1] + offsetBelow + tooltipOffset
        outParams.y = if (yAbove >= 0) yAbove else yBelow

        val tailOffset = anchorPos[0] + offsetX - outParams.x
        tooltipView.update(tailOffset, yAbove >= 0)
    }

    /** Returns the length of the visible text */
    private val Layout.visibleTextLength get() = (0 until lineCount).sumBy { getLineVisibleEnd(it) }

    companion object {
        private fun getAppRootView(anchorView: View): View {
            val rootView = anchorView.rootView
            val lp = rootView.layoutParams
            if (lp is WindowManager.LayoutParams && lp.type == WindowManager.LayoutParams.TYPE_APPLICATION) {
                // This covers regular app windows and Dialog windows.
                return rootView
            }
            // For non-application window types (such as popup windows) try to find the main app window
            // through the context.
            var context = anchorView.context
            while (context is ContextWrapper) {
                if (context is Activity) {
                    return context.window.decorView
                } else {
                    context = context.baseContext
                }
            }
            // Main app window not found, fall back to the anchor's root view. There is no guarantee
            // that the tooltip position will be computed correctly.
            return rootView
        }
    }
}
