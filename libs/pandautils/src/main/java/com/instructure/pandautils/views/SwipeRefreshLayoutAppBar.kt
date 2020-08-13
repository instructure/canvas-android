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
package com.instructure.pandautils.views

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.instructure.pandautils.R

/**
 * Prevents a pull-to-refresh when the appbar is not fully visible.
 * NOTE: must use the R.id.app_bar in the ids.xml in PandaUtils for this too work.
 *
 * http://stackoverflow.com/questions/30833589/scrolling-down-triggers-refresh-instead-of-revealing-the-toolbar
 */
class SwipeRefreshLayoutAppBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs), AppBarLayout.OnOffsetChangedListener {
    private var appBarLayout: AppBarLayout? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        appBarLayout = (context as? Activity)?.findViewById(R.id.appBarLayout)
        appBarLayout?.addOnOffsetChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        appBarLayout?.removeOnOffsetChangedListener(this)
        appBarLayout = null
        super.onDetachedFromWindow()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
        // Keep enabled if it's currently refreshing
        isEnabled = i == 0 || isRefreshing
    }
}
