/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

// Copied from http://android.cn-mirrors.com/samples/SwipeRefreshMultipleViews/src/com.example.android.swiperefreshmultipleviews/MultiSwipeRefreshLayout.html
package com.instructure.pandautils.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * A descendant of [androidx.swiperefreshlayout.widget.SwipeRefreshLayout] which supports multiple
 * child views triggering a refresh gesture. You set the views which can trigger the gesture via
 * [.setSwipeableChildren], providing it the child ids.
 */
class MultiSwipeRefreshLayout : SwipeRefreshLayout {

    private var mSwipeableChildren: Array<View?>? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    /**
     * Set the children which can trigger a refresh by swiping down when they are visible. These
     * views need to be a descendant of this view.
     */
    fun setSwipeableChildren(vararg ids: Int) {
        // Iterate through the ids and find the Views
        mSwipeableChildren = arrayOfNulls(ids.size)
        for (i in ids.indices) {
            mSwipeableChildren!![i] = findViewById(ids[i])
        }
    }

    /**
     * This method controls when the swipe-to-refresh gesture is triggered. By returning false here
     * we are signifying that the view is in a state where a refresh gesture can start.
     *
     *
     * As [androidx.swiperefreshlayout.widget.SwipeRefreshLayout] only supports one direct child by
     * default, we need to manually iterate through our swipeable children to see if any are in a
     * state to trigger the gesture. If so we return false to start the gesture.
     */
    override fun canChildScrollUp(): Boolean {
        if (mSwipeableChildren?.isNotEmpty() == true) {
            // Iterate through the scrollable children and check if any of them can not scroll up
            for (view in mSwipeableChildren!!) {
                if (view != null && view.isShown && !view.canScrollVertically(-1)) {
                    // If the view is shown, and can not scroll upwards, return false and start the
                    // gesture.
                    return false
                }
            }
        }
        return true
    }

}
