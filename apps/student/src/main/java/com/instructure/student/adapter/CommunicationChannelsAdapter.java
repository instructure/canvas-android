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

package com.instructure.student.adapter;

import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.instructure.student.R;
import com.instructure.canvasapi2.models.CommunicationChannel;

public class CommunicationChannelsAdapter extends ArrayAdapter<CommunicationChannel> {

    private LayoutInflater inflater;

    public CommunicationChannelsAdapter(FragmentActivity context, int resource, CommunicationChannel[] objects) {
        super(context, resource, objects);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            view = inflater.inflate(R.layout.actionbar_course_spinner, parent, false);
        }

        final TextView mTitle = view.findViewById(R.id.text1);
        final TextView mSubTitle = view.findViewById(R.id.text2);

        final String title = getContext().getString(R.string.notifications);
        final String subTitle = getItem(position).getAddress();


        mTitle.setText(title);
        mSubTitle.setText(subTitle);
        mSubTitle.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {

        view = inflater.inflate(R.layout.text_adapter_item, parent, false);

        TextView text1 = view.findViewById(R.id.text1);
        TextView text2 = view.findViewById(R.id.text2);
        text1.setText(getItem(position).getAddress());
        text2.setText(getStringForType(getItem(position).getType()));

        return view;
    }

    private String getStringForType(String type) {
        if("email".equalsIgnoreCase(type)) {
            return getContext().getString(R.string.notification_pref_type_email);
        } else if("push".equalsIgnoreCase(type)) {
            return getContext().getString(R.string.notification_pref_type_push);
        } else if("sms".equalsIgnoreCase(type)) {
            return getContext().getString(R.string.notification_pref_type_sms);
        } else {
            return type;
        }
    }

}
