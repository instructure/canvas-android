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

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_FILE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadDialogParent
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileFolderDeletedEvent
import com.instructure.pandautils.utils.FileFolderUpdatedEvent
import com.instructure.pandautils.utils.FileUploadEvent
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup
import com.instructure.pandautils.utils.isUser
import com.instructure.pandautils.utils.remove
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.adapters.FileListAdapter
import com.instructure.teacher.databinding.FragmentFileListBinding
import com.instructure.teacher.dialog.CreateFolderDialog
import com.instructure.teacher.dialog.NoInternetConnectionDialog
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.UUID

@PageView
@ScreenView(SCREEN_VIEW_FILE_LIST)
class FileListFragment : BaseSyncFragment<
        FileFolder,
        FileListPresenter,
        FileListView,
        FileFolderViewHolder,
        FileListAdapter>(), FileListView, FileUploadDialogParent {

    private val binding by viewBinding(FragmentFileListBinding::bind)

    private lateinit var mRecyclerView: RecyclerView

    private var canvasContext: CanvasContext by ParcelableArg(Course())
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
                    binding.addFab.contentDescription = getString(R.string.hideCreateFileFolderFabContentDesc)
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

    @Suppress("unused")
    @PageViewUrl
    fun makePageViewUrl(): String {
        var url = if (canvasContext.type == CanvasContext.Type.USER) "${ApiPrefs.fullDomain}/files"
        else "${ApiPrefs.fullDomain}/${canvasContext.contextId.replace("_", "s/")}/files"

        if (!currentFolder.isRoot) {
            url += "/folder/"
            if (canvasContext.type == CanvasContext.Type.USER) {
                url += "users_${canvasContext.id}/"
            }
            url += currentFolder.fullName?.split(" ", limit = 2)?.get(1)?.replaceFirst("files/", "") ?: ""
        }

        return url
    }

    override fun layoutResId() = R.layout.fragment_file_list
    override fun onCreateView(view: View) = Unit
    override fun getPresenterFactory() = FileListPresenterFactory(currentFolder, canvasContext)
    override val recyclerView: RecyclerView get() = binding.fileListRecyclerView

    override fun onPresenterPrepared(presenter: FileListPresenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(
                rootView = rootView,
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

    override fun createAdapter(): FileListAdapter {
        return FileListAdapter(requireContext(), canvasContext.color, presenter) {
            if (it.displayName.isValid()) {
                // This is a file
                val editableFile = EditableFile(it, presenter.usageRights, presenter.licenses, canvasContext.color, presenter.mCanvasContext, R.drawable.ic_document)
                recordFilePreviewEvent(it)
                if (it.isHtmlFile) {
                    /* An HTML file can reference other canvas files as resources (e.g. CSS files) and must be
                    accessed as an authenticated preview to work correctly */
                    val bundle = ViewHtmlFragment.makeAuthSessionBundle(canvasContext, it, it.displayName.orEmpty(), canvasContext.color, editableFile)
                    RouteMatcher.route(requireActivity(), Route(ViewHtmlFragment::class.java, null, bundle))
                } else {
                    viewMedia(requireActivity(), it.displayName.orEmpty(), it.contentType.orEmpty(), it.url, it.thumbnailUrl, it.displayName, R.drawable.ic_document, canvasContext.color, editableFile)
                }
            } else {
                // This is a folder
                val args = makeBundle(presenter.mCanvasContext, it)
                RouteMatcher.route(requireActivity(), Route(FileListFragment::class.java, presenter.mCanvasContext, args))
            }
        }
    }

    private fun recordFilePreviewEvent(file: FileFolder) {
        pageViewUtils.saveSingleEvent("FilePreview", "${makePageViewUrl()}?preview=${file.id}")
    }

    override fun onRefreshStarted() = with(binding) {
        //this prevents two loading spinners from happening during pull to refresh
        if (!swipeRefreshLayout.isRefreshing) {
            emptyPandaView.visibility = View.VISIBLE
        }
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() = with(binding) {
        when {
            !presenter.currentFolder.isRoot -> emptyPandaView.setMessageText(R.string.emptyFolder)
            canvasContext.isCourse -> emptyPandaView.setMessageText(R.string.noFilesSubtextCourse)
            canvasContext.isGroup -> emptyPandaView.setMessageText(R.string.noFilesSubtextGroup)
            else -> emptyPandaView.setMessageText(R.string.noFilesSubtext)
        }
        emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_nofiles))
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    override fun folderCreationError() = toast(R.string.folderCreationError)

    override fun folderCreationSuccess() {
        checkIfEmpty()
    }

    private fun setupViews() = with(binding) {
        ViewStyler.themeFAB(addFab)
        ViewStyler.themeFAB(addFileFab)
        ViewStyler.themeFAB(addFolderFab)

        addFab.setOnClickListener { animateFabs() }
        addFileFab.setOnClickListener {
            animateFabs()
            handleClick(childFragmentManager) {
                val bundle = FileUploadDialogFragment.createContextBundle(null, canvasContext, presenter.currentFolder.id)
                FileUploadDialogFragment.newInstance(bundle).show(childFragmentManager, FileUploadDialogFragment.TAG)
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

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && addFab.isShown) {
                    if (fabOpen) {
                        animateFabs()
                    }
                    addFab.hide()
                } else if (dy < 0 && !addFab.isShown) {
                    addFab.show()
                }
            }
        })
    }

    override fun workInfoLiveDataCallback(uuid: UUID?, workInfoLiveData: LiveData<WorkInfo>) {
        workInfoLiveData.observe(viewLifecycleOwner) {
            if (it.state == WorkInfo.State.SUCCEEDED) {
                presenter.refresh(true)
            }
        }
    }

    private fun setupToolbar() = with(binding) {
        fileListToolbar.setupBackButton(this@FileListFragment)

        fileListToolbar.subtitle = presenter.mCanvasContext.name

        fileListToolbar.setupMenu(R.menu.menu_file_list) {
            when (it.itemId) {
                R.id.edit -> {
                    val bundle = EditFileFolderFragment.makeBundle(presenter.currentFolder, presenter.usageRights, presenter.licenses, presenter.mCanvasContext.id)
                    RouteMatcher.route(requireActivity(), Route(EditFileFolderFragment::class.java, canvasContext, bundle))
                }
                R.id.search -> RouteMatcher.route(requireActivity(), Route(FileSearchFragment::class.java, canvasContext, Bundle()))
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

        if (canvasContext.isUser) {
            // User's files, no CanvasContext
            ViewStyler.themeToolbarColored(requireActivity(), fileListToolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        } else ViewStyler.themeToolbarColored(requireActivity(), fileListToolbar, canvasContext.color, requireContext().getColor(R.color.textLightest))
    }

    private fun animateFabs() = if (fabOpen) {
        binding.addFab.startAnimation(fabRotateBackwards)
        binding.addFab.announceForAccessibility(getString(R.string.a11y_create_file_folder_gone))
        binding.addFab.contentDescription = getString(R.string.createFileFolderFabContentDesc)
        binding.addFolderFab.startAnimation(fabHide)
        binding.addFolderFab.isClickable = false

        binding.addFileFab.startAnimation(fabHide)
        binding.addFileFab.isClickable = false

        // Needed for accessibility
        binding.addFileFab.setInvisible()
        binding.addFolderFab.setInvisible()
        fabOpen = false
    } else {
        binding.addFab.startAnimation(fabRotateForward)
        binding.addFab.announceForAccessibility(getString(R.string.a11y_create_file_folder_visible))
        binding.addFab.contentDescription = getString(R.string.hideCreateFileFolderFabContentDesc)
        binding.addFolderFab.apply {
            startAnimation(fabReveal)
            isClickable = true
        }

        binding.addFileFab.apply {
            startAnimation(fabReveal)
            isClickable = true
        }

        // Needed for accessibility
        binding.addFileFab.setVisible()
        binding.addFolderFab.setVisible()

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

        fun newInstance(canvasContext: CanvasContext, args: Bundle) = FileListFragment().apply {
            this.canvasContext = args.getParcelable(CANVAS_CONTEXT) ?: canvasContext
            currentFolder = args.getParcelable(CURRENT_FOLDER) ?: FileFolder(id = -1L, name = "")
        }

        fun makeBundle(canvasContext: CanvasContext, currentFolder: FileFolder? = null): Bundle {
            val args = Bundle()
            val folder = currentFolder ?: FileFolder(id = -1L, name = "")
            args.putParcelable(CANVAS_CONTEXT, canvasContext)
            args.putParcelable(CURRENT_FOLDER, folder)
            return args
        }

        fun createBundle(folderId: Long, canvasContext: CanvasContext): Bundle {
            val args = Bundle()
            args.putParcelable(CANVAS_CONTEXT, canvasContext)
            args.putLong(Const.FOLDER_ID, folderId)
            return args
        }
    }
}
