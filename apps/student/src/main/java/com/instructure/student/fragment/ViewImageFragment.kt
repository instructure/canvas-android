/*
 * Copyright (C) 2018 - present  Instructure, Inc.
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

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.instructure.interactions.router.Route
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.interfaces.ShareableFile
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.FragmentViewImageBinding
import org.greenrobot.eventbus.EventBus

class ViewImageFragment : BaseCanvasFragment(), ShareableFile {

    private val binding by viewBinding(FragmentViewImageBinding::bind)

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

        if (mShowToolbar) setupToolbar() else binding.toolbar.setGone()
    }

    private fun setupToolbar() = with(binding) {

        mEditableFile?.let {

            // Check if we need to update the file name
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                it.file = event.updatedFileFolder
            }

            toolbar.title = it.file.displayName ?: it.file.url
        }

        if (isTablet && mToolbarColor != 0) {
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, mToolbarColor, requireContext().getColor(R.color.white))
        } else {
            toolbar.setupAsBackButton {
                requireActivity().onBackPressed()
            }
            ViewStyler.themeToolbarLight(requireActivity(), toolbar)
            ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
        }
    }

    private val requestListener = object : RequestListener<Drawable> {

        override fun onLoadFailed(p0: GlideException?, p1: Any?, target: Target<Drawable>, p3: Boolean): Boolean {
            binding.photoView.setGone()
            binding.progressBar.setGone()
            binding.errorContainer.setVisible()
            ViewStyler.themeButton(binding.openExternallyButton)
            binding.openExternallyButton.onClick { mUri.viewExternally(requireContext(), mContentType) }
            return false
        }

        override fun onResourceReady(
            resource: Drawable,
            model: Any,
            p2: Target<Drawable>?,
            dataSource: DataSource,
            p4: Boolean
        ): Boolean {
            binding.progressBar.setGone()

            // Try to set the background color using palette if we can
            (resource as? BitmapDrawable)?.bitmap?.let { colorBackground(it) }
            return false
        }
    }

    override fun onStart() {
        super.onStart()
        binding.progressBar.announceForAccessibility(getString(R.string.loading))
        Glide.with(this)
                .load(mUri)
                .listener(requestListener)
                .into(binding.photoView)
    }

    override fun viewExternally() {
        mUri.viewExternally(requireContext(), mContentType)
    }

    fun colorBackground(bitmap: Bitmap) {
        // Generate palette asynchronously
        Palette.from(bitmap).generate { palette ->
            palette?.let { binding.viewImageRootView.setBackgroundColor(it.getDarkMutedColor(requireContext().getColor(R.color.backgroundLightest))) }
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

        fun newInstance(route: Route) = ViewImageFragment().apply { arguments = route.arguments }
    }
}
