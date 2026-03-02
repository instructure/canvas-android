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
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_FILE_SEARCH
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyDisplayCutoutInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.isUser
import com.instructure.pandautils.utils.onChangeDebounce
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onTextChanged
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.setInvisible
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentFileSearchBinding
import com.instructure.teacher.dialog.ConfirmDeleteFileFolderDialog
import com.instructure.teacher.fragments.EditFileFolderFragment
import com.instructure.teacher.holders.FileFolderViewHolder
import com.instructure.teacher.interfaces.ConfirmDeleteFileCallback
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.viewMedia
import com.instructure.teacher.utils.withRequireNetwork
import com.instructure.pandautils.utils.ColorUtils as PandaColorUtils

@ScreenView(SCREEN_VIEW_FILE_SEARCH)
class FileSearchFragment : BaseSyncFragment<
        FileFolder,
        FileSearchPresenter,
        FileSearchView,
        FileFolderViewHolder,
        FileSearchAdapter>(), FileSearchView, ConfirmDeleteFileCallback {

    private val binding by viewBinding(FragmentFileSearchBinding::bind)

    var canvasContext: CanvasContext? by NullableParcelableArg(key = Const.CANVAS_CONTEXT)

    private val searchAdapter by lazy {
        FileSearchAdapter(requireContext(), canvasContext.color, presenter, callback = {
            val editableFile = EditableFile(it, presenter.usageRights, presenter.licenses, canvasContext.color, presenter.canvasContext, R.drawable.ic_document)
            viewMedia(requireActivity(), it.displayName.orEmpty(), it.contentType.orEmpty(), it.url, it.thumbnailUrl, it.displayName, R.drawable.ic_document, canvasContext.color, editableFile)
        }, menuCallback = { fileFolder, view ->
            showOptionMenu(fileFolder, view)
        })
    }

    private fun showOptionMenu(item: FileFolder, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.inflate(R.menu.menu_file_list_item)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit -> {
                    val bundle = EditFileFolderFragment.makeBundle(item, presenter.usageRights, presenter.licenses, presenter.canvasContext.id)
                    RouteMatcher.route(requireActivity(), Route(EditFileFolderFragment::class.java, canvasContext, bundle))
                }
                R.id.delete -> withRequireNetwork { ConfirmDeleteFileFolderDialog.show(childFragmentManager, item) }
            }
            true
        }

        popup.show()
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
        setupWindowInsets()
    }

    private fun setupWindowInsets() = with(binding) {
        root.applyDisplayCutoutInsets()
        searchHeader.applyTopSystemBarInsets()
        ViewCompat.setOnApplyWindowInsetsListener(fileSearchRecyclerView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }
        if (fileSearchRecyclerView.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(fileSearchRecyclerView)
        }
    }

    override fun createAdapter(): FileSearchAdapter = searchAdapter

    override fun onRefreshStarted() {
        binding.progressBar.setVisible()
    }

    override fun onRefreshFinished() {
        binding.progressBar.setInvisible()
    }

    override val onConfirmDeleteFile: (fileFolder: FileFolder) -> Unit
        get() = { presenter.deleteFileFolder(it) }

    override fun fileFolderDeleted(fileFolder: FileFolder) {
        presenter.data.remove(fileFolder)
        checkIfEmpty()
    }

    override fun fileFolderDeleteError(message: Int) = toast(message)

    private fun setupViews() = with(binding) {
        themeSearchBar()

        // Set up empty state
        emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_nofiles))
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
        queryInput.onChangeDebounce(FileSearchPresenter.MIN_QUERY_LENGTH, FileSearchPresenter.QUERY_DEBOUNCE) {
            presenter.searchQuery = it
        }
    }

    private fun themeSearchBar() = with(binding) {
        val primaryTextColor = if (canvasContext?.isUser.orDefault()) ThemePrefs.primaryTextColor else requireContext().getColor(R.color.textLightest)
        val primaryColor = canvasContext.color
        ViewStyler.setStatusBarDark(requireActivity(), primaryColor)
        searchHeader.setBackgroundColor(primaryColor)
        queryInput.setTextColor(primaryTextColor)
        queryInput.setHintTextColor(ColorUtils.setAlphaComponent(primaryTextColor, 0x66))
        PandaColorUtils.colorIt(primaryTextColor, backButton)
        PandaColorUtils.colorIt(primaryTextColor, clearButton)
    }

    override fun checkIfEmpty() = with(binding) {
        emptyPandaView.setTitleText(getString(R.string.noFilesFound))
        emptyPandaView.setMessageText(getString(R.string.noItemsMatchingQuery, presenter.searchQuery))
        emptyPandaView.setVisible(presenter.isEmpty && presenter.searchQuery.isNotBlank())
        fileSearchRecyclerView.setVisible(!presenter.isEmpty)

        val queryTooShort = presenter.searchQuery.length < FileSearchPresenter.MIN_QUERY_LENGTH
        instructions.setVisible(queryTooShort)

        // A11y
        when {
            queryTooShort -> instructions.announceForAccessibility(instructions.text)
            presenter.isEmpty -> emptyPandaView.announceForAccessibility(emptyPandaView.getMessage().text)
            else -> {
                val count = presenter.data.size()
                val announcement = resources.getQuantityString(R.plurals.fileSearchResultCount, count, count)
                fileSearchRecyclerView.announceForAccessibility(announcement)
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
