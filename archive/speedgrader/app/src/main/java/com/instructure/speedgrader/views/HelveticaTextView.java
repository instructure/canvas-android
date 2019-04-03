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
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class HelveticaTextView extends TextView {

    private static final String TAG = "HelveticaTextView";

    private static Map<String, Typeface> cachedTypefaces = new HashMap<>();

    private static Typeface getCachedTypeface(Context context, String typefaceName) {
        if (cachedTypefaces.containsKey(typefaceName)) return cachedTypefaces.get(typefaceName);
        try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), typefaceName);
            cachedTypefaces.put(typefaceName, typeface);
            return typeface;
        } catch (RuntimeException e) {
            Log.w(TAG, "Typeface not found: " + typefaceName);
            return Typeface.DEFAULT;
        }
    }

    public HelveticaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HelveticaTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public HelveticaTextView(Context context) {
        super(context);
    }

    public void setTypeface(Typeface tf, int style){
        if(style == Typeface.BOLD){
            setTypeface(getCachedTypeface(getContext(), "HelveticaNeueLTCom-BdCn.ttf"));
        }else if(style == Typeface.NORMAL){
            setTypeface(getCachedTypeface(getContext(), "HelveticaNeueLTCom-MdCn.ttf"));
        }else if(style == Typeface.ITALIC){
            setTypeface(getCachedTypeface(getContext(), "HelveticaNeueLTCom-LtIt.ttf"));
        }
    }
}
