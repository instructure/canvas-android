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

import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_QUIZ_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BaseExpandableSyncFragment
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.adapters.QuizListAdapter
import com.instructure.teacher.events.QuizUpdatedEvent
import com.instructure.teacher.factory.QuizListPresenterFactory
import com.instructure.teacher.presenters.QuizListPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import com.instructure.teacher.viewinterface.QuizListView
import kotlinx.android.synthetic.main.fragment_quiz_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_QUIZ_LIST)
class QuizListFragment : BaseExpandableSyncFragment<
        String,
        Quiz, QuizListView,
        QuizListPresenter,
        RecyclerView.ViewHolder,
        QuizListAdapter>(), QuizListView {

    private var mCanvasContext: CanvasContext by ParcelableArg(default = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, -1L, ""))

    private val mLinearLayoutManager by lazy { LinearLayoutManager(requireContext()) }
    private lateinit var mRecyclerView: RecyclerView
    private val mCourseColor by lazy { ColorKeeper.getOrGenerateColor(mCanvasContext) }

    private var mGradingPeriodMenu: PopupMenu? = null

    private var mNeedToForceNetwork = false

    override fun layoutResId(): Int = R.layout.fragment_quiz_list
    override val recyclerView: RecyclerView get() = quizRecyclerView
    override fun getPresenterFactory() = QuizListPresenterFactory(mCanvasContext)
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
        mLinearLayoutManager.orientation = RecyclerView.VERTICAL
    }

    override fun onReadySetGo(presenter: QuizListPresenter) {
        if(recyclerView.adapter == null) {
            mRecyclerView.adapter = adapter
        }
        presenter.loadData(mNeedToForceNetwork)
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
        if(mGradingPeriodMenu != null) {
            mGradingPeriodMenu?.dismiss()
        }
        super.onPause()
    }

    override fun createAdapter(): QuizListAdapter {
        return QuizListAdapter(requireContext(), presenter, mCourseColor) { quiz ->
            val args = QuizDetailsFragment.makeBundle(quiz)
            RouteMatcher.route(requireContext(), Route(null, QuizDetailsFragment::class.java, mCanvasContext, args))
        }
    }


    override fun onRefreshStarted() {
        //this prevents two loading spinners from happening during pull to refresh
        if(!swipeRefreshLayout.isRefreshing) {
            emptyPandaView.visibility  = View.VISIBLE
        }
        emptyPandaView.setLoading()
    }

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_quizzes_rocket))
        emptyPandaView.setMessageText(R.string.noQuizzesTeacher)
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    override fun perPageCount() = ApiPrefs.perPageCount

    private fun setupToolbar() {
        quizListToolbar.title = getString(R.string.tab_quizzes)
        quizListToolbar.subtitle = mCanvasContext.name
        quizListToolbar.setupBackButton(this)
        quizListToolbar.addSearch(getString(R.string.searchQuizzesHint)) { query ->
            if (query.isBlank()) {
                emptyPandaView?.emptyViewText(R.string.no_items_to_display_short)
            } else {
                emptyPandaView?.emptyViewText(getString(R.string.noItemsMatchingQuery, query))
            }
            presenter.searchQuery = query
        }
        ViewStyler.themeToolbar(requireActivity(), quizListToolbar, mCourseColor, Color.WHITE)
    }

    override fun displayLoadingError() = toast(R.string.errorOccurred)

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizUpdated(event: QuizUpdatedEvent) {
        event.once(javaClass.simpleName) {
            // need to set a flag here. Because we use the event bus in the fragment instead of the presenter for unit testing purposes,
            // when we come back to this fragment it will go through the life cycle events again and the cached data will immediately
            // overwrite the data from the network if we refresh the presenter from here.
            mNeedToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizGraded(event: QuizSubmissionGradedEvent) {
        event.once(javaClass.simpleName) { mNeedToForceNetwork = true }
    }

    override fun onHandleBackPressed() = quizListToolbar.closeSearch()

    companion object {

        fun newInstance(canvasContext: CanvasContext) = QuizListFragment().apply {
            mCanvasContext = canvasContext
        }
    }
}
