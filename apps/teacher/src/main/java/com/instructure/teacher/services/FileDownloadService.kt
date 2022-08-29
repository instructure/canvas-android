/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

package com.instructure.teacher.services

import android.app.Activity
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.HttpHelper
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.Utils
import com.instructure.teacher.R
import com.instructure.teacher.activities.RouteValidatorActivity
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class FileDownloadService @JvmOverloads constructor(name: String = FileDownloadService::class.java.simpleName) : IntentService(name) {

    private var isCanceled = false
    private var url = ""
    private var fileName = ""
    private var notificationBuilder: NotificationCompat.Builder? = null
    private lateinit var file: File
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle 'cancel' action in onStartCommand instead of onHandleIntent, because threading.
        if (ACTION_CANCEL_UPLOAD == intent!!.action) isCanceled = true
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {

        // Skip if canceled
        if (isCanceled) return

        val bundle = intent?.extras ?: return
        val route: Route? = bundle.getParcelable(Route.ROUTE) // Route could be null if coming from a Studio video download
        url = bundle.getString(Const.URL) ?: return
        fileName = bundle.getString(Const.FILENAME) ?: getFilename(url)

        if (route == null) {
            // Studio download - we don't associate a route with an Studio video download
            file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            showNotification()
            downloadFile(url, file, fileName)
        } else {
            showNotification()
            startDownload(route)
        }
    }

    //region Download functionality

    /**
     * We want to download this with the same filename and in the same location as we do with the normal routing so the user
     * can tap the notification and view the file in our app
     */
    private fun startDownload(route: Route) {
        try {
            // Handle download cancellation
            if (isCanceled) {
                stopForeground(true)
                notificationManager.cancel(NOTIFICATION_ID)
                stopSelf()
                return
            }
            if (route.queryParamsHash.containsKey(RouterParams.VERIFIER) && route.queryParamsHash.containsKey(RouterParams.DOWNLOAD_FRD)) {
                if (route.uri != null) {
                    downloadAttachmentsFile(this, route.uri.toString(), getFilename(route.uri.toString()))
                }
            } else {
                if (route.queryParamsHash.containsKey(RouterParams.PREVIEW)) {
                    // This is a link for a file preview, so we need to get the file id from the preview query param
                    getFile(this, route.queryParamsHash[RouterParams.PREVIEW] ?: "")
                } else {
                    getFile(this, route.paramsHash[RouterParams.FILE_ID] ?: "")
                }
            }

        } catch (exception: Exception) {
            updateNotificationError(getString(R.string.errorDownloadingFile))
        }

        stopSelf()
    }


    //endregion

    private fun getFile(context: Context, fileId: String) {
        val file = FileFolderManager.getFileFolderFromURLSynchronous("files/$fileId")
        val fileUrl = file?.url
        if (fileUrl != null) {
            downloadAttachmentsFile(context, fileUrl, getFilename(fileUrl))
        } else {
            Toast.makeText(context, R.string.errorDownloadingFile, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFilename(url: String): String {
        val hc = URL(url).openConnection() as HttpURLConnection
        val connection = HttpHelper.redirectURL(hc)
        var filename: String
        // parse filename from Content-Disposition header which is a response field that is normally used to set the file name
        val headerField = connection.getHeaderField("Content-Disposition")
        if (headerField != null) {
            filename = OpenMediaAsyncTaskLoader.parseFilename(headerField).orEmpty()
            filename = OpenMediaAsyncTaskLoader.makeFilenameUnique(filename, url)
        } else {
            filename = "" + url.hashCode()
        }

        return filename
    }

    @Throws(Exception::class)
    private fun downloadAttachmentsFile(context: Context, url: String, filename: String): File {
        // They have to download the content first... gross
        // Download it if the file doesn't exist in the external cache
        Log.d("FileDownloadService", "downloadFile URL: $url")
        val attachmentFile = File(Utils.getAttachmentsDirectory(context), filename)
        Log.d("FileDownloadService", "File $attachmentFile")
        if (!attachmentFile.exists()) {
            //Download the content from the url
            if (writeAttachmentsDirectoryFromURL(url, attachmentFile, filename)) {
                Log.d("FileDownloadService", "file not cached")
                updateNotificationComplete()
                return attachmentFile
            }
        }

        updateNotificationComplete()

        return attachmentFile
    }

    /**
     * Downloads a file to the users Download directory
     * Currently used only for Studio
     */
    @Throws(Exception::class)
    private fun downloadFile(url: String, file: File, fileName: String): File {
        // They have to download the content first... gross
        // Download it if the file doesn't exist in the external cache
        Log.d("FileDownloadService", "downloadFile URL: $url")
        Log.d("FileDownloadService", "File $file")
        if (!file.exists()) {
            // Download the content from the url
            if (writeAttachmentsDirectoryFromURL(url, file, fileName)) {
                Log.d("FileDownloadService", "file not cached")
                updateNotificationComplete(routeInternally = false)
                return file
            }
        }

        updateNotificationComplete(routeInternally = false)

        return file
    }


    @Suppress("DEPRECATION")
    @Throws(Exception::class)
    private fun writeAttachmentsDirectoryFromURL(url2: String, toWriteTo: File, fileName: String = ""): Boolean {
        // Create the new connection
        val url = URL(url2)
        val urlConnection = url.openConnection() as HttpURLConnection
        // Set up some things on the connection
        urlConnection.requestMethod = "GET"

        // And connect!
        urlConnection.connect()
        val connection = HttpHelper.redirectURL(urlConnection)

        // This will be used to write the downloaded uri into the file we created
        val name = if (fileName.isNotBlank()) fileName else toWriteTo.name
        toWriteTo.parentFile.mkdirs()
        val fileOutput: FileOutputStream?
        // If there is an external cache, we want to write to that
        fileOutput = if (applicationContext.externalCacheDir != null) {
            FileOutputStream(toWriteTo)
        } else { // Otherwise, use internal cache.
            applicationContext.openFileOutput(name,
                    Activity.MODE_WORLD_READABLE or Activity.MODE_WORLD_WRITEABLE)
        }

        fileOutput?.let {
            // This will be used in reading the uri from the internet
            val inputStream = connection.inputStream

            // Create a buffer...
            val buffer = ByteArray(1024)
            var bufferLength: Int // Used to store a temporary size of the buffer

            // Now, read through the input buffer and write the contents to the file
            while (true) {
                bufferLength = inputStream.read(buffer)
                if (bufferLength == -1) break
                it.write(buffer, 0, bufferLength)
            }

            it.flush()
            it.close()
            inputStream.close() // Close after fileOuput.flush() or else the fileOutput won't actually close leading to open failed: EBUSY (Device or resource busy)
        }
        return true
    }

    //region Notifications

    private fun createNotificationChannel(channelId: String) {
        // Prevents recreation of notification channel if it exists.
        if (notificationManager.notificationChannels?.any { it.id == channelId } == true) return

        val name = ContextKeeper.appContext.getString(com.instructure.pandautils.R.string.notificationChannelNameFileUploadsName)
        val description = ContextKeeper.appContext.getString(com.instructure.pandautils.R.string.notificationChannelNameFileUploadsDescription)

        // Create the channel and add the group
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        channel.description = description
        channel.enableLights(false)
        channel.enableVibration(false)

        // Create the channel
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification() {
        createNotificationChannel(CHANNEL_ID)

        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.canvas_logo_white)
                .setContentTitle(getString(R.string.downloadingFile))
                .setContentText(fileName)
                .setOnlyAlertOnce(true)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(0, 0, true)

        startForeground(NOTIFICATION_ID, notificationBuilder?.build())
    }

    private fun updateNotificationError(message: String) {
        notificationBuilder?.setContentText(message)?.setProgress(0, 0, false)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder?.build())
    }

    private fun updateNotificationComplete(routeInternally: Boolean = true) {
        notificationBuilder?.setProgress(0, 0, false)
                ?.setContentTitle(getString(R.string.fileDownloadedSuccessfully))
                ?.setContentText(fileName)
                ?.setSmallIcon(android.R.drawable.stat_sys_download_done)
                ?.setAutoCancel(true)

        if (!routeInternally) {
            // This is here specifically for Ark video downloads
            val contentIntent = Intent(Intent.ACTION_VIEW)
            contentIntent.apply {
                val context = this@FileDownloadService
                val fileUri = FileProvider.getUriForFile(context, context.packageName + Const.FILE_PROVIDER_AUTHORITY, file)
                setDataAndType(fileUri, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val pendingIntent = PendingIntent.getActivity(
                this@FileDownloadService,
                0,
                contentIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder?.setContentIntent(pendingIntent)
        } else {
            // All other downloads
            val intent = RouteValidatorActivity.createIntent(this, Uri.parse(url))
            val bundle = Bundle()
            bundle.putBoolean(Const.FILE_DOWNLOADED, true)
            intent.putExtras(bundle)

            val contentIntent = PendingIntent.getActivity(
                this@FileDownloadService,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder?.setContentIntent(contentIntent)

        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder?.build())
    }


    //endregion

    override fun onDestroy() {
        if (isCanceled) {
            notificationManager.cancel(NOTIFICATION_ID)
        } else {
            if (notificationBuilder != null) {
                notificationBuilder?.setOngoing(false)
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder?.build())
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 2
        const val CHANNEL_ID = "fileDownloadChannel"

        const val ACTION_CANCEL_UPLOAD = "ACTION_CANCEL_UPLOAD"

        fun scheduleDownloadJob(context: Context, fileUrl: String, fileName: String) {
            val intent = Intent(context, FileDownloadService::class.java)
            val bundle = Bundle().apply {
                putString(Const.URL, fileUrl)
                putString(Const.FILENAME, fileName)
            }

            intent.putExtras(bundle)
            context.startService(intent)
        }

        //endregion
    }
}
