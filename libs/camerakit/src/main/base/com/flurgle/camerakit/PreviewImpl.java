/*
MIT License

Copyright (c) 2016 WonderKiln

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.flurgle.camerakit;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

abstract class PreviewImpl {

    interface Callback {
        void onSurfaceChanged();
    }

    private Callback mCallback;

    private int mWidth;
    private int mHeight;

    protected int mTrueWidth;
    protected int mTrueHeight;

    void setCallback(Callback callback) {
        mCallback = callback;
    }

    abstract Surface getSurface();

    abstract View getView();

    abstract Class getOutputClass();

    abstract void setDisplayOrientation(int displayOrientation);

    abstract boolean isReady();

    protected void dispatchSurfaceChanged() {
        mCallback.onSurfaceChanged();
    }

    SurfaceHolder getSurfaceHolder() {
        return null;
    }

    SurfaceTexture getSurfaceTexture() {
        return null;
    }

    void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;

        // Refresh true preview size to adjust scaling
        setTruePreviewSize(mTrueWidth, mTrueHeight);
    }

    int getWidth() {
        return mWidth;
    }

    int getHeight() {
        return mHeight;
    }

    void setTruePreviewSize(final int width, final int height) {
        this.mTrueWidth = width;
        this.mTrueHeight = height;
        getView().post(new Runnable() {
            @Override
            public void run() {
                if (width != 0 && height != 0) {
                    AspectRatio aspectRatio = AspectRatio.of(width, height);
                    int targetHeight = (int) (getView().getWidth() * aspectRatio.toFloat());
                    float scaleY;
                    if (getView().getHeight() > 0) {
                        scaleY = (float) targetHeight / (float) getView().getHeight();
                    } else {
                        scaleY = 1;
                    }

                    if (scaleY > 1) {
                        getView().setScaleX(1);
                        getView().setScaleY(scaleY);
                    } else {
                        getView().setScaleX(1 / scaleY);
                        getView().setScaleY(1);
                    }
                }
            }
        });
    }

    int getTrueWidth() {
        return mTrueWidth;
    }

    int getTrueHeight() {
        return mTrueHeight;
    }

}
