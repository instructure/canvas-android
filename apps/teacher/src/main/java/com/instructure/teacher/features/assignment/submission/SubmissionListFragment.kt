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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASSIGNMENT_SUBMISSION_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDefaultValues
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDisabledFields
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.withArgs
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.adapters.StudentContextFragment
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.events.SubmissionCommentsUpdated
import com.instructure.teacher.events.SubmissionFilterChangedEvent
import com.instructure.teacher.features.postpolicies.ui.PostPolicyFragment
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_ASSIGNMENT_SUBMISSION_LIST)
@AndroidEntryPoint
class SubmissionListFragment : BaseCanvasFragment() {

    private val viewModel: SubmissionListViewModel by viewModels()
    private var course: Course by ParcelableArg(Course())

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                CanvasTheme {
                    SubmissionListScreen(uiState) {
                        requireActivity().onBackPressed()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.collectOneOffEvents(viewModel.events, this::handleActions)
    }

    private fun handleActions(action: SubmissionListViewModelAction) {
        when (action) {
            is SubmissionListViewModelAction.RouteToSubmission -> {
                val bundle = SpeedGraderActivity.makeBundle(
                    action.courseId,
                    action.assignmentId,
                    action.selectedIdx,
                    action.anonymousGrading,
                    action.filteredSubmissionIds,
                    action.filter,
                    action.filterValue
                )
                RouteMatcher.route(requireActivity(), Route(bundle, RouteContext.SPEED_GRADER))
            }

            is SubmissionListViewModelAction.ShowPostPolicy -> {
                RouteMatcher.route(
                    requireActivity(),
                    PostPolicyFragment.makeRoute(action.course, action.assignment)
                )
            }

            is SubmissionListViewModelAction.SendMessage -> {
                val options = InboxComposeOptions.buildNewMessage().copy(
                    defaultValues = InboxComposeOptionsDefaultValues(
                        contextCode = action.contextCode,
                        contextName = action.contextName,
                        recipients = action.recipients,
                        subject = action.subject
                    ),
                    disabledFields = InboxComposeOptionsDisabledFields(
                        isContextDisabled = true,
                        isSubjectDisabled = true
                    )
                )
                val route = InboxComposeFragment.makeRoute(options)
                RouteMatcher.route(requireActivity(), route)
            }

            is SubmissionListViewModelAction.RouteToUser -> {
                val bundle = StudentContextFragment.makeBundle(action.userId, action.courseId)
                RouteMatcher.route(requireActivity(), Route(StudentContextFragment::class.java, null, bundle))
            }
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentGraded(event: AssignmentGradedEvent) {
        viewModel.uiState.value.actionHandler(SubmissionListAction.Refresh)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizGraded(event: QuizSubmissionGradedEvent) {
        viewModel.uiState.value.actionHandler(SubmissionListAction.Refresh)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onSubmissionCommentUpdated(event: SubmissionCommentsUpdated) {
        viewModel.uiState.value.actionHandler(SubmissionListAction.Refresh)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSubmissionFilterChanged(event: SubmissionFilterChangedEvent) {
        viewModel.uiState.value.actionHandler(SubmissionListAction.Refresh)
    }

    companion object {
        const val ASSIGNMENT = "assignment"
        const val FILTER_TYPE = "filter_type"
        const val COURSE = "course"

        fun newInstance(course: Course, args: Bundle): SubmissionListFragment {
            args.putParcelable(COURSE, course)
            return SubmissionListFragment().withArgs(args).apply {
                this.course = course
            }
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
