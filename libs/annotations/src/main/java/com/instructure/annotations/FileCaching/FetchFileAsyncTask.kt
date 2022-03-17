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

import android.os.AsyncTask
import android.util.Log
import android.webkit.CookieManager
import com.instructure.annotations.FileCaching.FetchFileAsyncTask.FetchFileCallback
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.validOrNull
import okhttp3.Request
import okio.Okio
import okio.buffer
import okio.sink
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.net.ProtocolException

/**
 * Attempts to download the contents of a url to a file. [FetchFileCallback.onFileLoaded] will be
 * called with the resulting [File] upon successful completion, or with null if there was an exception
 * or if the task was canceled. Download progress will be posted to [FetchFileCallback.onProgress]
 * wherever possible.
 */
class FetchFileAsyncTask private constructor(
        private val mCache: SimpleDiskCache,
        private val mUrl: String,
        private val mCallback: FetchFileAsyncTask.FetchFileCallback
) : AsyncTask<Void, Void, File>() {

    interface FetchFileCallback {
        fun onProgress(progress: Float)
        fun onFileLoaded(fileInputStream: File?)
    }

    override fun doInBackground(vararg params: Void): File? {
        return try {
            // Return the file if already cached, otherwise attempt to download and cache
            mCache.getFile(mUrl) ?: downloadAndCacheFile(mUrl)
        } catch (e: IOException) {
            Log.d(LOG_TAG, "Download failed for url: $mUrl", e)
            null
        }
    }

    private fun downloadAndCacheFile(downloadUrl: String): File? {
        // Create a file to temporarily store download contents
        val tmpFile = File.createTempFile(LOG_TAG, null, ContextKeeper.appContext.cacheDir)

        try {
            /* We want to use the shared OkHttpClient instance to retain connection/thread pools
            and configuration, as well as the Espresso IdlingResource for UI testing. However,
            because we're working with potentially large files, we want avoid pushing the smaller
            API responses out of the cache, so we'll derive a new client with caching disabled. */
            val client = CanvasRestAdapter.okHttpClient
                    .newBuilder()
                    .cache(null)
                    .build()

            // We don't need the token information to download the file
            val params = RestParams(shouldIgnoreToken = true)
            val requestBuilder = Request.Builder()
                    .url(downloadUrl)
                    .tag(params)

            // Use the cookies cached for this URL, if available
            CookieManager.getInstance().getCookie(downloadUrl)?.validOrNull()?.let {
                requestBuilder.addHeader("Cookie", it)
            }

            val request = requestBuilder.build()

            val response = client
                    .newCall(request)
                    .execute()

            if (!response.isSuccessful) throw IOException("Unable to download. Error code ${response.code}")

            // Get the total expected download size. If this size is unknown this will be -1, and incremental progress updates will not be posted.
            val total = response.body?.contentLength()?.toFloat() ?: 0f

            // Set up source and sink
            val sink = tmpFile.sink().buffer()
            val source = response.body?.source()

            var downloaded = 0L
            var lastUpdate = System.currentTimeMillis()
            var read: Long

            // Perform download.
            read = source?.read(sink.buffer, BUFFER_SIZE) ?: 0
            if (total > 0) mCallback.onProgress(0f)
            while (read > 0 && !isCancelled) {
                sink.flush()
                if (total > 0) {
                    downloaded += read
                    if (System.currentTimeMillis() >= lastUpdate + MIN_UPDATE_THRESHOLD) {
                        lastUpdate = System.currentTimeMillis()
                        mCallback.onProgress(downloaded / total)
                    }
                }
                read = source?.read(sink.buffer, BUFFER_SIZE) ?: 0
            }
            if (total > 0) mCallback.onProgress(1f)

            // Cleanup
            sink.flush()
            sink.close()
            source?.close()

            // Assume success if we haven't been canceled
            if (!isCancelled) {
                mCache.put(downloadUrl, FileInputStream(tmpFile))
                tmpFile.delete()
                return mCache.getFile(downloadUrl)
            }

        } catch (e: FileNotFoundException) {
            Log.d(LOG_TAG, "File not Found Exception")
            e.printStackTrace()
        } catch (e: ProtocolException) {
            Log.d(LOG_TAG, "ProtocolException : " + downloadUrl)
            e.printStackTrace()
        } catch (e: MalformedURLException) {
            Log.d(LOG_TAG, "MalformedURLException" + downloadUrl)
            e.printStackTrace()
        } catch (e: IOException) {
            Log.d(LOG_TAG, "Failed to save inputStream to cache")
            e.printStackTrace()
        } finally {
            // Clean up the temporary file
            tmpFile.delete()
        }

        return null
    }

    override fun onPostExecute(file: File?) {
        super.onPostExecute(file)
        if (!isCancelled) mCallback.onFileLoaded(file)
    }

    fun cancel() = cancel(false)

    companion object {
        private const val LOG_TAG = "SpeedGrader.FetchTask"

        // Buffer size. This should generally fall between 1024 and 4096
        private const val BUFFER_SIZE = 2048L

        // Minimum time between progress updates in milliseconds; necessary to avoid spamming progress listeners.
        // A value of 33 targets ~30 updates per second
        private const val MIN_UPDATE_THRESHOLD: Long = 33

        fun download(cache: SimpleDiskCache, url: String, callback: FetchFileCallback) = FetchFileAsyncTask(cache, url, callback).apply { execute() }
    }
}
