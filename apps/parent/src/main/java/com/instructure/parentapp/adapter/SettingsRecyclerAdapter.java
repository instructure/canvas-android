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
package com.instructure.parentapp.adapter;

import android.content.Context;
import android.view.View;

import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.models.User;
import com.instructure.parentapp.binders.SettingsBinder;
import com.instructure.parentapp.holders.SettingsViewHolder;
import com.instructure.parentapp.interfaces.AdapterToFragmentCallback;

import instructure.androidblueprint.SyncPresenter;
import instructure.androidblueprint.SyncRecyclerAdapter;


public class SettingsRecyclerAdapter extends SyncRecyclerAdapter<User, SettingsViewHolder> {

    private AdapterToFragmentCallback<User> mAdapterToFragmentCallback;

    public SettingsRecyclerAdapter(Context context, SyncPresenter presenter, AdapterToFragmentCallback<User> adapterToFragmentCallback) {
        super(context, presenter);
        mAdapterToFragmentCallback = adapterToFragmentCallback;
    }

    @Override
    public void bindHolder(User student, SettingsViewHolder holder, int position) {
        SettingsBinder.bind(getContext(), holder, student, mAdapterToFragmentCallback);
    }

    @Override
    public SettingsViewHolder createViewHolder(View v, int viewType) {
        return new SettingsViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return SettingsViewHolder.holderResId();
    }
}
