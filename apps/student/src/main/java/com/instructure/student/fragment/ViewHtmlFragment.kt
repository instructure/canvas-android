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
package com.instructure.student.fragment

import android.os.Bundle
import com.instructure.pandautils.utils.filecache.FileCache
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_HTML
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.filecache.awaitFileDownload
import com.instructure.student.R
import org.greenrobot.eventbus.EventBus
import java.io.File

@ScreenView(SCREEN_VIEW_VIEW_HTML)
//TODO: make this generic enough teacher and student can use most/all of the code
class ViewHtmlFragment : InternalWebviewFragment() {

    private var mHtmlUrl by StringArg()
    private var mJob: WeaveJob? = null
    private var mToolbarColor by IntArg()
    private var mEditableFile: EditableFile? by NullableParcelableArg()

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        setShouldLoadUrl(false)
        super.onActivityCreated(savedInstanceState)
        mJob = weave {
            binding.webViewLoading.apply {
                setVisible()
                announceForAccessibility(getString(R.string.loading))
            }

            val tempFile: File? = FileCache.awaitFileDownload(mHtmlUrl)
            if (tempFile == null) {
                toast(R.string.errorLoadingFiles)
                activity?.onBackPressed()
            } else {
                loadHtml(tempFile.readText(), "text/html", "UTF-8", null)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // If returning from editing this file, check if it was deleted so we can immediately go back
        val fileFolderDeletedEvent = EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        if (fileFolderDeletedEvent != null)
            requireActivity().finish()

        setupToolbar()
    }

    private fun setupToolbar() {
        mEditableFile?.let {
            // Check if we need to update the file name
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                it.file = event.updatedFileFolder
            }

            binding.toolbar.title = it.file.displayName
        }

        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, mToolbarColor, requireContext().getColor(R.color.white))
    }

    override fun onDestroy() {
        super.onDestroy()
        mJob?.cancel()
    }

    companion object {
         @JvmOverloads fun newInstance(htmlUrl: String, title: String, toolbarColor: Int = 0, editableFile: EditableFile? = null) = ViewHtmlFragment().apply {
            mHtmlUrl = htmlUrl
            this.title = title
            mToolbarColor = toolbarColor
            mEditableFile = editableFile
        }

         fun newInstance(bundle: Bundle) = ViewHtmlFragment().apply { arguments = bundle }

         fun newInstance(route: Route) = ViewHtmlFragment().apply { arguments = route.arguments }
    }
}
