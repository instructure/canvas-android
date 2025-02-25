package com.instructure.student.fragment

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
 */

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.instructure.pandautils.base.BaseCanvasFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.FragmentUnsupportedFileTypeBinding
import org.greenrobot.eventbus.EventBus

//TODO: make this generic enough teacher and student can use most/all of the code
class ViewUnsupportedFileFragment : BaseCanvasFragment() {

    private val binding by viewBinding(FragmentUnsupportedFileTypeBinding::bind)

    private var mUri by ParcelableArg(Uri.EMPTY)
    private var mDisplayName by StringArg()
    private var mContentType by StringArg()
    private var mPreviewUri by ParcelableArg(Uri.EMPTY)
    private var mFallbackIcon by IntArg(R.drawable.ic_attachment)
    private var mEditableFile: EditableFile? by NullableParcelableArg()
    private var mToolbarColor by IntArg(0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_unsupported_file_type, container, false)
    }

    override fun onResume() {
        super.onResume()
        // If returning from editing this file, check if it was deleted so we can immediately go back
        val fileFolderDeletedEvent = EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        if (fileFolderDeletedEvent != null)
            requireActivity().finish()

        mEditableFile?.let { setupToolbar() } ?: binding.toolbar.setGone()
    }
    private fun setupToolbar() = with(binding) {

        mEditableFile?.let {
            // Check if we need to update the file name
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                it.file = event.updatedFileFolder
            }

            toolbar.title = it.file.displayName

            //update the name that is displayed above the open button
            fileNameView.text = it.file.displayName
        }

        if(isTablet && mToolbarColor != 0) {
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, mToolbarColor, requireContext().getColor(R.color.white))
        } else {
            toolbar.setupAsBackButton {
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
        openExternallyButton.onClick { mUri.viewExternally(requireContext(), mContentType) }
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
                editableFile: EditableFile? = null
        ) = ViewUnsupportedFileFragment().apply {
            mUri = uri
            mDisplayName = displayName
            mContentType = contentType
            if (previewUri != null) mPreviewUri = previewUri
            mFallbackIcon = fallbackIcon
            mToolbarColor = toolbarColor
            mEditableFile = editableFile
        }

        fun newInstance(bundle: Bundle) = ViewUnsupportedFileFragment().apply { arguments = bundle }

        fun newInstance(route: Route) = ViewUnsupportedFileFragment().apply { arguments = route.arguments }
    }
}
