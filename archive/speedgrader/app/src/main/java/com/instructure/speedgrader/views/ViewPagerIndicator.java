/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.speedgrader.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import com.instructure.speedgrader.R;
import java.util.ArrayList;

public class ViewPagerIndicator extends LinearLayout {

    /*
     * 	EXAMPLE USAGE:
     *
            //Set the pager with an adapter
            //Bind the title indicator to the adapter
            ViewPagerIndicator indicator = (LinePageIndicator)findViewById(R.id.titles);
             indicator.setViewPager(viewPager);

            //Bind the title indicator to the PageChangeListener
            indicator.setOnPageChangeListener(new PageChangedListener());
     */
    private Context context;

    private OnPageChangeListener mListener;
    private ArrayList<View> views = new ArrayList<View>();

    private int currentPage = 0;
    private int dividerColorNonCurrent = Color.WHITE;

    public ViewPagerIndicator(Context context) {
        super(context);
        this.context = context;
        this.setOrientation(LinearLayout.HORIZONTAL);
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setOrientation(LinearLayout.HORIZONTAL);
    }

    public void setDividerColorNonCurrentPage(int color) {
        dividerColorNonCurrent = color;
    }

    public void setNumberOfPages(int pages) {
        views.clear();
        removeAllViews();

        for (int i = 0; i < pages; i++) {
            final View indicator = new View(context);
            indicator.setBackgroundResource(R.drawable.indicator_bubble);
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
            LayoutParams layoutParams = (new LayoutParams(size, size));
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
            layoutParams.setMargins(margin, 0, margin, 0);
            indicator.setLayoutParams(layoutParams);

            if (i == currentPage) {
                setIndicatorColor(indicator, getResources().getColor(R.color.courseBlue));
            } else {
                setIndicatorColor(indicator, dividerColorNonCurrent);
            }

            views.add(indicator);
            addView(indicator);
        }
    }

    private void invalidateCurrentPage() {
        for (int i = 0; i < views.size(); i++) {
            if (i == currentPage) {
                setIndicatorColor(views.get(i), getResources().getColor(R.color.courseBlue));
            } else {
                setIndicatorColor(views.get(i), dividerColorNonCurrent);
                bringToFront();
            }
        }
    }

    private void setIndicatorColor(View indicator, int color) {
        Drawable drawable = context.getResources().getDrawable(R.drawable.indicator_bubble);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        indicator.setBackgroundDrawable(drawable);
    }

    public void setViewPager(ViewPager pager) {
        pager.setOnPageChangeListener(new VPIPageChangeListener());
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mListener = listener;
    }

    private class VPIPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
            if (mListener != null) {
                mListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (mListener != null) {
                mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            double current = (position + positionOffset) + .5; //round to nearest;
            currentPage = (int) current;
            invalidateCurrentPage();

        }

        @Override
        public void onPageSelected(int position) {
            if (mListener != null) {
                mListener.onPageSelected(position);
            }
        }

    }
}
