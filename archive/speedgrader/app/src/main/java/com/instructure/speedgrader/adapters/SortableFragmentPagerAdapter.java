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

package com.instructure.speedgrader.adapters;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
/**
 * * Copyright (C) 2014 The Android Open Source Project
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
 *
 * Implementation of {@link android.support.v4.view.PagerAdapter} that
 * uses a {@link android.support.v4.app.Fragment} to manage each page. This class also handles
 * saving and restoring of fragment's state.
 *
 * <p>This version of the pager is more useful when there are a large number
 * of pages, working more like a list view.  When pages are not visible to
 * the user, their entire fragment may be destroyed, only keeping the saved
 * state of that fragment.  This allows the pager to hold on to much less
 * memory associated with each visited page as compared to
 * {@link android.support.v4.app.FragmentPagerAdapter} at the cost of potentially more overhead when
 * switching between pages.
 *
 * <p>When using FragmentPagerAdapter the host ViewPager must have a
 * valid ID set.</p>
 *
 * <p>Subclasses only need to implement {@link #getItem(int)}
 * and {@link #getCount()} to have a working adapter.
 *
 * <p>Here is an example implementation of a pager containing fragments of
 * lists:
 *
 * {@sample development/samples/Support13Demos/src/com/example/android/supportv13/app/FragmentStatePagerSupport.java
 *      complete}
 *
 * <p>The <code>R.layout.fragment_pager</code> resource of the top-level fragment is:
 *
 * {@sample development/samples/Support13Demos/res/layout/fragment_pager.xml
 *      complete}
 *
 * <p>The <code>R.layout.fragment_pager_list</code> resource containing each
 * individual fragment's layout is:
 *
 * {@sample development/samples/Support13Demos/res/layout/fragment_pager_list.xml
 *      complete}
 */
public abstract class SortableFragmentPagerAdapter extends PagerAdapter {
    private static final String TAG = "SortableFragmentStatePagerAdapter";
    private static final boolean DEBUG = false;

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;

    private long[] mItemIds = new long[] {};
    private boolean[] needsRefresh = new boolean[] {};

    private ArrayList<Fragment.SavedState> mSavedState = new ArrayList<Fragment.SavedState>();
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
    protected Fragment mCurrentPrimaryItem = null;


    public SortableFragmentPagerAdapter(FragmentManager fm) {

        mFragmentManager = fm;
    }

    /**
     * Return the Fragment associated with a specified position.
     */
    public abstract Fragment getItem(int position);

    /**
     * Return a unique identifier for the item at the given position.
     */
    public abstract long getItemId(int position);

    public Fragment getCachedFragment(int position){
        return mFragments.get(position);
    }

    @Override
    public void startUpdate(ViewGroup container) {
    }

    @Override
    public int getItemPosition(Object object) {
        int position = mFragments.indexOf(object);
        if(position >= 0 && needsRefresh[position] != true) {
            return position;
        }
        return POSITION_NONE;
    }

    public void setNeedsRefresh(int position){
        needsRefresh[position] = true;
    }

    private void checkForIdChanges() {
        long[] newItemIds = new long[getCount()];
        for (int i = 0; i < newItemIds.length; i++) {
            newItemIds[i] = getItemId(i);
        }

        if (!Arrays.equals(mItemIds, newItemIds)) {
            ArrayList<Fragment.SavedState> newSavedState = new ArrayList<Fragment.SavedState>();
            ArrayList<Fragment> newFragments = new ArrayList<Fragment>();

            for (int oldPosition = 0; oldPosition < mItemIds.length; oldPosition++) {
                int newPosition = POSITION_NONE;
                for (int i = 0; i < newItemIds.length; i++) {
                    if (mItemIds[oldPosition] == newItemIds[i]) {
                        newPosition = i;
                        break;
                    }
                }
                if (newPosition >= 0) {
                    if (oldPosition < mSavedState.size()) {
                        Fragment.SavedState savedState = mSavedState.get(oldPosition);
                        if (savedState != null) {
                            while (newSavedState.size() <= newPosition) {
                                newSavedState.add(null);
                            }
                            newSavedState.set(newPosition, savedState);
                        }
                    }
                    if (oldPosition < mFragments.size()) {
                        Fragment fragment = mFragments.get(oldPosition);
                        if (fragment != null) {
                            while (newFragments.size() <= newPosition) {
                                newFragments.add(null);
                            }
                            newFragments.set(newPosition, fragment);
                        }
                    }
                }
            }

            mItemIds = newItemIds;
            mSavedState = newSavedState;
            mFragments = newFragments;
        }
    }

    @Override
    public void notifyDataSetChanged() {
        checkForIdChanges();

        super.notifyDataSetChanged();
    }

    /**
     * Create the initial set of item IDs. Run this after you have set your adapter data.
     */
    public void createIdCache() {
        // If we have already stored ids, don't overwrite them
        if (mItemIds.length == 0) {
            // getCount might have overhead, so run it as late as possible
            final int count = getCount();
            if (count > 0) {
                mItemIds = new long[count];
                needsRefresh = new boolean[count];
                for (int i = 0; i < count; i++) {
                    mItemIds[i] = getItemId(i);
                    needsRefresh[i] = false;
                }
            }
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        createIdCache();

        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        if (mFragments.size() > position) {
            Fragment f = mFragments.get(position);
            if (f != null) {
                return f;
            }
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        Fragment fragment = getItem(position);
        if (DEBUG) Log.v(TAG, "Adding item #" + position + ": f=" + fragment.getClass().getSimpleName());
        if (mSavedState.size() > position) {
            Fragment.SavedState fss = mSavedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            }
        }
        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        mFragments.set(position, fragment);
        mCurTransaction.add(container.getId(), fragment);

        return fragment;
    }

    public void replaceFragment(int position, Fragment fragment){
        mFragments.set(position, fragment);
        if(mSavedState.size() > position){
            mSavedState.set(position, null);
        }

        notifyDataSetChanged();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment)object;
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        if (DEBUG) Log.v(TAG, "Removing item #" + position + ": f=" + object
                + " v=" + ((Fragment)object).getView());
        while (mSavedState.size() <= position) {
            mSavedState.add(null);
        }
        try {
            mSavedState.set(position, mFragmentManager.saveFragmentInstanceState(fragment));
        }catch(Exception e){
        }
        if(mFragments.size() > position) mFragments.set(position, null);

        mCurTransaction.remove(fragment);
    }

    public void destroyAllFragments() {

        final int length = mFragments.size();
        while (mSavedState.size() <= length) {
            mSavedState.add(null);
        }
        for (int i = 0; i < length; i++) {
            final Fragment fr = mFragments.get(i);
            if (fr != null) {
                mSavedState.set(i, mFragmentManager.saveFragmentInstanceState(fr));
                mFragments.set(i, null);
            }
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment)object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment)object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;

        mItemIds = new long[getCount()];
        for (int i = 0; i < mItemIds.length; i++) {
            mItemIds[i] = getItemId(i);
        }
        if (mSavedState.size() > 0) {
            state = new Bundle();

            if (mItemIds.length > 0) {
                state.putLongArray("itemids", mItemIds);
            }

            Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
            mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        for (int i=0; i<mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null) {
                if (state == null) {
                    state = new Bundle();
                }
                String key = "f" + i;
                if(f.isAdded()){
                    mFragmentManager.putFragment(state, key, f);
                }
            }
        }
        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle)state;
            bundle.setClassLoader(loader);

            mItemIds = bundle.getLongArray("itemids");
            if (mItemIds == null) {
                mItemIds = new long[] {};
            }

            Parcelable[] fss = bundle.getParcelableArray("states");
            mSavedState.clear();
            mFragments.clear();
            if (fss != null) {
                for (int i=0; i<fss.length; i++) {
                    mSavedState.add((Fragment.SavedState)fss[i]);
                }
            }
            Iterable<String> keys = bundle.keySet();
            for (String key: keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    try {
                        Fragment f = mFragmentManager.getFragment(bundle, key);
                        if (f != null) {
                            while (mFragments.size() <= index) {
                                mFragments.add(null);
                            }
                            f.setMenuVisibility(false);
                            mFragments.set(index, f);
                        } else {
                            Log.w(TAG, "Bad fragment at key " + key);
                        }
                    } catch (Exception ex) {
                        Log.w(TAG, "Cannot restore fragment " + key, ex);
                    }
                }
            }
            checkForIdChanges();
        }
    }
}