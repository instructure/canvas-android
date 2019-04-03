/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.view;

import android.content.Context;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class PatchedScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior {

    public PatchedScrollingViewBehavior() {
        super();
    }

    public PatchedScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        if(child.getLayoutParams().height == -1) {
            List dependencies = parent.getDependencies(child);
            if(dependencies.isEmpty()) {
                return false;
            }

            AppBarLayout appBar = findFirstAppBarLayout(dependencies);
            if(appBar != null && ViewCompat.isLaidOut(appBar)) {
                if(ViewCompat.getFitsSystemWindows(appBar)) {
                    ViewCompat.setFitsSystemWindows(child, true);
                }

                int scrollRange = appBar.getTotalScrollRange();
//                int height = parent.getHeight() - appBar.getMeasuredHeight() + scrollRange;
                int parentHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
                int height = parentHeight - appBar.getMeasuredHeight() + scrollRange;
                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);
                parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
                return true;
            }
        }

        return false;
    }


    private static AppBarLayout findFirstAppBarLayout(List<View> views) {
        int i = 0;

        for(int z = views.size(); i < z; ++i) {
            View view = (View)views.get(i);
            if(view instanceof AppBarLayout) {
                return (AppBarLayout)view;
            }
        }

        return null;
    }
}
