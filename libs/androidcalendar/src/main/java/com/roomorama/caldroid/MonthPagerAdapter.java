/*
 * Copyright (c) 2013 by Roomorama Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.roomorama.caldroid;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * MonthPagerAdapter holds 4 fragments, which provides fragment for current
 * month, previous month and next month. The extra fragment helps for recycle
 * fragments.
 */
public class MonthPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<DateGridFragment> fragments;

    // Lazily create the fragments
    public ArrayList<DateGridFragment> getFragments() {
        if (fragments == null) {
            fragments = new ArrayList<DateGridFragment>();
            for (int i = 0; i < getCount(); i++) {
                fragments.add(new DateGridFragment());
            }
        }
        return fragments;
    }

    public void setFragments(ArrayList<DateGridFragment> fragments) {
        this.fragments = fragments;
    }

    public MonthPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        DateGridFragment fragment = getFragments().get(position);
        return fragment;
    }

    @Override
    public int getCount() {
        // We need 4 gridviews for previous month, current month and next month,
        // and 1 extra fragment for fragment recycle
        return CaldroidFragment.NUMBER_OF_PAGES;
    }

}
