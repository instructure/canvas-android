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
package com.instructure.student.features.files.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageViewUtils
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_FILE_SEARCH
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.fragment.ParentFragment
import kotlinx.android.synthetic.main.fragment_file_search.*

@ScreenView(SCREEN_VIEW_FILE_SEARCH)
class FileSearchFragment : ParentFragment(), FileSearchView {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private fun makePageViewUrl() =
        if (canvasContext.type == CanvasContext.Type.USER) "${ApiPrefs.fullDomain}/files"
        else "${ApiPrefs.fullDomain}/${canvasContext.contextId.replace("_", "s/")}/files"

    private val searchAdapter by lazy { FileSearchAdapter(requireContext(), canvasContext, this) }

    override fun title() = ""
    override fun applyTheme() = Unit

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_file_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fileSearchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        fileSearchRecyclerView.adapter = searchAdapter
        setupViews()
    }

    override fun onRefreshStarted() {
        progressBar.setVisible()
    }

    override fun onRefreshFinished() {
        progressBar.setInvisible()
    }

    private fun setupViews() {
        ViewStyler.setStatusBarLight(requireActivity())

        // Set up empty state
        emptyPandaView.getEmptyViewImage()?.setImageResource(R.drawable.ic_panda_nofiles)
        emptyPandaView.setListEmpty()
        checkIfEmpty()

        // Set up toolbar
        clearButton.onClick { queryInput.setText("") }
        backButton.onClick {
            if (activity is MasterDetailInteractions) {
                requireActivity().finish()
            } else {
                activity?.onBackPressed()
            }
        }

        // Set up query input
        queryInput.onTextChanged { clearButton.setVisible(it.isNotEmpty()) }
        queryInput.onChangeDebounce(MIN_QUERY_LENGTH, QUERY_DEBOUNCE) {
            searchAdapter.searchQuery = it
        }

        // Manually request focus and show keyboard after a delay...because Student app
        queryInput.requestFocus()
        queryInput.postDelayed({ queryInput.showKeyboard() }, 500)
    }

    override fun checkIfEmpty() {
        emptyPandaView.setTitleText(getString(R.string.noFilesFound))
        emptyPandaView.setMessageText(getString(R.string.noItemsMatchingQuery, searchAdapter.searchQuery))
        emptyPandaView.setVisible(searchAdapter.isEmpty && searchAdapter.searchQuery.isNotBlank())
        fileSearchRecyclerView.setVisible(!searchAdapter.isEmpty)

        val queryTooShort = searchAdapter.searchQuery.length < MIN_QUERY_LENGTH
        instructions.setVisible(queryTooShort)

        // A11y
        when {
            queryTooShort -> instructions.announceForAccessibility(instructions.text)
            searchAdapter.isEmpty -> emptyPandaView.announceForAccessibility(emptyPandaView.getMessage().text)
            else -> {
                val count = searchAdapter.size()
                val announcement = resources.getQuantityString(R.plurals.fileSearchResultCount, count, count)
                fileSearchRecyclerView.announceForAccessibility(announcement)
            }
        }
    }

    override fun fileClicked(file: FileFolder) {
        PageViewUtils.saveSingleEvent("FilePreview", "${makePageViewUrl()}?preview=${file.id}")
        openMedia(file.contentType, file.url, file.displayName, canvasContext)
    }

    override fun onMediaLoadingStarted() {
        mediaLoadingView.setVisible()
        mediaLoadingView.announceForAccessibility(getString(R.string.loading))
    }

    override fun onMediaLoadingComplete() {
        mediaLoadingView.setGone()
    }

    override fun displayError() = toast(R.string.errorLoadingFiles)

    companion object {

        const val MIN_QUERY_LENGTH = 3
        const val QUERY_DEBOUNCE = 200L

        private fun validateRoute(route: Route): Boolean {
            return route.canvasContext != null
        }

        fun newInstance(route: Route): FileSearchFragment? {
            if (!validateRoute(route)) return null
            return FileSearchFragment().withArgs(route.canvasContext!!.makeBundle())
        }

    }

}
