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

package com.instructure.pandautils.features.assignments.details

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.isDiscussionAuthorNull
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.isNullOrEmpty
import com.instructure.canvasapi2.utils.isRtl
import com.instructure.canvasapi2.utils.isValid
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptItemViewModel
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptViewData
import com.instructure.pandautils.features.assignments.details.gradecellview.GradeCellViewData
import com.instructure.pandautils.features.assignments.details.itemviewmodels.ReminderItemViewModel
import com.instructure.pandautils.features.reminder.ReminderItem
import com.instructure.pandautils.features.reminder.ReminderManager
import com.instructure.pandautils.features.reminder.ReminderViewState
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.isAudioVisualExtension
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toFormattedString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    private val assignmentDetailsRepository: AssignmentDetailsRepository,
    private val resources: Resources,
    private val htmlContentFormatter: HtmlContentFormatter,
    private val application: Application,
    private val apiPrefs: ApiPrefs,
    private val submissionHandler: AssignmentDetailsSubmissionHandler,
    private val assignmentDetailsColorProvider: AssignmentDetailsColorProvider,
    private val reminderManager: ReminderManager,
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<AssignmentDetailsViewData>
        get() = _data
    private val _data = MutableLiveData<AssignmentDetailsViewData>()

    val events: LiveData<Event<AssignmentDetailAction>>
        get() = _events
    private val _events = MutableLiveData<Event<AssignmentDetailAction>>()

    private val courseId = savedStateHandle.get<Long>(Const.COURSE_ID).orDefault()
    val course: LiveData<Course>
        get() = _course
    private val _course = MutableLiveData(Course(id = courseId))

    private val assignmentId = savedStateHandle.get<Long>(Const.ASSIGNMENT_ID).orDefault()

    var bookmarker = Bookmarker(true, course.value).withParam(RouterParams.ASSIGNMENT_ID, assignmentId.toString())

    private var isObserver: Boolean = false
    private var quizResult: Quiz? = null

    private var externalLTITool: LTITool? = null
    private var studioLTITool: LTITool? = null

    private var restrictQuantitativeData = false
    private var gradingScheme = emptyList<GradingSchemeRow>()

    private var isAssignmentEnhancementEnabled = false

    var assignment: Assignment? = null
        private set

    private var selectedSubmission: Submission? = null

    private val _reminderViewState = MutableStateFlow(ReminderViewState())
    val reminderViewState = _reminderViewState.asStateFlow()

    var checkingReminderPermission = false
    var checkingNotificationPermission = false

    init {
        markSubmissionAsRead()
        submissionHandler.addAssignmentSubmissionObserver(
            context,
            assignmentId,
            apiPrefs.user?.id.orDefault(),
            resources,
            _data,
            ::refreshAssignment
        )
        _state.postValue(ViewState.Loading)
        loadData()

        reminderManager.observeRemindersLiveData(apiPrefs.user?.id.orDefault(), assignmentId) { reminderEntities ->
            _data.value?.reminders = mapReminders(reminderEntities)
            _reminderViewState.update { it.copy(
                reminders = reminderEntities.map { ReminderItem(it.id, it.text, Date(it.time)) },
                dueDate = assignment?.dueDate
            ) }
            _data.value?.notifyPropertyChanged(BR.reminders)
        }
    }

    fun getVideoUri(fragment: FragmentActivity): Uri? = submissionHandler.getVideoUri(fragment)

    override fun onCleared() {
        super.onCleared()
        reminderManager.removeLiveDataObserver()
        submissionHandler.removeAssignmentSubmissionObserver()
    }

    private fun markSubmissionAsRead() {
        viewModelScope.launch {
            SubmissionManager.markSubmissionAsReadAsync(courseId.orDefault(), assignmentId).await()
        }
    }

    private fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            try {
                val courseResult = assignmentDetailsRepository.getCourseWithGrade(courseId.orDefault(), forceNetwork)
                _course.postValue(courseResult)
                restrictQuantitativeData = courseResult.settings?.restrictQuantitativeData ?: false
                gradingScheme = courseResult.gradingScheme

                isObserver = courseResult.enrollments?.firstOrNull { it.isObserver } != null

                val assignmentResult = assignmentDetailsRepository.getAssignment(
                    isObserver,
                    assignmentId,
                    courseId.orDefault(),
                    forceNetwork
                )

                quizResult = if (assignmentResult.turnInType == Assignment.TurnInType.QUIZ && assignmentResult.quizId != 0L) {
                    assignmentDetailsRepository.getQuiz(courseId.orDefault(), assignmentResult.quizId, forceNetwork)
                } else null

                val ltiToolId = assignmentResult.externalToolAttributes?.contentId.orDefault()
                externalLTITool = if (ltiToolId != 0L) {
                    assignmentDetailsRepository.getExternalToolLaunchUrl(courseId.orDefault(), ltiToolId, assignmentId, forceNetwork)
                } else {
                    if (!assignmentResult.url.isNullOrEmpty() && assignmentResult.getSubmissionTypes().contains(SubmissionType.EXTERNAL_TOOL)) {
                        assignmentDetailsRepository.getLtiFromAuthenticationUrl(assignmentResult.url.orEmpty(), forceNetwork)
                    } else {
                        null
                    }
                }?.apply {
                    assignmentId = assignmentResult.id
                    courseId = assignmentResult.courseId
                }

                studioLTITool = submissionHandler.getStudioLTITool(assignmentResult, courseId)

                assignmentResult.isStudioEnabled = studioLTITool != null

                bookmarker = bookmarker.copy(url = assignmentResult.htmlUrl)

                val hasDraft = submissionHandler.lastSubmissionIsDraft

                isAssignmentEnhancementEnabled = assignmentDetailsRepository.isAssignmentEnhancementEnabled(courseId.orDefault(), forceNetwork)

                assignment = assignmentResult
                _reminderViewState.update { it.copy(
                    dueDate = if (assignment?.submission?.excused.orDefault()) null else assignment?.dueDate
                ) }
                _data.postValue(getViewData(assignmentResult, hasDraft))
                _state.postValue(ViewState.Success)
            } catch (ex: Exception) {
                val errorString = if (ex is IllegalAccessException) {
                    resources.getString(R.string.assignmentNoLongerAvailable)
                } else {
                    resources.getString(R.string.errorLoadingAssignment)
                }
                _state.postValue(ViewState.Error(errorString))
            }
        }
    }

    private fun refreshAssignment() {
        viewModelScope.launch {
            try {
                val assignmentResult = assignmentDetailsRepository.getAssignment(isObserver, assignmentId, courseId.orDefault(), true)
                _data.postValue(getViewData(assignmentResult, submissionHandler.lastSubmissionIsDraft))
            } catch (e: Exception) {
                _events.value = Event(AssignmentDetailAction.ShowToast(resources.getString(R.string.assignmentRefreshError)))
            }
        }
    }

    private suspend fun getViewData(assignment: Assignment, hasDraft: Boolean): AssignmentDetailsViewData {
        val points = if (restrictQuantitativeData) {
            ""
        } else {
            resources.getQuantityString(
                R.plurals.quantityPointsAbbreviated,
                assignment.pointsPossible.toInt(),
                NumberHelper.formatDecimal(assignment.pointsPossible, 1, true)
            )
        }

        val assignmentState = AssignmentUtils2.getAssignmentState(assignment, assignment.submission, false)

        // Don't mark LTI assignments as missing when overdue as they usually won't have a real submission for it
        val isMissing = assignment.isMissing() || (assignment.turnInType != Assignment.TurnInType.EXTERNAL_TOOL
                && assignment.dueAt != null
                && assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_MISSING)

        val submittedLabelText = resources.getString(
            if (isMissing) {
                R.string.missingAssignment
            } else if (!assignment.isSubmitted) {
                R.string.notSubmitted
            } else if (assignment.isGraded()) {
                R.string.gradedSubmissionLabel
            } else {
                R.string.submitted
            }
        )

        val submissionStatusTint = if (assignment.isSubmitted) {
            R.color.textSuccess
        } else if (isMissing) {
            R.color.textDanger
        } else {
            R.color.textDark
        }

        val submittedStatusIcon = if (assignment.isSubmitted) R.drawable.ic_complete_solid else R.drawable.ic_no

        // Submission Status under title - We only show Graded or nothing at all for PAPER/NONE
        val submissionStatusVisible =
            assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_GRADED
                    || assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_MISSING
                    || assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING
                    || (assignment.turnInType != Assignment.TurnInType.ON_PAPER && assignment.turnInType != Assignment.TurnInType.NONE)

        if (assignment.isLocked) {
            val lockedMessage = if (assignment.lockInfo?.contextModule != null) {
                val name = assignment.lockInfo?.lockedModuleName
                resources.getString(R.string.lockedModule, name)
            } else {
                assignment.unlockDate?.let {
                    val dateString = DateFormat.getDateInstance().format(it)
                    val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(it)
                    resources.getString(R.string.lockedSubtext, dateString, timeString)
                }
            }.orEmpty()

            return AssignmentDetailsViewData(
                courseColor = assignmentDetailsColorProvider.getContentColor(course.value),
                submissionAndRubricLabelColor = assignmentDetailsColorProvider.submissionAndRubricLabelColor,
                assignmentName = assignment.name.orEmpty(),
                points = points,
                submissionStatusText = submittedLabelText,
                submissionStatusIcon = submittedStatusIcon,
                submissionStatusTint = submissionStatusTint,
                submissionStatusVisible = submissionStatusVisible,
                fullLocked = true,
                lockedMessage = lockedMessage
            )
        }

        val partialLockedMessage = assignment.lockExplanation.takeIf { it.isValid() && assignment.lockDate?.before(Date()).orDefault() }.orEmpty()

        val submissionHistory = assignment.submission?.submissionHistory
        val attempts = submissionHistory?.reversed()?.mapIndexedNotNull { index, submission ->
            submission?.submittedAt?.toFormattedString()?.let {
                AssignmentDetailsAttemptItemViewModel(
                    AssignmentDetailsAttemptViewData(
                        resources.getString(R.string.attempt, submissionHistory.size - index),
                        it,
                        submission
                    )
                )
            }
        }.orEmpty()

        val submissionTypes = assignment.getSubmissionTypes()
            .map { Assignment.submissionTypeToPrettyPrintString(it, resources, assignment.ltiToolType()) }
            .joinToString()

        val allowedFileTypes = assignment.allowedExtensions.joinToString().takeIf {
            assignment.getSubmissionTypes().contains(SubmissionType.ONLINE_UPLOAD)
        }.orEmpty()

        val due = assignment.dueDate?.let {
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault()).format(it)
        } ?: resources.getString(R.string.toDoNoDueDate)

        val submitEnabled = assignment.allowedAttempts == -1L || (assignment.submission?.attempt?.let {
            it < assignment.allowedAttempts
        }.orDefault(true))

        val submitButtonText = resources.getString(
            when {
                !submitEnabled -> R.string.noAttemptsLeft
                assignment.turnInType == Assignment.TurnInType.QUIZ -> R.string.viewQuiz
                assignment.turnInType == Assignment.TurnInType.DISCUSSION -> R.string.viewDiscussion
                assignment.turnInType == Assignment.TurnInType.EXTERNAL_TOOL -> assignment.ltiToolType().openButtonRes
                assignment.isSubmitted -> R.string.resubmitAssignment
                else -> R.string.submitAssignment
            }
        )

        // Observers shouldn't see the submit button OR if the course is soft concluded
        val submitVisible = when {
            isObserver -> false
            !course.value?.isBetweenValidDateRange().orDefault() -> false
            assignment.submission?.excused.orDefault() -> false
            else -> when (assignment.turnInType) {
                Assignment.TurnInType.QUIZ, Assignment.TurnInType.DISCUSSION -> true
                Assignment.TurnInType.ONLINE, Assignment.TurnInType.EXTERNAL_TOOL -> assignment.isAllowedToSubmit
                else -> false
            }
        }

        val descriptionLabel = resources.getString(
            if (assignment.turnInType == Assignment.TurnInType.QUIZ) R.string.instructions else R.string.description
        )

        val quizViewViewData = if (quizResult != null) QuizViewViewData(
            questionCount = resources.getString(R.string.quizQuestions, quizResult?.questionCount.orDefault()),
            timeLimit = resources.getString(
                R.string.quizTimeLimit, if (quizResult?.timeLimit != 0) {
                    NumberHelper.formatInt(quizResult?.timeLimit)
                } else {
                    resources.getString(R.string.quizNoTimeLimit)
                }
            ),
            allowedAttempts = resources.getString(
                R.string.allowedAttempts, if (quizResult?.allowedAttempts.orDefault() < 0) {
                    resources.getString(R.string.unlimited)
                } else {
                    NumberHelper.formatInt(quizResult?.allowedAttempts)
                }
            )
        ) else null

        val attemptsViewData = if (assignment.allowedAttempts > 0) AttemptsViewData(
            resources.getString(R.string.allowedAttempts, assignment.allowedAttempts.toString()),
            resources.getString(R.string.usedAttempts, assignment.submission?.attempt.orDefault())
        ) else null

        val discussionText = assignment.discussionTopicHeader?.message ?: assignment.discussionTopicHeader?.title
        val description = if (assignment.turnInType == Assignment.TurnInType.DISCUSSION && discussionText?.isNotBlank().orDefault()) {
            discussionText
        } else {
            assignment.description
        }
        val formattedDescription = description?.let {
            htmlContentFormatter.formatHtmlWithIframes(
                if (Locale.getDefault().isRtl) {
                    "<body dir=\"rtl\">${it}</body>"
                } else {
                    it
                }
            )
        }.orEmpty()

        val discussionTopicHeader = assignment.discussionTopicHeader
        val discussionHeaderViewData = DiscussionHeaderViewData(
            authorAvatarUrl = discussionTopicHeader?.author?.avatarImageUrl.orEmpty(),
            authorName = discussionTopicHeader?.author?.displayName ?: resources.getString(R.string.discussions_unknown_author),
            authorNameWithPronouns = Pronouns.span(
                discussionTopicHeader?.author?.displayName ?: resources.getString(R.string.discussions_unknown_author),
                discussionTopicHeader?.author?.pronouns.orEmpty(),
            ),
            authoredDate = DateHelper.getMonthDayAtTime(
                application.applicationContext,
                discussionTopicHeader?.postedDate,
                resources.getString(R.string.at)
            ) ?: resources.getString(R.string.discussions_unknown_date),
            attachmentIconVisible = !discussionTopicHeader?.attachments.isNullOrEmpty(),
            onAttachmentClicked = {
                postAction(AssignmentDetailAction.OnDiscussionHeaderAttachmentClicked(discussionTopicHeader?.attachments.orEmpty()))
            }
        ).takeIf {
            !discussionTopicHeader?.author.isDiscussionAuthorNull().orDefault(true)
        }

        return AssignmentDetailsViewData(
            courseColor = assignmentDetailsColorProvider.getContentColor(course.value),
            submissionAndRubricLabelColor = assignmentDetailsColorProvider.submissionAndRubricLabelColor,
            assignmentName = assignment.name.orEmpty(),
            points = points,
            submissionStatusText = submittedLabelText,
            submissionStatusIcon = submittedStatusIcon,
            submissionStatusTint = submissionStatusTint,
            submissionStatusVisible = submissionStatusVisible,
            lockedMessage = partialLockedMessage,
            submitButtonText = submitButtonText,
            submitEnabled = (submitEnabled && assignmentDetailsRepository.isOnline()) || (submitEnabled && assignment.turnInType == Assignment.TurnInType.DISCUSSION),
            submitVisible = submitVisible,
            attempts = attempts,
            selectedGradeCellViewData = GradeCellViewData.fromSubmission(
                resources,
                assignmentDetailsColorProvider.getContentColor(course.value),
                assignmentDetailsColorProvider.submissionAndRubricLabelColor,
                assignment,
                assignment.submission,
                restrictQuantitativeData,
                gradingScheme = gradingScheme
            ),
            dueDate = due,
            submissionTypes = submissionTypes,
            allowedFileTypes = allowedFileTypes,
            description = formattedDescription,
            descriptionLabelText = descriptionLabel,
            discussionHeaderViewData = discussionHeaderViewData,
            quizDetails = quizViewViewData,
            attemptsViewData = attemptsViewData,
            hasDraft = hasDraft,
            reminders = _data.value?.reminders.orEmpty(),
        )
    }

    private fun postAction(action: AssignmentDetailAction) {
        _events.postValue(Event(action))
    }

    private fun mapReminders(reminders: List<ReminderEntity>) = reminders
        .sortedBy {
            it.time
        }
        .map {
            ReminderItemViewModel(ReminderViewData(it.id, it.text)) {
                postAction(AssignmentDetailAction.ShowDeleteReminderConfirmationDialog(it))
            }
        }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        loadData(true)
    }

    fun onAttemptSelected(position: Int) {
        val assignment = assignment
        val attempt = _data.value?.attempts?.getOrNull(position)?.data
        val selectedSubmission = attempt?.submission
        this.selectedSubmission = selectedSubmission
        _data.value?.selectedGradeCellViewData = GradeCellViewData.fromSubmission(
            resources,
            assignmentDetailsColorProvider.getContentColor(course.value),
            assignmentDetailsColorProvider.submissionAndRubricLabelColor,
            assignment,
            selectedSubmission,
            restrictQuantitativeData,
            attempt?.isUploading.orDefault(),
            attempt?.isFailed.orDefault(),
            gradingScheme
        )
        _data.value?.notifyPropertyChanged(BR.selectedGradeCellViewData)
    }

    fun onLtiButtonPressed(url: String) {
        postAction(AssignmentDetailAction.NavigateToLtiScreen(url))
    }

    fun onGradeCellClicked() {
        if (submissionHandler.isUploading) {
            when (submissionHandler.lastSubmissionSubmissionType) {
                SubmissionType.ONLINE_TEXT_ENTRY.apiString -> onDraftClicked()
                SubmissionType.ONLINE_UPLOAD.apiString, SubmissionType.MEDIA_RECORDING.apiString -> postAction(
                    AssignmentDetailAction.NavigateToUploadStatusScreen(submissionHandler.lastSubmissionAssignmentId.orDefault())
                )
                SubmissionType.ONLINE_URL.apiString -> postAction(
                    AssignmentDetailAction.NavigateToUrlSubmissionScreen(
                        assignment?.name,
                        submissionHandler.lastSubmissionEntry,
                        submissionHandler.lastSubmissionIsDraft
                    )
                )
            }
        } else {
            Analytics.logEvent(AnalyticsEventConstants.SUBMISSION_CELL_SELECTED)
            postAction(
                AssignmentDetailAction.NavigateToSubmissionScreen(
                    isObserver,
                    selectedSubmission?.attempt,
                    assignment?.htmlUrl,
                    isAssignmentEnhancementEnabled
                )
            )
        }
    }

    fun onDraftClicked() {
        postAction(
            AssignmentDetailAction.NavigateToTextEntryScreen(
                assignment?.name,
                submissionHandler.lastSubmissionEntry,
                submissionHandler.lastSubmissionIsDraft
            )
        )
    }

    fun onSubmitButtonClicked() {
        val course = course ?: return
        val assignment = assignment ?: return
        val turnInType = assignment.turnInType
        val submissionTypes = assignment.getSubmissionTypes()
        val hasSingleSubmissionType = submissionTypes.size == 1
                && !(submissionTypes.contains(SubmissionType.ONLINE_UPLOAD) && assignment.isStudioEnabled)

        if (turnInType == Assignment.TurnInType.QUIZ) {
            val quiz = quizResult ?: return
            Analytics.logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_QUIZLAUNCH)
            postAction(AssignmentDetailAction.NavigateToQuizScreen(quiz))
        } else if (turnInType == Assignment.TurnInType.DISCUSSION) {
            course.value?.let {
                Analytics.logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_DISCUSSIONLAUNCH)
                postAction(
                    AssignmentDetailAction.NavigateToDiscussionScreen(
                        assignment.discussionTopicHeader?.id.orDefault(),
                        it
                    )
                )
            }
        } else if (hasSingleSubmissionType) {
            when (submissionTypes.first()) {
                SubmissionType.ONLINE_UPLOAD -> postAction(AssignmentDetailAction.NavigateToUploadScreen(assignment))
                SubmissionType.ONLINE_TEXT_ENTRY -> postAction(AssignmentDetailAction.NavigateToTextEntryScreen(assignment.name))
                SubmissionType.ONLINE_URL -> postAction(AssignmentDetailAction.NavigateToUrlSubmissionScreen(assignment.name))
                SubmissionType.STUDENT_ANNOTATION -> postAction(AssignmentDetailAction.NavigateToAnnotationSubmissionScreen(assignment))
                SubmissionType.MEDIA_RECORDING -> postAction(AssignmentDetailAction.ShowMediaDialog(assignment))
                SubmissionType.EXTERNAL_TOOL, SubmissionType.BASIC_LTI_LAUNCH -> {
                    externalLTITool.let {
                        Analytics.logEvent(AnalyticsEventConstants.ASSIGNMENT_LAUNCHLTI_SELECTED)
                        postAction(AssignmentDetailAction.NavigateToLtiLaunchScreen(assignment.name.orEmpty(), it, assignment.ltiToolType().openInternally))
                    }
                }
                else -> Unit
            }
        } else {
            postAction(AssignmentDetailAction.ShowSubmitDialog(assignment, studioLTITool))
        }
    }

    fun uploadAudioSubmission(context: Context?, file: File?) {
        submissionHandler.uploadAudioSubmission(context, course.value, assignment, file)
    }

    fun showContent(viewState: ViewState?): Boolean {
        return (viewState == ViewState.Success || viewState == ViewState.Refresh) && assignment != null
    }

    fun onAddReminderClicked() {
        postAction(AssignmentDetailAction.ShowReminderDialog)
    }

    fun isStudioAccepted(): Boolean {
        if (assignment?.isStudioEnabled == false) return false

        if (assignment?.getSubmissionTypes()?.contains(SubmissionType.ONLINE_UPLOAD) == false) return false

        if (assignment?.allowedExtensions?.isEmpty() == true) return true

        return assignment?.allowedExtensions?.any { isAudioVisualExtension(it) } ?: true
    }

    fun updateReminderColor(@ColorInt color: Int) {
        _reminderViewState.update { it.copy(themeColor = Color(color)) }
    }

    fun showCreateReminderDialog(context: Context, @ColorInt color: Int) {
        assignment?.let { assignment ->
            viewModelScope.launch {
                when {
                    assignment.dueDate == null -> reminderManager.showCustomReminderDialog(
                        context,
                        apiPrefs.user?.id.orDefault(),
                        assignment.id,
                        assignment.name.orEmpty(),
                        assignment.htmlUrl.orEmpty(),
                        assignment.dueDate
                    )
                    assignment.dueDate?.before(Date()).orDefault() -> reminderManager.showCustomReminderDialog(
                        context,
                        apiPrefs.user?.id.orDefault(),
                        assignment.id,
                        assignment.name.orEmpty(),
                        assignment.htmlUrl.orEmpty(),
                        assignment.dueDate
                    )
                    else -> reminderManager.showBeforeDueDateReminderDialog(
                        context,
                        apiPrefs.user?.id.orDefault(),
                        assignment.id,
                        assignment.name.orEmpty(),
                        assignment.htmlUrl.orEmpty(),
                        assignment.dueDate ?: Date(),
                        color
                    )
                }
            }
        }
    }

    fun showDeleteReminderConfirmationDialog(context: Context, reminderId: Long, @ColorInt color: Int) {
        viewModelScope.launch { reminderManager.showDeleteReminderDialog(context, reminderId, color) }
    }
}
