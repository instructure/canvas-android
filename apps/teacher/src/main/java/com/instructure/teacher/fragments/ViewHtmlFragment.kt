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
import com.instructure.pandautils.utils.filecache.FileCache
import com.instructure.pandautils.utils.filecache.awaitFileDownload
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_HTML
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.Utils.copyToClipboard
import com.instructure.teacher.R
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import org.greenrobot.eventbus.EventBus
import java.io.File

@ScreenView(SCREEN_VIEW_VIEW_HTML)
class ViewHtmlFragment : InternalWebViewFragment() {

    private val downloadUrl by NullableStringArg(key = DOWNLOAD_URL)
    private val toolbarColor by IntArg(key = TOOLBAR_COLOR)
    private val editableFile: EditableFile? by NullableParcelableArg(key = EDITABLE_FILE)
    private val isInModulesPager by BooleanArg(key = IS_IN_MODULES_PAGER)

    private var job: WeaveJob? = null

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        setShouldLoadUrl(!downloadUrl.isValid())
        super.onActivityCreated(savedInstanceState)
        if (downloadUrl.isValid()) job = weave {
            binding.loading.setVisible()
            binding.loading.announceForAccessibility(getString(R.string.loading))
            val tempFile: File? = FileCache.awaitFileDownload(downloadUrl!!)
            if (tempFile == null) {
                toast(R.string.errorLoadingFiles)
                activity?.onBackPressed()
            } else {
                loadHtml(tempFile.readText())
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // If returning from editing this file, check if it was deleted so we can immediately go back
        val fileFolderDeletedEvent = EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        if (fileFolderDeletedEvent != null && fileFolderDeletedEvent.deletedFileFolder.id == editableFile?.file?.id) {
            requireActivity().finish()
        }

        editableFile?.let {
            setupToolbar(toolbarColor)
        }
    }

    override fun setupToolbar(courseColor: Int) {
        editableFile?.let {
            // Check if we need to update the file name
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                if (it.file.id == event.updatedFileFolder.id) {
                    it.file = event.updatedFileFolder
                }
            }

            toolbar?.title = it.file.displayName
            toolbar?.setupMenu(R.menu.menu_file_details) { menu ->
                when (menu.itemId) {
                    R.id.edit -> {
                        val args = EditFileFolderFragment.makeBundle(it.file, it.usageRights, it.licenses, it.canvasContext!!.id)
                        RouteMatcher.route(requireActivity(), Route(EditFileFolderFragment::class.java, it.canvasContext, args))
                    }
                    R.id.copyLink -> {
                        if(it.file.url != null) {
                            copyToClipboard(requireContext(), it.file.url!!)
                        }
                    }
                }
            }
        }

        if (isInModulesPager) {
            toolbar.setupBackButtonWithExpandCollapseAndBack(this@ViewHtmlFragment) {
                toolbar.updateToolbarExpandCollapseIcon(this@ViewHtmlFragment)
                ViewStyler.themeToolbarColored(requireActivity(), toolbar!!, toolbarColor, requireContext().getColor(R.color.textLightest))
                (activity as MasterDetailInteractions).toggleExpandCollapse()
            }
            ViewStyler.themeToolbarColored(requireActivity(), toolbar!!, toolbarColor, requireContext().getColor(R.color.textLightest))
        } else if (isTablet && toolbarColor != 0) {
            val textColor = if (toolbarColor == ThemePrefs.primaryColor) ThemePrefs.primaryTextColor else requireContext().getColor(R.color.textLightest)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar!!, toolbarColor, textColor)
        } else {
            super.setupToolbar(courseColor)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    companion object {
        private const val DOWNLOAD_URL = "downloadUrl"
        private const val TOOLBAR_COLOR = "toolbarColor"
        private const val EDITABLE_FILE = "editableFile"
        private const val IS_IN_MODULES_PAGER = "isInModulesPager"

        fun makeAuthSessionBundle(
            canvasContext: CanvasContext,
            file: FileFolder,
            title: String,
            toolbarColor: Int = 0,
            editableFile: EditableFile? = null
        ): Bundle {
            return makeBundle(
                url = file.getFilePreviewUrl(ApiPrefs.fullDomain, canvasContext),
                title = title,
                shouldAuthenticate = true
            ).apply {
                putInt(TOOLBAR_COLOR, toolbarColor)
                putParcelable(EDITABLE_FILE, editableFile)
            }
        }

        @JvmOverloads
        fun makeDownloadBundle(
            downloadUrl: String,
            title: String,
            toolbarColor: Int = 0,
            editableFile: EditableFile? = null,
            isInModulesPager:Boolean = false
        ) = Bundle().apply {
            putString(DOWNLOAD_URL, downloadUrl)
            putString(Const.TITLE, title)
            putInt(TOOLBAR_COLOR, toolbarColor)
            putParcelable(EDITABLE_FILE, editableFile)
            putBoolean(IS_IN_MODULES_PAGER, isInModulesPager)
        }

        @JvmStatic fun newInstance(bundle: Bundle) = ViewHtmlFragment().apply { arguments = bundle }
    }
}
