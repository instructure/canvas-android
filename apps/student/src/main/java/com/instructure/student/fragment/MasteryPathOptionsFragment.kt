/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
package com.instructure.student.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.managers.ModuleManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentSet
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.MasteryPathAssignment
import com.instructure.canvasapi2.models.MasteryPathSelectResponse
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_MASTERY_PATH_OPTIONS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ParcelableArrayListArg
import com.instructure.pandautils.utils.argsWithContext
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.peekingFragment
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.R
import com.instructure.student.adapter.MasteryPathOptionsRecyclerAdapter
import com.instructure.student.databinding.FragmentMasteryPathsOptionsBinding
import com.instructure.student.features.modules.list.ModuleListFragment
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher

@ScreenView(SCREEN_VIEW_MASTERY_PATH_OPTIONS)
class MasteryPathOptionsFragment : ParentFragment() {

    private val binding by viewBinding(FragmentMasteryPathsOptionsBinding::bind)

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)

    private lateinit var mRecyclerAdapter: MasteryPathOptionsRecyclerAdapter

    private var assignments by ParcelableArrayListArg<MasteryPathAssignment>(key = Const.ASSIGNMENT)
    private var assignmentSet by ParcelableArg<AssignmentSet>(key = Const.ASSIGNMENT_SET)
    private var moduleObjectId by LongArg(key = Const.MODULE_ID)
    private var moduleItemId by LongArg(key = Const.MODULE_ITEM)

    override fun applyTheme() = Unit

    override fun title(): String = getString(R.string.choose_assignment_group)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mastery_paths_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerAdapter = MasteryPathOptionsRecyclerAdapter(
            requireContext(),
            assignments.toTypedArray(),
            canvasContext.color,
            object : AdapterToFragmentCallback<Assignment> {
                override fun onRowClicked(assignment: Assignment, position: Int, isOpenDetail: Boolean) {
                    val route = AssignmentBasicFragment.makeRoute(canvasContext, assignment)
                    RouteMatcher.route(requireActivity(), route)
                }

                override fun onRefreshFinished() = Unit
            })

        configureRecyclerView(
            view,
            requireContext(),
            mRecyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView
        )

        // Disable the swipeRefreshLayout because we don't want to pull to refresh. It doesn't make an API call, so it wouldn't refresh anything
        binding.swipeRefreshLayout.isEnabled = false

        binding.selectButton.onClick { performSelection() }
    }

    private fun performSelection() {
        tryWeave {
            awaitApi<MasteryPathSelectResponse> {
                ModuleManager.selectMasteryPath(canvasContext, moduleObjectId, moduleItemId, assignmentSet.id, it)
            }
            /* The top fragment is the course module progression, the next one (peeking fragment) is the
            ModuleListFragment. We need to refresh that because they now have selected something. */
            val listFragment = peekingFragment as? ModuleListFragment
            activity?.supportFragmentManager?.popBackStack()
            listFragment?.refreshModuleList()
        } catch {
            toast(R.string.errorOccurred)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView(
            requireView(),
            requireContext(),
            mRecyclerAdapter,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView
        )
    }

    companion object {

        fun makeRoute(
            canvasContext: CanvasContext,
            assignments: Array<MasteryPathAssignment>,
            assignmentSet: AssignmentSet,
            moduleObjectId: Long,
            moduleItemId: Long
        ): Route {
            val bundle = Bundle().apply {
                putParcelableArrayList(Const.ASSIGNMENT, ArrayList(assignments.toList()))
                putParcelable(Const.ASSIGNMENT_SET, assignmentSet)
                putLong(Const.MODULE_ID, moduleObjectId)
                putLong(Const.MODULE_ITEM, moduleItemId)
            }
            return Route(MasteryPathOptionsFragment::class.java, canvasContext, bundle)
        }

        private fun validateRoute(route: Route): Boolean {
            return route.canvasContext != null
                    && route.arguments.containsKey(Const.ASSIGNMENT)
                    && route.arguments.containsKey(Const.ASSIGNMENT_SET)
                    && route.arguments.getLong(Const.MODULE_ID) > 0
                    && route.arguments.getLong(Const.MODULE_ITEM) > 0
        }

        fun newInstance(route: Route): MasteryPathOptionsFragment? {
            if (!validateRoute(route)) return null
            return MasteryPathOptionsFragment().withArgs(route.argsWithContext)
        }

    }
}
