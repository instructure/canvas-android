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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CenterCrop {

    private byte[] croppedJpeg;

    public CenterCrop(YuvImage yuv, AspectRatio targetRatio, int jpegCompression) {
        Rect crop = getCrop(yuv.getWidth(), yuv.getHeight(), targetRatio);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(crop, jpegCompression, out);
        this.croppedJpeg = out.toByteArray();
    }

    public CenterCrop(byte[] jpeg, AspectRatio targetRatio, int jpegCompression) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, options);

        Rect crop = getCrop(options.outWidth, options.outHeight, targetRatio);
        try {
            Bitmap bitmap = BitmapRegionDecoder.newInstance(
                    jpeg,
                    0,
                    jpeg.length,
                    true
            ).decodeRegion(crop, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, jpegCompression, out);
            this.croppedJpeg = out.toByteArray();
        } catch (IOException e) {
            Log.e("CameraKit", e.toString());
        }
    }

    private static Rect getCrop(int currentWidth, int currentHeight, AspectRatio targetRatio) {
        AspectRatio currentRatio = AspectRatio.of(currentWidth, currentHeight);

        Rect crop;
        if (currentRatio.toFloat() > targetRatio.toFloat()) {
            int width = (int) (currentHeight * targetRatio.toFloat());
            int widthOffset = (currentWidth - width) / 2;
            crop = new Rect(widthOffset, 0, currentWidth - widthOffset, currentHeight);
        } else {
            int height = (int) (currentWidth * targetRatio.inverse().toFloat());
            int heightOffset = (currentHeight - height) / 2;
            crop = new Rect(0, heightOffset, currentWidth, currentHeight - heightOffset);
        }

        return crop;
    }

    public byte[] getJpeg() {
        return croppedJpeg;
    }

}
