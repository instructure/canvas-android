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
 */

package com.instructure.teacheraid.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.instructure.canvasapi.model.User;
import com.instructure.teacheraid.fragments.GuessWhoStudentFragment;

import java.util.ArrayList;
import java.util.WeakHashMap;



public class GuessWhoAdapter extends FragmentStatePagerAdapter {

    private FragmentActivity context;
    private FragmentManager fm;


    private ViewPager pager;

    private ArrayList<User> userList = new ArrayList<User>();
    private WeakHashMap<Integer, Fragment> mPageReferenceMap;


    public GuessWhoAdapter(FragmentActivity context, FragmentManager fm, ViewPager pager) {
        super(fm);
        this.fm = fm;
        this.context = context;
        this.pager = pager;
        mPageReferenceMap = new WeakHashMap<>();
    }

    @Override
    public Fragment getItem(int position) {

        Fragment curFragment= GuessWhoStudentFragment.newInstance(context, userList.get(position));
        mPageReferenceMap.put(position, curFragment);

        return curFragment;
    }

    public ArrayList<User> getAllItems() {
        return userList;
    }
    public void addItem(User user) {
        userList.add(user);
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {

        return POSITION_NONE; //To make notifyDataSetChanged() do something
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        super.destroyItem(container, position, object);
        mPageReferenceMap.remove(position);

    }

    @Override
    public int getCount() {
        return userList.size();
    }



}
