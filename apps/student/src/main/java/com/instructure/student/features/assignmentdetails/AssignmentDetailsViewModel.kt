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

import android.app.Application
import android.content.Context
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
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptItemViewModel
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptViewData
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.db.StudentDb
import com.instructure.student.features.assignmentdetails.gradecellview.GradeCellViewData
import com.instructure.student.mobius.assignmentDetails.getFormattedAttemptDate
import com.instructure.student.mobius.assignmentDetails.uploadAudioRecording
import com.instructure.student.util.getStudioLTITool
import com.squareup.sqldelight.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.DateFormat
import java.util.*
import javax.inject.Inject
import com.instructure.student.Submission as DatabaseSubmission

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseManager: CourseManager,
    private val assignmentManager: AssignmentManager,
    private val quizManager: QuizManager,
    private val submissionManager: SubmissionManager,
    private val resources: Resources,
    private val htmlContentFormatter: HtmlContentFormatter,
    private val colorKeeper: ColorKeeper,
    private val application: Application,
    apiPrefs: ApiPrefs,
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

    init {
        markSubmissionAsRead()
        submissionQuery.addListener(this)
        loadData()
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
                        add(0, AssignmentDetailsAttemptItemViewModel(
                            AssignmentDetailsAttemptViewData(
                                resources.getString(R.string.attempt, attempts.size + 1),
                                dateString,
                                isUploading = true
                            )
                        ))
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
        _state.postValue(ViewState.Loading)
        viewModelScope.launch {
            try {
                val courseResult = courseManager.getCourseWithGradeAsync(course?.id.orDefault(), forceNetwork).await().dataOrThrow
                restrictQuantitativeData = courseResult.settings?.restrictQuantitativeData ?: false
                gradingScheme = courseResult.gradingScheme.orEmpty()

                isObserver = courseResult.enrollments?.firstOrNull { it.isObserver } != null

                val assignmentResult = if (isObserver) {
                    assignmentManager.getAssignmentIncludeObserveesAsync(
                        assignmentId,
                        course?.id.orDefault(),
                        forceNetwork
                    ).await().dataOrThrow.toAssignmentForObservee()
                } else {
                    assignmentManager.getAssignmentWithHistoryAsync(
                        assignmentId,
                        course?.id.orDefault(),
                        forceNetwork
                    ).await().dataOrThrow
                } as Assignment

                quizResult = if (assignmentResult.turnInType == Assignment.TurnInType.QUIZ && assignmentResult.quizId != 0L) {
                    quizManager.getQuizAsync(course?.id.orDefault(), assignmentResult.quizId, forceNetwork).await().dataOrThrow
                } else null

                val ltiToolId = assignmentResult.externalToolAttributes?.contentId.orDefault()
                externalLTITool = if (ltiToolId != 0L) {
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
                _state.postValue(ViewState.Error(resources.getString(R.string.errorLoadingAssignment)))
            }
        }
    }

    private fun refreshAssignment() {
        viewModelScope.launch {
            try {
                val assignmentResult = if (isObserver) {
                    assignmentManager.getAssignmentIncludeObserveesAsync(assignmentId, course?.id.orDefault(), true)
                } else {
                    assignmentManager.getAssignmentWithHistoryAsync(assignmentId, course?.id.orDefault(), true)
                }.await().dataOrThrow as Assignment

                _data.postValue(getViewData(assignmentResult, dbSubmission?.isDraft.orDefault()))
            } catch (e: Exception) {
                _events.value = Event(AssignmentDetailAction.ShowToast(resources.getString(R.string.assignmentRefreshError)))
            }
        }
    }

    @Suppress("DEPRECATION")
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
        val isMissing = assignment.submission?.missing.orDefault() || (assignment.turnInType != Assignment.TurnInType.EXTERNAL_TOOL
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
        val submissionStatusVisible = assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_GRADED
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
            submitEnabled = submitEnabled,
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
            hasDraft = hasDraft
        )
    }

    private fun postAction(action: AssignmentDetailAction) {
        _events.postValue(Event(action))
    }

    fun refresh() {
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
}
