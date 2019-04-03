package com.instructure.teacher.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.instructure.canvasapi2.utils.Logger;

//Adapted from http://stackoverflow.com/questions/18742274/how-to-animate-the-width-and-height-of-a-layout
public class ResizeAnimation extends Animation {

    private static final long DEFAULT_RESIZE_DURATION  = 400;

    private int startWidth;
    private int endWidth;
    private int deltaWidth;

    private int startHeight;
    private int endHeight;
    private int deltaHeight;

    private int originalWidth;
    private int originalHeight;

    private View view;

    private boolean fillEnabled = false;

    /**
     * constructor, do not forget to use the setHeightParams(int, int) or setWidthParams(int, int)
     * method before starting the animation
     * @param view The View to resize
     */
    public ResizeAnimation(View view) {
        this.view = view;
        setDuration(DEFAULT_RESIZE_DURATION);
        originalHeight = view.getHeight();
        originalWidth = view.getWidth();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if(interpolatedTime == 1.0 && !fillEnabled) {
            view.getLayoutParams().height = originalHeight;
            view.getLayoutParams().width = originalWidth;
        } else {
            if(deltaHeight != 0) {
                Logger.d("HEIGHT ADJUSTMENT");
                if (interpolatedTime >= 0.99F) {
                    view.getLayoutParams().height = endHeight;
                } else {
                    view.getLayoutParams().height = Math.round((startHeight + deltaHeight * interpolatedTime));
                }
            }
            if(deltaWidth != 0) {
                Logger.d("WIDTH ADJUSTMENT");
                if(interpolatedTime >= 0.99F) {
                    view.getLayoutParams().width = endWidth;
                } else {
                    view.getLayoutParams().width =  Math.round((startWidth + deltaWidth * interpolatedTime));
                }
            }
        }
        this.view.requestLayout();
    }

    /**
     * set the starting and ending width for the resize animation
     * starting width is usually the views current width, the end width is the width
     * we want to reach after the animation is completed
     * @param startWidth width in pixels
     * @param endWidth width in pixels
     */
    public void setWidthParams(int startWidth, int endWidth) {
        this.startWidth = startWidth;
        this.endWidth = endWidth;
        this.deltaWidth = endWidth - startWidth;
    }

    /**
     * set the starting and ending height for the resize animation
     * starting width is usually the views current height, the end height is the height
     * we want to reach after the animation is completed
     * @param startHeight width in pixels
     * @param endHeight width in pixels
     */
    public void setHeightParams( int startHeight, int endHeight) {
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.deltaHeight = endHeight - startHeight;
    }

    @Override
    public void setFillEnabled(boolean enabled) {
        fillEnabled = enabled;
        super.setFillEnabled(enabled);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }
}
