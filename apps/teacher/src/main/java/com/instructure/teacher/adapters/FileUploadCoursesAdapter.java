/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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

package com.instructure.teacher.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.pandautils.utils.ColorKeeper;
import com.instructure.teacher.R;
import com.instructure.teacher.binders.BaseBinder;

import java.util.ArrayList;
import java.util.List;


public class FileUploadCoursesAdapter extends ArrayAdapter<Course> {

    // Filter Types
    public enum Type{TEACHER, STUDENT, NONE}

    // Member Variables
    private List<Course> mCourses = new ArrayList<>();
    private LayoutInflater mInflater;

    public FileUploadCoursesAdapter(Context context, LayoutInflater inflater, List<Course> courses) {
        super(context, R.layout.canvas_context_spinner_adapter_item, courses);
        mInflater = inflater;
        mCourses = courses;
    }

    public void setCourses(List<Course> courses){
        mCourses = courses;
        notifyDataSetChanged();
    }

    //region Adapter Overrides

    @Override
    public int getCount() {
        return mCourses.size();
    }

    @Override
    public View getDropDownView(int position, View convertView,  ViewGroup parent) {
        final CourseViewHolder viewHolder;

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.canvas_context_spinner_adapter_item, null);
            viewHolder = new CourseViewHolder();
            viewHolder.title = (TextView)convertView.findViewById(R.id.title);
            viewHolder.indicator = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CourseViewHolder)convertView.getTag();
        }

        Course item = mCourses.get(position);

        if(item != null) {
            viewHolder.title.setText(item.getName());
            viewHolder.title.setTypeface(null, Typeface.NORMAL);
            viewHolder.indicator.setVisibility(View.VISIBLE);
            viewHolder.indicator.setBackgroundDrawable(BaseBinder.createIndicatorBackground(
                    ColorKeeper.getOrGenerateColor(item)));
        } else {
            viewHolder.indicator.setVisibility(View.GONE);
            viewHolder.title.setText("");
        }

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CourseViewHolder viewHolder;

        if(convertView == null) {
            Logger.d("creating view holder, getView");
            convertView = mInflater.inflate(R.layout.canvas_context_spinner_adapter_item, null);
            viewHolder = new CourseViewHolder();
            viewHolder.title = (TextView)convertView.findViewById(R.id.title);
            viewHolder.indicator = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CourseViewHolder)convertView.getTag();
        }

        Course item = mCourses.get(position);

        if(item != null) {
            viewHolder.title.setText(item.getName());
            viewHolder.indicator.setVisibility(View.VISIBLE);
            viewHolder.indicator.setBackgroundDrawable(BaseBinder.createIndicatorBackground(
                    ColorKeeper.getOrGenerateColor(item)));
        } else {
            viewHolder.indicator.setVisibility(View.GONE);
            viewHolder.title.setText("");
        }
        return convertView;
    }

    private static class CourseViewHolder {
        TextView title;
        ImageView indicator;
    }

    //endregion

    //region Helpers

    public static List<Course> getFilteredCourseList(List<Course> courses, Type filterType){
        List<Course> newCourseList = new ArrayList<>();

        for(Course course : courses){
            if(shouldAddCourse(course, filterType)){
                newCourseList.add(course);
            }
        }
        return newCourseList;
    }

    private static boolean shouldAddCourse(Course course, Type filterType){
        switch(filterType){
            case STUDENT:
                return course.isStudent();
            case TEACHER:
                return course.isTeacher();
            default:
                return false;
        }
    }

    //endregion
}
