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

package com.instructure.pandautils.features.offline.offlinecontent

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentOfflineContentBinding
import com.instructure.pandautils.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfflineContentFragment : BaseCanvasFragment(), FragmentInteractions {

    private val viewModel: OfflineContentViewModel by viewModels()

    private var canvasContext: CanvasContext? by NullableParcelableArg(key = Const.CANVAS_CONTEXT)

    private lateinit var binding: FragmentOfflineContentBinding

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (viewModel.shouldShowDiscardDialog()) {
                showDialog(
                    getString(R.string.offline_content_discard_dialog_title),
                    getString(R.string.offline_content_discard_dialog_message),
                    getString(R.string.offline_content_discard_dialog_positive)
                ) {
                    navigateBack()
                }
            } else {
                navigateBack()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOfflineContentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@OfflineContentFragment
            viewModel = this@OfflineContentFragment.viewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()

        viewModel.data.observe(viewLifecycleOwner) { data ->
            updateMenuText(data.selectedCount)
        }

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.onBackPressedDispatcher?.addCallback(this, backPressedCallback)
    }

    private fun navigateBack() {
        backPressedCallback.remove()
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun handleAction(action: OfflineContentAction) {
        when (action) {
            is OfflineContentAction.Back -> navigateBack()
            is OfflineContentAction.Dialog -> showDialog(
                title = action.title,
                message = action.message,
                positive = action.positive,
                positiveCallback = action.positiveCallback
            )
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.offline_content_toolbar_title)

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        binding.toolbar.apply {
            subtitle = canvasContext?.name ?: getString(R.string.offline_content_all_courses)
            setBackgroundColor(ThemePrefs.primaryColor)
            setupAsBackButton(this@OfflineContentFragment)
            setMenu(R.menu.menu_offline_content) {
                viewModel.toggleSelection()
            }
        }

        viewModel.data.value?.let { data ->
            updateMenuText(data.selectedCount)
        }
    }

    override fun getFragment(): Fragment = this

    private fun showDialog(title: String, message: String, positive: String, positiveCallback: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(positive) { _, _ -> positiveCallback() }
            .showThemed()
    }

    private fun updateMenuText(selectedCount: Int) {
        binding.toolbar.menu.items.firstOrNull()?.title = getString(
            if (selectedCount > 0) R.string.offline_content_deselect_all else R.string.offline_content_select_all
        )
    }

    companion object {

        fun makeRoute(canvasContext: CanvasContext? = null) = Route(OfflineContentFragment::class.java, canvasContext)

        private fun validRoute(route: Route) = route.primaryClass == OfflineContentFragment::class.java

        fun newInstance(route: Route) = if (validRoute(route)) OfflineContentFragment().withArgs(route.argsWithContext) else null
    }
}