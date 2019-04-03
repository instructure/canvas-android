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

import android.graphics.YuvImage;
import android.hardware.Camera;

class ProcessStillTask implements Runnable {

    private byte[] data;
    private Camera camera;
    private int rotation;
    private OnStillProcessedListener onStillProcessedListener;

    public ProcessStillTask(byte[] data, Camera camera, int rotation, OnStillProcessedListener onStillProcessedListener) {
        this.data = data;
        this.camera = camera;
        this.rotation = rotation;
        this.onStillProcessedListener = onStillProcessedListener;
    }

    @Override
    public void run() {
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
        byte[] rotatedData = new Rotation(data, width, height, rotation).getYuv();

        int postWidth;
        int postHeight;

        switch (rotation) {
            case 90:
            case 270:
                postWidth = height;
                postHeight = width;
                break;

            case 0:
            case 180:
            default:
                postWidth = width;
                postHeight = height;
                break;
        }

        YuvImage yuv = new YuvImage(rotatedData, parameters.getPreviewFormat(), postWidth, postHeight, null);

        onStillProcessedListener.onStillProcessed(yuv);
    }

    interface OnStillProcessedListener {
        void onStillProcessed(YuvImage yuv);
    }

}