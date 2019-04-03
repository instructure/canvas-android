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

package com.instructure.teacheraid.rowfactories;

import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.util.ViewUtils;

import java.util.List;

public class CourseRowFactory {

    public interface CourseRowFactoryCallbacks {
        public void onClickCourse(Course course, ChildViewHolder holder);

        public List<CanvasContext> courseGroupList();
    }

    private static class GroupViewHolder {
        TextView name;
    }

    public static class ChildViewHolder {
        TextView courseName;
    }

    public static View buildRowView(final FragmentActivity activity, LayoutInflater inflater, CanvasContext canvasContext, View convertView) {
        final ChildViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.course_expandlist_child, null);
            holder = new ChildViewHolder();
            holder.courseName = (TextView) convertView.findViewById(R.id.tvCourseName);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        holder.courseName.setText(canvasContext.getName());

        int fiveDips = (int) ViewUtils.convertDipsToPixels(5, activity);
        holder.courseName.setPadding(0, fiveDips, 0, fiveDips);

        return convertView;
    }

    public static View buildGroupView(LayoutInflater inflater, String name, View convertView) {
        GroupViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.expandable_list_group_two, null);
            holder = new GroupViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tvGroup);
            convertView.setTag(holder);
        }
        else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        if (TextUtils.isEmpty(name)) {
            holder.name.setVisibility(View.GONE);
        } else {
            holder.name.setText(name);
            holder.name.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
