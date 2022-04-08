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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_QUIZ_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.QuizListRecyclerAdapter
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.panda_recycler_refresh_layout.*
import kotlinx.android.synthetic.main.quiz_list_layout.*

@ScreenView(SCREEN_VIEW_QUIZ_LIST)
@PageView(url = "{canvasContext}/quizzes")
class QuizListFragment : ParentFragment(), Bookmarkable {

    private var canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private var recyclerAdapter: QuizListRecyclerAdapter? = null

    private var adapterToFragmentCallback: AdapterToFragmentCallback<Quiz> = object : AdapterToFragmentCallback<Quiz> {
        override fun onRowClicked(quiz: Quiz, position: Int, isOpenDetail: Boolean) {
            rowClick(quiz)
        }

        override fun onRefreshFinished() {
            setRefreshing(false)
            if (recyclerAdapter?.size() == 0) {
                setEmptyView(emptyView, R.drawable.ic_panda_quizzes_rocket, R.string.noQuizzes, R.string.noQuizzesSubtext)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = layoutInflater.inflate(R.layout.quiz_list_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerAdapter = QuizListRecyclerAdapter(requireContext(), canvasContext, adapterToFragmentCallback)
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
        setupToolbarMenu(toolbar)
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        toolbar.addSearch(getString(R.string.searchQuizzesHint)) { query ->
            if (query.isBlank()) {
                emptyView?.emptyViewText(R.string.noItemsToDisplayShort)
            } else {
                emptyView?.emptyViewText(getString(R.string.noItemsMatchingQuery, query))
            }
            recyclerAdapter?.searchQuery = query
        }
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
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
            emptyView.changeTextSize()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isTablet) {
                    emptyView.setGuidelines(.26f, .54f, .65f, .12f, .88f)
                } else {
                    emptyView.setGuidelines(.28f, .6f, .73f, .12f, .88f)

                }
            } else {
                if (isTablet) {
                    //change nothing, at least for now
                } else {
                    emptyView.setGuidelines(.25f, .7f, .74f, .15f, .85f)
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
                RouteMatcher.routeUrl(requireContext(), quiz.htmlUrl!!, ApiPrefs.domain, secondaryClass = secondaryClass)
            } else {
                RouteMatcher.route(requireContext(), BasicQuizViewFragment.makeRoute(canvasContext, quiz, quiz.url!!))
            }
        }
    }

    override fun handleBackPressed() = toolbar.closeSearch()

    companion object {
        fun makeRoute(canvasContext: CanvasContext): Route = Route(QuizListFragment::class.java, canvasContext, Bundle())

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun newInstance(route: Route): QuizListFragment? {
            if (!validateRoute(route)) return null
            return QuizListFragment().withArgs(route.canvasContext!!.makeBundle())
        }
    }
}
