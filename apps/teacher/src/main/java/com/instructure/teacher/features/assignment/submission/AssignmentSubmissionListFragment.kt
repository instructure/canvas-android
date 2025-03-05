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
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.withArgs
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.events.SubmissionCommentsUpdated
import com.instructure.teacher.events.SubmissionFilterChangedEvent
import com.instructure.teacher.features.submission.SubmissionListScreen
import com.instructure.teacher.features.submission.SubmissionListViewModel
import com.instructure.teacher.features.submission.SubmissionListViewModelAction
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@ScreenView(SCREEN_VIEW_ASSIGNMENT_SUBMISSION_LIST)
@AndroidEntryPoint
class AssignmentSubmissionListFragment : BaseCanvasFragment() {

    private val viewModel: SubmissionListViewModel by viewModels()
    private var course: Course by ParcelableArg(Course())

    private var needToForceNetwork = false


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
        }
    }

    private fun setupListeners() {
//TODO: Implement this
//        addMessage.setOnClickListener {
//            val options = InboxComposeOptions.buildNewMessage().copy(
//                defaultValues = InboxComposeOptionsDefaultValues(
//                    contextCode = course.contextId,
//                    contextName = course.name,
//                    recipients = presenter.getRecipients(),
//                    subject = filterTitle.text.toString() + " " + getString(R.string.on) + " " + assignment.name
//                ),
//                disabledFields = InboxComposeOptionsDisabledFields(
//                    isContextDisabled = true,
//                    isSubjectDisabled = true
//                )
//            )
//            val route = InboxComposeFragment.makeRoute(options)
//            RouteMatcher.route(requireActivity(), route)
//        }
    }

    override fun onResume() {
        super.onResume()
        setupListeners()
    }


    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentGraded(event: AssignmentGradedEvent) {

    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizGraded(event: QuizSubmissionGradedEvent) {

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
    }

    companion object {
        private val ASSIGNMENT = "assignment"
        @JvmStatic val FILTER_TYPE = "filter_type"

        fun newInstance(course: Course, args: Bundle): AssignmentSubmissionListFragment {
            args.putParcelable("course", course)
            return AssignmentSubmissionListFragment().withArgs(args).apply {
                this.course = course
            }
        }

        fun makeBundle(assignment: Assignment): Bundle {
            return makeBundle(assignment, SubmissionListFilter.ALL)
        }

        fun makeBundle(assignment: Assignment, filter: SubmissionListFilter): Bundle {
            val args = Bundle()
            args.putSerializable("filter", filter)
            args.putParcelable("assignment", assignment)
            return args
        }
    }
}
