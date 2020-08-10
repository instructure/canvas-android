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
package com.instructure.teacher.fragments

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.interactions.router.Route
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.adapters.FileListAdapter
import com.instructure.teacher.dialog.CreateFolderDialog
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.FileFolderDeletedEvent
import com.instructure.teacher.events.FileFolderUpdatedEvent
import com.instructure.teacher.factory.FileListPresenterFactory
import com.instructure.teacher.features.files.search.FileSearchFragment
import com.instructure.teacher.holders.FileFolderViewHolder
import com.instructure.teacher.presenters.FileListPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.viewMedia
import com.instructure.teacher.viewinterface.FileListView
import kotlinx.android.synthetic.main.fragment_file_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FileListFragment : BaseSyncFragment<
        FileFolder,
        FileListPresenter,
        FileListView,
        FileFolderViewHolder,
        FileListAdapter>(), FileListView {

    private lateinit var mRecyclerView: RecyclerView

    private val courseColor by lazy { ColorKeeper.getOrGenerateColor(mCanvasContext) }

    private var mCanvasContext: CanvasContext by ParcelableArg(Course())
    private var currentFolder: FileFolder by ParcelableArg(FileFolder())
    private var fabOpen = false

    // FAB animations
    private val fabRotateForward by lazy { AnimationUtils.loadAnimation(requireActivity(), R.anim.fab_rotate_forward) }
    private val fabRotateBackwards by lazy { AnimationUtils.loadAnimation(requireActivity(), R.anim.fab_rotate_backward) }
    private val fabReveal by lazy {
        AnimationUtils.loadAnimation(requireActivity(), R.anim.fab_reveal).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) = Unit
                override fun onAnimationRepeat(animation: Animation?) = Unit
                override fun onAnimationEnd(animation: Animation?) {
                    /* A11y traversal order for the FABs is specified in the XML layout file, but is only supported
                    in API 22+. If API level is less than that we need to manually request focus on the first FAB. */
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) addFolderFab.requestAccessibilityFocus(0)
                    addFab.contentDescription = getString(R.string.hideCreateFileFolderFabContentDesc)
                }
            })
        }
    }
    private val fabHide by lazy { AnimationUtils.loadAnimation(requireActivity(), R.anim.fab_hide) }

    private val handleClick: (FragmentManager, () -> Unit) -> Unit = { fragmentManager, onNetwork ->
        if (APIHelper.hasNetworkConnection()) {
            onNetwork.invoke()
        } else {
            NoInternetConnectionDialog.show(fragmentManager)
        }
    }

    // Handles File/Folder Updated/Deleted events
    // FileFolder - The modified file/folder
    // Boolean - Whether this is a delete event
    private val handleFileFolderUpdatedDeletedEvent: (FileFolder, Boolean) -> Unit = { fileFolder, delete ->
        when {
            presenter.currentFolder.id == fileFolder.id -> {
                // We are in the folder we just edited

                if (delete) {
                    // Back out of the folder we deleted
                    // Use of Handler prevents issue with FragmentManager being in the middle of a transaction
                    val handler = Handler()
                    handler.post {
                        requireActivity().onBackPressed()
                    }
                } else {
                    // The folder we are currently in was modified, update the presenter
                    presenter.currentFolder = fileFolder
                }
            }

            presenter.data.indexOfItemById(fileFolder.id) != -2 -> {
                // The modified file/folder is in the current directory
                if (delete) {
                    // A file in this folder was deleted, remove it
                    presenter.data.remove(fileFolder)
                    // Remove the sticky event once we've handled it in the list where this file/folder appears as an item
                    EventBus.getDefault().removeStickyEvent(FileFolderDeletedEvent::class.java)
                } else {
                    // A file in this folder was modified, update it
                    presenter.data.addOrUpdate(fileFolder)
                    EventBus.getDefault().removeStickyEvent(FileFolderUpdatedEvent::class.java)
                }
            }
        }
    }

    override fun layoutResId() = R.layout.fragment_file_list
    override fun getList() = presenter.data
    override fun onCreateView(view: View?) = Unit
    override fun getPresenterFactory() = FileListPresenterFactory(currentFolder, mCanvasContext)
    override fun getRecyclerView(): RecyclerView = fileListRecyclerView

    override fun onPresenterPrepared(presenter: FileListPresenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(
            rootView = mRootView,
            context = requireContext(),
            recyclerAdapter = adapter,
            presenter = presenter,
            swipeToRefreshLayoutResId = R.id.swipeRefreshLayout,
            recyclerViewResId = R.id.fileListRecyclerView,
            emptyViewResId = R.id.emptyPandaView,
            emptyViewText = getString(R.string.noFiles)
        )
    }

    override fun onReadySetGo(presenter: FileListPresenter) {
        if (recyclerView.adapter == null) {
            mRecyclerView.adapter = adapter
        }

        // Check if we need to update after a delete
        val fileFolderDeletedEvent = EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)

        // Handle file/folder events, if any
        fileFolderDeletedEvent?.let { handleFileFolderUpdatedDeletedEvent(it.deletedFileFolder, true) }
        fileFolderUpdatedEvent?.let { handleFileFolderUpdatedDeletedEvent(it.updatedFileFolder, false) }
        checkIfEmpty()

        if (fileFolderDeletedEvent == null && fileFolderUpdatedEvent == null) {
            // No file/folder update events, load the data like normal
            presenter.loadData(true)
        }

        setupToolbar()
        setupViews()
    }

    override fun getAdapter(): FileListAdapter {
        if (mAdapter == null) {
            mAdapter = FileListAdapter(requireContext(), courseColor, presenter) {

                if (it.displayName.isValid()) {
                    // This is a file
                    val editableFile = EditableFile(it, presenter.usageRights, presenter.licenses, courseColor, presenter.mCanvasContext, R.drawable.vd_document)
                    if (it.isHtmlFile) {
                        /* An HTML file can reference other canvas files as resources (e.g. CSS files) and must be
                        accessed as an authenticated preview to work correctly */
                        val bundle = ViewHtmlFragment.makeAuthSessionBundle(mCanvasContext, it, it.displayName.orEmpty(), courseColor, editableFile)
                        RouteMatcher.route(requireActivity(), Route(ViewHtmlFragment::class.java, null, bundle))
                    } else {
                        viewMedia(requireContext(), it.displayName.orEmpty(), it.contentType.orEmpty(), it.url, it.thumbnailUrl, it.displayName, R.drawable.vd_document, courseColor, editableFile)
                    }
                } else {
                    // This is a folder
                    val args = FileListFragment.makeBundle(presenter.mCanvasContext, it)
                    RouteMatcher.route(requireContext(), Route(FileListFragment::class.java, presenter.mCanvasContext, args))
                }
            }
        }

        return mAdapter
    }

    override fun onRefreshStarted() {
        //this prevents two loading spinners from happening during pull to refresh
        if (!swipeRefreshLayout.isRefreshing) {
            emptyPandaView.visibility = View.VISIBLE
        }
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        when {
            !presenter.currentFolder.isRoot -> emptyPandaView.setMessageText(R.string.emptyFolder)
            mCanvasContext.isCourse -> emptyPandaView.setMessageText(R.string.noFilesSubtextCourse)
            mCanvasContext.isGroup -> emptyPandaView.setMessageText(R.string.noFilesSubtextGroup)
            else -> emptyPandaView.setMessageText(R.string.noFilesSubtext)
        }
        emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.vd_panda_nofiles))
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }
    override fun folderCreationError() = toast(R.string.folderCreationError)

    private fun setupViews() {
        ViewStyler.themeFAB(addFab, ThemePrefs.buttonColor)
        ViewStyler.themeFAB(addFileFab, ThemePrefs.buttonColor)
        ViewStyler.themeFAB(addFolderFab, ThemePrefs.buttonColor)

        addFab.setOnClickListener { animateFabs() }
        addFileFab.setOnClickListener {
            animateFabs()
            handleClick(requireFragmentManager()) {
                val bundle = UploadFilesDialog.createContextBundle(null, mCanvasContext, presenter.currentFolder.id)
                UploadFilesDialog.show(fragmentManager, bundle) { _ -> }
            }
        }

        addFolderFab.setOnClickListener { _ ->
            animateFabs()
            handleClick(requireFragmentManager()) {
                CreateFolderDialog.show(requireFragmentManager()) {
                    presenter.createFolder(it)
                }
            }
        }
    }

    private fun setupToolbar() {
        fileListToolbar.setupBackButton(this)

        fileListToolbar.subtitle = presenter.mCanvasContext.name

        fileListToolbar.setupMenu(R.menu.menu_file_list) {
            when (it.itemId) {
                R.id.edit -> {
                    val bundle = EditFileFolderFragment.makeBundle(presenter.currentFolder, presenter.usageRights, presenter.licenses, presenter.mCanvasContext.id)
                    RouteMatcher.route(requireContext(), Route(EditFileFolderFragment::class.java, mCanvasContext, bundle))
                }
                R.id.search -> RouteMatcher.route(requireContext(), Route(FileSearchFragment::class.java, mCanvasContext, Bundle()))
            }
        }

        if (presenter.currentFolder.parentFolderId != 0L) {
            // This isn't a root folder - User can edit it
            fileListToolbar.title = presenter.currentFolder.name
        } else {
            // Toolbar title is files for root, otherwise folder name
            fileListToolbar.menu.findItem(R.id.edit)?.isVisible = false
            fileListToolbar.title = getString(R.string.sg_tab_files)
        }

        if (mCanvasContext.isUser) {
            // User's files, no CanvasContext
            ViewStyler.themeToolbar(requireActivity(), fileListToolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        } else ViewStyler.themeToolbar(requireActivity(), fileListToolbar, courseColor, Color.WHITE)
    }

    private fun animateFabs() = if (fabOpen) {
        addFab.startAnimation(fabRotateBackwards)
        addFab.contentDescription = getString(R.string.createFileFolderFabContentDesc)
        addFolderFab.startAnimation(fabHide)
        addFolderFab.isClickable = false

        addFileFab.startAnimation(fabHide)
        addFileFab.isClickable = false

        // Needed for accessibility
        addFileFab.setInvisible()
        addFolderFab.setInvisible()
        fabOpen = false
    } else {
        addFab.startAnimation(fabRotateForward)
        addFolderFab.apply {
            startAnimation(fabReveal)
            isClickable = true
        }

        addFileFab.apply {
            startAnimation(fabReveal)
            isClickable = true
        }

        // Needed for accessibility
        addFileFab.setVisible()
        addFolderFab.setVisible()

        fabOpen = true
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onFileEvent(event: FileUploadEvent) {
        event.get {
            event.remove()
            presenter.refresh(true)
        }
    }

    companion object {
        private const val CANVAS_CONTEXT = "canvasContext"
        private const val CURRENT_FOLDER = "currentFolder"

        @JvmStatic
        fun newInstance(canvasContext: CanvasContext, args: Bundle) = FileListFragment().apply {
            mCanvasContext = args.getParcelable(CANVAS_CONTEXT) ?: canvasContext
            currentFolder = args.getParcelable(CURRENT_FOLDER) ?: FileFolder(id = -1L, name = "")
        }

        @JvmStatic
        fun makeBundle(canvasContext: CanvasContext, currentFolder: FileFolder? = null): Bundle {
            val args = Bundle()
            val folder = currentFolder ?: FileFolder(id = -1L, name = "")
            args.putParcelable(CANVAS_CONTEXT, canvasContext)
            args.putParcelable(CURRENT_FOLDER, folder)
            return args
        }

        @JvmStatic
        fun createBundle(folderId: Long, canvasContext: CanvasContext): Bundle {
            val args = Bundle()
            args.putParcelable(CANVAS_CONTEXT, canvasContext)
            args.putLong(Const.FOLDER_ID, folderId)
            return args
        }
    }
}
