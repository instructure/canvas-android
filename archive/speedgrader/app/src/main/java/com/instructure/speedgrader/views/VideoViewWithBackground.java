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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * The only real use for this class is so that we can have a background before the video plays, but
 * after it starts we remove the background so we can actually see the video play.
 */
public class VideoViewWithBackground extends VideoView {

    public VideoViewWithBackground(Context context) {
        super(context);
    }

    public VideoViewWithBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoViewWithBackground(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initBackground(Bitmap background) {
        this.setBackgroundDrawable(new BitmapDrawable(background));
    }

    public void setBackgroundTransparent() {
        this.setBackgroundDrawable(null);
    }
    @Override
    public void start() {
        setBackgroundTransparent();
        super.start();
    }
}
