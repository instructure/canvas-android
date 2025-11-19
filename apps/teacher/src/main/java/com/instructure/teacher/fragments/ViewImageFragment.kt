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
 *
 */
package com.instructure.teacher.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_IMAGE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.interfaces.ShareableFile
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.FileFolderDeletedEvent
import com.instructure.pandautils.utils.FileFolderUpdatedEvent
import com.instructure.pandautils.utils.IntArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.Utils.copyToClipboard
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.filecache.FileCache
import com.instructure.pandautils.utils.filecache.awaitFileDownload
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.viewExternally
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentViewImageBinding
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import java.io.File

@ScreenView(SCREEN_VIEW_VIEW_IMAGE)
class ViewImageFragment : BaseCanvasFragment(), ShareableFile {

    private val binding by viewBinding(FragmentViewImageBinding::bind)

    private var url by StringArg()
    private var uri by NullableParcelableArg<Uri>()
    private var contentType by StringArg()
    private var title by StringArg()
    private var showToolbar by BooleanArg()
    private var toolbarColor by IntArg()
    private var editableFile: EditableFile? by NullableParcelableArg()
    private var isInModulesPager by BooleanArg()

    private var fileJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_view_image, container, false)

    override fun onResume() {
        super.onResume()

        // If returning from editing this file, check if it was deleted so we can immediately go back
        val fileFolderDeletedEvent =
            EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        if (fileFolderDeletedEvent != null && fileFolderDeletedEvent.deletedFileFolder.id == editableFile?.file?.id) {
            requireActivity().finish()
        }

        if (showToolbar) setupToolbar() else binding.toolbar.setGone()
    }

    private fun setupToolbar() = with(binding) {
        toolbar.applyTopSystemBarInsets()

        editableFile?.let {

            // Check if we need to update the file name
            val fileFolderUpdatedEvent =
                EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                if (it.file.id == event.updatedFileFolder.id) {
                    it.file = event.updatedFileFolder
                }
            }

            toolbar.title = it.file.displayName
            toolbar.setupMenu(R.menu.menu_file_details) { menu ->
                when (menu.itemId) {
                    R.id.edit -> {
                        val args = EditFileFolderFragment.makeBundle(
                            it.file,
                            it.usageRights,
                            it.licenses,
                            it.canvasContext!!.id
                        )
                        RouteMatcher.route(
                            requireActivity(),
                            Route(EditFileFolderFragment::class.java, it.canvasContext, args)
                        )
                    }

                    R.id.copyLink -> {
                        if (it.file.url != null) {
                            copyToClipboard(requireContext(), it.file.url!!)
                        }
                    }
                }
            }
        }

        if (isInModulesPager) {
            toolbar.setupBackButtonWithExpandCollapseAndBack(this@ViewImageFragment) {
                toolbar.updateToolbarExpandCollapseIcon(this@ViewImageFragment)
                ViewStyler.themeToolbarColored(
                    requireActivity(),
                    toolbar,
                    toolbarColor,
                    requireContext().getColor(R.color.textLightest)
                )
                (activity as MasterDetailInteractions).toggleExpandCollapse()
            }
            ViewStyler.themeToolbarColored(
                requireActivity(),
                toolbar,
                toolbarColor,
                requireContext().getColor(R.color.textLightest)
            )
        } else if (isTablet && toolbarColor != 0) {
            val textColor =
                if (toolbarColor == ThemePrefs.primaryColor) ThemePrefs.primaryTextColor else requireContext().getColor(
                    R.color.textLightest
                )
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, toolbarColor, textColor)
        } else {
            toolbar.setupBackButton {
                requireActivity().onBackPressed()
            }
            ViewStyler.themeToolbarLight(requireActivity(), toolbar)
            ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        }
    }

    private val requestListener = object : RequestListener<Bitmap> {

        override fun onLoadFailed(
            p0: GlideException?,
            p1: Any?,
            target: Target<Bitmap>,
            p3: Boolean
        ): Boolean = with(binding) {
            photoView.setGone()
            progressBar.setGone()
            errorContainer.setVisible()
            ViewStyler.themeButton(openExternallyButton)
            openExternallyButton.onClick { uri?.viewExternally(requireContext(), contentType) }
            return false
        }

        override fun onResourceReady(
            resource: Bitmap,
            model: Any,
            p2: Target<Bitmap>?,
            dataSource: DataSource,
            p4: Boolean
        ): Boolean {
            binding.progressBar.setGone()

            // Try to set the background color using palette if we can
            colorBackground(resource)
            return false
        }
    }

    override fun onStart() {
        super.onStart()
        binding.progressBar.announceForAccessibility(getString(R.string.loading))

        uri?.let {
            Glide.with(requireContext())
                .asBitmap()
                .load(it)
                .listener(requestListener)
                .into(binding.photoView)
        } ?: run {
            load(url) {
                Glide.with(requireContext())
                    .asBitmap()
                    .load(it)
                    .listener(requestListener)
                    .into(binding.photoView)
            }
        }

    }

    override fun viewExternally() {
        uri?.viewExternally(requireContext(), contentType)
    }

    fun colorBackground(bitmap: Bitmap) {
        // Generate palette asynchronously
        Palette.from(bitmap).generate { palette ->
            if (view != null && palette != null) {
                binding.viewImageRootView.setBackgroundColor(
                    palette.getDarkMutedColor(
                        requireContext().getColor(R.color.backgroundLightest)
                    )
                )
            }
        }
    }

    private fun load(url: String?, onFinished: (Uri) -> Unit) {
        tryWeave {
            val authUrl = OAuthManager.getAuthenticatedSessionAsync(url!!).await().dataOrNull!!.sessionUrl
            // If we don't have a url we'll display an error
            val tempFile: File? = FileCache.awaitFileDownload(authUrl) {}

            if (tempFile != null) {
                onFinished(Uri.fromFile(tempFile))
            } else {
                onFinished(Uri.EMPTY)
            }

        } catch {
            it.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fileJob?.cancel()
    }

    companion object {
        @JvmOverloads
        fun newInstance(
            title: String,
            uri: Uri,
            contentType: String,
            showToolbar: Boolean = true,
            toolbarColor: Int = 0,
            editableFile: EditableFile? = null,
            isInModulesPager: Boolean = false
        ) = ViewImageFragment().apply {
            this.title = title
            this.uri = uri
            this.contentType = contentType
            this.showToolbar = showToolbar
            this.toolbarColor = toolbarColor
            this.editableFile = editableFile
            this.isInModulesPager = isInModulesPager
        }

        fun newInstance(bundle: Bundle) = ViewImageFragment().apply { arguments = bundle }

        fun createBundle(
            title: String,
            url: String,
            contentType: String,
            showToolbar: Boolean = true,
            toolbarColor: Int = 0,
            editableFile: EditableFile? = null,
            isInModulesPager: Boolean = false
        ) = Bundle().apply {
            putString("title", title)
            putString("url", url)
            putString("contentType", contentType)
            putBoolean("showToolbar", showToolbar)
            putInt("toolbarColor", toolbarColor)
            putParcelable("editableFile", editableFile)
            putBoolean("isInModulesPager", isInModulesPager)
        }
    }
}
