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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.instructure.canvasapi.model.User;
import com.instructure.teacheraid.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class StudentGridAdapter extends ArrayAdapter<User> {

    StudentHolder holder;
    Context context;
    ArrayList<User> users = new ArrayList<>();

    public StudentGridAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(R.layout.student_notes_grid_item, parent, false);

            holder = new StudentHolder();
            holder.avatar = (CircleImageView) row.findViewById(R.id.avatar);
            holder.studentName = (TextView) row.findViewById(R.id.studentName);
            row.setTag(holder);
        } else {
            holder = (StudentHolder) row.getTag();
        }

        User user = users.get(position);
        holder.studentName.setText(user.getName());
        Picasso.with(context).load(user.getAvatarURL()).centerCrop().fit().into(holder.avatar);
        return row;

    }

    static class StudentHolder {
        TextView studentName;
        CircleImageView avatar;

    }
}
