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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

@TargetApi(14)
class TextureViewPreview extends PreviewImpl {

    private final TextureView mTextureView;

    private int mDisplayOrientation;

    TextureViewPreview(Context context, ViewGroup parent) {
        final View view = View.inflate(context, R.layout.texture_view, parent);
        mTextureView = (TextureView) view.findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setSize(width, height);
                dispatchSurfaceChanged();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                setSize(width, height);
                dispatchSurfaceChanged();
                setTruePreviewSize(mTrueWidth, mTrueHeight);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                setSize(0, 0);
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
    }

    @Override
    void setCallback(Callback callback) {
        super.setCallback(callback);
    }

    @Override
    Surface getSurface() {
        return new Surface(mTextureView.getSurfaceTexture());
    }

    @Override
    View getView() {
        return mTextureView;
    }

    @Override
    Class getOutputClass() {
        return SurfaceTexture.class;
    }

    @Override
    void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
    }

    @Override
    boolean isReady() {
        return mTextureView.getSurfaceTexture() != null;
    }

    @Override
    protected void dispatchSurfaceChanged() {
        super.dispatchSurfaceChanged();
    }

    @Override
    SurfaceTexture getSurfaceTexture() {
        return mTextureView.getSurfaceTexture();
    }

    @TargetApi(15)
    @Override
    void setTruePreviewSize(int width, int height) {
        super.setTruePreviewSize(width, height);
        if (mTextureView.getSurfaceTexture() != null) {
            mTextureView.getSurfaceTexture().setDefaultBufferSize(width, height);
        }
    }

}
