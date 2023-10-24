/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.annotations.FileCaching

import com.instructure.annotations.BuildConfig
import com.instructure.canvasapi2.utils.ContextKeeper
import java.io.InputStream

object FileCache {

    var versionCode = 0

    private val DEFAULT_DISK_CACHE_MAX_SIZE_MB = 250
    private val MEGABYTE = 1024 * 1024
    private val DEFAULT_DISK_CACHE_SIZE = DEFAULT_DISK_CACHE_MAX_SIZE_MB * MEGABYTE

    private val mSimpleDiskCache: SimpleDiskCache by lazy {
        SimpleDiskCache.open(ContextKeeper.appContext.externalCacheDir!!, versionCode, DEFAULT_DISK_CACHE_SIZE.toLong())
    }

    fun getInputStream(url: String, callback: FetchFileAsyncTask.FetchFileCallback) = FetchFileAsyncTask.download(mSimpleDiskCache, url, callback)

    fun putInputStream(url: String, inputStream: InputStream) {
        mSimpleDiskCache.put(url, inputStream)
    }

}
