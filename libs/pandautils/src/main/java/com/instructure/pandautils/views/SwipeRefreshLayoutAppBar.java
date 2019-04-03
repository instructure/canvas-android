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

package com.instructure.pandautils.views;

import android.app.Activity;
import android.content.Context;
import com.google.android.material.appbar.AppBarLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.instructure.pandautils.R;

/**
 * Prevents a pull-to-refresh when the appbar is not fully visible.
 * NOTE: must use the R.id.app_bar in the ids.xml in PandaUtils for this too work.
 *
 * http://stackoverflow.com/questions/30833589/scrolling-down-triggers-refresh-instead-of-revealing-the-toolbar
 */
public class SwipeRefreshLayoutAppBar extends SwipeRefreshLayout implements AppBarLayout.OnOffsetChangedListener{

    private AppBarLayout mAppBarLayout;

    public SwipeRefreshLayoutAppBar(Context context) {
        super(context);
    }

    public SwipeRefreshLayoutAppBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getContext() instanceof Activity) {
            mAppBarLayout = (AppBarLayout) ((Activity) getContext()).findViewById(R.id.appBarLayout);
            if(mAppBarLayout != null) {
                mAppBarLayout.addOnOffsetChangedListener(this);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if(mAppBarLayout != null){
            mAppBarLayout.removeOnOffsetChangedListener(this);
            mAppBarLayout = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        // Keep enabled if it's currently refreshing
        setEnabled(i == 0 || isRefreshing());
    }
}
