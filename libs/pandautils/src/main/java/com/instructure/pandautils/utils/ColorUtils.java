/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.widget.ImageView;


public class ColorUtils {

    @SuppressWarnings("ConstantConditions")
    public static Drawable tintIt(int color, Drawable drawable) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        if (wrappedDrawable != null && drawable != null) DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    public static Drawable tintIt(Context context, int color, @DrawableRes int drawableResId) {
        Drawable wrappedDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, drawableResId));
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    public static Drawable colorIt(int color, Drawable drawable) {
        drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        return drawable;
    }

    public static void colorIt(int color, ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if(drawable == null) return;

        drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        imageView.setImageDrawable(drawable);
    }

    public static Drawable colorIt(Context context, int color, int drawableResId) throws NullPointerException {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        return drawable;
    }

    public static Bitmap colorIt(int color, Bitmap map) {
        Bitmap mutableBitmap = map.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(mutableBitmap, 0, 0, paint);
        return mutableBitmap;
    }
}
