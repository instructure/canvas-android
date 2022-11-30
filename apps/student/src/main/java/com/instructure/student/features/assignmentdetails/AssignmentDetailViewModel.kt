/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.student.features.assignmentdetails

import android.content.res.Resources
import androidx.lifecycle.*
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.utils.*
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.BR
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.orDefault
import com.instructure.student.R
import com.instructure.student.features.assignmentdetails.gradecellview.GradeCellViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseManager: CourseManager,
    private val assignmentManager: AssignmentManager,
    private val quizManager: QuizManager,
    private val submissionManager: SubmissionManager,
    private val resources: Resources
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<AssignmentDetailViewData>
        get() = _data
    private val _data = MutableLiveData<AssignmentDetailViewData>()

    val events: LiveData<Event<AssignmentDetailAction>>
        get() = _events
    private val _events = MutableLiveData<Event<AssignmentDetailAction>>()

    val course = savedStateHandle.get<Course>(Const.CANVAS_CONTEXT)
    private val assignmentId = savedStateHandle.get<Long>(Const.ASSIGNMENT_ID).orDefault()

    var bookmarker = Bookmarker(true, course).withParam(RouterParams.ASSIGNMENT_ID, assignmentId.toString())

    private var isObserver: Boolean = false
    private var quizResult: Quiz? = null

    init {
        loadData()
    }

    private fun loadData(forceNetwork: Boolean = true) {
        _state.postValue(ViewState.Loading)
        viewModelScope.launch {
            try {
                val courseResult = courseManager.getCourseWithGradeAsync(course?.id.orDefault(), true).await().dataOrThrow

                isObserver = courseResult.enrollments?.firstOrNull { it.isObserver } != null

                val assignmentResult = if (isObserver) {
                    assignmentManager.getAssignmentIncludeObserveesAsync(assignmentId, course?.id.orDefault(), forceNetwork)
                } else {
                    assignmentManager.getAssignmentAsync(assignmentId, course?.id.orDefault(), forceNetwork)
                }.await().dataOrThrow as Assignment

                quizResult = if (assignmentResult.turnInType == Assignment.TurnInType.QUIZ && assignmentResult.quizId != 0L) {
                    quizManager.getQuizAsync(course?.id.orDefault(), assignmentResult.quizId, forceNetwork).await().dataOrThrow
                } else null

                val ltiToolId = assignmentResult.externalToolAttributes?.contentId.orDefault()
                val ltiToolResult = if (ltiToolId != 0L) {
                    assignmentManager.getExternalToolLaunchUrlAsync(course?.id.orDefault(), ltiToolId, assignmentId).await().dataOrThrow
                } else {
                    if (!assignmentResult.url.isNullOrEmpty() && assignmentResult.getSubmissionTypes().contains(SubmissionType.EXTERNAL_TOOL)) {
                        submissionManager.getLtiFromAuthenticationUrlAsync(assignmentResult.url.orEmpty(), forceNetwork).await().dataOrThrow
                    } else {
                        null
                    }
                }?.apply {
                    assignmentId = assignmentResult.id
                    courseId = assignmentResult.courseId
                }

                bookmarker = bookmarker.copy(url = assignmentResult.htmlUrl)

                dataLoaded(assignmentResult, ltiToolResult)
            } catch (ex: Exception) {
                _state.postValue(ViewState.Error())
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun dataLoaded(assignment: Assignment, ltiTool: LTITool?) {
        val points = resources.getQuantityString(
            R.plurals.quantityPointsAbbreviated,
            assignment.pointsPossible.toInt(),
            NumberHelper.formatDecimal(assignment.pointsPossible, 1, true)
        )

        val assignmentState = AssignmentUtils2.getAssignmentState(assignment, assignment.submission, false)

        val submittedLabelText = resources.getString(
            if (assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_GRADED) {
                R.string.gradedSubmissionLabel
            } else {
                R.string.submitted
            }
        )

        // Don't mark LTI assignments as missing when overdue as they usually won't have a real submission for it
        val isMissingFromDueDate = assignment.turnInType != Assignment.TurnInType.EXTERNAL_TOOL
                && assignment.dueAt != null
                && assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_MISSING

        val submissionStatusTint = resources.getColor(
            if (assignment.isSubmitted) {
                R.color.backgroundSuccess
            } else if (assignment.submission?.missing.orDefault() || isMissingFromDueDate) {
                R.color.backgroundDanger
            } else {
                R.color.backgroundDark
            }
        )

        val submissionHistory = assignment.submission?.submissionHistory
        val attempts = submissionHistory?.reversed()?.mapIndexedNotNull { index, submission ->
            submission?.submittedAt?.let { getFormattedDate(it) }?.let {
                AssignmentDetailAttemptItemViewModel(
                    AssignmentDetailAttemptViewData(
                        resources.getString(R.string.attempt, submissionHistory.size - index),
                        it,
                        submission
                    )
                )
            }
        }.orEmpty()

        val submissionTypes = assignment.getSubmissionTypes()
            .map { Assignment.submissionTypeToPrettyPrintString(it, resources) }
            .joinToString()

        val allowedFileTypes = assignment.allowedExtensions.joinToString().takeIf {
            assignment.getSubmissionTypes().contains(SubmissionType.ONLINE_UPLOAD)
        }.orEmpty()

        val due = assignment.dueDate?.let { getFormattedDate(it) } ?: resources.getString(R.string.toDoNoDueDate)

        val submitEnabled = assignment.allowedAttempts == -1L || (assignment.submission?.attempt?.let {
            it < assignment.allowedAttempts
        }.orDefault(true))

        val submitButtonText = resources.getString(
            when {
                !submitEnabled -> R.string.noAttemptsLeft
                assignment.turnInType == Assignment.TurnInType.QUIZ -> R.string.viewQuiz
                assignment.turnInType == Assignment.TurnInType.DISCUSSION -> R.string.viewDiscussion
                assignment.turnInType == Assignment.TurnInType.EXTERNAL_TOOL -> R.string.launchExternalTool
                assignment.isSubmitted -> R.string.resubmitAssignment
                else -> R.string.submitAssignment
            }
        )

        // Observers shouldn't see the submit button OR if the course is soft concluded
        val submitVisible = if (isObserver || !course?.isBetweenValidDateRange().orDefault()) {
            false
        } else {
            when (assignment.turnInType) {
                Assignment.TurnInType.QUIZ, Assignment.TurnInType.DISCUSSION -> true
                Assignment.TurnInType.ONLINE, Assignment.TurnInType.EXTERNAL_TOOL -> assignment.isAllowedToSubmit
                else -> false
            }
        }

        val descriptionLabel = resources.getString(
            if (assignment.turnInType == Assignment.TurnInType.QUIZ) R.string.instructions else R.string.description
        )

        val quizViewViewData = if (quizResult != null) QuizViewViewData(
            questionCount = NumberHelper.formatInt(quizResult?.questionCount),
            timeLimit = if (quizResult?.timeLimit != 0) {
                resources.getString(R.string.timeLimit)
                NumberHelper.formatInt(quizResult?.timeLimit)
            } else {
                resources.getString(R.string.quizNoTimeLimit)
            },
            allowedAttempts = if (quizResult?.allowedAttempts.orDefault() < 0) {
                resources.getString(R.string.unlimited)
            } else {
                NumberHelper.formatInt(quizResult?.allowedAttempts)
            }
        ) else null

        val attemptsViewData = if (assignment.allowedAttempts > 0) AttemptsViewData(
            assignment.allowedAttempts.toString(),
            assignment.submission?.attempt.orDefault().toString()
        ) else null

        val viewData = AssignmentDetailViewData(
            assignment,
            assignment.name.orEmpty(),
            points,
            submittedLabelText,
            if (assignment.isSubmitted) R.drawable.ic_complete_solid else R.drawable.ic_no,
            submissionStatusTint,
            submitButtonText,
            attempts,
            GradeCellViewData.fromSubmission(resources, assignment, assignment.submission),
            due,
            submissionTypes,
            allowedFileTypes,
            assignment.description.orEmpty(),
            ltiTool,
            submitEnabled,
            submitVisible,
            descriptionLabel,
            quizViewViewData,
            attemptsViewData
        )

        _data.postValue(viewData)
        _state.postValue(ViewState.Success)
    }

    private fun getFormattedDate(date: Date) = SimpleDateFormat("yyyy. MMM. dd. HH:mm", Locale.getDefault()).format(date)

    private fun postAction(action: AssignmentDetailAction) {
        _events.postValue(Event(action))
    }

    fun refresh() {
        loadData()
    }

    fun onAttemptSelected(position: Int) {
        val assignment = _data.value?.assignment
        val selectedSubmission = _data.value?.attempts?.getOrNull(position)?.data?.submission
        _data.value?.selectedGradeCellViewData = GradeCellViewData.fromSubmission(resources, assignment, selectedSubmission)
        _data.value?.notifyPropertyChanged(BR.selectedGradeCellViewData)
    }

    fun onLtiButtonPressed(url: String) {
        postAction(AssignmentDetailAction.NavigateToLtiScreen(url))
    }

    fun onGradeCellClicked() {
        Analytics.logEvent(AnalyticsEventConstants.SUBMISSION_CELL_SELECTED)
        postAction(AssignmentDetailAction.NavigateToSubmissionScreen(isObserver))
    }

    fun onSubmitButtonClicked() {
        val course = course ?: return
        val assignment = _data.value?.assignment ?: return
        val turnInType = assignment.turnInType
        val submissionTypes = assignment.getSubmissionTypes()
        val hasSingleSubmissionType = submissionTypes.size == 1
                && !(submissionTypes.contains(SubmissionType.ONLINE_UPLOAD) && assignment.isStudioEnabled)

        if (turnInType == Assignment.TurnInType.QUIZ) {
            val quiz = quizResult ?: return
            Analytics.logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_QUIZLAUNCH)
            postAction(AssignmentDetailAction.NavigateToQuizScreen(quiz))
        } else if (turnInType == Assignment.TurnInType.DISCUSSION) {
            Analytics.logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_DISCUSSIONLAUNCH)
            postAction(AssignmentDetailAction.NavigateToDiscussionScreen(assignment.discussionTopicHeader?.id.orDefault(), course))
        } else if (hasSingleSubmissionType) {
            when (submissionTypes.first()) {
                SubmissionType.ONLINE_QUIZ -> {
                    val url = APIHelper.getQuizURL(course.id, assignment.quizId)
                    postAction(AssignmentDetailAction.NavigateByUrl(url))
                }
                SubmissionType.DISCUSSION_TOPIC -> {
                    val url = DiscussionTopic.getDiscussionURL(
                        ApiPrefs.protocol,
                        ApiPrefs.domain,
                        assignment.courseId,
                        assignment.discussionTopicHeader?.id.orDefault()
                    )
                    postAction(AssignmentDetailAction.NavigateByUrl(url))
                }
                SubmissionType.ONLINE_UPLOAD -> postAction(AssignmentDetailAction.NavigateToUploadScreen(assignment))
                SubmissionType.ONLINE_TEXT_ENTRY -> postAction(AssignmentDetailAction.NavigateToTextEntryScreen(assignment.name))
                SubmissionType.ONLINE_URL -> postAction(AssignmentDetailAction.NavigateToUrlSubmissionScreen(assignment.name))
                SubmissionType.STUDENT_ANNOTATION -> postAction(AssignmentDetailAction.NavigateToAnnotationSubmissionScreen(assignment))
                SubmissionType.MEDIA_RECORDING -> postAction(AssignmentDetailAction.ShowMediaDialog(assignment))
                SubmissionType.EXTERNAL_TOOL, SubmissionType.BASIC_LTI_LAUNCH -> {
                    _data.value?.ltiTool?.let {
                        Analytics.logEvent(AnalyticsEventConstants.ASSIGNMENT_LAUNCHLTI_SELECTED)
                        postAction(AssignmentDetailAction.NavigateToLtiLaunchScreen(assignment.name.orEmpty(), it))
                    }
                }
                else -> {}
            }
        } else {
            postAction(AssignmentDetailAction.ShowSubmitDialog(assignment))
        }
    }
}
