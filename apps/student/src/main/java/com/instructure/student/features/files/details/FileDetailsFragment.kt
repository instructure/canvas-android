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

package com.instructure.student.features.files.details

import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.pageview.PageViewUrlQuery
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_FILE_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.FragmentFileDetailsBinding
import com.instructure.student.events.ModuleUpdatedEvent
import com.instructure.student.events.post
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.util.StringUtilities
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_FILE_DETAILS)
@PageView(url = "{canvasContext}/files/{fileId}")
@AndroidEntryPoint
class FileDetailsFragment : ParentFragment() {

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var repository: FileDetailsRepository

    private val binding by viewBinding(FragmentFileDetailsBinding::bind)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private var moduleObject by ParcelableArg<ModuleObject>(key = Const.MODULE_OBJECT)
    private var itemId: Long by LongArg(key = Const.ITEM_ID)

    private var file: FileFolder? = null
    private var fileUrl: String by StringArg(key = Const.FILE_URL)
    private var fileId: Long by LongArg(key = Const.FILE_ID)

    private val moduleItemId: Long?
        get() = this.getModuleItemId()

    @PageViewUrlParam(name = "fileId")
    fun getFileIdValue(): Long = fileId

    @Suppress("unused") // For page view stats
    @PageViewUrlQuery(name = "module_item_id")
    fun getModuleIdValue(): Long? = moduleItemId

    private fun setPageViewReady() {
        completePageViewPrerequisite("pageViewReady")
    }

    override fun beforePageViewPrerequisites(): List<String> {
        return listOf("pageViewReady")
    }

    override fun title(): String {
        return if (file != null && file!!.lockInfo == null) file!!.displayName ?: getString(R.string.file) else getString(R.string.file)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_file_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.downloadButton.setVisible(repository.isOnline())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getFileFolder()
    }

    override fun applyTheme() {
        with (binding) {
            setupToolbarMenu(toolbar)
            toolbar.setupAsBackButton(this@FileDetailsFragment)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
            ViewStyler.themeButton(openButton)
            ViewStyler.themeButton(downloadButton)
        }
    }

    private fun setupTextViews() {
        binding.fileName.text = file?.displayName
        binding.fileType.text = file?.contentType
    }

    private fun setupClickListeners() {
        binding.openButton.setOnClickListener {
            file?.let { fileFolder ->
                openMedia(fileFolder.contentType, fileFolder.url, fileFolder.displayName, canvasContext, fileFolder.isLocalFile)
                markAsRead()
            }
        }

        binding.downloadButton.setOnClickListener {
            if (PermissionUtils.hasPermissions(requireActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                downloadFile()
            } else {
                requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onMediaLoadingStarted() {
        binding.fileLoadingProgressBar?.setVisible()
    }

    override fun onMediaLoadingComplete() {
        binding.fileLoadingProgressBar?.setGone()
    }

    private fun downloadFile() {
        workManager.enqueue(FileDownloadWorker.createOneTimeWorkRequest(file?.displayName.orEmpty(), file?.url.orEmpty()))
        markAsRead()
    }

    private fun markAsRead() {
        // Mark the module as read
        lifecycleScope.tryLaunch {
            repository.markAsRead(canvasContext, moduleObject.id, itemId, true)
            ModuleUpdatedEvent(moduleObject).post()
        }.catch {
            Logger.e("Error marking module item as read. " + it.message)
        }
    }

    @Suppress("deprecation")
    private fun getFileFolder() = with(binding) {
        lifecycleScope.tryLaunch {
            file = repository.getFileFolderFromURL(fileUrl, fileId, true)
            // Set up everything else now, we should have a file
            file?.let {
                if (it.lockInfo != null) {
                    // File is locked
                    fileIcon.setImageResource(R.drawable.ic_lock)
                    fileIcon.contentDescription = getString(R.string.locked_icon)
                    openButton.visibility = View.GONE
                    downloadButton.visibility = View.GONE
                    fileType.visibility = View.INVISIBLE
                    var lockedMessage = ""

                    if (it.lockInfo?.lockedModuleName != null) {
                        lockedMessage = "<p>" + String.format(requireActivity().getString(R.string.lockedFileDesc), "<b>" + it.lockInfo!!.lockedModuleName + "</b>") + "</p>"
                    }
                    if (it.lockInfo?.modulePrerequisiteNames?.size ?: 0 > 0) {
                        // We only want to add this text if there are module completion requirements
                        lockedMessage += requireActivity().getString(R.string.mustComplete) + "<br>"
                        // textViews can't display <ul> and <li> tags, so we need to use "&#8226; " instead
                        for (i in 0 until it.lockInfo!!.modulePrerequisiteNames!!.size) {
                            lockedMessage += "&#8226; " + it.lockInfo!!.modulePrerequisiteNames!![i]  //"&#8226; "
                        }
                        lockedMessage += "<br><br>"
                    }

                    // Check to see if there is an unlocked date
                    if (it.lockInfo!!.unlockDate!= null && it.lockInfo!!.unlockDate!!.after(Date())) {
                        lockedMessage += DateHelper.createPrefixedDateTimeString(activity, getString(R.string.unlockedAt) + "<br>&#8226; ", it.lockInfo!!.unlockDate)
                    }

                    fileName.text = StringUtilities.simplifyHTML(Html.fromHtml(lockedMessage, Html.FROM_HTML_MODE_LEGACY))
                } else {
                    setupTextViews()
                    setupClickListeners()
                    // If the file has a thumbnail then show it. Make it a little bigger since the thumbnail size is pretty small
                    if (repository.isOnline()) {
                        if (!TextUtils.isEmpty(it.thumbnailUrl)) {

                            fileIcon.layoutParams.apply {
                                height = requireActivity().DP(0).toInt()
                                width = height
                            }

                            fileIcon.contentDescription =
                                getString(R.string.filePreviewContentDescription)
                            Glide.with(requireActivity()).load(it.thumbnailUrl)
                                .apply(RequestOptions().fitCenter()).into(fileIcon)
                        }
                    }
                    else {
                        if (it.contentType?.contains("image") == true) {
                            fileIcon.layoutParams.apply {
                                height = requireActivity().DP(230).toInt()
                                width = height
                            }
                            fileIcon.setImageURI(it.url?.toUri())
                        }
                    }
                }
                setPageViewReady()
            }
            toolbar.title = title()
        } catch {
            Logger.e("Error getting file folder " + it.message)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.permissionGranted(permissions, grantResults, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                downloadFile()
            }
        }
    }

    companion object {

        fun makeRoute(canvasContext: CanvasContext, fileUrl: String, fileId: Long): Route {
            val bundle = Bundle().apply {
                putString(Const.FILE_URL, fileUrl)
                putLong(Const.FILE_ID, fileId)
            }
            return Route(null, FileDetailsFragment::class.java, canvasContext, bundle)
        }

        fun makeRoute(canvasContext: CanvasContext, moduleObject: ModuleObject, itemId: Long, fileUrl: String, fileId: Long): Route {
            val bundle = Bundle().apply {
                putString(Const.FILE_URL, fileUrl)
                putParcelable(Const.MODULE_OBJECT, moduleObject)
                putLong(Const.ITEM_ID, itemId)
                putLong(Const.FILE_ID, fileId)
            }
            return Route(null, FileDetailsFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            return route.canvasContext != null && route.arguments.containsKey(Const.FILE_URL)
        }

        fun newInstance(route: Route): FileDetailsFragment? {
            if (!validateRoute(route)) return null
            return FileDetailsFragment().withArgs(route.canvasContext!!.makeBundle(route.arguments))
        }
    }
}
