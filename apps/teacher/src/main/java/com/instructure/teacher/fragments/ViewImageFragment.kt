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
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_IMAGE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.interfaces.ShareableFile
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.Utils.copyToClipboard
import com.instructure.teacher.R
import com.instructure.teacher.events.FileFolderDeletedEvent
import com.instructure.teacher.events.FileFolderUpdatedEvent
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.setupMenu
import kotlinx.android.synthetic.main.fragment_view_image.*
import org.greenrobot.eventbus.EventBus

@ScreenView(SCREEN_VIEW_VIEW_IMAGE)
class ViewImageFragment : Fragment(), ShareableFile {

    private var mUri by ParcelableArg(Uri.EMPTY)
    private var mContentType by StringArg()
    private var mTitle by StringArg()
    private var mShowToolbar by BooleanArg()
    private var mToolbarColor by IntArg()
    private var mEditableFile: EditableFile? by NullableParcelableArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_view_image, container, false)

    override fun onResume() {
        super.onResume()

        // If returning from editing this file, check if it was deleted so we can immediately go back
        val fileFolderDeletedEvent = EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        if (fileFolderDeletedEvent != null)
            requireActivity().finish()

        if (mShowToolbar) setupToolbar() else toolbar.setGone()
    }

    private fun setupToolbar() {

        mEditableFile?.let {

            // Check if we need to update the file name
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                it.file = event.updatedFileFolder
            }

            toolbar.title = it.file.displayName
            toolbar.setupMenu(R.menu.menu_file_details) { menu ->
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

        if (isTablet && mToolbarColor != 0) {
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, mToolbarColor, requireContext().getColor(R.color.white))
        } else {
            toolbar.setupBackButton {
                requireActivity().onBackPressed()
            }
            ViewStyler.themeToolbarLight(requireActivity(), toolbar)
            ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        }
    }

    private val requestListener = object : RequestListener<Bitmap> {

        override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Bitmap>?, p3: Boolean): Boolean {
            photoView.setGone()
            progressBar.setGone()
            errorContainer.setVisible()
            ViewStyler.themeButton(openExternallyButton)
            openExternallyButton.onClick { mUri.viewExternally(requireContext(), mContentType) }
            return false
        }

        override fun onResourceReady(bitmap: Bitmap?, p1: Any?, p2: Target<Bitmap>?, p3: DataSource?, p4: Boolean): Boolean {
            progressBar.setGone()

            // Try to set the background color using palette if we can
            bitmap?.let { colorBackground(it) }
            return false
        }
    }

    override fun onStart() {
        super.onStart()
        progressBar.announceForAccessibility(getString(R.string.loading))
        Glide.with(this)
                .asBitmap()
                .load(mUri)
                .listener(requestListener)
                .into(photoView)
    }

    override fun viewExternally() {
        mUri.viewExternally(requireContext(), mContentType)
    }

    fun colorBackground(bitmap: Bitmap) {
        // Generate palette asynchronously
        Palette.from(bitmap).generate { palette ->
            palette?.let { viewImageRootView.setBackgroundColor(it.getDarkMutedColor(requireContext().getColor(R.color.backgroundLightest))) }
        }
    }

    companion object {
        @JvmOverloads
        fun newInstance(title: String, uri: Uri, contentType: String, showToolbar: Boolean = true, toolbarColor: Int = 0, editableFile: EditableFile? = null) = ViewImageFragment().apply {
            mTitle = title
            mUri = uri
            mContentType = contentType
            mShowToolbar = showToolbar
            mToolbarColor = toolbarColor
            mEditableFile = editableFile
        }

        fun newInstance(bundle: Bundle) = ViewImageFragment().apply { arguments = bundle }
    }
}
