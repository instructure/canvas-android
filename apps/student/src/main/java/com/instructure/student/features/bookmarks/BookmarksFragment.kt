package com.instructure.student.features.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.setupAsCloseButton
import com.instructure.student.R
import com.instructure.student.activity.BookmarkShortcutActivity
import com.instructure.student.databinding.FragmentBookmarksBinding
import com.instructure.student.fragment.ParentFragment
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