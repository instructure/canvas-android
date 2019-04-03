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

package com.instructure.speedgrader.activities;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.instructure.speedgrader.R;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.speedgrader.fragments.TutorialFragment;
import com.instructure.speedgrader.util.App;
import com.instructure.speedgrader.views.ViewPagerIndicator;

public class TutorialActivity extends FragmentActivity implements
        View.OnClickListener,
        ViewPager.OnPageChangeListener,
        TutorialFragment.TutorialException {

    private static final int NUM_PAGES = 6;

    private ViewPager pager;
    private PagerAdapter adapter;
    private ImageView next;
    private TextView skip;
    private boolean allowBackPress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        boolean isSmallTablet = getResources().getBoolean(R.bool.isSmallTablet);
        if(!isTablet || isSmallTablet) {
            //Phone or small tablet
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            //large tablet
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        setContentView(R.layout.tutorial_activity);

        pager = (ViewPager) findViewById(R.id.pager);
        next = (ImageView) findViewById(R.id.next);
        skip = (TextView) findViewById(R.id.skip);

        adapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(0);

        final ViewPagerIndicator indicator = (ViewPagerIndicator)findViewById(R.id.indicator);
        indicator.setNumberOfPages(NUM_PAGES);
        indicator.setDividerColorNonCurrentPage(Color.WHITE);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(this);

        next.setOnClickListener(this);
        skip.setOnClickListener(this);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == NUM_PAGES - 1) {
                allowBackPress = true;
            }

            final TutorialFragment fragment = new TutorialFragment();
            Bundle args = new Bundle();
            args.putInt(TutorialFragment.PAGE_NUMBER, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public void onBackPressed() {
        if(allowBackPress) {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                int currentItem = pager.getCurrentItem();
                if(currentItem >= 5) {
                    startActivity(HomeActivity.createIntent(getApplicationContext()));
                    finish();
                } else {
                    pager.setCurrentItem(currentItem + 1);
                }
                break;
            case R.id.skip:
                startActivity(HomeActivity.createIntent(getApplicationContext()));
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        ((App)getApplication()).setShouldShowTutorial(false);
        super.finish();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if(state == ViewPager.SCROLL_STATE_IDLE) {
            if(pager.getCurrentItem() == 5) {
                next.setImageResource(R.drawable.ic_tutorial_action_done);
            } else {
                next.setImageResource(R.drawable.ic_tutorial_arrow_right);
            }
        }
    }

    @Override
    public void forceExit_Exception(OutOfMemoryError e) {
        Utils.e("FORCING EXIT FROM TUTORIAL DUE TO OUT OF MEMORY ERROR: " + e);
        super.onBackPressed();
    }
}