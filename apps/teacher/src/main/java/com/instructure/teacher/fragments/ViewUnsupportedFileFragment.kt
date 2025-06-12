/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.instructure.pandautils.base.BaseCanvasFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.instructure.pandautils.utils.filecache.FileCache
import com.instructure.pandautils.utils.filecache.awaitFileDownload
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_UNSUPPORTED_FILE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.FileFolderDeletedEvent
import com.instructure.pandautils.utils.FileFolderUpdatedEvent
import com.instructure.pandautils.utils.IntArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.Utils.copyToClipboard
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.viewExternally
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentUnsupportedFileTypeBinding
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import java.io.File

@ScreenView(SCREEN_VIEW_VIEW_UNSUPPORTED_FILE)
class ViewUnsupportedFileFragment : BaseCanvasFragment() {

    private val binding by viewBinding(FragmentUnsupportedFileTypeBinding::bind)

    private var downloadFileJob: Job? = null

    private var mUri by ParcelableArg(Uri.EMPTY)
    private var mDisplayName by StringArg()
    private var mContentType by StringArg()
    private var mPreviewUri by ParcelableArg(Uri.EMPTY)
    private var mFallbackIcon by IntArg(R.drawable.ic_attachment)
    private var mEditableFile: EditableFile? by NullableParcelableArg()
    private var mToolbarColor by IntArg(0)
    private var isInModulesPager by BooleanArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_unsupported_file_type, container, false)
    }

    override fun onResume() {
        super.onResume()
        // If returning from editing this file, check if it was deleted so we can immediately go back
        val fileFolderDeletedEvent = EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        if (fileFolderDeletedEvent != null && fileFolderDeletedEvent.deletedFileFolder.id == mEditableFile?.file?.id) {
            requireActivity().finish()
        }

        mEditableFile?.let { setupToolbar() } ?: binding.toolbar.setGone()
    }

    private fun setupToolbar() = with(binding) {

        mEditableFile?.let {
            // Check if we need to update the file name
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                if (it.file.id == event.updatedFileFolder.id) {
                    it.file = event.updatedFileFolder
                }
            }

            toolbar.title = it.file.displayName

            // Update the name that is displayed above the open button
            fileNameView.text = it.file.displayName
            toolbar.setupMenu(R.menu.menu_file_details) { menu ->
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
            toolbar.setupBackButtonWithExpandCollapseAndBack(this@ViewUnsupportedFileFragment) {
                toolbar.updateToolbarExpandCollapseIcon(this@ViewUnsupportedFileFragment)
                ViewStyler.themeToolbarColored(requireActivity(), toolbar, mToolbarColor, requireContext().getColor(R.color.textLightest))
                (activity as MasterDetailInteractions).toggleExpandCollapse()
            }
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, mToolbarColor, requireContext().getColor(R.color.textLightest))
        } else if (isTablet && mToolbarColor != 0) {
            val textColor = if (mToolbarColor == ThemePrefs.primaryColor) ThemePrefs.primaryTextColor else requireContext().getColor(R.color.textLightest)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, mToolbarColor, textColor)
        } else {
            toolbar.setupBackButton {
                requireActivity().onBackPressed()
            }
            ViewStyler.themeToolbarLight(requireActivity(), toolbar)
            ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        }
    }

    override fun onStart() = with(binding) {
        super.onStart()
        Glide.with(requireContext())
                .load(mPreviewUri)
                .apply(RequestOptions().error(mFallbackIcon))
                .into(previewImageView.setVisible())
        fileNameView.text = mDisplayName
        ViewStyler.themeButton(openExternallyButton)

        openExternallyButton.onClick {

            downloadFileJob = tryWeave {
                if (mUri.scheme == "content") {
                    mUri.viewExternally(requireContext(), mContentType)
                    return@tryWeave
                }

                openExternallyButton.isEnabled = false
                openExternallyButton.text = getString(R.string.downloading)

                // Download the file first
                val tempFile = FileCache.awaitFileDownload(mUri.toString())

                // File cache overwrites the file name and extension, so we need to rename it back to the original name because some apps rely on the file extension
                val renamed = File(tempFile?.parentFile, mDisplayName)
                tempFile?.copyTo(renamed, overwrite = true)

                openExternallyButton.text = getText(R.string.openWithAnotherApp)
                openExternallyButton.isEnabled = true

                if (tempFile != null)
                    if (mDisplayName.contains(".doc") || mDisplayName.contains(".docx")) {
                        // Microsoft Word appears to search for their extensions specifically in the URI, regardless of the set MIME type - Our LRU cache does not keep track of the file name, including the extension,
                        // so, as a temporary solution, we append copy the file, appending the extension. This means that caching does not work for doc files for now (see TODO below)
                        // TODO: MBL-12338 (https://instructure.atlassian.net/browse/MBL-12338)
                        val docTempFile = File("${tempFile.absolutePath}${mDisplayName.substring(mDisplayName.indexOf("."), mDisplayName.length)}")
                        tempFile.renameTo(docTempFile)
                        Uri.fromFile(docTempFile).viewExternally(requireContext(), mContentType)
                    } else Uri.fromFile(renamed).viewExternally(requireContext(), mContentType)
                else {
                    throw RuntimeException("File download error")
                }
            } catch {
                toast(R.string.errorLoadingFiles)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        downloadFileJob?.cancel()
    }

    companion object {
        @JvmOverloads
        fun newInstance(
            uri: Uri,
            displayName: String,
            contentType: String,
            previewUri: Uri?,
            @DrawableRes fallbackIcon: Int,
            toolbarColor: Int = 0,
            editableFile: EditableFile? = null,
            isInModulesPager: Boolean = false
        ) = ViewUnsupportedFileFragment().apply {
            mUri = uri
            mDisplayName = displayName
            mContentType = contentType
            if (previewUri != null) mPreviewUri = previewUri
            mFallbackIcon = fallbackIcon
            mToolbarColor = toolbarColor
            mEditableFile = editableFile
            this.isInModulesPager = isInModulesPager
        }

        fun newInstance(bundle: Bundle) = ViewUnsupportedFileFragment().apply { arguments = bundle }
    }
}
