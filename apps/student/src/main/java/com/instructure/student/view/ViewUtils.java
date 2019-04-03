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

package com.instructure.student.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.student.activity.InternalWebViewActivity;
import com.instructure.student.activity.InterwebsToApplication;
import com.instructure.student.router.RouteMatcher;
import com.instructure.canvasapi2.utils.ApiPrefs;

import java.lang.reflect.Method;

public class ViewUtils {
    public static float convertDipsToPixels(float dp, Context context){
        Resources resources = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static void showCroutonFromBundle(Activity activity, Bundle bundle) {
        if(bundle.containsKey("croutonStyle") && bundle.containsKey("croutonMessage")) {
            String message = bundle.getString("croutonMessage");
            if(!TextUtils.isEmpty(message)) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static Bundle createCroutonBundle(int croutonStyle, String message) {
        Bundle bundle = new Bundle();
        bundle.putInt("croutonStyle", croutonStyle);
        bundle.putString("croutonMessage", message);
        return bundle;
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

    /**
     * Parse the links to make them clickable and override what they do (so we don't just open a different app)
     * @param tv
     */
    public static void linkifyTextView(TextView tv) {
        SpannableString current = (SpannableString) tv.getText();
        URLSpan[] spans =
                current.getSpans(0, current.length(), URLSpan.class);

        for (URLSpan span : spans) {
            int start = current.getSpanStart(span);
            int end = current.getSpanEnd(span);

            current.removeSpan(span);
            current.setSpan(new DefensiveURLSpan(span.getURL()), start, end,
                    0);
        }
    }

    public static class DefensiveURLSpan extends URLSpan {
        private String url;

        private DefensiveURLSpan(String url) {
            super(url);
            this.url = url;
        }

        @Override
        public void onClick(View widget) {
            if (RouteMatcher.INSTANCE.getInternalRoute(url, ApiPrefs.getDomain()) != null) {
                // Normally we would do the normal routing, but we need an activity for that, which we don't have. So we'll use the more generic routing for the app
                widget.getContext().startActivity(InterwebsToApplication.Companion.createIntent(widget.getContext(), Uri.parse(url)));
            } else {
                Intent intent = InternalWebViewActivity.createIntent(widget.getContext(), url, "", false);
                widget.getContext().startActivity(intent);
            }
        }
    }
}
