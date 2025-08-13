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
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.ToDo
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.analytics.SCREEN_VIEW_TO_DO
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.speedgrader.SpeedGraderFragment
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.requestAccessibilityFocus
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.activities.InitActivity
import com.instructure.teacher.adapters.ToDoAdapter
import com.instructure.teacher.databinding.FragmentTodoBinding
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.factory.ToDoPresenterFactory
import com.instructure.teacher.holders.ToDoViewHolder
import com.instructure.teacher.interfaces.AdapterToFragmentCallback
import com.instructure.teacher.presenters.ToDoPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setupBackButtonAsBackPressedOnly
import com.instructure.teacher.viewinterface.ToDoView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@PageView
@ScreenView(SCREEN_VIEW_TO_DO)
class ToDoFragment : BaseSyncFragment<ToDo, ToDoPresenter, ToDoView, ToDoViewHolder, ToDoAdapter>(), ToDoView {

    private val binding by viewBinding(FragmentTodoBinding::bind)

    private var mNeedToForceNetwork = false

    override fun layoutResId(): Int = R.layout.fragment_todo
    override fun withPagination() = true
    override val recyclerView: RecyclerView get() = binding.toDoRecyclerView
    override fun checkIfEmpty() {
        binding.emptyPandaView.setMessageText(R.string.noTodosSubtext)
        RecyclerViewUtils.checkIfEmpty(binding.emptyPandaView, recyclerView, binding.swipeRefreshLayout, adapter, presenter.isEmpty)
    }
    override fun getPresenterFactory() = ToDoPresenterFactory()
    override fun onCreateView(view: View) {}

    override fun onPresenterPrepared(presenter: ToDoPresenter) = with(binding) {
        RecyclerViewUtils.buildRecyclerView(rootView, requireContext(), adapter,
                presenter, R.id.swipeRefreshLayout, R.id.toDoRecyclerView, R.id.emptyPandaView, getString(R.string.noTodos))
        emptyPandaView.setEmptyViewImage(requireContext().getDrawableCompat(R.drawable.ic_panda_sleeping))
        emptyPandaView.setMessageText(R.string.noTodosSubtext)
        addSwipeToRefresh(swipeRefreshLayout)
        addPagination()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    //pagination
    override fun hitRockBottom() {
        presenter.nextPage()
    }

    override fun onReadySetGo(presenter: ToDoPresenter) {
        if(recyclerView.adapter == null) {
            recyclerView.adapter = adapter
        }

        presenter.loadData(mNeedToForceNetwork)
        mNeedToForceNetwork = false

        setupToolbar()
    }

    private fun setupToolbar() = with(binding) {
        val activity = requireActivity()
        if (activity is InitActivity) {
            activity.attachNavigationDrawer()
            activity.attachToolbar(toDoToolbar)
        } else {
            toDoToolbar.setupBackButtonAsBackPressedOnly(this@ToDoFragment)
        }

        ViewStyler.themeToolbarColored(requireActivity(), toDoToolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        toDoToolbar.requestAccessibilityFocus()
    }

    public override fun createAdapter(): ToDoAdapter {
        return ToDoAdapter(requireActivity(), presenter, mAdapterCallback)
    }

    private val mAdapterCallback = object : AdapterToFragmentCallback<ToDo> {
        override fun onRowClicked(model: ToDo, position: Int) {
            // if the layout is refreshing we don't want them to select a different item
            if (binding.swipeRefreshLayout.isRefreshing) return

            if (model.assignment == null) {
                toast(R.string.errorOccurred)
                return
            }
            presenter.goToUngradedSubmissions(model.assignment!!, model.canvasContext!!.id)
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
        binding.emptyPandaView.visibility = View.GONE
    }

    override fun perPageCount(): Int = ApiPrefs.perPageCount

    override fun onRouteSuccessfully(course: Course, assignment: Assignment, submissions: List<GradeableStudentSubmission>) {
        if (submissions.isEmpty()) {
            showToast(R.string.toDoNoSubmissions)
            return
        }
        val submissionIds = submissions.map { if (RemoteConfigUtils.getBoolean(RemoteConfigParam.SPEEDGRADER_V2)) it.assigneeId else it.id }.toLongArray()
        val anonymousGrading = assignment.anonymousGrading || assignment.anonymousSubmissions
        val bundle = SpeedGraderFragment.makeBundle(courseId = course.id, assignmentId = assignment.id, filteredSubmissionIds = submissionIds, anonymousGrading = anonymousGrading, selectedIdx = 0)
        RouteMatcher.route(requireActivity(), Route(bundle, RouteContext.SPEED_GRADER))
    }

    override fun onRouteFailed() {
        toast(R.string.errorToDoRouteFailed)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentGraded(event: AssignmentGradedEvent) {
        event.once(javaClass.simpleName) {
            // force network call on resume
            mNeedToForceNetwork = true
        }
    }

    companion object {
        fun newInstance() = ToDoFragment()
    }
}
