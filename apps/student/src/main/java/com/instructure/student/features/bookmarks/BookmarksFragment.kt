package com.instructure.student.features.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.models.Bookmark
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.setupAsCloseButton
import com.instructure.student.R
import com.instructure.student.activity.BookmarkShortcutActivity
import com.instructure.student.databinding.FragmentBookmarksBinding
import com.instructure.student.features.bookmarks.edit.BookmarkEditFragment
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.util.ShortcutUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_bookmarks_fragment.*

@AndroidEntryPoint
class BookmarksFragment : ParentFragment() {

    private val viewModel: BookmarksViewModel by viewModels()

    private val binding by viewBinding(FragmentBookmarksBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_bookmarks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.events.observe(this) {
            it.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun handleAction(action: BookmarksAction) {
        when (action) {
            is BookmarksAction.OpenPopup -> {
                openPopup(action.view, action.id)
            }
            is BookmarksAction.CreateShortcut -> {
                createShortcut(action.bookmark)
            }
            is BookmarksAction.ShowSnackbar -> {
                Snackbar.make(requireView(), action.snackbar, Snackbar.LENGTH_SHORT).show()
            }
            is BookmarksAction.ShowDeleteConfirmation -> {
                showDeleteConfirmationDialog(action.bookmark)
            }
            is BookmarksAction.ShowEditDialog -> {
                BookmarkEditFragment.newInstance().show(childFragmentManager, null)
            }
        }
    }

    private fun showDeleteConfirmationDialog(bookmark: Bookmark) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.bookmarkDelete)
            .setMessage(bookmark.name)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deleteBookmark(bookmark.id)
            }
            .setNegativeButton(R.string.no, null)
            .create()
            .show()
    }

    private fun createShortcut(bookmark: Bookmark) {
        ShortcutUtils.generateShortcut(requireContext(), bookmark)
    }

    private fun openPopup(view: View, id: Long) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.bookmark_add_shortcut_edit_delete, popup.menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_add_to_homescreen -> {
                    viewModel.createShortcut(id)
                    return@OnMenuItemClickListener true
                }
                R.id.menu_edit -> {
                    viewModel.editBookmarkClicked(id)
                    return@OnMenuItemClickListener true
                }
                R.id.menu_delete -> {
                    viewModel.deleteBookmarkClicked(id)
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        popup.show()
    }

    override fun title(): String = getString(R.string.bookmarks)

    override fun applyTheme() {
        when (requireActivity()) {
            is BookmarkShortcutActivity -> {
                toolbar.title = getString(R.string.bookmarkShortcut)
                toolbar.setupAsCloseButton { activity?.finish() }
            }
            else -> {
                title()
                toolbar.setupAsBackButton(this)
            }
        }

        ViewStyler.themeToolbarColored(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        applyTheme()
    }

    companion object {
        fun newInstance() = BookmarksFragment()
    }
}