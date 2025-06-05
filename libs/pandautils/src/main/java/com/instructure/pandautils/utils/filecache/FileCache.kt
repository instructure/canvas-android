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

package com.instructure.pandautils.utils.filecache

import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.weave.resumeSafely
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.InputStream

object FileCache {

    var versionCode = 0

    private val DEFAULT_DISK_CACHE_MAX_SIZE_MB = 250
    private val MEGABYTE = 1024 * 1024
    private val DEFAULT_DISK_CACHE_SIZE = DEFAULT_DISK_CACHE_MAX_SIZE_MB * MEGABYTE

    private val mSimpleDiskCache: SimpleDiskCache by lazy {
        SimpleDiskCache.open(ContextKeeper.appContext.externalCacheDir!!, versionCode, DEFAULT_DISK_CACHE_SIZE.toLong())
    }

    fun getInputStream(url: String, callback: FetchFileAsyncTask.FetchFileCallback) =
        FetchFileAsyncTask.download(mSimpleDiskCache, url, callback)

    fun putInputStream(url: String, inputStream: InputStream) {
        mSimpleDiskCache.put(url, inputStream)
    }

}

/**
 * Attempts to download a file from a URL and return the resulting [File] object. Internally this
 * uses a size-limited disk cache for quick retrieval of the most frequently-accessed files.
 *
 * @param url The URL of the file to be downloaded
 * @param onProgressChanged A callback for download progress updates. Progress is between 0f (0%)
 * and 1f (100%), and will be updated no more than 30 times per second. Note that this is called from
 * a background thread; if you need to manipulate UI based on these updates, you may wrap your code in
 * a [onUI][com.instructure.canvasapi2.utils.weave.WeaveCoroutine.onUI] block.
 * @return The file if it was successfully downloaded or retrieved from cache, or null if there was an error.
 */
@Suppress("EXPERIMENTAL_FEATURE_WARNING")
suspend fun FileCache.awaitFileDownload(url: String, onProgressChanged: ((Float) -> Unit)? = null): File? =
    suspendCancellableCoroutine { continuation ->

        val task = this.getInputStream(url, object : FetchFileAsyncTask.FetchFileCallback {
            override fun onProgress(progress: Float) {
                if (!continuation.isCancelled) onProgressChanged?.invoke(progress)
            }

            override fun onFileLoaded(fileInputStream: File?) {
                continuation.resumeSafely(fileInputStream)
            }
        })

        continuation.invokeOnCancellation { task.cancel() }
    }
