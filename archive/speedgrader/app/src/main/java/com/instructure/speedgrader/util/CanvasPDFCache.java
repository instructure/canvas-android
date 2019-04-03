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
import android.os.Environment;
import android.util.Log;

import com.instructure.annotations_library.FetchFileAsyncTask;
import com.instructure.annotations_library.SimpleDiskCache;

import java.io.File;

public class CanvasPDFCache {

    public static final String TAG = "SpeedGrader";

    private static final int DEFAULT_DISK_CACHE_MAX_SIZE_MB = 10;
    private static final int MEGABYTE = 1024 * 1024;
    private static final int DEFAULT_DISK_CACHE_SIZE = Integer.MAX_VALUE;

    /**
     *  Application Version, you can define it by default otherwise you can
     * get it from Android Manifest
     */
    private static int APP_VERSION = 1;

    /**
     * There has to be only an instance of this class, that´s why we
     * use singleton pattern
     */

    private static CanvasPDFCache mInstance = null;

    /** SimpleDiskCache is an easy class to work with
     * JackeWharton DiskLruCache:
     * https://github.com/JakeWharton/DiskLruCache
     */
    private SimpleDiskCache mSimpleDiskCache;



    public static CanvasPDFCache getInstance(Context context){
        if(mInstance == null){
            mInstance = new CanvasPDFCache(context);
        }
        return mInstance;
    }

    //Constructor
    private CanvasPDFCache(Context context){
        try{
            final File diskCacheDir = getDiskCacheDir(context, TAG );
            mSimpleDiskCache = SimpleDiskCache.open(diskCacheDir, APP_VERSION, DEFAULT_DISK_CACHE_SIZE);
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    public String getKeyForUrl(String url){
        return mSimpleDiskCache.toInternalKey(url);
    }

    public void getInputStream(Context context, String url, FetchFileAsyncTask.FetchFileCallback callback){
        FetchFileAsyncTask.download(context, mSimpleDiskCache, url, callback);
    }

    /**
     * Check if media is mounted or storage is built-in, if so, try and use external cache dir
     * otherwise use internal cache dir
     * @param context
     * @param uniqueName
     * @return
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ?
                        context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }
}