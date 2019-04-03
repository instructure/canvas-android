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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;

import java.lang.reflect.Method;

public class ViewUtils {

    public static float convertDipsToPixels(float dp, Context context){
        Resources resources = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }


    @SuppressLint("NewApi")
    public static int getWindowHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();

        try {
            // For JellyBean 4.2 (API 17) and onward
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);
                return metrics.heightPixels;
            } else {
                Method mGetRawHeight = Display.class.getMethod("getRawHeight");
                return (Integer) mGetRawHeight.invoke(display);
            }
        } catch (Exception e) {
            // if neither of the above work, we should at least return something
            // that's fairly close, even if it doesn't account for the navigation bar
            // see: http://stackoverflow.com/questions/10991194/android-displaymetrics-returns-incorrect-screen-size-in-pixels-on-ics
            display.getMetrics(metrics);
            return metrics.heightPixels;
        }
    }

}
