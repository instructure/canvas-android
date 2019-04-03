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
import android.os.Bundle
import com.instructure.annotations.FileCaching.FileCache
import com.instructure.annotations.awaitFileDownload
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.events.FileFolderDeletedEvent
import com.instructure.teacher.events.FileFolderUpdatedEvent
import com.instructure.interactions.router.Route
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.Utils.copyToClipboard
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import kotlinx.android.synthetic.main.fragment_internal_webview.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class ViewHtmlFragment : InternalWebViewFragment() {

    private var mHtmlUrl by StringArg()
    private var mJob: WeaveJob? = null
    private var mToolbarColor by IntArg()
    private var mEditableFile: EditableFile? by NullableParcelableArg()

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        setShouldLoadUrl(false)
        super.onActivityCreated(savedInstanceState)
        mJob = weave {
            loading.setVisible()
            loading.announceForAccessibility(getString(R.string.loading))
            val tempFile: File? = FileCache.awaitFileDownload(mHtmlUrl)
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
        if (fileFolderDeletedEvent != null)
            requireActivity().finish()

        mEditableFile?.let {
            setupToolbar(mToolbarColor)
        }
    }

    override fun setupToolbar(courseColor: Int) {
        mEditableFile?.let {
            // Check if we need to update the file name
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                it.file = event.updatedFileFolder
            }

            toolbar?.title = it.file.displayName
            toolbar?.setupMenu(R.menu.menu_file_details) { menu ->
                when (menu.itemId) {
                    R.id.edit -> {
                        val args = EditFileFolderFragment.makeBundle(it.file, it.usageRights, it.licenses, it.canvasContext!!.id)
                        RouteMatcher.route(requireContext(), Route(EditFileFolderFragment::class.java, it.canvasContext, args))
                    }
                    R.id.copyLink -> {
                        if(it.file.url != null) {
                            copyToClipboard(requireContext(), it.file.url!!)
                        }
                    }
                }
            }
        }

        if(isTablet && mToolbarColor != 0) {
            ViewStyler.themeToolbar(requireActivity(), toolbar!!, mToolbarColor, Color.WHITE)
        } else {
            super.setupToolbar(courseColor)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob?.cancel()
    }

    companion object {
        @JvmStatic @JvmOverloads fun newInstance(htmlUrl: String, title: String, toolbarColor: Int = 0, editableFile: EditableFile? = null) = ViewHtmlFragment().apply {
            mHtmlUrl = htmlUrl
            this.title = title
            mToolbarColor = toolbarColor
            mEditableFile = editableFile
        }
        @JvmStatic fun newInstance(bundle: Bundle) = ViewHtmlFragment().apply { arguments = bundle }
    }
}
