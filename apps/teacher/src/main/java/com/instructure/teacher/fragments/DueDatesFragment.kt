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

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_DUE_DATES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.discussion.create.CreateDiscussionWebViewFragment
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.bind
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.EmptyView
import com.instructure.teacher.R
import com.instructure.teacher.adapters.DueDatesAdapter
import com.instructure.teacher.databinding.FragmentAssignmentDueDatesBinding
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.factory.DueDatesPresenterFactory
import com.instructure.teacher.holders.DueDateViewHolder
import com.instructure.teacher.models.DueDateGroup
import com.instructure.teacher.presenters.DueDatesPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButtonAsBackPressedOnly
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.viewinterface.DueDatesView

@ScreenView(SCREEN_VIEW_DUE_DATES)
class DueDatesFragment : BaseSyncFragment<DueDateGroup, DueDatesPresenter, DueDatesView, DueDateViewHolder, DueDatesAdapter>(), DueDatesView {

    private val binding by viewBinding(FragmentAssignmentDueDatesBinding::bind)

    var mAssignment: Assignment by ParcelableArg(key = ASSIGNMENT)
    var mCourse: Course by ParcelableArg(Course())

    private val dueDateRecyclerView by bind<RecyclerView>(R.id.recyclerView)
    val swipeRefreshLayout by bind<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
    val emptyPandaView by bind<EmptyView>(R.id.emptyPandaView)

    override fun layoutResId() = R.layout.fragment_assignment_due_dates
    override val recyclerView get() = dueDateRecyclerView
    override fun withPagination() = false
    override fun getPresenterFactory() = DueDatesPresenterFactory(mAssignment)
    override fun onCreateView(view: View) {}

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    override fun hideMenu() {
        binding.toolbar.menu.clear()
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setupBackButtonAsBackPressedOnly(this@DueDatesFragment)
        toolbar.title = getString(R.string.page_title_due_dates)
        if(!isTablet) {
            toolbar.subtitle = mCourse.name
        }
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, mCourse.color, requireContext().getColor(R.color.textLightest))
    }

    override fun showMenu(assignment: Assignment) {
        binding.toolbar.setupMenu(R.menu.menu_edit_generic) {
            if(APIHelper.hasNetworkConnection()) {
                when {
                    assignment.submissionTypesRaw.contains(Assignment.SubmissionType.ONLINE_QUIZ.apiString) -> {
                        val args = EditQuizDetailsFragment.makeBundle(assignment.quizId)
                        RouteMatcher.route(requireActivity(), Route(EditQuizDetailsFragment::class.java, mCourse, args))
                    }
                    assignment.submissionTypesRaw.contains(Assignment.SubmissionType.DISCUSSION_TOPIC.apiString) -> {
                        val discussionTopicHeader = assignment.discussionTopicHeader
                        val route = CreateDiscussionWebViewFragment.makeRoute(mCourse, false, discussionTopicHeader?.id)

                        RouteMatcher.route(requireActivity(), route)
                    }
                    else -> {
                        val args = EditAssignmentDetailsFragment.makeBundle(assignment, true)
                        RouteMatcher.route(requireActivity(), Route(EditAssignmentDetailsFragment::class.java, mCourse, args))
                    }
                }
            } else {
                NoInternetConnectionDialog.show(requireFragmentManager())
            }
        }
    }

    override fun onPresenterPrepared(presenter: DueDatesPresenter) {
        RecyclerViewUtils.buildRecyclerView(
            rootView = rootView,
            context = requireContext(),
            recyclerAdapter = adapter,
            presenter = presenter,
            swipeToRefreshLayoutResId = R.id.swipeRefreshLayout,
            recyclerViewResId = R.id.recyclerView,
            emptyViewResId = R.id.emptyPandaView,
            emptyViewText = getString(R.string.no_items_to_display_short)
        )
        addSwipeToRefresh(swipeRefreshLayout)
    }

    override fun onReadySetGo(presenter: DueDatesPresenter) {
        dueDateRecyclerView.adapter = adapter
        presenter.loadData(false)
    }

    override fun onRefreshStarted() = emptyPandaView.setLoading()

    override fun onRefreshFinished() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, dueDateRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    override fun createAdapter(): DueDatesAdapter {
        return DueDatesAdapter(requireContext(), presenter)
    }

    companion object {
        @JvmStatic val ASSIGNMENT = "assignment"

        fun getInstance(course: Course, args: Bundle) = DueDatesFragment().withArgs(args).apply {
            mCourse = course
        }

        fun makeBundle(assignment: Assignment): Bundle {
            val args = Bundle()
            args.putParcelable(DueDatesFragment.ASSIGNMENT, assignment)
            return args
        }
    }
}
