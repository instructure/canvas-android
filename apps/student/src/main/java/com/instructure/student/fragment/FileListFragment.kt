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

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.canvasapi2.utils.pageview.PageViewUtils
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_FILE_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.FileFolderCallback
import com.instructure.student.adapter.FileListRecyclerAdapter
import com.instructure.student.dialog.EditTextDialog
import com.instructure.student.features.files.search.FileSearchFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.FileDownloadJobIntentService
import com.instructure.student.util.StudentPrefs
import kotlinx.android.synthetic.main.fragment_file_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_FILE_LIST)
@PageView
class FileListFragment : ParentFragment(), Bookmarkable {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    @Suppress("unused")
    @PageViewUrl
    private fun makePageViewUrl() =
        if (canvasContext.type == CanvasContext.Type.USER) "${ApiPrefs.fullDomain}/files"
        else "${ApiPrefs.fullDomain}/${canvasContext.contextId.replace("_", "s/")}/files"

    private var recyclerAdapter: FileListRecyclerAdapter? = null

    private var folder: FileFolder? by NullableParcelableArg(key = Const.FOLDER)
    private var folderId: Long by LongArg(key = Const.FOLDER_ID)

    private lateinit var adapterCallback: FileFolderCallback
    private var mFabOpen = false

    // FAB animations
    private val fabRotateForward by lazy { AnimationUtils.loadAnimation(requireActivity(), R.anim.fab_rotate_forward) }
    private val fabRotateBackwards by lazy { AnimationUtils.loadAnimation(requireActivity(), R.anim.fab_rotate_backward) }
    private val fabReveal by lazy { AnimationUtils.loadAnimation(requireActivity(), R.anim.fab_reveal) }
    private val fabHide by lazy { AnimationUtils.loadAnimation(requireActivity(), R.anim.fab_hide) }

    override fun title(): String = getString(R.string.files)
    override fun getSelectedParamName(): String = RouterParams.FILE_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // We only want full screen dialog style if its user files
        if (canvasContext.type == CanvasContext.Type.USER) {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.LightStatusBarDialog)
        }
        setUpCallbacks()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            layoutInflater.inflate(R.layout.fragment_file_list, container, false)

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerAdapter?.cancel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search) {
            RouteMatcher.route(requireContext(), Route(FileSearchFragment::class.java, canvasContext, Bundle()))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.title = title()
        toolbar.subtitle = canvasContext.name
        addFab.setInvisible()
        toolbar.setMenu(R.menu.menu_file_list) {}

        if (canvasContext.type == CanvasContext.Type.USER) applyTheme()
        if (folder != null) {
            configureViews()
        } else {
            tryWeave {
                folder = if (folderId != 0L) {
                    // If folderId is valid, get folder by ID
                    awaitApi<FileFolder> { FileFolderManager.getFolder(folderId, true, it) }
                } else {
                    // Otherwise get root folder of the CanvasContext
                    awaitApi<FileFolder> { FileFolderManager.getRootFolderForContext(canvasContext, true, it) }
                }
                configureViews()
            } catch {
                toast(R.string.errorOccurred)
                activity?.onBackPressed()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (recyclerAdapter?.size() == 0) {
            emptyView.changeTextSize()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isTablet) {
                    emptyView.setGuidelines(.24f, .53f, .62f, .12f, .88f)
                } else {
                    emptyView.setGuidelines(.28f, .6f, .73f, .12f, .88f)

                }
            } else {
                if (isTablet) {
                    //change nothing, at least for now
                } else {
                    emptyView.setGuidelines(.25f, .7f, .74f, .15f, .85f)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun setUpCallbacks() {
        adapterCallback = object : FileFolderCallback {

            override fun onItemClicked(item: FileFolder) {
                if (item.fullName != null) {
                    RouteMatcher.route(requireContext(), FileListFragment.makeRoute(canvasContext, item))
                } else {
                    recordFilePreviewEvent(item)
                    if (item.isHtmlFile) {
                        /* An HTML file can reference other canvas files as resources (e.g. CSS files) and must be
                        accessed as an authenticated preview to work correctly */
                        RouteMatcher.route(requireContext(), InternalWebviewFragment.makeRoute(
                            canvasContext = canvasContext,
                            url = item.getFilePreviewUrl(ApiPrefs.fullDomain, canvasContext),
                            authenticate = true,
                            isUnsupportedFeature = false,
                            allowUnsupportedRouting = false,
                            shouldRouteInternally = true,
                            allowRoutingTheSameUrlInternally = false
                        ))
                    } else {
                        openMedia(item.contentType, item.url, item.displayName, canvasContext)
                    }
                }
            }

            override fun onOpenItemMenu(item: FileFolder, anchorView: View) {
                showOptionMenu(item, anchorView)
            }

            override fun onRefreshFinished() {
                setRefreshing(false)

                if (recyclerAdapter?.size() == 0) {
                    setEmptyView(emptyView, R.drawable.ic_panda_nofiles, R.string.noFiles, getNoFileSubtextId())
                }
            }
        }
    }

    override fun onMediaLoadingStarted() {
        fileLoadingProgressBar.setVisible()
    }

    override fun onMediaLoadingComplete() {
        fileLoadingProgressBar.setGone()
    }

    private fun recordFilePreviewEvent(file: FileFolder) {
        PageViewUtils.saveSingleEvent("FilePreview", "${makePageViewUrl()}?preview=${file.id}")
    }

    override fun applyTheme() {
        themeToolbar()
        if (canvasContext.type == CanvasContext.Type.USER) ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        toolbar.setupAsBackButton(this)
        ViewStyler.themeFAB(addFab, ThemePrefs.buttonColor)
        ViewStyler.themeFAB(addFileFab, ThemePrefs.buttonColor)
        ViewStyler.themeFAB(addFolderFab, ThemePrefs.buttonColor)
    }

    private fun themeToolbar() {
        // We style the toolbar white for user files
        if (canvasContext.type == CanvasContext.Type.USER) {
            ViewStyler.themeProgressBar(fileLoadingProgressBar, Color.BLACK)
            ViewStyler.themeToolbar(requireActivity(), toolbar, Color.WHITE, Color.BLACK, false)
        } else {
            ViewStyler.themeProgressBar(fileLoadingProgressBar, Color.WHITE)
            ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
        }
    }

    private fun configureViews() {
        val isUserFiles = canvasContext.type == CanvasContext.Type.USER

        if (recyclerAdapter == null) {
            recyclerAdapter = FileListRecyclerAdapter(requireContext(), canvasContext, getFileMenuOptions(folder!!, canvasContext), folder!!, adapterCallback)
        }

        configureRecyclerView(requireView(), requireContext(), recyclerAdapter!!, R.id.swipeRefreshLayout, R.id.emptyView, R.id.listView)

        setupToolbarMenu(toolbar)

        // Update toolbar title with folder name if it's not a root folder
        if (!folder!!.isRoot) toolbar.title = folder?.name

        themeToolbar()

        // Only show FAB for user files
        if ((isUserFiles || folder?.canUpload == true) && folder?.forSubmissions == false) {
            addFab.setVisible()
            addFab.onClickWithRequireNetwork { animateFabs() }
            addFileFab.onClickWithRequireNetwork {
                animateFabs()
                uploadFile()
            }
            addFolderFab.onClickWithRequireNetwork {
                animateFabs()
                createFolder()
            }

            // Add padding to bottom of RecyclerView to account for FAB
            listView.post {
                var bottomPad = addFab.height
                bottomPad += (addFab.layoutParams as? MarginLayoutParams)?.let { it.topMargin + it.bottomMargin }
                        ?: requireContext().DP(32).toInt()
                listView.setPadding(
                        listView.paddingLeft,
                        listView.paddingTop,
                        listView.paddingRight,
                        bottomPad
                )
            }
        }
    }

    private fun showOptionMenu(item: FileFolder, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.inflate(R.menu.file_folder_options)
        with(popup.menu) {
            val options = getFileMenuOptions(item, canvasContext)
            // Only show alternate-open option for PDF files
            findItem(R.id.openAlternate).isVisible = options.contains(FileMenuType.OPEN_IN_ALTERNATE)
            findItem(R.id.download).isVisible = options.contains(FileMenuType.DOWNLOAD)
            findItem(R.id.rename).isVisible = options.contains(FileMenuType.RENAME)
            findItem(R.id.delete).isVisible = options.contains(FileMenuType.DELETE)
        }

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.openAlternate -> {
                    recordFilePreviewEvent(item)
                    openMedia(item.contentType, item.url, item.displayName, true, canvasContext)
                }
                R.id.download -> downloadItem(item)
                R.id.rename -> renameItem(item)
                R.id.delete -> confirmDeleteItem(item)
            }
            true
        }

        popup.show()
    }

    enum class FileMenuType {
        DOWNLOAD, RENAME, DELETE, OPEN_IN_ALTERNATE
    }


    private fun downloadItem(item: FileFolder) {
        // First check if the Download Manager exists, and is enabled
        // Then check for permissions
        if (PermissionUtils.hasPermissions(requireActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
            FileDownloadJobIntentService.scheduleDownloadJob(requireContext(), item)
        } else {
            // Need permission
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun renameItem(item: FileFolder) {
        val title = getString(if (item.isFile) R.string.renameFile else R.string.renameFolder)
        EditTextDialog.show(requireFragmentManager(), title, item.displayName ?: item.name ?: "") {
            if (it.isBlank()) {
                toast(R.string.blankName)
                return@show
            }
            tryWeave {
                val body = UpdateFileFolder(name = it)
                val updateItem: FileFolder = if (item.isFile) {
                    awaitApi { FileFolderManager.updateFile(item.id, body, it) }
                } else {
                    awaitApi { FileFolderManager.updateFolder(item.id, body, it) }
                }
                recyclerAdapter?.add(updateItem)
                StudentPrefs.staleFolderIds = StudentPrefs.staleFolderIds + folder!!.id
            } catch {
                toast(R.string.errorOccurred)
            }
        }
    }

    private fun confirmDeleteItem(item: FileFolder) {
        val message = when {
            item.isFile -> getString(R.string.confirmDeleteFile, item.displayName)
            item.filesCount + item.foldersCount == 0 -> getString(R.string.confirmDeleteEmptyFolder, item.name)
            else -> {
                val itemCount = item.filesCount + item.foldersCount
                resources.getQuantityString(R.plurals.confirmDeleteFolder, itemCount, item.name, itemCount)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm)
                .setMessage(message)
                .setPositiveButton(R.string.delete) { _, _ -> deleteItem(item) }
                .setNegativeButton(R.string.cancel, null)
                .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }

        dialog.show()
    }

    private fun deleteItem(item: FileFolder) {
        tryWeave {
            val deletedItem: FileFolder = if (item.isFile) {
                awaitApi { FileFolderManager.deleteFile(item.id, it) }
            } else {
                awaitApi { FileFolderManager.deleteFolder(item.id, it) }
            }
            recyclerAdapter?.remove(deletedItem)
            if (recyclerAdapter?.size() == 0) {
                setEmptyView(emptyView, R.drawable.ic_panda_nofiles, R.string.noFiles, getNoFileSubtextId())
            }
            StudentPrefs.staleFolderIds = StudentPrefs.staleFolderIds + folder!!.id
        } catch {
            toast(R.string.errorOccurred)
        }
    }

    private fun getNoFileSubtextId(): Int {
        return when {
            folder?.isRoot == false -> R.string.emptyFolder
            canvasContext.isCourse -> R.string.noFilesSubtextCourse
            canvasContext.isGroup -> R.string.noFilesSubtextGroup
            else -> R.string.noFilesSubtext
        }
    }

    private fun uploadFile() {
        folder?.let {
            val bundle = UploadFilesDialog.createContextBundle(null, canvasContext, it.id)
            UploadFilesDialog.show(fragmentManager, bundle) { _ -> }
        }
    }

    private fun createFolder() {
        EditTextDialog.show(requireFragmentManager(), getString(R.string.createFolder), "") { name ->
            tryWeave {
                val newFolder = awaitApi<FileFolder> {
                    FileFolderManager.createFolder(folder!!.id, CreateFolder(name), it)
                }
                recyclerAdapter?.add(newFolder)
                StudentPrefs.staleFolderIds = StudentPrefs.staleFolderIds + folder!!.id
            } catch {
                toast(R.string.folderCreationError)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.permissionGranted(permissions, grantResults, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(requireActivity(), R.string.filePermissionGranted, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireActivity(), R.string.filePermissionDenied, Toast.LENGTH_LONG).show()
            }
        }
    }

    override val bookmark: Bookmarker
        get() = Bookmarker(canvasContext.isCourseOrGroup, canvasContext)

    private fun animateFabs() = if (mFabOpen) {
        addFab.startAnimation(fabRotateBackwards)
        addFab.announceForAccessibility(getString(R.string.a11y_create_file_folder_gone))
        addFab.contentDescription = getString(R.string.createFileFolderFabContentDesc)
        addFolderFab.startAnimation(fabHide)
        addFolderFab.isClickable = false

        addFileFab.startAnimation(fabHide)
        addFileFab.isClickable = false

        // Needed for accessibility
        addFileFab.setInvisible()
        addFolderFab.setInvisible()
        mFabOpen = false
    } else {
        addFab.startAnimation(fabRotateForward)
        addFab.announceForAccessibility(getString(R.string.a11y_create_file_folder_visible))
        addFab.contentDescription = getString(R.string.hideCreateFileFolderFabContentDesc)
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

        mFabOpen = true
    }

    @Suppress("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FileUploadEvent) {
        event.once(FileListFragment::class.java.name + folder?.id) { _ ->
            recyclerAdapter?.refresh()
            folder?.let {
                StudentPrefs.staleFolderIds = StudentPrefs.staleFolderIds + it.id
            }
        }
    }

    companion object {

        fun makeRoute(canvasContext: CanvasContext, fileFolder: FileFolder): Route {
            val bundle = Bundle().apply { putParcelable(Const.FOLDER, fileFolder) }
            return Route(null, FileListFragment::class.java, canvasContext, bundle)
        }

        fun makeRoute(canvasContext: CanvasContext, folderId: Long = 0L): Route {
            val bundle = Bundle().apply { putLong(Const.FOLDER_ID, folderId) }
            return Route(null, FileListFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            return route.canvasContext != null
        }

        fun newInstance(route: Route): FileListFragment? {
            if (!validateRoute(route)) return null
            return FileListFragment().withArgs(route.canvasContext!!.makeBundle(route.arguments))
        }

        /**
         * @return A list of possible actions the user is able to perform on the file/folder
         */
        fun getFileMenuOptions(fileFolder: FileFolder, canvasContext: CanvasContext): List<FileMenuType> {
            val options: MutableList<FileMenuType> = mutableListOf()

            if (canvasContext.type == CanvasContext.Type.USER) {
                // We're in the user's files, they should have options in the options menu
                if (!fileFolder.isLockedForUser) {
                    // File is not locked for this user
                    if (!fileFolder.forSubmissions) {
                        // File/folder is not for a submission, so we can rename/delete
                        with(options) {
                            add(FileMenuType.RENAME)
                            add(FileMenuType.DELETE)
                        }
                    }

                    if (fileFolder.isFile) {
                        // File is the user's and it's not locked, allow them to
                        // download the file, or open it in another app (if it's a PDF)
                        options.add(FileMenuType.DOWNLOAD)
                        if ("pdf" in fileFolder.contentType.orEmpty()) {
                            options.add(FileMenuType.OPEN_IN_ALTERNATE)
                        }
                    }
                }
            }

            if (canvasContext.type == CanvasContext.Type.COURSE) {
                // Course files, check if the user is able to mess with a file/folder
                val course = (canvasContext as Course)

                if (course.isStudent) {
                    // User is a student; Students can only download course files
                    if (!fileFolder.isLockedForUser && fileFolder.isFile) {
                        // File isn't locked, let them download it
                        options.add(FileMenuType.DOWNLOAD)

                        if ("pdf" in fileFolder.contentType.orEmpty()) {
                            options.add(FileMenuType.OPEN_IN_ALTERNATE)
                        }
                    }

                } else if (course.isTeacher || course.isTA) {
                    // User is a Teacher or Ta, they can rename, delete, or (if this is a file) download
                    with(options) {
                        add(FileMenuType.RENAME)
                        add(FileMenuType.DELETE)
                    }

                    if (fileFolder.isFile) {
                        options.add(FileMenuType.DOWNLOAD)
                        if ("pdf" in fileFolder.contentType.orEmpty()) {
                            options.add(FileMenuType.OPEN_IN_ALTERNATE)
                        }
                    }
                }
            }

            // If there's some case we didn't catch, default to not showing the menu
            return options
        }
    }
}
