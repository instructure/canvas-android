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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CommonAspectRatioFilter {

    private List<Size> mPreviewSizes;
    private List<Size> mCaptureSizes;

    public CommonAspectRatioFilter(List<Size> previewSizes, List<Size> captureSizes) {
        this.mPreviewSizes = previewSizes;
        this.mCaptureSizes = captureSizes;
    }

    public TreeSet<AspectRatio> filter() {
        Set<AspectRatio> previewAspectRatios = new HashSet<>();
        for (Size size : mPreviewSizes) {
            if (size.getWidth() >= CameraKit.Internal.screenHeight && size.getHeight() >= CameraKit.Internal.screenWidth) {
                previewAspectRatios.add(AspectRatio.of(size.getWidth(), size.getHeight()));
            }
        }

        Set<AspectRatio> captureAspectRatios = new HashSet<>();
        for (Size size : mCaptureSizes) {
            captureAspectRatios.add(AspectRatio.of(size.getWidth(), size.getHeight()));
        }

        TreeSet<AspectRatio> output = new TreeSet<>();
        for (AspectRatio aspectRatio : previewAspectRatios) {
            if (captureAspectRatios.contains(aspectRatio)) {
                output.add(aspectRatio);
            }
        }

        return output;
    }

}
