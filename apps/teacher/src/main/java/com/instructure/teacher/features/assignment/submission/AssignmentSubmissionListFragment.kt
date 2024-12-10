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
package com.instructure.teacher.features.assignment.submission

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
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDefaultValues
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDisabledFields
import com.instructure.pandautils.fragments.BaseSyncFragment
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.SerializableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.withArgs
import com.instructure.teacher.R
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.adapters.GradeableStudentSubmissionAdapter
import com.instructure.teacher.databinding.FragmentAssignmentSubmissionListBinding
import com.instructure.teacher.dialog.FilterSubmissionByPointsDialog
import com.instructure.teacher.dialog.PeopleListFilterDialog
import com.instructure.teacher.dialog.RadioButtonDialog
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.events.SubmissionCommentsUpdated
import com.instructure.teacher.events.SubmissionFilterChangedEvent
import com.instructure.teacher.factory.AssignmentSubmissionListPresenterFactory
import com.instructure.teacher.features.postpolicies.ui.PostPolicyFragment
import com.instructure.teacher.holders.GradeableStudentSubmissionViewHolder
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.RecyclerViewUtils
import com.instructure.teacher.utils.setHeaderVisibilityListener
import com.instructure.teacher.utils.setupBackButtonAsBackPressedOnly
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.withRequireNetwork
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import com.instructure.teacher.viewinterface.AssignmentSubmissionListView
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_ASSIGNMENT_SUBMISSION_LIST)
@AndroidEntryPoint
class AssignmentSubmissionListFragment : BaseSyncFragment<
        GradeableStudentSubmission,
        AssignmentSubmissionListPresenter,
        AssignmentSubmissionListView,
        GradeableStudentSubmissionViewHolder,
        GradeableStudentSubmissionAdapter>(), AssignmentSubmissionListView {

    @Inject
    lateinit var assignmentSubmissionRepository: AssignmentSubmissionRepository

    private val binding by viewBinding(FragmentAssignmentSubmissionListBinding::bind)

    private var assignment: Assignment by ParcelableArg(Assignment(), ASSIGNMENT)
    private var course: Course by ParcelableArg(Course())
    private lateinit var mRecyclerView: RecyclerView
    private var filter by SerializableArg(SubmissionListFilter.ALL, FILTER_TYPE)
    private var canvasContextsSelected = ArrayList<CanvasContext>()

    private var needToForceNetwork = false

    private val submissionFilters: Map<Int, String> by lazy {
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
    override val recyclerView: RecyclerView get() = binding.submissionsRecyclerView
    override fun getPresenterFactory() = AssignmentSubmissionListPresenterFactory(assignment, filter, assignmentSubmissionRepository)
    override fun onCreateView(view: View) = Unit
    override fun onPresenterPrepared(presenter: AssignmentSubmissionListPresenter) = with(binding) {
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

        presenter.refresh(needToForceNetwork)
        needToForceNetwork = false

        updateFilterTitle()
        binding.clearFilterTextView.setTextColor(ThemePrefs.textButtonColor)
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
        return GradeableStudentSubmissionAdapter(assignment, course.id, requireContext(), presenter) { gradeableStudentSubmission ->
            withRequireNetwork {
                val filteredSubmissions = (0 until presenter.data.size()).map { presenter.data[it] }
                val selectedIdx = filteredSubmissions.indexOf(gradeableStudentSubmission)
                val bundle = SpeedGraderActivity.makeBundle(
                    courseId = course.id,
                    assignmentId = assignment.id,
                    selectedIdx = selectedIdx,
                    anonymousGrading = assignment.anonymousGrading,
                    filter = presenter.getFilter(),
                    filterValue = presenter.getFilterPoints(),
                    filteredSubmissionIds = filteredSubmissions.map { it.id }.toLongArray(),
                )
                RouteMatcher.route(requireActivity(), Route(bundle, RouteContext.SPEED_GRADER))
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

    override fun onRefreshFinished() = with(binding) {
        swipeRefreshLayout.isRefreshing = false

        // Theme the toolbar again since visibilities may have changed
        ViewStyler.themeToolbarColored(requireActivity(), assignmentSubmissionListToolbar, course.color, requireContext().getColor(R.color.textLightest))

        updateStatuses() // Muted is now also set by not being in the new gradebook
    }

    override fun checkIfEmpty() = with(binding) {
        // We don't want to leave the fab hidden if the list is empty
        if(presenter.isEmpty) {
            addMessage.show()
        }
        RecyclerViewUtils.checkIfEmpty(emptyPandaView, mRecyclerView, swipeRefreshLayout, adapter, presenter.isEmpty)
    }

    private fun setupToolbar() = with(binding) {
        //setup toolbar icon to access this menu
        assignmentSubmissionListToolbar.setupMenu(R.menu.menu_filter_submissions, menuItemCallback)
        assignmentSubmissionListToolbar.setupBackButtonAsBackPressedOnly(this@AssignmentSubmissionListFragment)

        if(isTablet) {
            assignmentSubmissionListToolbar.title = assignment.name
        } else {
            assignmentSubmissionListToolbar.setNavigationIcon(R.drawable.ic_back_arrow)
            assignmentSubmissionListToolbar.title = getString(R.string.submissions)
            assignmentSubmissionListToolbar.subtitle = course.name
        }
        ViewStyler.themeToolbarColored(requireActivity(), assignmentSubmissionListToolbar, course.color, requireContext().getColor(R.color.textLightest))
        ViewStyler.themeFAB(addMessage)
    }

    private fun setupListeners() = with(binding) {
        clearFilterTextView.setOnClickListener {
            presenter.clearFilterList()
            presenter.setFilter(SubmissionListFilter.ALL)
            filterTitle.setText(R.string.all_submissions)
            clearFilterTextView.setGone()
        }

        addMessage.setOnClickListener {
            val options = InboxComposeOptions.buildNewMessage().copy(
                defaultValues = InboxComposeOptionsDefaultValues(
                    contextCode = course.contextId,
                    contextName = course.name,
                    recipients = presenter.getRecipients(),
                    subject = filterTitle.text.toString() + " " + getString(R.string.on) + " " + assignment.name
                ),
                disabledFields = InboxComposeOptionsDisabledFields(
                    isContextDisabled = true,
                    isSubjectDisabled = true
                )
            )
            val route = InboxComposeFragment.makeRoute(options)
            RouteMatcher.route(requireActivity(), route)
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
        setupListeners()
    }

    private fun updateFilterTitle() = with(binding) {
        clearFilterTextView.setVisible()
        when (presenter.getFilter()) {
            SubmissionListFilter.ALL -> {
                filterTitle.setText(R.string.all_submissions)
                if (presenter.getSectionFilterText().isEmpty()) {
                    clearFilterTextView.setGone()
                }
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

    private fun setFilter(filterIndex: Int = -1, canvasContexts: ArrayList<CanvasContext>? = null) = with(binding) {
        canvasContexts?.let {
            canvasContextsSelected = ArrayList()
            canvasContextsSelected.addAll(canvasContexts)

            presenter.setSections(canvasContexts)

            updateFilterTitle()
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
                FilterSubmissionByPointsDialog.getInstance(requireFragmentManager(), getString(R.string.scored_less_than), assignment.pointsPossible) { points ->
                    presenter.setFilter(SubmissionListFilter.BELOW_VALUE, points)
                    updateFilterTitle()
                }.show(requireActivity().supportFragmentManager, FilterSubmissionByPointsDialog::class.java.simpleName)
            }
            SubmissionListFilter.ABOVE_VALUE.ordinal -> {
                FilterSubmissionByPointsDialog.getInstance(requireFragmentManager(), getString(R.string.scored_more_than), assignment.pointsPossible) { points ->
                    presenter.setFilter(SubmissionListFilter.ABOVE_VALUE, points)
                    updateFilterTitle()
                }.show(requireActivity().supportFragmentManager, FilterSubmissionByPointsDialog::class.java.simpleName)
            }
        }
    }

    val menuItemCallback: (MenuItem) -> Unit = { item ->
        when (item.itemId) {
            R.id.filterSubmissions -> {
                val (keys, values) = submissionFilters.toList().unzip()
                val dialog = RadioButtonDialog.getInstance(requireActivity().supportFragmentManager, getString(R.string.filter_submissions), values as ArrayList<String>, keys.indexOf(presenter.getFilter().ordinal)) { idx ->
                    EventBus.getDefault().post(SubmissionFilterChangedEvent(keys[idx]))
                }
                dialog.show(requireActivity().supportFragmentManager, RadioButtonDialog::class.java.simpleName)

            }
            R.id.filterBySection -> {
                //let the user select the course/group they want to see
                PeopleListFilterDialog.getInstance(requireActivity().supportFragmentManager, presenter.getSectionListIds(), course, false) { canvasContexts ->
                    EventBus.getDefault().post(SubmissionFilterChangedEvent(canvasContext = canvasContexts))
                }.show(requireActivity().supportFragmentManager, PeopleListFilterDialog::class.java.simpleName)
            }
            R.id.menuPostPolicies -> {
                RouteMatcher.route(requireActivity(), PostPolicyFragment.makeRoute(course, assignment))
            }
        }
    }

    private fun updateStatuses() {
        if (presenter.assignment.anonymousGrading)
            binding.anonGradingStatusView.setVisible().text = getString(R.string.anonymousGradingLabel)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentGraded(event: AssignmentGradedEvent) {
        event.once(javaClass.simpleName) {
            //force network call on resume
            if(presenter.assignment.id == it) needToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizGraded(event: QuizSubmissionGradedEvent) {
        event.once(javaClass.simpleName) {
            // Force network call on resume
            if (presenter.assignment.id == it.assignmentId) needToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onSubmissionCommentUpdated(event: SubmissionCommentsUpdated) {
        event.once(AssignmentSubmissionListFragment::class.java.simpleName) {
            needToForceNetwork = true
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
            this.course = course
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
