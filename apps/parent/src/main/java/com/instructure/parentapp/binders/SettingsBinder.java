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
package com.instructure.parentapp.binders;

import android.content.Context;
import android.view.View;

import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.models.User;
import com.instructure.pandautils.utils.Utils;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.R;
import com.instructure.parentapp.holders.SettingsViewHolder;
import com.instructure.parentapp.interfaces.AdapterToFragmentCallback;
import com.squareup.picasso.Picasso;

public class SettingsBinder extends BaseBinder {

    public static void bind(
            final Context context,
            final SettingsViewHolder holder,
            final User student,
            final AdapterToFragmentCallback<User> adapterToFragmentCallback) {

        holder.name.setText(student.getShortName());
        Utils.testSafeContentDescription(holder.name,
                String.format(context.getString(R.string.name_text_content_desc), holder.getAdapterPosition()),
                student.getShortName(),
                BuildConfig.IS_TESTING);

        Picasso.with(context).load(student.getAvatarUrl()).placeholder(R.drawable.ic_cv_user).error(R.drawable.ic_cv_user).fit().into(holder.avatar);
        Utils.testSafeContentDescription(holder.avatar,
                String.format(context.getString(R.string.avatar_content_desc), holder.getAdapterPosition()),
                "", // we don't want duplicate a11y elements for this
                BuildConfig.IS_TESTING);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterToFragmentCallback.onRowClicked(student, holder.getAdapterPosition(), true);
            }
        });
    }
}
