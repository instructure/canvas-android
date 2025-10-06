/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

package com.instructure.student.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger
import com.instructure.interactions.FullScreenInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader
import com.instructure.pandautils.models.PushNotification
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LoaderUtils
import com.instructure.pandautils.utils.RouteUtils
import com.instructure.pandautils.utils.toast
import com.instructure.student.R
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.FileUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//Intended to handle all routing to fragments from links both internal and external
abstract class BaseRouterActivity : CallbackActivity(), FullScreenInteractions {

    private var routeCanvasContextJob: Job? = null
    private var routeModuleProgressionJob: Job? = null
    private var routeLTIJob: Job? = null

    protected abstract fun existingFragmentCount(): Int
    protected abstract fun loadLandingPage(clearBackStack: Boolean = false)

    protected abstract fun showLoadingIndicator()
    protected abstract fun hideLoadingIndicator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("BaseRouterActivity: onCreate()")

        RouteMatcher.resetRoutes()

        if (savedInstanceState == null) {
            parse(intent)
        }

        LoaderUtils.restoreLoaderFromBundle(
            LoaderManager.getInstance(this),
            savedInstanceState,
            loaderCallbacks,
            R.id.openMediaLoaderID,
            Const.OPEN_MEDIA_LOADER_BUNDLE
        )

        if (savedInstanceState?.getBundle(Const.OPEN_MEDIA_LOADER_BUNDLE) != null) {
            showLoadingIndicator()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LoaderUtils.saveLoaderBundle(outState, openMediaBundle, Const.OPEN_MEDIA_LOADER_BUNDLE)
        hideLoadingIndicator()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Logger.d("BaseRouterActivity: onNewIntent()")
        parse(intent)
    }

    //region OpenMediaAsyncTaskLoader

    private var openMediaBundle: Bundle? = null
    private var openMediaCallbacks: LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>? =
        null

    // Show pdf with PSPDFkit - set to null, otherwise the progressDialog will appear again
    private val loaderCallbacks: LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>
        get() {
            if (openMediaCallbacks == null) {
                openMediaCallbacks =
                    object : LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> {
                        override fun onCreateLoader(
                            id: Int,
                            args: Bundle?
                        ): Loader<OpenMediaAsyncTaskLoader.LoadedMedia> {
                            showLoadingIndicator()
                            return OpenMediaAsyncTaskLoader(context, args)
                        }

                        override fun onLoadFinished(
                            loader: Loader<OpenMediaAsyncTaskLoader.LoadedMedia>,
                            loadedMedia: OpenMediaAsyncTaskLoader.LoadedMedia
                        ) {
                            hideLoadingIndicator()

                            try {
                                if (loadedMedia.isError) {
                                    toast(loadedMedia.errorMessage, Toast.LENGTH_LONG)
                                } else if (loadedMedia.isHtmlFile) {
                                    InternalWebviewFragment.loadInternalWebView(
                                        this@BaseRouterActivity,
                                        InternalWebviewFragment.makeRoute(loadedMedia.bundle!!)
                                    )
                                } else if (loadedMedia.intent != null) {
                                    if (loadedMedia.intent!!.type!!.contains("pdf")) {
                                        val uri = loadedMedia.intent!!.data
                                        FileUtils.showPdfDocument(uri!!, loadedMedia, context)
                                    } else {
                                        context.startActivity(loadedMedia.intent)
                                    }
                                }
                            } catch (e: ActivityNotFoundException) {
                                toast(R.string.noApps, Toast.LENGTH_LONG)
                            }

                            openMediaBundle = null
                        }

                        override fun onLoaderReset(loader: Loader<OpenMediaAsyncTaskLoader.LoadedMedia>) {

                        }
                    }
            }
            return openMediaCallbacks!!
        }

    // endregion

    /**
     * The intent will have information about the url to open (usually from clicking on a link in an email)
     * @param intent
     */
    private fun parse(intent: Intent?) {
        if (intent == null || intent.extras == null) return

        val extras = intent.extras!!
        Logger.logBundle(extras)

        if (extras.containsKey(Route.ROUTE)) {
            handleRoute(extras.getParcelable(Route.ROUTE)!!)
            return
        }

        if (extras.containsKey(Const.MESSAGE) && extras.containsKey(Const.MESSAGE_TYPE)) {
            showMessage(extras.getString(Const.MESSAGE))
        }

        when {
            extras.containsKey(Const.PARSE) || extras.containsKey(Const.BOOKMARK) -> {
                val url = extras.getString(Const.URL).orEmpty()
                RouteMatcher.routeUrl(this, url)
            }

            extras.containsKey(PushExternalReceiver.NEW_PUSH_NOTIFICATION) -> {
                val url = extras.getString(PushNotification.HTML_URL).orEmpty()
                RouteMatcher.routeUrl(this, url)
            }
        }
    }

    protected fun handleSpecificFile(courseId: Long, fileID: String) {
        val canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, courseId, "")
        Logger.d("BaseRouterActivity: handleSpecificFile()")
        //If the file no longer exists (404), we want to show a different crouton than the default.
        val fileFolderCanvasCallback = object : StatusCallback<FileFolder>() {
            override fun onResponse(
                response: retrofit2.Response<FileFolder>,
                linkHeaders: LinkHeaders,
                type: ApiType
            ) {
                response.body()?.let {
                    if (it.isLocked || it.isLockedForUser) {
                        Toast.makeText(
                            context,
                            String.format(
                                context.getString(R.string.fileLocked),
                                if (it.displayName == null) getString(R.string.file) else it.displayName
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        openMedia(
                            canvasContext,
                            it.contentType.orEmpty(),
                            it.url.orEmpty(),
                            it.displayName.orEmpty(),
                            fileID
                        )
                    }
                }
            }
        }

        FileFolderManager.getFileFolderFromURL("files/$fileID", true, fileFolderCanvasCallback)
    }

    private fun handleSpecificFile(fileID: String) {
        Logger.d("BaseRouterActivity: handleSpecificFile() no context")
        //If the file no longer exists (404), we want to show a different crouton than the default.
        val fileFolderCanvasCallback = object : StatusCallback<FileFolder>() {
            override fun onResponse(
                response: retrofit2.Response<FileFolder>,
                linkHeaders: LinkHeaders,
                type: ApiType
            ) {
                response.body()?.let {
                    if (it.isLocked || it.isLockedForUser) {
                        Toast.makeText(
                            context,
                            String.format(
                                context.getString(R.string.fileLocked),
                                if (it.displayName == null) getString(R.string.file) else it.displayName
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        openMedia(
                            CanvasContext.emptyUserContext(),
                            it.contentType.orEmpty(),
                            it.url.orEmpty(),
                            it.displayName.orEmpty(),
                            fileID
                        )
                    }
                }
            }
        }

        FileFolderManager.getFileFolderFromURL("files/$fileID", true, fileFolderCanvasCallback)
    }

    fun openMedia(canvasContext: CanvasContext?, url: String, fileID: String?) {
        showLoadingIndicator()
        lifecycleScope.launch {
            if (shouldOpenInternally(url)) {
                startActivity(
                    VideoViewActivity.createIntent(
                        this@BaseRouterActivity,
                        url
                    )
                )
            } else {
                openMediaBundle =
                    OpenMediaAsyncTaskLoader.createBundle(url, null, fileID, canvasContext)
                LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(
                    LoaderManager.getInstance(this@BaseRouterActivity),
                    openMediaBundle,
                    loaderCallbacks,
                    R.id.openMediaLoaderID
                )
            }
        }
    }

    fun openMedia(
        canvasContext: CanvasContext?,
        mime: String,
        url: String,
        filename: String,
        fileID: String?
    ) {
        showLoadingIndicator()
        lifecycleScope.launch {
            if (shouldOpenInternally(url)) {
                startActivity(
                    VideoViewActivity.createIntent(
                        this@BaseRouterActivity,
                        url
                    )
                )
            } else {
                openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(
                    canvasContext,
                    mime,
                    url,
                    filename,
                    fileID
                )
                LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(
                    LoaderManager.getInstance(this@BaseRouterActivity),
                    openMediaBundle,
                    loaderCallbacks,
                    R.id.openMediaLoaderID
                )
            }
        }
    }

    private suspend fun shouldOpenInternally(url: String): Boolean {
        val mediaUrl = RouteUtils.getMediaUri(Uri.parse(url)).toString()
        return (mediaUrl.endsWith(".mpd") || mediaUrl.endsWith(".m3u8") || mediaUrl.endsWith(".mp4"))
    }

    override fun onDestroy() {
        super.onDestroy()
        routeCanvasContextJob?.cancel()
        routeModuleProgressionJob?.cancel()
        routeLTIJob?.cancel()
    }

    companion object {
        // region Used for param handling
        var SUBMISSIONS_ROUTE = "submissions"
        var RUBRIC_ROUTE = "rubric"

        fun parseCourseId(courseId: String): Long? {
            return try {
                courseId.toLong()
            } catch (e: NumberFormatException) {
                Logger.e("Course ID  ($courseId) passed to Router is invalid: ${e.message}")
                null
            }
        }
    }
}
