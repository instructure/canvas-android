/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.fragment

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import com.instructure.pandautils.blueprint.BaseCanvasDialogFragment
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.NetworkUtils
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.pandarecycler.BaseRecyclerAdapter
import com.instructure.pandarecycler.PandaRecyclerView
import com.instructure.pandarecycler.interfaces.EmptyViewInterface
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LoaderUtils
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.hasPermissions
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.views.EmptyView
import com.instructure.student.R
import com.instructure.student.activity.VideoViewActivity
import com.instructure.student.util.FileUtils
import com.instructure.student.util.LoggingUtility
import com.instructure.student.util.onMainThread
import java.io.File
import java.io.FileOutputStream

abstract class ParentFragment : BaseCanvasDialogFragment(), FragmentInteractions, NavigationCallbacks {

    private var openMediaBundle: Bundle? = null
    private var openMediaCallbacks: LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>? = null

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private var loadedMedia: OpenMediaAsyncTaskLoader.LoadedMedia? = null

    override val navigation: Navigation?
        get() = if (activity is Navigation) {
            activity as Navigation
        } else null

    // region OpenMediaAsyncTaskLoader

    private val loaderCallbacks: LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>
        get() {
            if (openMediaCallbacks == null) {
                openMediaCallbacks = object : LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> {
                    override fun onCreateLoader(id: Int, args: Bundle?): Loader<OpenMediaAsyncTaskLoader.LoadedMedia> {
                        onMediaLoadingStarted()
                        return OpenMediaAsyncTaskLoader(requireContext(), args)
                    }

                    override fun onLoadFinished(loader: Loader<OpenMediaAsyncTaskLoader.LoadedMedia>, loadedMedia: OpenMediaAsyncTaskLoader.LoadedMedia) {
                        try {
                            if (loadedMedia.isError) {
                                if (loadedMedia.errorType == OpenMediaAsyncTaskLoader.ErrorType.NO_APPS && isAdded && view != null) {
                                    this@ParentFragment.loadedMedia = loadedMedia
                                    Snackbar.make(view!!, getString(R.string.noAppsShort), Snackbar.LENGTH_LONG)
                                            .setAction(getString(R.string.download), snackbarClickListener)
                                            .setActionTextColor(Color.WHITE)
                                            .show()
                                } else {
                                    if (activity != null) {
                                        Toast.makeText(requireActivity(), requireActivity().resources.getString(loadedMedia.errorMessage), Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else if (loadedMedia.isHtmlFile) {
                                InternalWebviewFragment.loadInternalWebView(activity, InternalWebviewFragment.makeRoute(loadedMedia.bundle!!))
                            } else if (loadedMedia.intent != null && context != null) {
                                // Show pdf with PSPDFkit
                                if (loadedMedia.intent?.type?.contains("pdf") == true && !loadedMedia.isUseOutsideApps) {
                                    val uri = loadedMedia.intent!!.data
                                    FileUtils.showPdfDocument(uri!!, loadedMedia, requireContext())
                                } else if (loadedMedia.intent?.type == "video/mp4") {
                                    activity?.startActivity(VideoViewActivity.createIntent(requireContext(), loadedMedia.intent!!.dataString))
                                } else {
                                    activity?.startActivity(loadedMedia.intent)
                                }
                            }
                        } catch (e: ActivityNotFoundException) {
                            if (activity != null) {
                                Toast.makeText(requireActivity(), R.string.noApps, Toast.LENGTH_LONG).show()
                            }
                        }

                        openMediaBundle = null // Set to null, otherwise the progressDialog will appear again
                        if (isAdded) onMediaLoadingComplete()
                    }

                    override fun onLoaderReset(loader: Loader<OpenMediaAsyncTaskLoader.LoadedMedia>) {}
                }
            }
            return openMediaCallbacks!!
        }

    open fun onMediaLoadingStarted(){}
    open fun onMediaLoadingComplete(){}

    var snackbarClickListener: View.OnClickListener = View.OnClickListener {
        try {
            downloadFileToDownloadDir(loadedMedia?.intent?.data?.path!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireActivity(), R.string.errorOccurred, Toast.LENGTH_LONG).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // First saving my state, so the bundle wont be empty
        LoaderUtils.saveLoaderBundle(outState, openMediaBundle, Const.OPEN_MEDIA_LOADER_BUNDLE)

        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE")
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LoggingUtility.log(Log.DEBUG, Logger.getFragmentName(this) + " --> On Create")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        LoggingUtility.log(Log.DEBUG, Logger.getFragmentName(this) + " --> On Create View")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        LoggingUtility.log(Log.DEBUG, Logger.getFragmentName(this) + " --> On Activity Created")

        LoaderUtils.restoreLoaderFromBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(requireActivity().supportLoaderManager, savedInstanceState, loaderCallbacks, R.id.openMediaLoaderID, Const.OPEN_MEDIA_LOADER_BUNDLE)
    }

    override fun onAttach(context: Context) {
        super.onAttach(requireContext())
        setHasOptionsMenu(true)
    }

    override fun onDetach() {
        // This could go wrong, but we don't want to crash the app since we are just dismissing the soft keyboard
        try {
            val view = requireActivity().currentFocus
            if (view != null) {
                val inputManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        } catch (e: Exception) {
            LoggingUtility.log(Log.DEBUG, "An exception was thrown while trying to dismiss the keyboard: " + e.message)
        }
        super.onDetach()
    }

    override fun onStart() {
        super.onStart()
        LoggingUtility.log(Log.DEBUG, Logger.getFragmentName(this) + " --> On Start")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        return dialog
    }

    override fun onDestroyView() {
        if (retainInstance)
            dialog?.setDismissMessage(null)
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        LoggingUtility.log(Log.DEBUG, Logger.getFragmentName(this) + " --> On Resume")
    }

    override fun onPause() {
        super.onPause()
        LoggingUtility.log(Log.DEBUG, Logger.getFragmentName(this) + " --> On Pause.")
    }

    open fun handleBackPressed(): Boolean = false

    override fun onHandleBackPressed(): Boolean = handleBackPressed()

    //region Toolbar & Menus

    /**
     * General setup method for toolbar menu items
     * All menu item selections are returned to the onToolbarMenuItemClick() function
     * @param toolbar a toolbar
     */
    fun setupToolbarMenu(toolbar: Toolbar) {
        addBookmarkMenuIfAllowed(toolbar)
        addOnMenuItemClickListener(toolbar)
    }

    /**
     * General setup method for toolbar menu items
     * All menu item selections are returned to the onToolbarMenuItemClick() function
     * @param toolbar a toolbar
     * @param menu xml menu resource id, R.menu.matthew_rice_is_great
     */
    fun setupToolbarMenu(toolbar: Toolbar, @MenuRes menu: Int) {
        toolbar.menu.clear()
        addBookmarkMenuIfAllowed(toolbar)
        toolbar.inflateMenu(menu)
        addOnMenuItemClickListener(toolbar)
    }

    private fun addBookmarkMenuIfAllowed(toolbar: Toolbar) {
        val navigation = activity as? Navigation
        val bookmarkFeatureAllowed = navigation?.canBookmark() ?: false
        if (bookmarkFeatureAllowed && this is Bookmarkable && this.bookmark.canBookmark && toolbar.menu.findItem(R.id.bookmark) == null) {
            toolbar.inflateMenu(R.menu.bookmark_menu)
        }
    }

    private fun addOnMenuItemClickListener(toolbar: Toolbar) {
        toolbar.setOnMenuItemClickListener { item -> onOptionsItemSelected(item) }
    }

    /**
     * Override to handle toolbar menu item clicks
     * Super() should be called most if not all of the time.
     * @param item a menu item
     * @return true if the menu item click was handled
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.bookmark) {
            if (APIHelper.hasNetworkConnection()) {
                navigation?.addBookmark()
            } else {
                Toast.makeText(requireContext(), requireContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return false
    }

    // Fragment-ception fix:
    // Some fragments (currently our AssigmentFragment) have children fragments.
    // In the module progression view pager these child fragments don't get
    // destroyed when the root fragment gets destroyed. Override this function
    // in the appropriate activity to remove child fragments.  For example, in
    // the module progression class we call this function when onDestroyItem
    // is called and it is implemented in the AssignmentFragment class.
    fun removeChildFragments() {}

    override fun startActivity(intent: Intent) {
        if (context == null) {
            return
        }
        super.startActivity(intent)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        if (context == null) {
            return
        }
        super.startActivityForResult(intent, requestCode)
    }
    // region RecyclerView Methods

    // The paramName is used to specify which param should be selected when the list loads for the first time
    protected open fun getSelectedParamName(): String = ""

    fun setRefreshing(isRefreshing: Boolean) {
        swipeRefreshLayout?.isRefreshing = isRefreshing
    }

    fun setRefreshingEnabled(isEnabled: Boolean) {
        swipeRefreshLayout?.isEnabled = isEnabled
    }

    // endregion

    fun configureRecyclerView(
            rootView: View,
            context: Context,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int): PandaRecyclerView {
        return configureRecyclerView(rootView, context, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, resources.getString(R.string.noItemsToDisplayShort))
    }

    fun configureRecyclerView(
            rootView: View,
            context: Context,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int,
            emptyViewStringResId: Int): PandaRecyclerView {
        return configureRecyclerView(rootView, context, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, resources.getString(emptyViewStringResId))
    }

    private fun configureRecyclerView(
            rootView: View,
            context: Context,
            baseRecyclerAdapter: BaseRecyclerAdapter<*>,
            swipeRefreshLayoutResId: Int,
            emptyViewResId: Int,
            recyclerViewResId: Int,
            emptyViewString: String
    ): PandaRecyclerView {
        val emptyViewInterface = rootView.findViewById<View>(emptyViewResId) as EmptyViewInterface
        val recyclerView = rootView.findViewById<PandaRecyclerView>(recyclerViewResId)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setEmptyView(emptyViewInterface)
        emptyViewInterface.emptyViewText(emptyViewString)
        emptyViewInterface.setNoConnectionText(getString(R.string.noConnection))
        recyclerView.isSelectionEnabled = true
        recyclerView.adapter = baseRecyclerAdapter

        swipeRefreshLayout = rootView.findViewById(swipeRefreshLayoutResId)
        swipeRefreshLayout!!.setOnRefreshListener {
            if (!NetworkUtils.isNetworkAvailable) {
                swipeRefreshLayout!!.isRefreshing = false
            } else {
                baseRecyclerAdapter.refresh()
            }
        }

        return recyclerView
    }

    fun openMedia(mime: String?, url: String?, filename: String?, canvasContext: CanvasContext, localFile: Boolean = false, useOutsideApps: Boolean = false) {
        val owner = activity ?: return

        openMediaBundle = if (localFile) {
            OpenMediaAsyncTaskLoader.createLocalBundle(canvasContext, mime, url, filename, useOutsideApps)
        } else {
            OpenMediaAsyncTaskLoader.createBundle(canvasContext, mime, url, filename, useOutsideApps)
        }

        onMainThread {
            try {
                LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(LoaderManager.getInstance(owner), openMediaBundle, loaderCallbacks, R.id.openMediaLoaderID)
            } catch (e: Exception) {
                toast(R.string.unexpectedErrorOpeningFile)
                onMediaLoadingComplete()
            }
        }
    }

    fun openMedia(canvasContext: CanvasContext, url: String, filename: String?) {
        val owner = activity ?: return
        onMainThread {
            openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(url, filename, canvasContext)
            LoaderUtils.restartLoaderWithBundle<LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>>(LoaderManager.getInstance(owner), openMediaBundle, loaderCallbacks, R.id.openMediaLoaderID)
        }
    }

    private fun downloadFileToDownloadDir(url: String): File? {
        // We should have the file cached locally at this point; We'll just move it to the user's Downloads folder

        if (!requireContext().hasPermissions(PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE)
            return null
        }

        Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "downloadFile URL: $url")
        val attachmentFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), loadedMedia!!.intent!!.data!!.lastPathSegment)

        // We've downloaded and cached this file already, so we'll just move it to the download directory
        val src = requireContext().contentResolver.openInputStream(loadedMedia!!.intent!!.data!!)
        val dst = FileOutputStream(attachmentFile)

        val buffer = ByteArray(1024)
        var len: Int = src!!.read(buffer)
        while (len > 0) {
            dst.write(buffer, 0, len)
            len = src.read(buffer)
        }

        Toast.makeText(requireContext(), getString(R.string.downloadSuccessful), Toast.LENGTH_SHORT).show()

        return attachmentFile
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.permissionGranted(permissions, grantResults, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(requireContext(), R.string.filePermissionGranted, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), R.string.filePermissionDenied, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun showToast(stringResId: Int) {
        if (isAdded) {
            Toast.makeText(requireActivity(), stringResId, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getFragment(): Fragment? = this

    fun setEmptyView(emptyView: EmptyView, drawableId: Int, titleId: Int, messageId: Int) {
        if (context != null) {
            emptyView.setEmptyViewImage(requireContext().getDrawableCompat(drawableId))
            emptyView.setTitleText(titleId)
            emptyView.setMessageText(messageId)
            emptyView.setListEmpty()
        }
    }
}
