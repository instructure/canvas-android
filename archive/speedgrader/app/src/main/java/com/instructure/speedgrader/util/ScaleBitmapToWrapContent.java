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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.opengl.GLES10;

import com.squareup.picasso.Transformation;

public class ScaleBitmapToWrapContent implements Transformation {

    /**
     * This Transform will take a potentially large image and scale it to have a maximum size of 2048 x 2048, which is the
     * smallest, maximum bitmap texture size in android devices.
     *
     * */
    private int size;
    private int maxBitmapSize = 2048;

    public ScaleBitmapToWrapContent(int size){
        this.size = size;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        float scale;
        int newWidth;
        Bitmap scaledBitmap;
        int newImageHeight = (this.maxBitmapSize > source.getHeight() ? source.getHeight() : this.maxBitmapSize);
        scale = (float) newImageHeight / source.getHeight();
        newWidth = Math.round(source.getWidth() * scale);
        scaledBitmap = Bitmap.createScaledBitmap(source, newWidth,  newImageHeight, true);

        if(scaledBitmap != source){
            source.recycle();
        }

        return scaledBitmap;
    }

    @Override
    public String key() {
        return new StringBuilder("scaledBitmapToWrapContent")
                .append(this.size)
                .toString();
    }
}