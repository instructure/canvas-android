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

package com.instructure.speedgrader.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.ImageView;

import com.instructure.speedgrader.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ViewUtils {

    public static float convertDipsToPixels(float dp, Context context) {
        Resources resources = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    //Adapted from
    //http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public static Target getNewTarget(final int defaultImageResource, final ImageView view){
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                view.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {
                view.setImageResource(defaultImageResource);
            }

            @Override
            public void onPrepareLoad(Drawable drawable) {
                view.setImageResource(defaultImageResource);
            }
        };
    }

    public static Target getNewTarget(final ImageView view){
        return getNewTarget(R.drawable.ic_cv_user, view);
    }
}