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

package com.instructure.teacher.binders;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;



public class BaseBinder {

    public static void setVisible(View v) {
        if (v == null) return;
        v.setVisibility(View.VISIBLE);
    }

    public static void setInvisible(View v) {
        if (v == null) return;
        v.setVisibility(View.INVISIBLE);
    }

    public static void setGone(View v) {
        if (v == null) return;
        v.setVisibility(View.GONE);
    }

    public static void ifHasTextSetVisibleElseGone(TextView v) {
        if (v == null) return;
        if (TextUtils.isEmpty(v.getText())) {
            setGone(v);
        } else {
            setVisible(v);
        }
    }

    public static void ifHasTextSetVisibleElseInvisible(TextView v) {
        if (v == null) return;
        if (TextUtils.isEmpty(v.getText())) {
            setInvisible(v);
        } else {
            setVisible(v);
        }
    }

    public static float getListItemHeight(Context context) {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
        ((WindowManager) (context.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getMetrics(metrics);
        return TypedValue.complexToDimension(value.data, metrics);
    }


    public static ShapeDrawable createIndicatorBackground(int color) {
        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.getPaint().setColor(color);
        return circle;
    }


    public static void setCleanText(TextView textView, String text) {
        if(!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText("");
        }
    }

    public static void setCleanText(TextView textView, String text, String defaultText) {
        if(!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            textView.setText(defaultText);
        }
    }

    public static void updateShadows(boolean isFirstItem, boolean isLastItem, View top, View bottom) {
        if(isFirstItem) {
            setVisible(top);
        } else {
            setInvisible(top);
        }

        if(isLastItem) {
            setVisible(bottom);
        } else {
            setInvisible(bottom);
        }
    }
}

