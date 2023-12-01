/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.teacher.features.files.details

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.backgroundColor
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentFileDetailsBinding
import com.instructure.teacher.fragments.ViewHtmlFragment
import com.instructure.teacher.fragments.ViewImageFragment
import com.instructure.teacher.fragments.ViewMediaFragment
import com.instructure.teacher.fragments.ViewPdfFragment
import com.instructure.teacher.fragments.ViewUnsupportedFileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FileDetailsFragment : Fragment() {

    private val viewModel: FileDetailsViewModel by viewModels()
    private lateinit var binding: FragmentFileDetailsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFileDetailsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.data.observe(viewLifecycleOwner) {
            setupFragment(getFragment(it.fileData))
        }
    }

    private fun setupFragment(fragment: Fragment) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container, fragment, fragment::class.java.name)
        fragmentTransaction.commitAllowingStateLoss()
    }

    private fun getFragment(fileData: FileViewData): Fragment {
        val toolbarColor = viewModel.canvasContext.backgroundColor
        return when (fileData) {
            is FileViewData.Pdf -> ViewPdfFragment.newInstance(
                fileData.url,
                toolbarColor,
                fileData.editableFile,
                true
            )

            is FileViewData.Media -> ViewMediaFragment.newInstance(
                Uri.parse(fileData.url),
                fileData.thumbnailUrl,
                fileData.contentType,
                fileData.displayName,
                true,
                toolbarColor,
                fileData.editableFile
            )

            is FileViewData.Image -> ViewImageFragment.newInstance(
                fileData.title,
                Uri.parse(fileData.url),
                fileData.contentType,
                true,
                toolbarColor,
                fileData.editableFile,
                true
            )

            is FileViewData.Html -> ViewHtmlFragment.newInstance(
                ViewHtmlFragment.makeDownloadBundle(
                    fileData.url,
                    fileData.fileName,
                    toolbarColor,
                    fileData.editableFile,
                    true
                )
            )

            is FileViewData.Other -> ViewUnsupportedFileFragment.newInstance(
                Uri.parse(fileData.url),
                fileData.fileName,
                fileData.contentType,
                tryOrNull { Uri.parse(fileData.thumbnailUrl) },
                R.drawable.ic_document,
                toolbarColor,
                fileData.editableFile,
                true
            )
        }
    }

    companion object {
        fun makeBundle(canvasContext: CanvasContext, fileUrl: String): Bundle {
            return canvasContext.makeBundle { putString(Const.FILE_URL, fileUrl) }
        }

        fun newInstance(bundle: Bundle) = FileDetailsFragment().withArgs(bundle)
    }
}
