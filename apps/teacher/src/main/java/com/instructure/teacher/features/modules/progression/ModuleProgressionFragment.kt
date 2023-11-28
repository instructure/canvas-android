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

package com.instructure.teacher.features.modules.progression

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.makeBundle
import com.instructure.teacher.databinding.FragmentModuleProgressionBinding
import com.instructure.teacher.features.discussion.DiscussionsDetailsFragment
import com.instructure.teacher.fragments.AssignmentDetailsFragment
import com.instructure.teacher.fragments.InternalWebViewFragment
import com.instructure.teacher.fragments.PageDetailsFragment
import com.instructure.teacher.fragments.QuizDetailsFragment
import com.instructure.teacher.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ModuleProgressionFragment : Fragment() {

    private val viewModel: ModuleProgressionViewModel by viewModels()
    private lateinit var binding: FragmentModuleProgressionBinding

    private val route: Route by ParcelableArg(key = ROUTE)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModuleProgressionBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.data.observe(viewLifecycleOwner) {
            setupPager(it.moduleItems, it.initialPosition)
        }

        viewModel.events.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { action ->
                handleAction(action)
            }
        }
    }

    private fun setupPager(items: List<ModuleItemViewData>, initialPosition: Int) = with(binding.itemPager) {
        adapter = ModuleProgressionAdapter(childFragmentManager, items.map { createFragment(it) })
        setCurrentItem(initialPosition, false)
    }

    private fun handleAction(action: ModuleProgressionAction) = when (action) {
        is ModuleProgressionAction.RedirectToAsset -> {
            RouteMatcher.route(requireActivity(), route.copy(secondaryClass = action.asset.routeClass, removePreviousScreen = true))
        }
    }

    private fun createFragment(item: ModuleItemViewData) = when (item) {
        is ModuleItemViewData.Page -> PageDetailsFragment.newInstance(
            viewModel.canvasContext, PageDetailsFragment.makeBundle(item.pageUrl)
        )

        is ModuleItemViewData.Assignment -> AssignmentDetailsFragment.newInstance(
            viewModel.canvasContext as Course, AssignmentDetailsFragment.makeBundle(item.assignmentId)
        )

        is ModuleItemViewData.Discussion -> if (item.isDiscussionRedesignEnabled) {
            DiscussionDetailsWebViewFragment.newInstance(
                DiscussionDetailsWebViewFragment.makeRoute(viewModel.canvasContext, item.discussionTopicHeaderId)
            )!!
        } else {
            DiscussionsDetailsFragment.newInstance(
                viewModel.canvasContext, DiscussionsDetailsFragment.makeBundle(item.discussionTopicHeaderId)
            )
        }

        is ModuleItemViewData.Quiz -> QuizDetailsFragment.newInstance(
            viewModel.canvasContext as Course, QuizDetailsFragment.makeBundle(item.quizId)
        )

        is ModuleItemViewData.External -> InternalWebViewFragment.newInstance(
            InternalWebViewFragment.makeBundle(
                item.url,
                item.title,
                darkToolbar = true,
                shouldAuthenticate = true,
                navButtonClose = false,
                allowRoutingTheSameUrlInternally = false
            )
        )
    }

    companion object {
        private const val ROUTE = "route"
        const val ASSET_TYPE = "asset_type"
        const val ASSET_ID = "asset_id"

        fun makeRoute(canvasContext: CanvasContext, moduleItemId: Long): Route {
            val bundle = canvasContext.makeBundle { putLong(RouterParams.MODULE_ITEM_ID, moduleItemId) }
            return Route(null, ModuleProgressionFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route): ModuleProgressionFragment? {
            if (!validRoute(route)) return null
            return ModuleProgressionFragment().apply {
                arguments = route.canvasContext?.makeBundle {
                    putAll(route.arguments)
                    putParcelable(ROUTE, route)
                    val asset = getAssetTypeAndId(route)
                    putString(ASSET_TYPE, asset.first.name)
                    putString(ASSET_ID, asset.second)
                }
            }
        }

        private fun getAssetTypeAndId(route: Route): Pair<ModuleItemAsset, String> {
            val queryParams = route.queryParamsHash
            val params = route.paramsHash

            if (queryParams.containsKey(RouterParams.MODULE_ITEM_ID)) {
                return ModuleItemAsset.MODULE_ITEM to queryParams[RouterParams.MODULE_ITEM_ID].orEmpty()
            }

            for (asset in ModuleItemAsset.values()) {
                if (params.containsKey(asset.assetIdParamName)) {
                    return asset to (params[asset.assetIdParamName].orEmpty())
                }
            }

            return ModuleItemAsset.MODULE_ITEM to ""
        }

        private fun validRoute(route: Route): Boolean = route.canvasContext != null
                && (route.queryParamsHash.keys.contains(RouterParams.MODULE_ITEM_ID)
                || route.arguments.containsKey(RouterParams.MODULE_ITEM_ID))
                || route.paramsHash.keys.any { isModuleItemAsset(it) }

        private fun isModuleItemAsset(paramName: String) = ModuleItemAsset.values().find { it.assetIdParamName == paramName } != null
    }
}
