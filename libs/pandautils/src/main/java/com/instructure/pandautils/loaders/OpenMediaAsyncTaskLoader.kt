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
import java.net.MalformedURLException
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

    data class DownloadResult(
        val file: File,
        val mimeType: String?,
        val filename: String?
    )

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
                filename = makeFilenameUnique(filename, url, fileId)
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
                if (url.isNotBlank()) {
                    // For remote URLs, we'll get the URI and metadata during or after download
                    val uri = Uri.parse(url)
                    intent.setDataAndType(uri, mimeType)
                    loadedMedia.intent = intent
                    if (extras != null) loadedMedia.bundle = extras
                    attemptDownloadFile(context, intent, loadedMedia, url, filename)
                } else {
                    val file = File(path)
                    val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY, file)
                    intent.setDataAndType(uri, mimeType)
                    loadedMedia.intent = intent
                    if (extras != null) loadedMedia.bundle = extras
                    Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "Intent can be handled: " + isIntentHandledByActivity(intent))
                    attemptDownloadFile(context, intent, loadedMedia, url, filename)
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

    private fun downloadFile(context: Context, url: String, filenameParam: String?): File {
        // They have to download the content first... gross.
        // Download it if the file doesn't exist in the external cache
        Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "downloadFile URL: $url")

        // If we don't have a filename yet, we need to download to get it from headers
        // We'll use a temporary approach first
        val needsFilenameFromHeaders = !filenameParam.isValid()

        if (needsFilenameFromHeaders) {
            // Download and get filename from headers
            val result = downloadWithHeaders(context, url, null)
            // Update instance variables with values from headers
            if (!mimeType.isValid() && result.mimeType != null) {
                mimeType = result.mimeType
            }
            if (!filename.isValid() && result.filename != null) {
                filename = result.filename
            }
            return result.file
        } else {
            // We have a filename, check if file exists
            val attachmentFile = if (filenameParam?.endsWith(".pdf").orDefault()) {
                File(File(context.filesDir, "pdfs-${ApiPrefs.user?.id}"), filenameParam)
            } else {
                File(getAttachmentsDirectory(context), filenameParam)
            }
            Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "File: $attachmentFile")

            if (!attachmentFile.exists()) {
                // Download the file and get headers in the same request
                val result = downloadWithHeaders(context, url, attachmentFile)
                // Update mimeType if we didn't have it
                if (!mimeType.isValid() && result.mimeType != null) {
                    mimeType = result.mimeType
                }
                return result.file
            } else {
                // File exists, ensure we have mimeType
                if (!mimeType.isValid()) {
                    mimeType = guessMimeTypeFromFilename(attachmentFile.name)
                }
                return attachmentFile
            }
        }
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
    private fun downloadWithHeaders(context: Context, url: String, targetFile: File?): DownloadResult {
        val client = okHttpClient.newBuilder().cache(null).build()
        val params = RestParams(null, null, "/api/v1/", false, true, false, false, null)
        val requestBuilder = Request.Builder().url(url).tag(params)
        val cookie: String? = CookieManager.getInstance().getCookie(url)
        if (cookie.isValid()) requestBuilder.addHeader("Cookie", cookie)
        val request = requestBuilder.build()
        val response = client.newCall(request).execute()

        return response.use { resp ->
            if (!resp.isSuccessful) {
                throw IOException("Unable to download. Error code ${resp.code}")
            }

            // Extract headers from response (headers are available before reading the body)
            var responseMimeType = resp.header("Content-Type")
            var responseFilename: String? = null

            // Parse filename from Content-Disposition header
            val contentDisposition = resp.header("Content-Disposition")
            if (contentDisposition != null) {
                responseFilename = parseFilename(contentDisposition)
            }

            // If we still don't have a filename, generate one from URL
            if (responseFilename == null) {
                responseFilename = url.hashCode().toString()
            }

            // Make filename unique if we have the necessary info
            responseFilename = makeFilenameUnique(responseFilename, url, fileId)

            // Determine the actual file to write to
            val toWriteTo = targetFile ?: run {
                if (responseFilename.endsWith(".pdf", ignoreCase = true)) {
                    File(File(context.filesDir, "pdfs-${ApiPrefs.user?.id}"), responseFilename)
                } else {
                    File(getAttachmentsDirectory(context), responseFilename)
                }
            }

            // Check if file already exists in cache
            if (toWriteTo.exists() && toWriteTo.length() > 0) {
                Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "File found in cache, skipping download: $toWriteTo")

                // If mimeType is generic, try to guess from filename
                if (responseMimeType == "binary/octet-stream" || responseMimeType == "*/*") {
                    val guessedMimeType = guessMimeTypeFromFilename(responseFilename)
                    if (guessedMimeType != null) {
                        responseMimeType = guessedMimeType
                    }
                }

                // Response will be closed automatically by use() when we return
                return@use DownloadResult(
                    file = toWriteTo,
                    mimeType = responseMimeType,
                    filename = responseFilename
                )
            }

            // File not in cache, proceed with download
            Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "Downloading file with mimeType: $responseMimeType, filename: $responseFilename")

            toWriteTo.parentFile?.mkdirs()

            resp.body!!.use { body ->
                val sink = toWriteTo.sink().buffer()
                sink.use { s ->
                    body.source().use { source ->
                        s.writeAll(source)
                    }
                }
            }

            Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "Download completed: $toWriteTo")

            // If mimeType is generic, try to guess from filename
            if (responseMimeType == "binary/octet-stream" || responseMimeType == "*/*") {
                val guessedMimeType = guessMimeTypeFromFilename(responseFilename)
                if (guessedMimeType != null) {
                    responseMimeType = guessedMimeType
                }
            }

            DownloadResult(
                file = toWriteTo,
                mimeType = responseMimeType,
                filename = responseFilename
            )
        }
    }

    private fun guessMimeTypeFromFilename(filename: String): String? {
        val guessedMimeType = URLConnection.guessContentTypeFromName(filename)
        Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "Guessed mimeType: $guessedMimeType for filename: $filename")
        return if (guessedMimeType.isValid()) guessedMimeType else null
    }

    companion object {
        fun parseFilename(headerField: String?): String? {
            var filename = headerField
            val matcher = Pattern.compile("filename=\"(.*)\"").matcher(headerField ?: "")
            if (matcher.find()) {
                filename = matcher.group(1)
            }
            return filename
        }

        fun makeFilenameUnique(filename: String?, url: String, fileId: String? = null): String {
            val matcher = Pattern.compile("(.*)\\.(.*)").matcher(filename ?: "")
            return if (matcher.find()) {
                val actualFilename = matcher.group(1)
                val fileType = matcher.group(2)
                String.format("%s_%s.%s", actualFilename, fileId ?: url.hashCode(), fileType)
            } else {
                url.hashCode().toString() + filename
            }
        }

        fun createBundle(canvasContext: CanvasContext?, mime: String?, url: String?, filename: String?, fileId: String?): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.MIME, mime)
            openMediaBundle.putString(Const.URL, url)
            openMediaBundle.putString(Const.FILE_URL, filename)
            openMediaBundle.putString(Const.FILE_ID, fileId)
            openMediaBundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            return openMediaBundle
        }

        fun createLocalBundle(canvasContext: CanvasContext?, mime: String?, path: String?, filename: String?, fileId: String?, useOutsideApps: Boolean): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.MIME, mime)
            openMediaBundle.putString(Const.PATH, path)
            openMediaBundle.putString(Const.FILE_URL, filename)
            openMediaBundle.putString(Const.FILE_ID, fileId)
            openMediaBundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            openMediaBundle.putBoolean(Const.OPEN_OUTSIDE, useOutsideApps)
            return openMediaBundle
        }

        fun createBundle(canvasContext: CanvasContext?, mime: String?, url: String?, filename: String?, fileId: String?, useOutsideApps: Boolean): Bundle {
            val openMediaBundle = createBundle(canvasContext, mime, url, filename, fileId)
            openMediaBundle.putBoolean(Const.OPEN_OUTSIDE, useOutsideApps)
            return openMediaBundle
        }

        fun createBundle(url: String?, filename: String?, fileId: String?, canvasContext: CanvasContext? = null): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.URL, url)
            openMediaBundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            openMediaBundle.putString(Const.FILE_URL, filename)
            openMediaBundle.putString(Const.FILE_ID, fileId)
            return openMediaBundle
        }

        fun createBundle(url: String?): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.URL, url)
            return openMediaBundle
        }

        fun createBundle(mime: String?, url: String?, filename: String?, fileId: String?): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.MIME, mime)
            openMediaBundle.putString(Const.URL, url)
            openMediaBundle.putString(Const.FILE_URL, filename)
            openMediaBundle.putString(Const.FILE_ID, fileId)
            return openMediaBundle
        }

        fun createBundle(mime: String?, url: String?, filename: String?, fileId: String?, extras: Bundle?): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.MIME, mime)
            openMediaBundle.putString(Const.URL, url)
            openMediaBundle.putString(Const.FILE_URL, filename)
            openMediaBundle.putString(Const.FILE_ID, fileId)
            openMediaBundle.putBundle(Const.EXTRAS, extras)
            return openMediaBundle
        }

        fun createBundle(canvasContext: CanvasContext?, isSubmission: Boolean, mime: String?, url: String?, filename: String?, fileId: String?): Bundle {
            val openMediaBundle = Bundle()
            openMediaBundle.putString(Const.MIME, mime)
            openMediaBundle.putString(Const.URL, url)
            openMediaBundle.putString(Const.FILE_URL, filename)
            openMediaBundle.putString(Const.FILE_ID, fileId)
            openMediaBundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
            openMediaBundle.putBoolean(Const.IS_SUBMISSION, isSubmission)
            return openMediaBundle
        }
    }
}