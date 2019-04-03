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

import android.content.Context;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;

public abstract class DisplayOrientationDetector {

    private final OrientationEventListener mOrientationEventListener;

    static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();
    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90);    // Display rotation 3 is actually an orientation of 90
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270);  // Display rotation of 1 is actually an orientation of 270
    }

    private Display mDisplay;

    private int mLastKnownDisplayOrientation = 0;
    private int mLastKnownDeviceOrientation = 0;

    public DisplayOrientationDetector(Context context) {
        mOrientationEventListener = new OrientationEventListener(context) {

            private int mLastKnownDisplayRotation = -1;

            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN || mDisplay == null) {
                    return;
                }

                boolean somethingChanged = false;

                final int displayRotation = mDisplay.getRotation();
                if (mLastKnownDisplayRotation != displayRotation) {
                    mLastKnownDisplayRotation = displayRotation;
                    somethingChanged = true;
                }

                int deviceOrientation;
                if (orientation >= 60 && orientation <= 140){
                    // the mDisplay.getRotation stuff is messed. This keeps it consistent.
                    deviceOrientation = 270;
                } else if (orientation >= 140 && orientation <= 220) {
                    deviceOrientation = 180;
                } else if (orientation >= 220 && orientation <= 300) {
                    // the mDisplay.getRotation stuff is messed. This keeps it consistent.
                    deviceOrientation = 90;
                } else {
                    deviceOrientation = 0;
                }

                if (mLastKnownDeviceOrientation != deviceOrientation) {
                    mLastKnownDeviceOrientation = deviceOrientation;
                    somethingChanged = true;
                }

                if(somethingChanged){
                    dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(displayRotation));
                }
            }

        };
    }

    public void enable(Display display) {
        mDisplay = display;
        mOrientationEventListener.enable();
        dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(display.getRotation()));
    }

    public void disable() {
        mOrientationEventListener.disable();
        mDisplay = null;
    }

    public int getLastKnownDisplayOrientation() {
        return mLastKnownDisplayOrientation;
    }

    void dispatchOnDisplayOrientationChanged(int displayOrientation) {
        mLastKnownDisplayOrientation = displayOrientation;

        // If we don't have accelerometers, we can't detect the device orientation.
        if(mOrientationEventListener.canDetectOrientation()){
            onDisplayOrientationChanged(displayOrientation, mLastKnownDeviceOrientation);
        } else {
            onDisplayOrientationChanged(displayOrientation, displayOrientation);
        }

    }

    public abstract void onDisplayOrientationChanged(int displayOrientation, int deviceOrientation);

}