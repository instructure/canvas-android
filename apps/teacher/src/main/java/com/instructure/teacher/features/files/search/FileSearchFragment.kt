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
package com.instructure.teacher.features.files.search

import android.view.View
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.pandautils.analytics.SCREEN_VIEW_FILE_SEARCH
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentFileSearchBinding
import com.instructure.teacher.holders.FileFolderViewHolder
import com.instructure.teacher.utils.viewMedia
import com.instructure.pandautils.utils.ColorUtils as PandaColorUtils

@ScreenView(SCREEN_VIEW_FILE_SEARCH)
class FileSearchFragment : BaseSyncFragment<
        FileFolder,
        FileSearchPresenter,
        FileSearchView,
        FileFolderViewHolder,
        FileSearchAdapter>(), FileSearchView {

    private val binding by viewBinding(FragmentFileSearchBinding::bind)

    private val searchAdapter by lazy {
        FileSearchAdapter(requireContext(), canvasContext.textAndIconColor, presenter) {
            val editableFile = EditableFile(it, presenter.usageRights, presenter.licenses, canvasContext.backgroundColor, presenter.canvasContext, R.drawable.ic_document)
            viewMedia(requireContext(), it.displayName.orEmpty(), it.contentType.orEmpty(), it.url, it.thumbnailUrl, it.displayName, R.drawable.ic_document, canvasContext.backgroundColor, editableFile)
        }
    }

    override fun layoutResId() = R.layout.fragment_file_search
    override fun onCreateView(view: View) = Unit
    override fun getPresenterFactory() = FileSearchPresenterFactory(canvasContext!!)
    override val recyclerView: RecyclerView get() = binding.fileSearchRecyclerView

    override fun onPresenterPrepared(presenter: FileSearchPresenter) {
        binding.fileSearchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onReadySetGo(presenter: FileSearchPresenter) {
        if (recyclerView.adapter == null) binding.fileSearchRecyclerView.adapter = createAdapter()
        setupViews()
    }

    override fun createAdapter(): FileSearchAdapter = searchAdapter

    override fun onRefreshStarted() {
        binding.progressBar.setVisible()
    }

    override fun onRefreshFinished() {
        binding.progressBar.setInvisible()
    }

    private fun setupViews() {
        themeSearchBar()

        // Set up empty state
        binding.emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_nofiles))
        binding.emptyPandaView.setListEmpty()
        checkIfEmpty()

        // Set up toolbar
        binding.clearButton.onClick { binding.queryInput.setText("") }
        binding.backButton.onClick {
            if (activity is MasterDetailInteractions) {
                requireActivity().finish()
            } else {
                activity?.onBackPressed()
            }
        }

        // Set up query input
        binding.queryInput.onTextChanged { binding.clearButton.setVisible(it.isNotEmpty()) }
        binding.queryInput.onChangeDebounce(FileSearchPresenter.MIN_QUERY_LENGTH, FileSearchPresenter.QUERY_DEBOUNCE) {
            presenter.searchQuery = it
        }
    }

    private fun themeSearchBar() {
        val primaryTextColor = if (canvasContext?.isUser.orDefault()) ThemePrefs.primaryTextColor else requireContext().getColor(R.color.white)
        val primaryColor = canvasContext.backgroundColor
        ViewStyler.setStatusBarDark(requireActivity(), primaryColor)
        binding.searchHeader.setBackgroundColor(primaryColor)
        binding.queryInput.setTextColor(primaryTextColor)
        binding.queryInput.setHintTextColor(ColorUtils.setAlphaComponent(primaryTextColor, 0x66))
        PandaColorUtils.colorIt(primaryTextColor, binding.backButton)
        PandaColorUtils.colorIt(primaryTextColor, binding.clearButton)
    }

    override fun checkIfEmpty() {
        binding.emptyPandaView.setTitleText(getString(R.string.noFilesFound))
        binding.emptyPandaView.setMessageText(getString(R.string.noItemsMatchingQuery, presenter.searchQuery))
        binding.emptyPandaView.setVisible(presenter.isEmpty && presenter.searchQuery.isNotBlank())
        binding.fileSearchRecyclerView.setVisible(!presenter.isEmpty)

        val queryTooShort = presenter.searchQuery.length < FileSearchPresenter.MIN_QUERY_LENGTH
        binding.instructions.setVisible(queryTooShort)

        // A11y
        when {
            queryTooShort -> binding.instructions.announceForAccessibility(binding.instructions.text)
            presenter.isEmpty -> binding.emptyPandaView.announceForAccessibility(binding.emptyPandaView.getMessage().text)
            else -> {
                val count = presenter.data.size()
                val announcement = resources.getQuantityString(R.plurals.fileSearchResultCount, count, count)
                binding.fileSearchRecyclerView.announceForAccessibility(announcement)
            }
        }
    }

    override fun displayError() = toast(R.string.errorLoadingFiles)

    companion object {

        fun newInstance(canvasContext: CanvasContext) = FileSearchFragment().withArgs {
            putParcelable(Const.CANVAS_CONTEXT, canvasContext)
        }

    }

}
