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

import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.student.activity.InternalWebViewActivity;
import com.instructure.student.activity.InterwebsToApplication;
import com.instructure.student.router.RouteMatcher;

public class ViewUtils {
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
