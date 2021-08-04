/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.resources

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.LTITool
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentResourcesBinding
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ResourcesRouter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ResourcesFragment : Fragment() {

    @Inject
    lateinit var resourcesRouter: ResourcesRouter

    private val viewModel: ResourcesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentResourcesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.events.observe(viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        return binding.root
    }

    private fun handleAction(action: ResourcesAction) {
        when (action) {
            is ResourcesAction.OpenLtiApp -> showCourseSelectorDialog(action.ltiTools)
            is ResourcesAction.OpenComposeMessage -> resourcesRouter.openComposeMessage(action.recipient)
        }
    }

    private fun showCourseSelectorDialog(ltiTools: List<LTITool>) {
        val dialogEntries = ltiTools
            .map { it.contextName }
            .toTypedArray()

        AlertDialog.Builder(context, R.style.AccentDialogTheme)
            .setTitle(R.string.chooseACourse)
            .setItems(dialogEntries) { dialog, which -> openSelectedLti(dialog, which, ltiTools) }
            .setNegativeButton(R.string.sortByDialogCancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openSelectedLti(dialog: DialogInterface?, index: Int, ltiTools: List<LTITool>) {
        dialog?.dismiss()
        val ltiTool = ltiTools[index]
        resourcesRouter.openLti(ltiTool)
    }

    companion object {
        fun newInstance(): ResourcesFragment {
            return ResourcesFragment()
        }
    }
}