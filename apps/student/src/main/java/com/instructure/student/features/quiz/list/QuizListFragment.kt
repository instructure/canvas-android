/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.student.features.quiz.list

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_QUIZ_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.PandaRecyclerRefreshLayoutBinding
import com.instructure.student.databinding.QuizListLayoutBinding
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.student.features.assignments.list.AssignmentListFragment
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_QUIZ_LIST)
@PageView(url = "{canvasContext}/quizzes")
@AndroidEntryPoint
class QuizListFragment : ParentFragment(), Bookmarkable {

    @Inject
    lateinit var quizListRepository: QuizListRepository

    private val binding by viewBinding(QuizListLayoutBinding::bind)
    private lateinit var recyclerBinding: PandaRecyclerRefreshLayoutBinding

    @get:PageViewUrlParam("canvasContext")
    var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private var recyclerAdapter: QuizListRecyclerAdapter? = null

    private var adapterToFragmentCallback: AdapterToFragmentCallback<Quiz> = object : AdapterToFragmentCallback<Quiz> {
        override fun onRowClicked(quiz: Quiz, position: Int, isOpenDetail: Boolean) {
            rowClick(quiz)
        }

        override fun onRefreshFinished() {
            setRefreshing(false)
            if (recyclerAdapter?.size() == 0) {
                setEmptyView(recyclerBinding.emptyView, R.drawable.ic_panda_quizzes_rocket, R.string.noQuizzes, R.string.noQuizzesSubtext)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = layoutInflater.inflate(R.layout.quiz_list_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerBinding = PandaRecyclerRefreshLayoutBinding.bind(binding.root)
        recyclerAdapter = QuizListRecyclerAdapter(requireContext(), canvasContext, adapterToFragmentCallback, quizListRepository, lifecycleScope)
        configureRecyclerView(
            view,
            requireContext(),
            recyclerAdapter!!,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView
        )
    }

    override fun applyTheme() {
        with (binding) {
            setupToolbarMenu(toolbar)
            toolbar.title = title()
            toolbar.setupAsBackButton(this@QuizListFragment)
            toolbar.addSearch(getString(R.string.searchQuizzesHint)) { query ->
                if (query.isBlank()) {
                    recyclerBinding.emptyView.emptyViewText(R.string.noItemsToDisplayShort)
                } else {
                    recyclerBinding.emptyView.emptyViewText(getString(R.string.noItemsMatchingQuery, query))
                }
                recyclerAdapter?.searchQuery = query
            }
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configureRecyclerView(
            requireView(),
            requireContext(),
            recyclerAdapter!!,
            R.id.swipeRefreshLayout,
            R.id.emptyView,
            R.id.listView,
                R.string.noQuizzes
        )
        if (recyclerAdapter!!.size() == 0) {
            recyclerBinding.emptyView.changeTextSize()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isTablet) {
                    recyclerBinding.emptyView.setGuidelines(.26f, .54f, .65f, .12f, .88f)
                } else {
                    recyclerBinding.emptyView.setGuidelines(.28f, .6f, .73f, .12f, .88f)

                }
            } else {
                if (isTablet) {
                    //change nothing, at least for now
                } else {
                    recyclerBinding.emptyView.setGuidelines(.25f, .7f, .74f, .15f, .85f)
                }
            }
        }
    }

    override fun onDestroy() {
        recyclerAdapter?.cancel()
        super.onDestroy()
    }

    override fun title(): String = getString(R.string.quizzes)

    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext)

    @androidx.annotation.OptIn(com.google.android.material.badge.ExperimentalBadgeUtils::class)
    private fun rowClick(quiz: Quiz) {
        val navigation = navigation
        if (navigation != null) {
            /* The quiz list endpoint is currently missing the quiz question types, so we'll route using the quiz url
            which should pull the full quiz details including the question types. */
            if (RouteMatcher.canRouteInternally(requireActivity(), quiz.htmlUrl!!, ApiPrefs.domain, false)) {
                val route = RouteMatcher.getInternalRoute(quiz.htmlUrl!!, ApiPrefs.domain)
                val secondaryClass = when (route?.primaryClass) {
                    QuizListFragment::class.java -> BasicQuizViewFragment::class.java
                    AssignmentListFragment::class.java -> AssignmentDetailsFragment::class.java
                    else -> null
                }
                RouteMatcher.routeUrl(requireActivity(), quiz.htmlUrl!!, ApiPrefs.domain, secondaryClass = secondaryClass)
            } else {
                RouteMatcher.route(requireActivity(), BasicQuizViewFragment.makeRoute(canvasContext, quiz, quiz.url!!))
            }
        }
    }

    override fun handleBackPressed() = binding.toolbar.closeSearch()

    companion object {
        fun makeRoute(canvasContext: CanvasContext): Route = Route(QuizListFragment::class.java, canvasContext, Bundle())

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): QuizListFragment? {
            if (!validateRoute(route)) return null
            return QuizListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }
}
