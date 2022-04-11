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
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASSIGNMENT_SUBMISSION_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.adapters.GradeableStudentSubmissionAdapter
import com.instructure.teacher.dialog.FilterSubmissionByPointsDialog
import com.instructure.teacher.dialog.PeopleListFilterDialog
import com.instructure.teacher.dialog.RadioButtonDialog
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.events.SubmissionCommentsUpdated
import com.instructure.teacher.events.SubmissionFilterChangedEvent
import com.instructure.teacher.factory.AssignmentSubmissionListPresenterFactory
import com.instructure.teacher.features.postpolicies.ui.PostPolicyFragment
import com.instructure.teacher.holders.GradeableStudentSubmissionViewHolder
import com.instructure.teacher.presenters.AssignmentSubmissionListPresenter
import com.instructure.teacher.presenters.AssignmentSubmissionListPresenter.SubmissionListFilter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import com.instructure.teacher.viewinterface.AssignmentSubmissionListView
import kotlinx.android.synthetic.main.fragment_assignment_submission_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_ASSIGNMENT_SUBMISSION_LIST)
class AssignmentSubmissionListFragment : BaseSyncFragment<
        GradeableStudentSubmission,
        AssignmentSubmissionListPresenter,
        AssignmentSubmissionListView,
        GradeableStudentSubmissionViewHolder,
        GradeableStudentSubmissionAdapter>(), AssignmentSubmissionListView {

    private var mAssignment: Assignment by ParcelableArg(Assignment(), ASSIGNMENT)
    private var mCourse: Course by ParcelableArg(Course())
    private lateinit var mRecyclerView: RecyclerView
    private val mCourseColor by lazy { ColorKeeper.getOrGenerateColor(mCourse) }
    private var mFilter by SerializableArg(SubmissionListFilter.ALL, FILTER_TYPE)
    private var mCanvasContextsSelected = ArrayList<CanvasContext>()

    private var mNeedToForceNetwork = false

    private val mSubmissionFilters: Map<Int, String> by lazy {
        sortedMapOf(
                Pair(SubmissionListFilter.ALL.ordinal, getString(R.string.all_submissions)),
                Pair(SubmissionListFilter.LATE.ordinal, getString(R.string.submitted_late)),
                Pair(SubmissionListFilter.MISSING.ordinal, getString(R.string.not_submitted)),
                Pair(SubmissionListFilter.NOT_GRADED.ordinal, getString(R.string.not_graded)),
                Pair(SubmissionListFilter.BELOW_VALUE.ordinal, getString(R.string.scored_less_than)),
                Pair(SubmissionListFilter.ABOVE_VALUE.ordinal, getString(R.string.scored_more_than))

        )
    }

    override fun layoutResId(): Int = R.layout.fragment_assignment_submission_list
    override val recyclerView: RecyclerView get() = submissionsRecyclerView
    override fun getPresenterFactory() = AssignmentSubmissionListPresenterFactory(mAssignment, mFilter)
    override fun onCreateView(view: View) = Unit
    override fun onPresenterPrepared(presenter: AssignmentSubmissionListPresenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(rootView, requireContext(), adapter, presenter, R.id.swipeRefreshLayout,
                R.id.submissionsRecyclerView, R.id.emptyPandaView, getString(R.string.no_items_to_display_short))
        mRecyclerView.setHeaderVisibilityListener(divider)

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && addMessage.visibility == View.VISIBLE) {
                    addMessage.hide()
                } else if (dy < 0 && addMessage.visibility != View.VISIBLE) {
                    addMessage.show()
                }
            }
        })
    }

    override fun onReadySetGo(presenter: AssignmentSubmissionListPresenter) {
        if(mRecyclerView.adapter == null) {
            mRecyclerView.adapter = adapter
        }

        presenter.refresh(mNeedToForceNetwork)
        mNeedToForceNetwork = false

        updateFilterTitle()
        clearFilterTextView.setTextColor(ThemePrefs.buttonColor)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun createAdapter(): GradeableStudentSubmissionAdapter {
        return GradeableStudentSubmissionAdapter(mAssignment, mCourse.id, requireContext(), presenter) { gradeableStudentSubmission ->
            withRequireNetwork {
                val filteredSubmissions = (0 until presenter.data.size()).map { presenter.data[it] }
                val selectedIdx = filteredSubmissions.indexOf(gradeableStudentSubmission)
                val bundle = SpeedGraderActivity.makeBundle(mCourse.id, mAssignment.id, filteredSubmissions, selectedIdx, mAssignment.anonymousGrading)
                RouteMatcher.route(requireContext(), Route(bundle, RouteContext.SPEED_GRADER))
            }
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

        // Theme the toolbar again since visibilities may have changed
        ViewStyler.themeToolbar(requireActivity(), assignmentSubmissionListToolbar, mCourseColor, Color.WHITE)

        updateStatuses() // Muted is now also set by not being in the new gradebook
    }

    override fun checkIfEmpty() {
        // We don't want to leave the fab hidden if the list is empty
        if(presenter.isEmpty) {
            addMessage.show()
        }
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    private fun setupToolbar() {
        //setup toolbar icon to access this menu
        assignmentSubmissionListToolbar.setupMenu(R.menu.menu_filter_submissions, menuItemCallback)
        assignmentSubmissionListToolbar.setupBackButtonAsBackPressedOnly(this)

        if(isTablet) {
            assignmentSubmissionListToolbar.title = mAssignment.name
        } else {
            assignmentSubmissionListToolbar.setNavigationIcon(R.drawable.ic_back_arrow)
            assignmentSubmissionListToolbar.title = getString(R.string.submissions)
            assignmentSubmissionListToolbar.subtitle = mCourse.name
        }
        ViewStyler.themeToolbar(requireActivity(), assignmentSubmissionListToolbar, mCourseColor, Color.WHITE)
        ViewStyler.themeFAB(addMessage, ThemePrefs.buttonColor)
    }

    private fun setupListeners() {
        clearFilterTextView.setOnClickListener {
            presenter.setFilter(SubmissionListFilter.ALL)
            presenter.clearFilterList()
            filterTitle.setText(R.string.all_submissions)
            clearFilterTextView.setGone()
        }

        addMessage.setOnClickListener {
            val args = AddMessageFragment.createBundle(presenter.getRecipients(), filterTitle.text.toString() + " " + getString(R.string.on) + " " + mAssignment.name, mCourse.contextId, false)
            RouteMatcher.route(requireContext(), Route(AddMessageFragment::class.java, null, args))
        }
    }
    override fun onResume() {
        super.onResume()
        setupToolbar()
        setupListeners()
    }

    private fun updateFilterTitle() {
        clearFilterTextView.setVisible()
        when (presenter.getFilter()) {
            SubmissionListFilter.ALL -> {
                filterTitle.setText(R.string.all_submissions)
                clearFilterTextView.setGone()
            }
            SubmissionListFilter.LATE -> filterTitle.setText(R.string.submitted_late)
            SubmissionListFilter.MISSING -> filterTitle.setText(R.string.havent_submitted_yet)
            SubmissionListFilter.NOT_GRADED -> filterTitle.setText(R.string.havent_been_graded)
            SubmissionListFilter.GRADED -> filterTitle.setText(R.string.graded)
            SubmissionListFilter.BELOW_VALUE -> {
                filterTitle.text = requireActivity().resources.getString(
                        R.string.scored_less_than_value,
                        NumberHelper.formatDecimal(presenter.getFilterPoints(), 2, true)
                )
            }
            SubmissionListFilter.ABOVE_VALUE -> {
                filterTitle.text = requireActivity().resources.getString(
                        R.string.scored_more_than_value,
                        NumberHelper.formatDecimal(presenter.getFilterPoints(), 2, true)
                )
            }
        }

        filterTitle.text = filterTitle.text.toString().plus(presenter.getSectionFilterText())
    }

    private fun setFilter(filterIndex: Int = -1, canvasContexts: ArrayList<CanvasContext>? = null) {
        canvasContexts?.let {
            mCanvasContextsSelected = ArrayList()
            mCanvasContextsSelected.addAll(canvasContexts)

            presenter.setSections(canvasContexts)

            updateFilterTitle()

            filterTitle.text = filterTitle.text.toString().plus(presenter.getSectionFilterText())
            clearFilterTextView.setVisible()
            return
        }

        when(filterIndex) {
            SubmissionListFilter.ALL.ordinal -> {
                presenter.setFilter(SubmissionListFilter.ALL)
                updateFilterTitle()
            }
            SubmissionListFilter.LATE.ordinal -> {
                presenter.setFilter(SubmissionListFilter.LATE)
                updateFilterTitle()
            }
            SubmissionListFilter.MISSING.ordinal -> {
                presenter.setFilter(SubmissionListFilter.MISSING)
                updateFilterTitle()
            }
            SubmissionListFilter.NOT_GRADED.ordinal -> {
                presenter.setFilter(SubmissionListFilter.NOT_GRADED)
                updateFilterTitle()
            }
            SubmissionListFilter.BELOW_VALUE.ordinal -> {
                FilterSubmissionByPointsDialog.getInstance(requireFragmentManager(), getString(R.string.scored_less_than), mAssignment.pointsPossible) { points ->
                    presenter.setFilter(SubmissionListFilter.BELOW_VALUE, points)
                    updateFilterTitle()
                }.show(requireActivity().supportFragmentManager, FilterSubmissionByPointsDialog::class.java.simpleName)
            }
            SubmissionListFilter.ABOVE_VALUE.ordinal -> {
                FilterSubmissionByPointsDialog.getInstance(requireFragmentManager(), getString(R.string.scored_more_than), mAssignment.pointsPossible) { points ->
                    presenter.setFilter(SubmissionListFilter.ABOVE_VALUE, points)
                    updateFilterTitle()
                }.show(requireActivity().supportFragmentManager, FilterSubmissionByPointsDialog::class.java.simpleName)
            }
        }
    }

    val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.filterSubmissions -> {
                val (keys, values) = mSubmissionFilters.toList().unzip()
                val dialog = RadioButtonDialog.getInstance(requireActivity().supportFragmentManager, getString(R.string.filter_submissions), values as ArrayList<String>, keys.indexOf(presenter.getFilter().ordinal)) { idx ->
                    EventBus.getDefault().post(SubmissionFilterChangedEvent(keys[idx]))
                }
                dialog.show(requireActivity().supportFragmentManager, RadioButtonDialog::class.java.simpleName)

            }
            R.id.filterBySection -> {
                //let the user select the course/group they want to see
                PeopleListFilterDialog.getInstance(requireActivity().supportFragmentManager, presenter.getSectionListIds(), mCourse, false) { canvasContexts ->
                    EventBus.getDefault().post(SubmissionFilterChangedEvent(canvasContext = canvasContexts))
                }.show(requireActivity().supportFragmentManager, PeopleListFilterDialog::class.java.simpleName)
            }
            R.id.menuPostPolicies -> {
                RouteMatcher.route(requireContext(), PostPolicyFragment.makeRoute(mCourse, mAssignment))
            }
        }
    }

    private fun updateStatuses() {
        if (presenter.mAssignment.anonymousGrading)
            anonGradingStatusView.setVisible().text = getString(R.string.anonymousGradingLabel)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentGraded(event: AssignmentGradedEvent) {
        event.once(javaClass.simpleName) {
            //force network call on resume
            if(presenter.mAssignment.id == it) mNeedToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizGraded(event: QuizSubmissionGradedEvent) {
        event.once(javaClass.simpleName) {
            // Force network call on resume
            if (presenter.mAssignment.id == it.assignmentId) mNeedToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onSubmissionCommentUpdated(event: SubmissionCommentsUpdated) {
        event.once(AssignmentSubmissionListFragment::class.java.simpleName) {
            mNeedToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSubmissionFilterChanged(event: SubmissionFilterChangedEvent) {
        setFilter(event.filterIndex, event.canvasContext)
    }

    companion object {
        private val ASSIGNMENT = "assignment"
        @JvmStatic val FILTER_TYPE = "filter_type"

        fun newInstance(course: Course, args: Bundle) = AssignmentSubmissionListFragment().withArgs(args).apply {
            mCourse = course
        }

        fun makeBundle(assignment: Assignment): Bundle {
            return makeBundle(assignment, SubmissionListFilter.ALL)
        }

        fun makeBundle(assignment: Assignment, filter: SubmissionListFilter): Bundle {
            val args = Bundle()
            args.putSerializable(FILTER_TYPE, filter)
            args.putParcelable(ASSIGNMENT, assignment)
            return args
        }
    }
}
