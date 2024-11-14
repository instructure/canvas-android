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
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.setHidden
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentModuleProgressionBinding
import com.instructure.teacher.features.assignment.details.AssignmentDetailsFragment
import com.instructure.teacher.features.files.details.FileDetailsFragment
import com.instructure.teacher.fragments.InternalWebViewFragment
import com.instructure.teacher.fragments.PageDetailsFragment
import com.instructure.teacher.fragments.QuizDetailsFragment
import com.instructure.teacher.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ModuleProgressionFragment : BaseCanvasFragment() {

    private val viewModel: ModuleProgressionViewModel by viewModels()
    private val binding by viewBinding(FragmentModuleProgressionBinding::bind)

    private val canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private val moduleItemId by LongArg(key = RouterParams.MODULE_ITEM_ID, default = -1L)
    private val assetType by StringArg(key = ASSET_TYPE)
    private val assetId by StringArg(key = ASSET_ID)
    private val route: Route by ParcelableArg(key = ROUTE)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_module_progression, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.data.observe(viewLifecycleOwner) {
            setupPager(it)
        }

        viewModel.events.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { action ->
                handleAction(action)
            }
        }

        viewModel.loadData(canvasContext, moduleItemId, assetType, assetId)
    }

    private fun setupPager(data: ModuleProgressionViewData) = with(binding.itemPager) {
        adapter = ModuleProgressionAdapter(childFragmentManager, data.moduleItems.map { createFragment(it) })
        setCurrentItem(data.initialPosition, false)
        setupCarousel(data, data.initialPosition)
        addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                setupCarousel(data, position)
                viewModel.setCurrentPosition(position)
            }
        })
    }

    private fun setupCarousel(data: ModuleProgressionViewData, position: Int) = with(binding) {
        previous.setHidden(position == 0)
        next.setHidden(position == data.moduleItems.lastIndex)
        moduleName.text = data.moduleNames.getOrNull(position)
    }

    private fun handleAction(action: ModuleProgressionAction) = when (action) {
        is ModuleProgressionAction.RedirectToAsset -> {
            RouteMatcher.route(requireActivity(), route.copy(secondaryClass = action.asset.routeClass, removePreviousScreen = true))
        }
    }

    private fun createFragment(item: ModuleItemViewData) = when (item) {
        is ModuleItemViewData.Page -> PageDetailsFragment.newInstance(
            canvasContext, PageDetailsFragment.makeBundle(item.pageUrl)
        )

        is ModuleItemViewData.Assignment -> AssignmentDetailsFragment.newInstance(
            canvasContext as Course, AssignmentDetailsFragment.makeBundle(item.assignmentId)
        )

        is ModuleItemViewData.Discussion -> DiscussionDetailsWebViewFragment.newInstance(
            DiscussionDetailsWebViewFragment.makeRoute(canvasContext, item.discussionTopicHeaderId)
        )!!

        is ModuleItemViewData.Quiz -> QuizDetailsFragment.newInstance(
            canvasContext as Course, QuizDetailsFragment.makeBundle(item.quizId)
        )

        is ModuleItemViewData.External -> InternalWebViewFragment.newInstance(
            InternalWebViewFragment.makeBundle(
                item.url,
                item.title,
                darkToolbar = true,
                shouldAuthenticate = true,
                isInModulesPager = true,
                allowRoutingTheSameUrlInternally = false
            )
        )

        is ModuleItemViewData.File -> FileDetailsFragment.newInstance(
            FileDetailsFragment.makeBundle(canvasContext, item.fileUrl)
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

            for (asset in ModuleItemAsset.entries) {
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

        private fun isModuleItemAsset(paramName: String) = ModuleItemAsset.entries.find { it.assetIdParamName == paramName } != null
    }
}
