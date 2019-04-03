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

package com.instructure.teacheraid.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.canvasapi.model.User;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.util.Const;
import com.squareup.picasso.Picasso;

public class StudentChooserCarouselFragment extends Fragment {

    public static Fragment newInstance(FragmentActivity context, User user) {

        Bundle b = new Bundle();
        b.putParcelable(Const.USER, user);
        return Fragment.instantiate(context, StudentChooserCarouselFragment.class.getName(), b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        RelativeLayout l = (RelativeLayout)
                inflater.inflate(R.layout.user_fragment, container, false);


        User user = this.getArguments().getParcelable(Const.USER);
        TextView tv = (TextView) l.findViewById(R.id.viewID);
        tv.setText(user.getShortName());

        ImageView imageView = (ImageView) l.findViewById(R.id.content);
        Picasso.with(getActivity()).load(user.getAvatarURL()).into(imageView);

        return l;
    }
}