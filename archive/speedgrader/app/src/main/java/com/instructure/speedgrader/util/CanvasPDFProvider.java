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

package com.instructure.speedgrader.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pspdfkit.document.providers.InputStreamDataProvider;
import java.io.InputStream;

public class CanvasPDFProvider extends InputStreamDataProvider {

    private static final String LOG_TAG = "SpeedGrader.PDF";

    private CanvasPDFCache mCache;
    private String mUrl;
    private String mFilename = null;
    private long size = FILE_SIZE_UNKNOWN;
    private InputStream input;

    public CanvasPDFProvider(Context context, InputStream inputStream, String url,  String filename){
        if(mCache == null){
            mCache  = CanvasPDFCache.getInstance(context);
        }
        mUrl = url;
        input = inputStream;
        mFilename = filename;
    }

    @NonNull
    @Override
    protected InputStream openInputStream() {
        return input;
    }

    /**
     * This method returns the size of our resource.  Android only gives us an {@link InputStream} for
     * accessing the resources we have to
     */
    @Override public long getSize() {
        // If the file size is already known, return it immediately.
        if (size != FILE_SIZE_UNKNOWN) return size;

        try {
            // Since we can only access PDF's randomly and the inputstream class only allows stream access
            // we need to reopen the stream if we need to seek backwards.
            if (getInputStreamPosition() != 0) {
                reopenInputStream();
            }

            size = openInputStream().available();

            return size;
        } catch (Exception e) {
            return FILE_SIZE_UNKNOWN;
        }
    }

    @NonNull
    @Override
    public String getUid() {
        return mCache.getKeyForUrl(mUrl);
    }

    @Nullable
    @Override
    public String getTitle() {
        return mFilename;
    }
}
