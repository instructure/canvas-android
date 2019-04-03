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

package com.instructure.speedgrader.views;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.webkit.WebView;

public class DocumentWebView extends WebView {

    public DocumentWebView(Context context) {
        super(context);
    }
    public DocumentWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     *  Function: canScrollHorizontally
     *  Description: Checks the horizontal bounds of a webview and determines whether or not the webview can scroll
     *  horizontally. If so, we want the webview to scroll all the way to the right/left before paging to the next item
     *  in the viewpager
     * */
    public boolean canScrollHorizontally(int direction){
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }
}
