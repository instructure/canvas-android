/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.loginapi.login.materialdialogs;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.instructure.loginapi.login.R;

/**
 * Created by Lewis on 17/08/2014.
 */
class CustomListAdapter extends ArrayAdapter<String> {

    private int mRes;
    private String[] mItems;
    private TextView mItemView;

    CustomListAdapter(Context _context, int _res, String[] _items){
        super(_context, _res, _items);

        this.mRes = _res;
        this.mItems = _items;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;

        // Inflate a view if none present.
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(mRes, null);
        }

        // Reference the text view from layout.
        mItemView = (TextView) v.findViewById(R.id.item_dialog_list_item);

        // Apply properties.
        mItemView.setText(mItems[position]);
        mItemView.setTextSize(TypedValue.COMPLEX_UNIT_SP, CustomListDialog.mItemTextSize);
        mItemView.setGravity(CustomListDialog.getGravityFromAlignment(CustomListDialog.mItemAlignment) | Gravity.CENTER_VERTICAL);
        mItemView.setTypeface(CustomListDialog.mTypeface);

        try {
            mItemView.setTextColor(CustomListDialog.mItemColour);
        } catch(Exception e){}

        return v;
    }
}
