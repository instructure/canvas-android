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

package com.instructure.student.features.assignments.details

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.Assignment.SubmissionType
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
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptItemViewModel
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptViewData
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.orDefault
import com.instructure.student.R
import com.instructure.student.db.StudentDb
import com.instructure.student.features.assignments.details.gradecellview.GradeCellViewData
import com.instructure.student.features.assignments.details.itemviewmodels.ReminderItemViewModel
import com.instructure.student.features.assignments.reminder.AlarmScheduler
import com.instructure.student.mobius.assignmentDetails.getFormattedAttemptDate
import com.instructure.student.mobius.assignmentDetails.uploadAudioRecording
import com.instructure.student.util.getStudioLTITool
import com.squareup.sqldelight.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.instructure.student.Submission as DatabaseSubmission

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assignmentDetailsRepository: AssignmentDetailsRepository,
    private val resources: Resources,
    private val htmlContentFormatter: HtmlContentFormatter,
    private val colorKeeper: ColorKeeper,
    private val application: Application,
    private val apiPrefs: ApiPrefs,
    private val alarmScheduler: AlarmScheduler,
    database: StudentDb
) : ViewModel(), Query.Listener {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<AssignmentDetailsViewData>
        get() = _data
    private val _data = MutableLiveData<AssignmentDetailsViewData>()

    val events: LiveData<Event<AssignmentDetailAction>>
        get() = _events
    private val _events = MutableLiveData<Event<AssignmentDetailAction>>()

    val course = savedStateHandle.get<Course>(Const.CANVAS_CONTEXT)
    private val assignmentId = savedStateHandle.get<Long>(Const.ASSIGNMENT_ID).orDefault()

    var bookmarker = Bookmarker(true, course).withParam(RouterParams.ASSIGNMENT_ID, assignmentId.toString())

    private var isObserver: Boolean = false
    private var quizResult: Quiz? = null

    private var externalLTITool: LTITool? = null
    private var studioLTITool: LTITool? = null

    private var dbSubmission: DatabaseSubmission? = null
    private var isUploading = false
    private var restrictQuantitativeData = false
    private var gradingScheme = emptyList<GradingSchemeRow>()

    var assignment: Assignment? = null
        private set

    private var selectedSubmission: Submission? = null

    private val submissionQuery = database.submissionQueries.getSubmissionsByAssignmentId(assignmentId, apiPrefs.user?.id.orDefault())

    private val remindersObserver = Observer<List<ReminderEntity>> {
        _data.value?.reminders = mapReminders(it)
        _data.value?.notifyPropertyChanged(BR.reminders)
    }

    private val remindersLiveData = assignmentDetailsRepository.getRemindersByAssignmentIdLiveData(
        apiPrefs.user?.id.orDefault(), assignmentId
    ).apply {
        observeForever(remindersObserver)
    }

    var checkingReminderPermission = false

    init {
        markSubmissionAsRead()
        submissionQuery.addListener(this)
        _state.postValue(ViewState.Loading)
        loadData()
    }

    override fun onCleared() {
        super.onCleared()
        remindersLiveData.removeObserver(remindersObserver)
    }

    override fun queryResultsChanged() {
        viewModelScope.launch {
            val submission = submissionQuery.executeAsList().lastOrNull()
            dbSubmission = submission
            val attempts = _data.value?.attempts
            submission?.let { dbSubmission ->
                val isDraft = dbSubmission.isDraft.orDefault()
                _data.value?.hasDraft = isDraft
                _data.value?.notifyPropertyChanged(BR.hasDraft)

                val dateString = getFormattedAttemptDate(dbSubmission.lastActivityDate?.toInstant()?.toEpochMilli()?.let { Date(it) } ?: Date())
                if (!isDraft && !isUploading) {
                    isUploading = true
                    _data.value?.attempts = attempts?.toMutableList()?.apply {
                        add(
                            0, AssignmentDetailsAttemptItemViewModel(
                                AssignmentDetailsAttemptViewData(
                                    resources.getString(R.string.attempt, attempts.size + 1),
                                    dateString,
                                    isUploading = true
                                )
                            )
                        )
                    }.orEmpty()
                    _data.value?.notifyPropertyChanged(BR.attempts)
                }
                if (isUploading && submission.errorFlag) {
                    _data.value?.attempts = attempts?.toMutableList()?.apply {
                        if (isNotEmpty()) removeFirst()
                        add(0, AssignmentDetailsAttemptItemViewModel(
                            AssignmentDetailsAttemptViewData(
                                resources.getString(R.string.attempt, attempts.size),
                                dateString,
                                isFailed = true
                            )
                        )
                        )
                    }.orEmpty()
                    _data.value?.notifyPropertyChanged(BR.attempts)
                }
            } ?: run {
                if (isUploading) {
                    isUploading = false
                    refreshAssignment()
                }
            }
        }
    }

    private fun markSubmissionAsRead() {
        viewModelScope.launch {
            SubmissionManager.markSubmissionAsReadAsync(course?.id.orDefault(), assignmentId).await()
        }
    }

    private fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            try {
                val courseResult = assignmentDetailsRepository.getCourseWithGrade(course?.id.orDefault(), forceNetwork)
                restrictQuantitativeData = courseResult.settings?.restrictQuantitativeData ?: false
                gradingScheme = courseResult.gradingScheme

                isObserver = courseResult.enrollments?.firstOrNull { it.isObserver } != null

                val assignmentResult = assignmentDetailsRepository.getAssignment(
                    isObserver,
                    assignmentId,
                    course?.id.orDefault(),
                    forceNetwork
                )

                quizResult = if (assignmentResult.turnInType == Assignment.TurnInType.QUIZ && assignmentResult.quizId != 0L) {
                    assignmentDetailsRepository.getQuiz(course?.id.orDefault(), assignmentResult.quizId, forceNetwork)
                } else null

                val ltiToolId = assignmentResult.externalToolAttributes?.contentId.orDefault()
                externalLTITool = if (ltiToolId != 0L) {
                    assignmentDetailsRepository.getExternalToolLaunchUrl(course?.id.orDefault(), ltiToolId, assignmentId, forceNetwork)
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

                studioLTITool = if (assignmentResult.getSubmissionTypes().contains(SubmissionType.ONLINE_UPLOAD)) {
                    course?.id?.getStudioLTITool()?.dataOrNull
                } else null
                assignmentResult.isStudioEnabled = studioLTITool != null

                bookmarker = bookmarker.copy(url = assignmentResult.htmlUrl)

                val dbSubmission = submissionQuery.executeAsList().lastOrNull()
                this@AssignmentDetailsViewModel.dbSubmission = dbSubmission
                val hasDraft = dbSubmission?.isDraft.orDefault()

                assignment = assignmentResult
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
                val assignmentResult = assignmentDetailsRepository.getAssignment(isObserver, assignmentId, course?.id.orDefault(), true)
                _data.postValue(getViewData(assignmentResult, dbSubmission?.isDraft.orDefault()))
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
            } else if (assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_GRADED) {
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
                courseColor = colorKeeper.getOrGenerateColor(course),
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
            submission?.submittedAt?.let { getFormattedAttemptDate(it) }?.let {
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
            .map { Assignment.submissionTypeToPrettyPrintString(it, resources) }
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
                assignment.turnInType == Assignment.TurnInType.EXTERNAL_TOOL -> R.string.launchExternalTool
                assignment.isSubmitted -> R.string.resubmitAssignment
                else -> R.string.submitAssignment
            }
        )

        // Observers shouldn't see the submit button OR if the course is soft concluded
        val submitVisible = when {
            isObserver -> false
            !course?.isBetweenValidDateRange().orDefault() -> false
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
            courseColor = colorKeeper.getOrGenerateColor(course),
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
                colorKeeper.getOrGenerateColor(course),
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
            showReminders = assignment.dueDate?.after(Date()).orDefault(),
            reminders = mapReminders(remindersLiveData.value.orEmpty())
        )
    }

    private fun postAction(action: AssignmentDetailAction) {
        _events.postValue(Event(action))
    }

    private fun mapReminders(reminders: List<ReminderEntity>) = reminders.map {
        ReminderItemViewModel(ReminderViewData(it.id, resources.getString(R.string.reminderBefore, it.text))) {
            postAction(AssignmentDetailAction.ShowDeleteReminderConfirmationDialog {
                deleteReminderById(it)
            })
        }
    }

    private fun deleteReminderById(id: Long) {
        alarmScheduler.cancelAlarm(id)
        viewModelScope.launch {
            assignmentDetailsRepository.deleteReminderById(id)
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
            colorKeeper.getOrGenerateColor(course),
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
        if (isUploading) {
            when (dbSubmission?.submissionType) {
                SubmissionType.ONLINE_TEXT_ENTRY.apiString -> onDraftClicked()
                SubmissionType.ONLINE_UPLOAD.apiString, SubmissionType.MEDIA_RECORDING.apiString -> postAction(
                    AssignmentDetailAction.NavigateToUploadStatusScreen(dbSubmission?.assignmentId.orDefault())
                )
                SubmissionType.ONLINE_URL.apiString -> postAction(
                    AssignmentDetailAction.NavigateToUrlSubmissionScreen(
                        assignment?.name,
                        dbSubmission?.submissionEntry,
                        dbSubmission?.errorFlag.orDefault()
                    )
                )
            }
        } else {
            Analytics.logEvent(AnalyticsEventConstants.SUBMISSION_CELL_SELECTED)
            postAction(AssignmentDetailAction.NavigateToSubmissionScreen(isObserver, selectedSubmission?.attempt))
        }
    }

    fun onDraftClicked() {
        postAction(
            AssignmentDetailAction.NavigateToTextEntryScreen(
                assignment?.name,
                dbSubmission?.submissionEntry,
                dbSubmission?.errorFlag.orDefault()
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
            Analytics.logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_DISCUSSIONLAUNCH)
            postAction(AssignmentDetailAction.NavigateToDiscussionScreen(assignment.discussionTopicHeader?.id.orDefault(), course))
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
                        postAction(AssignmentDetailAction.NavigateToLtiLaunchScreen(assignment.name.orEmpty(), it))
                    }
                }
                else -> Unit
            }
        } else {
            postAction(AssignmentDetailAction.ShowSubmitDialog(assignment, studioLTITool))
        }
    }

    fun uploadAudioSubmission(context: Context?, file: File?) {
        val assignment = assignment
        if (context != null && file != null && assignment != null && course != null) {
            uploadAudioRecording(context, file, assignment, course)
        } else {
            postAction(AssignmentDetailAction.ShowToast(resources.getString(R.string.audioRecordingError)))
        }
    }

    fun showContent(viewState: ViewState?): Boolean {
        return (viewState == ViewState.Success || viewState == ViewState.Refresh) && assignment != null
    }

    fun onAddReminderClicked() {
        postAction(AssignmentDetailAction.ShowReminderDialog)
    }

    fun onReminderSelected(reminderChoice: ReminderChoice) {
        if (reminderChoice == ReminderChoice.Custom) {
            postAction(AssignmentDetailAction.ShowCustomReminderDialog)
        } else {
            setReminder(reminderChoice)
        }
    }

    private fun setReminder(reminderChoice: ReminderChoice) {
        val assignment = assignment ?: return
        val alarmTimeInMillis = getAlarmTimeInMillis(reminderChoice) ?: return
        val reminderText = reminderChoice.getText(resources)

        if (alarmTimeInMillis < System.currentTimeMillis()) {
            postAction(AssignmentDetailAction.ShowToast(resources.getString(R.string.reminderInPast)))
            return
        }

        if (remindersLiveData.value?.any { it.time == alarmTimeInMillis }.orDefault()) {
            postAction(AssignmentDetailAction.ShowToast(resources.getString(R.string.reminderAlreadySet)))
            return
        }

        viewModelScope.launch {
            val reminderId = assignmentDetailsRepository.addReminder(
                apiPrefs.user?.id.orDefault(),
                assignment,
                reminderText,
                alarmTimeInMillis
            )

            alarmScheduler.scheduleAlarm(
                assignment.id,
                assignment.htmlUrl.orEmpty(),
                assignment.name.orEmpty(),
                reminderText,
                alarmTimeInMillis,
                reminderId
            )
        }
    }

    private fun getAlarmTimeInMillis(reminderChoice: ReminderChoice): Long? {
        val dueDate = assignment?.dueDate?.time ?: return null
        return dueDate - reminderChoice.getTimeInMillis()
    }
}
