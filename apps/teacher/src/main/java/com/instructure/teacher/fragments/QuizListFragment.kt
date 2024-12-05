/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.fragments

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_QUIZ_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.fragments.BaseExpandableSyncFragment
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.addSearch
import com.instructure.pandautils.utils.closeSearch
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.closeSearch
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.adapters.QuizListAdapter
import com.instructure.teacher.databinding.FragmentQuizListBinding
import com.instructure.teacher.events.QuizUpdatedEvent
import com.instructure.teacher.factory.QuizListPresenterFactory
import com.instructure.teacher.features.assignment.details.AssignmentDetailsFragment
import com.instructure.teacher.features.assignment.list.AssignmentListFragment
import com.instructure.teacher.presenters.QuizListPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import com.instructure.teacher.viewinterface.QuizListView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@PageView("{canvasContext}/quizzes")
@ScreenView(SCREEN_VIEW_QUIZ_LIST)
class QuizListFragment : BaseExpandableSyncFragment<
        String,
        Quiz, QuizListView,
        QuizListPresenter,
        RecyclerView.ViewHolder,
        QuizListAdapter>(), QuizListView {

    private val binding by viewBinding(FragmentQuizListBinding::bind)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(default = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, -1L, ""))

    private val linearLayoutManager by lazy { LinearLayoutManager(requireContext()) }
    private lateinit var mRecyclerView: RecyclerView

    private var gradingPeriodMenu: PopupMenu? = null

    private var needToForceNetwork = false

    override fun layoutResId(): Int = R.layout.fragment_quiz_list
    override val recyclerView: RecyclerView get() = binding.quizRecyclerView
    override fun getPresenterFactory() = QuizListPresenterFactory(canvasContext)
    override fun onPresenterPrepared(presenter: QuizListPresenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(
            rootView = rootView,
            context = requireContext(),
            recyclerAdapter = adapter,
            presenter = presenter,
            swipeToRefreshLayoutResId = R.id.swipeRefreshLayout,
            recyclerViewResId = R.id.quizRecyclerView,
            emptyViewResId = R.id.emptyPandaView,
            emptyViewText = getString(R.string.noQuizzesSubtext)
        )
    }

    override fun onCreateView(view: View) {
        linearLayoutManager.orientation = RecyclerView.VERTICAL
    }

    override fun onReadySetGo(presenter: QuizListPresenter) {
        if(recyclerView.adapter == null) {
            mRecyclerView.adapter = adapter
        }
        presenter.loadData(needToForceNetwork)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        if(gradingPeriodMenu != null) {
            gradingPeriodMenu?.dismiss()
        }
        super.onPause()
    }

    override fun createAdapter(): QuizListAdapter {
        return QuizListAdapter(requireContext(), presenter, canvasContext.color) { quiz ->
            if (RouteMatcher.canRouteInternally(requireActivity(), quiz.htmlUrl, ApiPrefs.domain, false)) {
                val route = RouteMatcher.getInternalRoute(quiz.htmlUrl!!, ApiPrefs.domain)
                val secondaryClass = when (route?.primaryClass) {
                    QuizListFragment::class.java -> QuizDetailsFragment::class.java
                    AssignmentListFragment::class.java -> AssignmentDetailsFragment::class.java
                    else -> null
                }
                RouteMatcher.route(requireActivity(), route?.copy(canvasContext = canvasContext,  primaryClass = null, secondaryClass = secondaryClass))
            } else {
                val args = QuizDetailsFragment.makeBundle(quiz)
                RouteMatcher.route(requireActivity(), Route(null, QuizDetailsFragment::class.java, canvasContext, args))
            }
        }
    }


    override fun onRefreshStarted() = with(binding) {
        //this prevents two loading spinners from happening during pull to refresh
        if(!swipeRefreshLayout.isRefreshing) {
            emptyPandaView.visibility  = View.VISIBLE
        }
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() = with(binding) {
        emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_quizzes_rocket))
        emptyPandaView.setMessageText(R.string.noQuizzesTeacher)
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    override fun perPageCount() = ApiPrefs.perPageCount

    private fun setupToolbar() = with(binding) {
        quizListToolbar.title = getString(R.string.tab_quizzes)
        quizListToolbar.subtitle = canvasContext.name
        quizListToolbar.setupBackButton(this@QuizListFragment)
        quizListToolbar.addSearch(getString(R.string.searchQuizzesHint)) { query ->
            if (query.isBlank()) {
                emptyPandaView.emptyViewText(R.string.no_items_to_display_short)
            } else {
                emptyPandaView.emptyViewText(getString(R.string.noItemsMatchingQuery, query))
            }
            presenter.searchQuery = query
        }
        ViewStyler.themeToolbarColored(requireActivity(), quizListToolbar, canvasContext.color, requireContext().getColor(R.color.textLightest))
    }

    override fun displayLoadingError() = toast(R.string.errorOccurred)

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizUpdated(event: QuizUpdatedEvent) {
        event.once(javaClass.simpleName) {
            // need to set a flag here. Because we use the event bus in the fragment instead of the presenter for unit testing purposes,
            // when we come back to this fragment it will go through the life cycle events again and the cached data will immediately
            // overwrite the data from the network if we refresh the presenter from here.
            needToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizGraded(event: QuizSubmissionGradedEvent) {
        event.once(javaClass.simpleName) { needToForceNetwork = true }
    }

    override fun onHandleBackPressed() = binding.quizListToolbar.closeSearch()

    companion object {

        fun newInstance(canvasContext: CanvasContext) = QuizListFragment().apply {
            this.canvasContext = canvasContext
        }
    }
}
