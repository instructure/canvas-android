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
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import androidx.collection.SparseArrayCompat;
import android.util.SparseIntArray;

public class ConstantMapper {

    private abstract static class BaseMapper<T> {

        protected int mCameraKitConstant;

        protected BaseMapper(int cameraKitConstant) {
            this.mCameraKitConstant = cameraKitConstant;
        }

        abstract T map();

    }

    static class Flash extends BaseMapper<String> {

        private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();

        static {
            FLASH_MODES.put(CameraKit.Constants.FLASH_OFF, Camera.Parameters.FLASH_MODE_OFF);
            FLASH_MODES.put(CameraKit.Constants.FLASH_ON, Camera.Parameters.FLASH_MODE_ON);
            FLASH_MODES.put(CameraKit.Constants.FLASH_AUTO, Camera.Parameters.FLASH_MODE_AUTO);
            FLASH_MODES.put(CameraKit.Constants.FLASH_TORCH, Camera.Parameters.FLASH_MODE_TORCH);
        }

        protected Flash(int cameraKitConstant) {
            super(cameraKitConstant);
        }

        @Override
        String map() {
            return FLASH_MODES.get(mCameraKitConstant, FLASH_MODES.get(CameraKit.Constants.FLASH_OFF));
        }

    }

    @TargetApi(21)
    static class Flash2 extends BaseMapper<String> {

        protected Flash2(int cameraKitConstant) {
            super(cameraKitConstant);
        }

        @Override
        String map() {
            return null;
        }

    }

    static class Facing extends BaseMapper<Integer> {

        private static final SparseArrayCompat<Integer> FACING_MODES = new SparseArrayCompat<>();

        static {
            FACING_MODES.put(CameraKit.Constants.FACING_BACK, Camera.CameraInfo.CAMERA_FACING_BACK);
            FACING_MODES.put(CameraKit.Constants.FACING_FRONT, Camera.CameraInfo.CAMERA_FACING_FRONT);
        }

        protected Facing(int cameraKitConstant) {
            super(cameraKitConstant);
        }

        @Override
        Integer map() {
            return FACING_MODES.get(mCameraKitConstant, FACING_MODES.get(CameraKit.Constants.FACING_BACK));
        }

    }

    @TargetApi(21)
    static class Facing2 extends BaseMapper<Integer> {

        private static final SparseIntArray FACING_MODES = new SparseIntArray();

        static {
            FACING_MODES.put(CameraKit.Constants.FACING_BACK, CameraCharacteristics.LENS_FACING_BACK);
            FACING_MODES.put(CameraKit.Constants.FACING_FRONT, CameraCharacteristics.LENS_FACING_FRONT);
        }

        protected Facing2(int cameraKitConstant) {
            super(cameraKitConstant);
        }

        @Override
        Integer map() {
            return FACING_MODES.get(mCameraKitConstant, FACING_MODES.get(CameraKit.Constants.FACING_BACK));
        }

    }


}
