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
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

//import android.media.ExifInterface;

/**
 * Created by andrew on 2017-08-18.
 */

public class ExifUtil {

    public static int getExifOrientation(byte[] picture){
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        try {
            orientation = getExifOrientation(new ByteArrayInputStream(picture));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orientation;
    }

    public static Bitmap decodeBitmapWithRotation(byte[] picture, boolean frontFacing) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        Matrix matrix = getBitmapRotation(picture, frontFacing);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Matrix getBitmapRotation(byte[] picture, boolean frontFacing) {
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        try {
            orientation = getExifOrientation(new ByteArrayInputStream(picture));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            case ExifInterface.ORIENTATION_UNDEFINED:
                break;
        }

        if(frontFacing){
            matrix.postScale(-1, 1);
        }

        return matrix;
    }

    private static int getExifOrientation(InputStream inputStream) throws IOException {
        ExifInterface exif = new ExifInterface(inputStream);
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }
}
