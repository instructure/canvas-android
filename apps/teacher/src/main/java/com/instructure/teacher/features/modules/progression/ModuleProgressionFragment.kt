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
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.orDefault
import com.instructure.teacher.databinding.FragmentModuleProgressionBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ModuleProgressionFragment : Fragment() {

    private val viewModel: ModuleProgressionViewModel by viewModels()
    private lateinit var binding: FragmentModuleProgressionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentModuleProgressionBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPager()
    }

    private fun setupPager() = with(binding.itemPager) {
        adapter = ModuleProgressionAdapter(childFragmentManager)
    }

    companion object {
        fun makeRoute(canvasContext: CanvasContext, moduleItemId: Long): Route {
            val bundle = canvasContext.makeBundle { putLong(RouterParams.MODULE_ITEM_ID, moduleItemId) }
            return Route(null, ModuleProgressionFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route): ModuleProgressionFragment? {
            if (!validRoute(route)) return null
            return ModuleProgressionFragment().apply {
                arguments = route.canvasContext?.makeBundle {
                    putAll(route.arguments)
                    val moduleItemIdParam = route.queryParamsHash[RouterParams.MODULE_ITEM_ID] ?: route.paramsHash[RouterParams.MODULE_ITEM_ID]
                    moduleItemIdParam?.let { putLong(RouterParams.MODULE_ITEM_ID, it.toLongOrNull().orDefault()) }
                }
            }
        }

        private fun validRoute(route: Route): Boolean = route.canvasContext != null
                && (route.queryParamsHash.keys.contains(RouterParams.MODULE_ITEM_ID)
                || route.paramsHash.keys.contains(RouterParams.MODULE_ITEM_ID)
                || route.arguments.containsKey(RouterParams.MODULE_ITEM_ID))
    }
}
