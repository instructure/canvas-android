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

package com.instructure.teacheraid.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.teacheraid.R;

import java.util.HashMap;

public class CanvasContextColor {

    ///////////////////////////////////////////////////////////////////////////
    // Course Colors
    ///////////////////////////////////////////////////////////////////////////

    //helper to get course color drawable
    public static Drawable getColoredDrawable(Context context, int resource, CanvasContext canvasContext){
        return getColoredDrawable(context, resource, getColorForCourse(context, canvasContext));
    }
    public static Drawable getColoredDrawableFromCanvasId(Context context, int resource, long canvasContextId){
        return getColoredDrawable(context, resource, getColorFromCanvasId(context, canvasContextId));
    }

    //change the color of the drawable and return it
    public static Drawable getColoredDrawable(Context context, int resource, int color) {
        Drawable drawable = context.getResources().getDrawable(resource);
        drawable = drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }

    //a way to get colors for the courses. We could change this to be more sophisticated, but
    //right now it works

    private static HashMap<Long, Integer> courseColors;

    //gets the color for a course and saves it if necessary
    public static int getColorForCourse(Context context, CanvasContext canvasContext) {
        //if we don't have a course or we don't want to change the color
        if(canvasContext == null){
            return context.getResources().getColor(R.color.courseRed);
        }

        long canvasContextId = canvasContext.getId();

        return  getColorFromCanvasId(context, canvasContextId);


    }

   public static int getColorFromCanvasId(Context context, long canvasContextId){
        if (canvasContextId == 0) {
            return context.getResources().getColor(R.color.courseRed);
        }
        //if our hashmap is null from memory pressure, populate it from the shared preference
        if (courseColors == null) {
            if (getCourseColorsFromSharedPreferences(context)) {
                return context.getResources().getColor(R.color.courseRed);
            }
        }
        //check if we've saved this course color before
        Integer color = courseColors.get(canvasContextId);
        if (color == null) {
            int newColor = getNewCourseColor(context, courseColors.size());
            setNewColor(context, canvasContextId, newColor);
            return newColor;
        }
        //try to get it from our static hashmap
        else {
            return color;
        }
    }

    public static int getColorResourceIdForCourse(Context context, CanvasContext canvasContext) {
        final int color = getColorForCourse(context, canvasContext);
        return getResourceIdForColor(context, color);
    }

    private static void setNewColor(Context context, long courseId, int newColor) {
        //courseColors will be valid here due to a previous check in setColorForCourse

        courseColors.put(courseId, newColor);
        SharedPreferences settings = context.getSharedPreferences(ApplicationManager.PREF_FILE_NAME, Context.MODE_PRIVATE);
        settings.edit().putInt(Long.toString(courseId), newColor).commit();

        //now add the course to the course list that we can use to repopulate courseColors if it gets nulled out
        //due to memory pressure
        String courses = settings.getString("courses", null);
        if (courses == null) {
            courses = "";
        }
        courses += Long.toString(courseId) + ",";
        settings.edit().putString("courses", courses).commit();
    }

    private static boolean getCourseColorsFromSharedPreferences(Context context) {
        courseColors = new HashMap<Long, Integer>();
        SharedPreferences settings = context.getSharedPreferences(ApplicationManager.PREF_FILE_NAME,Context.MODE_PRIVATE);
        String courses = settings.getString("courses", null);
        if (courses == null) {
            //this shouldn't happen, but return something so we can have a color
            return true;
        } else {
            String courseList[] = courses.split(",");
            for (String stringCourseId : courseList) {
                int color = settings.getInt(stringCourseId, -1);
                courseColors.put(Long.parseLong(stringCourseId), color);
            }
        }
        return false;
    }

    public static void setColorForCourse(Context context, long courseId, int color) {
        if (courseColors == null) {
            if (getCourseColorsFromSharedPreferences(context)) {
                //something went wrong, we don't have any saved courses. Inform the user
                Toast.makeText((Activity) context, R.string.error, Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            setNewColor(context, courseId, context.getResources().getColor(color));
        }
    }

    //get a color based on the index
    private static int getNewCourseColor(Context context, int index) {
        switch (index % 6) {
            case 0:
                return context.getResources().getColor(R.color.courseOrange);
            case 1:
                return context.getResources().getColor(R.color.courseBlue);
            case 2:
                return context.getResources().getColor(R.color.courseGreen);
            case 3:
                return context.getResources().getColor(R.color.coursePurple);
            case 4:
                return context.getResources().getColor(R.color.courseGold);
            case 5:
                return context.getResources().getColor(R.color.courseRed);
            default:
                return context.getResources().getColor(R.color.courseViolet);
        }
    }

    private static int getResourceIdForColor(Context context, int color) {

        final Resources resources = context.getResources();

        if(color == resources.getColor(R.color.courseOrange)) {
            return R.color.courseOrange;
        } else if(color == resources.getColor(R.color.courseBlue)) {
            return R.color.courseBlue;
        } else if(color == resources.getColor(R.color.courseGreen)) {
            return R.color.courseGreen;
        } else if(color == resources.getColor(R.color.coursePurple)) {
            return R.color.coursePurple;
        } else if(color == resources.getColor(R.color.courseGold)) {
            return R.color.courseGold;
        } else if(color == resources.getColor(R.color.courseRed)) {
            return R.color.courseRed;
        } else if(color == resources.getColor(R.color.courseViolet)) {
            return R.color.courseViolet;
        } else if(color == resources.getColor(R.color.courseChartreuse)) {
            return R.color.courseChartreuse;
        } else if(color == resources.getColor(R.color.courseCyan)) {
            return R.color.courseCyan;
        } else if(color == resources.getColor(R.color.courseSlate)) {
            return R.color.courseSlate;
        } else if(color == resources.getColor(R.color.coursePink)) {
            return R.color.coursePink;
        } else if(color == resources.getColor(R.color.courseHotPink)) {
            return R.color.courseHotPink;
        } else if(color == resources.getColor(R.color.courseGrey)) {
            return R.color.courseGrey;
        } else if(color == resources.getColor(R.color.courseBlack)) {
            return R.color.courseBlack;
        } else if(color == resources.getColor(R.color.courseDarkGrey)) {
            return R.color.courseDarkGrey;
        } else {
            return R.color.courseRed;
        }
    }
}
