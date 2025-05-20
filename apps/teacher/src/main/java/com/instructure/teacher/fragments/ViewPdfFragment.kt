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

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_VIEW_PDF
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.utils.Utils.copyToClipboard
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentViewPdfBinding
import com.instructure.teacher.factory.ViewPdfFragmentPresenterFactory
import com.instructure.teacher.presenters.ViewPdfFragmentPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import com.instructure.teacher.viewinterface.ViewPdfFragmentView
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.ui.PdfFragment
import com.instructure.pandautils.blueprint.PresenterFragment
import org.greenrobot.eventbus.EventBus

@ScreenView(SCREEN_VIEW_VIEW_PDF)
class ViewPdfFragment : PresenterFragment<ViewPdfFragmentPresenter, ViewPdfFragmentView>(), ViewPdfFragmentView {

    private val binding by viewBinding(FragmentViewPdfBinding::bind)

    private var url by StringArg()
    private var toolbarColor by IntArg()
    private var editableFile: EditableFile? by NullableParcelableArg()
    private var isInModulesPager by BooleanArg()

    private val pdfConfiguration: PdfConfiguration = PdfConfiguration.Builder().scrollDirection(PageScrollDirection.VERTICAL).build()

    override fun onPresenterPrepared(presenter: ViewPdfFragmentPresenter) = Unit
    override fun onRefreshFinished() = Unit
    override fun onRefreshStarted() = Unit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_view_pdf, container, false)

    override fun onResume() {
        super.onResume()

        // Check if we need to update the file name
        updateFileNameIfNeeded()

        // If returning from editing this file, check if it was deleted so we can immediately go back
        val fileFolderDeletedEvent = EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        if (fileFolderDeletedEvent != null && fileFolderDeletedEvent.deletedFileFolder.id == editableFile?.file?.id) {
            requireActivity().finish()
        }
    }

    private fun updateFileNameIfNeeded() {
        editableFile?.let { editableFile ->
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                if (editableFile.file.id == event.updatedFileFolder.id) {
                    editableFile.file = event.updatedFileFolder
                }
            }
            binding.toolbar.title = editableFile.file.displayName
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) = with(binding) {
        super.onActivityCreated(savedInstanceState)
        toolbar.title = url

        editableFile?.let {
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
            toolbar.setupBackButtonWithExpandCollapseAndBack(this@ViewPdfFragment) {
                toolbar.updateToolbarExpandCollapseIcon(this@ViewPdfFragment)
                ViewStyler.themeToolbarColored(requireActivity(), toolbar, toolbarColor, requireContext().getColor(R.color.textLightest))
                (activity as MasterDetailInteractions).toggleExpandCollapse()
            }
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, toolbarColor, requireContext().getColor(R.color.textLightest))
        } else if (isTablet && toolbarColor != 0) {
            val textColor = if (toolbarColor == ThemePrefs.primaryColor) ThemePrefs.primaryTextColor else requireContext().getColor(R.color.textLightest)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, toolbarColor, textColor)
        } else {
            toolbar.setupBackButton {
                requireActivity().onBackPressed()
            }
            ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
            ViewStyler.themeToolbarLight(requireActivity(), toolbar)
        }
    }

    override fun getPresenterFactory() = ViewPdfFragmentPresenterFactory(url)

    override fun onReadySetGo(presenter: ViewPdfFragmentPresenter) { presenter.loadData(false) }

    override fun onLoadingStarted() {
        binding.pdfProgressBar.apply {
            setVisible()
            announceForAccessibility(getString(R.string.loading))
        }
    }

    override fun onLoadingProgress(progress: Float) {
        binding.pdfProgressBar.setVisible().setProgress(progress)
    }

    override fun onLoadingFinished(fileUri: Uri) {
        binding.pdfProgressBar.setGone()
        val newPdfFragment = PdfFragment.newInstance(fileUri, pdfConfiguration)
        childFragmentManager.beginTransaction().replace(R.id.pdfFragmentContainer, newPdfFragment).commit()
    }

    override fun onLoadingError() {
        toast(R.string.errorLoadingFiles)
        activity?.onBackPressed()
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            url: String,
            toolbarColor: Int = 0,
            editableFile: EditableFile? = null,
            isInModulesPager: Boolean = false
        ) = ViewPdfFragment().apply {
            this.url = url
            this.toolbarColor = toolbarColor
            this.editableFile = editableFile
            this.isInModulesPager = isInModulesPager
        }

        @JvmStatic
        fun newInstance(bundle: Bundle) = ViewPdfFragment().apply { arguments = bundle }
    }
}

