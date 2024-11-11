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
package com.instructure.pandautils.loaders

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import androidx.core.content.FileProvider
import androidx.loader.content.AsyncTaskLoader
import com.instructure.canvasapi2.CanvasRestAdapter.Companion.okHttpClient
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.HttpHelper.redirectURL
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.R
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader.LoadedMedia
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.Utils.getAttachmentsDirectory
import com.instructure.pandautils.utils.orDefault
import okhttp3.Request
import okio.Source
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.util.regex.Pattern

class OpenMediaAsyncTaskLoader(context: Context, args: Bundle?) : AsyncTaskLoader<LoadedMedia>(context) {
    enum class ErrorType { NO_APPS, UNKNOWN }

    class LoadedMedia {
        // Try to open a MediaComment or attachment
        var errorType = ErrorType.UNKNOWN
        var errorMessage = 0
            set(errorMessage) {
                field = errorMessage
                isError = true
            }

        var isError = false
            private set

        var isUseOutsideApps = false

        // Used to identify when we don't want to show annotations/etc for pspdfkit
        var isSubmission = false
        var intent: Intent? = null

        // Used for html files and submission target
        var bundle: Bundle? = null
        var isHtmlFile = false

        fun setHtmlBundle(bundle: Bundle?) {
            isHtmlFile = true
            this.bundle = bundle
        }

    }

    private var mimeType: String? = null
    var url: String = ""
    var path: String = ""
    var filename: String? = null
        private set
    var fileId: String? = null
        private set
    private var isSubmission = false
    private var canvasContext: CanvasContext? = null
    private var isUseOutsideApps = false
    private var extras: Bundle? = null
    private val packageManager: PackageManager = context.applicationContext.packageManager

    init {
        if (args != null) {
            path = args.getString(Const.PATH) ?: ""
            url = args.getString(Const.URL) ?: ""
            if (path.isBlank() && url.isBlank()) throw IllegalArgumentException("Both arguments ${Const.PATH} and ${Const.URL} cannot be null")
            isUseOutsideApps = args.getBoolean(Const.OPEN_OUTSIDE)
            if (args.containsKey(Const.MIME) && args.containsKey(Const.FILE_URL)) {
                mimeType = args.getString(Const.MIME)
                filename = args.getString(Const.FILE_URL)
                fileId = args.getString(Const.FILE_ID)
                filename = makeFilenameUnique(filename, url)
            } else if (args.containsKey(Const.FILE_URL)) {
                val name = args.getString(Const.FILE_URL)
                if (name.isValid()) filename = name
            }
            if (args.containsKey(Const.IS_SUBMISSION)) {
                isSubmission = args.getBoolean(Const.IS_SUBMISSION)
            }
            if (args.containsKey(Const.EXTRAS)) {
                extras = args.getBundle(Const.EXTRAS)
            }

            canvasContext = args.getParcelable(Const.CANVAS_CONTEXT)
        }
    }

    override fun loadInBackground(): LoadedMedia {
        val loadedMedia = LoadedMedia()
        if (isUseOutsideApps) loadedMedia.isUseOutsideApps = true
        if (isSubmission) loadedMedia.isSubmission = true
        val context = context
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.putExtra(Const.IS_MEDIA_TYPE, true)
            if (isHtmlFile && canvasContext != null) {
                val file = if (path.isNotBlank()) File(path) else downloadFile(context, url, filename)
                val bundle = FileUploadUtils.createTaskLoaderBundle(
                    canvasContext,
                    FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY,
                        file
                    ).toString(),
                    filename,
                    false
                )
                loadedMedia.setHtmlBundle(bundle)
            } else if (isHtmlFile && canvasContext == null) {
                // When the canvasContext is null we're routing from the teacher app, which just needs the url and title to get the html file
                val bundle = Bundle()
                bundle.putString(Const.INTERNAL_URL, url)
                bundle.putString(Const.ACTION_BAR_TITLE, filename)
                loadedMedia.setHtmlBundle(bundle)
            } else {
                loadedMedia.isHtmlFile = isHtmlFile
                val uri = if (url.isNotBlank()) {
                    attemptConnection(url)
                } else {
                    val file = File(path)
                    FileProvider.getUriForFile(context, context.applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY, file)
                }
                if (uri != null) {
                    intent.setDataAndType(uri, mimeType)
                    loadedMedia.intent = intent
                    if (extras != null) loadedMedia.bundle = extras
                    Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "Intent can be handled: " + isIntentHandledByActivity(intent))
                    attemptDownloadFile(context, intent, loadedMedia, url, filename)
                } else {
                    loadedMedia.errorMessage = R.string.noDataConnection
                }
            }
        } catch (e: MalformedURLException) {
            Log.e(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "MalformedURLException: $e")
        } catch (e: IOException) {
            Log.e(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "IOException: $e")
            if (e.message?.contains("404").orDefault()) {
                loadedMedia.errorMessage = R.string.fileNotFound
            } else {
                loadedMedia.errorMessage = R.string.unexpectedErrorOpeningFile
            }
        } catch (e: ActivityNotFoundException) {
            Log.e(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "ActivityNotFoundException: $e")
            loadedMedia.errorMessage = R.string.noApps
        } catch (e: Exception) {
            Log.e(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "Exception: $e")
            loadedMedia.errorMessage = R.string.unexpectedErrorOpeningFile
        }
        return loadedMedia
    }

    private val isHtmlFile: Boolean
        get() = filename?.endsWith(".htm", true) == true || filename?.endsWith(".html", true) == true

    /**
     * @return Uri if there's a connection, returns null otherwise
     */
    @Throws(IOException::class)
    private fun attemptConnection(url: String): Uri? {
        var uri: Uri? = null
        val hc = URL(url).openConnection() as HttpURLConnection
        val connection = redirectURL(hc)
        val connectedUrl = connection.url.toString()
        // When only the url is specified in the bundle arguments, mimeType and filename are null or empty.
        if (!mimeType.isValid()) {
            mimeType = connection.contentType // Gets content type from headers
            if (mimeType == null) {
                // Gets content type from url query param
                mimeType = Uri.parse(url).getQueryParameter("content_type")
            }
            if (mimeType == null) throw IOException()
        }
        Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "mimeType: $mimeType")
        if (!filename.isValid()) {
            // parse filename from Content-Disposition header which is a response field that is normally used to set the file name
            val headerField = connection.getHeaderField("Content-Disposition")
            if (headerField != null) {
                filename = parseFilename(headerField)
                filename = makeFilenameUnique(filename, url)
            } else {
                filename = "" + url.hashCode()
            }
        }
        if (connectedUrl.isValid()) {
            uri = Uri.parse(connectedUrl)
            if ("binary/octet-stream".equals(mimeType, true) || "*/*".equals(mimeType, true)) {
                val guessedMimeType = URLConnection.guessContentTypeFromName(uri.path)
                Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "guess mimeType: $guessedMimeType")
                if (guessedMimeType.isValid()) {
                    mimeType = guessedMimeType
                }
            }
        }
        connection.disconnect()
        return uri
    }

    private fun downloadFile(context: Context, url: String, filename: String?): File {
        // They have to download the content first... gross.
        // Download it if the file doesn't exist in the external cache
        Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "downloadFile URL: $url")
        val attachmentFile = if (filename?.endsWith(".pdf").orDefault()) {
            File(File(context.filesDir, "pdfs-${ApiPrefs.user?.id}"), filename)
        } else {
            File(getAttachmentsDirectory(context), filename)
        }
        Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "File: $attachmentFile")
        if (!attachmentFile.exists()) {
            // Download the content from the url
            if (writeAttachmentsDirectoryFromURL(url, attachmentFile)) {
                Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "file not cached")
                return attachmentFile
            }
        }
        return attachmentFile
    }

    private fun isIntentHandledByActivity(intent: Intent): Boolean {
        val cn = intent.resolveActivity(packageManager)
        return cn != null
    }

    private fun attemptDownloadFile(context: Context, intent: Intent, loadedMedia: LoadedMedia, url: String, filename: String?) {
        val file = if (path.isNotBlank()) File(path) else downloadFile(context, url, filename)
        val contentResolver = context.contentResolver
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY,
            file
        )

        // Sometimes the CanvasWebView download listener won't be able to resolve the
        // contentDisposition which causes the contentResolver to be unable to determine the
        // mime type since the file name will have no extension. In that case, we use the mime type.
        if (contentResolver.getType(fileUri)!!.contains("octet-stream") && mimeType!!.contains("pdf")) {
            intent.setDataAndType(fileUri, mimeType)
        } else {
            intent.setDataAndType(fileUri, contentResolver.getType(fileUri))
        }

        // We know that we can always handle pdf intents with pspdfkit, so we don't want to error out here
        if (!isIntentHandledByActivity(intent) && mimeType != "application/pdf") {
            loadedMedia.errorMessage = R.string.noApps
            loadedMedia.errorType = ErrorType.NO_APPS
        } else {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            loadedMedia.intent = intent
        }
    }

    @Throws(IOException::class)
    @Suppress("BooleanLiteralArgument")
    private fun writeAttachmentsDirectoryFromURL(url: String, toWriteTo: File): Boolean {
        val client = okHttpClient.newBuilder().cache(null).build()
        val params = RestParams(null, null, "/api/v1/", false, true, false, false, null)
        val requestBuilder = Request.Builder().url(url).tag(params)
        val cookie: String? = CookieManager.getInstance().getCookie(url)
        if (cookie.isValid()) requestBuilder.addHeader("Cookie", cookie)
        val request = requestBuilder.build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            response.body?.close()
            throw IOException("Unable to download. Error code ${response.code}")
        }
        toWriteTo.parentFile.mkdirs()
        val sink = toWriteTo.sink().buffer()
        val source: Source = response.body!!.source()
        sink.writeAll(source)
        sink.flush()
        sink.close()
        source.close()
        return true
    }

    companion object {
        fun parseFilename(headerField: String?): String? {
            var filename = headerField
            val matcher = Pattern.compile("filename=\"(.*)\"").matcher(headerField)
            if (matcher.find()) {
                filename = matcher.group(1)
            }
            return filename
        }

        fun makeFilenameUnique(filename: String?, url: String): String {
            val matcher = Pattern.compile("(.*)\\.(.*)").matcher(filename)
            return if (matcher.find()) {
                val actualFilename = matcher.group(1)
                val fileType = matcher.group(2)
                String.format("%s_%s.%s", actualFilename, url.hashCode(), fileType)
            } else {
                url.hashCode().toString() + filename
            }
        }

        fun createBundle(canvasContext: CanvasContext?, mime: String?, url: String?, filename: String?): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.MIME, mime)
            openMediaBundle.putString(Const.URL, url)
            openMediaBundle.putString(Const.FILE_URL, filename)
            openMediaBundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            return openMediaBundle
        }

        fun createLocalBundle(canvasContext: CanvasContext?, mime: String?, path: String?, filename: String?, useOutsideApps: Boolean): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.MIME, mime)
            openMediaBundle.putString(Const.PATH, path)
            openMediaBundle.putString(Const.FILE_URL, filename)
            openMediaBundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            openMediaBundle.putBoolean(Const.OPEN_OUTSIDE, useOutsideApps)
            return openMediaBundle
        }

        fun createBundle(canvasContext: CanvasContext?, mime: String?, url: String?, filename: String?, useOutsideApps: Boolean): Bundle {
            val openMediaBundle = createBundle(canvasContext, mime, url, filename)
            openMediaBundle.putBoolean(Const.OPEN_OUTSIDE, useOutsideApps)
            return openMediaBundle
        }

        fun createBundle(url: String?, filename: String?, canvasContext: CanvasContext? = null): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.URL, url)
            openMediaBundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            openMediaBundle.putString(Const.FILE_URL, filename)
            return openMediaBundle
        }

        fun createBundle(url: String?): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.URL, url)
            return openMediaBundle
        }

        fun createBundle(mime: String?, url: String?, filename: String?): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.MIME, mime)
            openMediaBundle.putString(Const.URL, url)
            openMediaBundle.putString(Const.FILE_URL, filename)
            return openMediaBundle
        }

        fun createBundle(mime: String?, url: String?, filename: String?, extras: Bundle?): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.MIME, mime)
            openMediaBundle.putString(Const.URL, url)
            openMediaBundle.putString(Const.FILE_URL, filename)
            openMediaBundle.putBundle(Const.EXTRAS, extras)
            return openMediaBundle
        }

        fun createBundle(canvasContext: CanvasContext?, isSubmission: Boolean, mime: String?, url: String?, filename: String?): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.MIME, mime)
            openMediaBundle.putString(Const.URL, url)
            openMediaBundle.putString(Const.FILE_URL, filename)
            openMediaBundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            openMediaBundle.putBoolean(Const.IS_SUBMISSION, isSubmission)
            return openMediaBundle
        }
    }
}
