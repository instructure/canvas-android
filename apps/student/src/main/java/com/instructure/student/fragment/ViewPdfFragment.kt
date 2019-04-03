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

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.student.R
import com.instructure.student.factory.ViewPdfFragmentPresenterFactory
import com.instructure.student.presenters.ViewPdfFragmentPresenter
import com.instructure.student.widget.ViewPdfFragmentView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.models.EditableFile
import com.pspdfkit.configuration.PdfConfiguration
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.ui.PdfFragment
import instructure.androidblueprint.PresenterFragment
import kotlinx.android.synthetic.main.fragment_view_pdf.*
import org.greenrobot.eventbus.EventBus

//TODO: make this generic enough teacher and student can use most/all of the code
class ViewPdfFragment : PresenterFragment<ViewPdfFragmentPresenter, ViewPdfFragmentView>(), ViewPdfFragmentView {

    private var mUrl by StringArg()
    private var mToolbarColor by IntArg()
    private var mEditableFile: EditableFile? by NullableParcelableArg()

    private val mPdfConfiguration: PdfConfiguration = PdfConfiguration.Builder().scrollDirection(PageScrollDirection.VERTICAL).build()

    override fun onPresenterPrepared(presenter: ViewPdfFragmentPresenter?) = Unit
    override fun onRefreshFinished() = Unit
    override fun onRefreshStarted() = Unit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_view_pdf, container, false)

    override fun onResume() {
        super.onResume()

        // If returning from editing this file, check if it was deleted so we can immediately go back
        val fileFolderDeletedEvent = EventBus.getDefault().getStickyEvent(FileFolderDeletedEvent::class.java)
        if (fileFolderDeletedEvent != null)
            requireActivity().finish()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar.title = mUrl

        mEditableFile?.let {
            // Check if we need to update the file name
            val fileFolderUpdatedEvent = EventBus.getDefault().getStickyEvent(FileFolderUpdatedEvent::class.java)
            fileFolderUpdatedEvent?.let { event ->
                it.file = event.updatedFileFolder
            }

            toolbar.title = it.file.displayName
        }

        if(isTablet && mToolbarColor != 0) {
            ViewStyler.themeToolbar(requireActivity(), toolbar, mToolbarColor, Color.WHITE)
        } else {
            toolbar.setupAsBackButton {
                requireActivity().onBackPressed()
            }
            ViewStyler.setToolbarElevationSmall(requireContext(), toolbar)
            ViewStyler.themeToolbar(requireActivity(), toolbar, Color.WHITE, Color.BLACK)
        }
    }

    override fun getPresenterFactory() = ViewPdfFragmentPresenterFactory(mUrl)

    override fun onReadySetGo(presenter: ViewPdfFragmentPresenter?) { presenter?.loadData(false) }

    override fun onLoadingStarted() {
        pdfProgressBar.setVisible()
        pdfProgressBar.announceForAccessibility(getString(R.string.loading))
    }

    override fun onLoadingProgress(progress: Float) {
        pdfProgressBar.setVisible().setProgress(progress)
    }

    override fun onLoadingFinished(fileUri: Uri) {
        pdfProgressBar.setGone()
        val newPdfFragment = PdfFragment.newInstance(fileUri, mPdfConfiguration)
        childFragmentManager.beginTransaction().replace(R.id.pdfFragmentContainer, newPdfFragment).commit()
    }

    override fun onLoadingError() {
        toast(R.string.errorLoadingFiles)
        activity?.onBackPressed()
    }

    companion object {
        @JvmStatic @JvmOverloads fun newInstance(url: String, toolbarColor: Int = 0, editableFile: EditableFile? = null) = ViewPdfFragment().apply {
            mUrl = url
            mToolbarColor = toolbarColor
            mEditableFile = editableFile
        }
        @JvmStatic fun newInstance(bundle: Bundle) = ViewPdfFragment().apply { arguments = bundle }

        @JvmStatic fun newInstance(route: Route) = ViewPdfFragment().apply { arguments = route.arguments }
    }
}
