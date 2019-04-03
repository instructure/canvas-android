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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.teacheraid.R;
import com.instructure.teacheraid.util.CanvasContextColor;

public class CourseColorAdapter extends BaseAdapter {

    public static Integer[] courseColors = {R.color.courseRed, R.color.courseOrange, R.color.courseGold, R.color.courseGreen, R.color.courseChartreuse, R.color.courseCyan,
            R.color.courseSlate, R.color.courseBlue, R.color.coursePurple, R.color.courseViolet, R.color.coursePink, R.color.courseHotPink,
            R.color.courseGrey, R.color.courseBlack, R.color.courseDarkGrey};

    CanvasContext canvasContext;
    Context context;
    Resources resources;

    public CourseColorAdapter(Context context, CanvasContext canvasContext) {
        this.context = context;
        this.resources = context.getResources();
        this.canvasContext = canvasContext;
    }


    @Override
    public int getCount() {
        return courseColors.length;
    }

    @Override
    public Object getItem(int position) {
        return courseColors[position];
    }

    @Override
    public long getItemId(int position) {
        return canvasContext.getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.course_color, null);
            viewHolder = new ViewHolder();
            viewHolder.courseColor = convertView.findViewById(R.id.course_color);
            convertView.setTag(viewHolder);
        }

        viewHolder = (ViewHolder)convertView.getTag();

        int currentColor = resources.getColor(courseColors[position]);
        viewHolder.courseColor.setBackgroundColor(currentColor);

        // if the current color of the course is the one that is the current index that we are looping through then we
        // want to make it a circle to show that the color is selected. Otherwise we want to set a click listener so
        // that a new color can be selected.
        if (currentColor == CanvasContextColor.getColorForCourse(context, canvasContext)) {
            //make a circle, fill it with the current color that we are
            //looping through
            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
            drawable.getPaint().setColor(currentColor);
            drawable.getPaint().setStyle(Paint.Style.FILL);
            drawable.getPaint().setAntiAlias(true);

            //set the circle as the background of the table row item
            viewHolder.courseColor.setBackgroundDrawable(drawable);
        }

        return convertView;
    }

    static class ViewHolder{
        View courseColor;
    }

}
