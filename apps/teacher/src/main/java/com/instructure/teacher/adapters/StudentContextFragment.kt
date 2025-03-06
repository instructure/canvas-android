/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import com.instructure.canvasapi2.StudentContextCardQuery.Analytics
import com.instructure.canvasapi2.StudentContextCardQuery.AsCourse
import com.instructure.canvasapi2.StudentContextCardQuery.Submission
import com.instructure.canvasapi2.StudentContextCardQuery.User
import com.instructure.canvasapi2.managers.StudentContextManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.isValid
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.blueprint.PresenterFragment
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDefaultValues
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDisabledFields
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ProfileUtils
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.asStateList
import com.instructure.pandautils.utils.children
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getContentDescriptionForMinusGradeString
import com.instructure.pandautils.utils.isVisible
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.databinding.FragmentStudentContextBinding
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.factory.StudentContextPresenterFactory
import com.instructure.teacher.holders.StudentContextSubmissionView
import com.instructure.teacher.presenters.StudentContextPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.displayText
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import com.instructure.teacher.viewinterface.StudentContextView
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class StudentContextFragment : PresenterFragment<StudentContextPresenter, StudentContextView>(), StudentContextView {

    @Inject
    lateinit var studentContextManager: StudentContextManager

    private val binding by viewBinding(FragmentStudentContextBinding::bind)

    private var mStudentId by LongArg()
    private var mCourseId by LongArg()
    private var mLaunchSubmissions by BooleanArg()
    private var mNeedToForceNetwork = false
    private var mHasLoaded = false

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentGraded(event: AssignmentGradedEvent) {
        event.once(javaClass.simpleName) {
            //force network call on resume
            mNeedToForceNetwork = true
            mHasLoaded = false
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mHasLoaded = false
        return inflater.inflate(R.layout.fragment_student_context, container, false)
    }

    override fun getPresenterFactory() = StudentContextPresenterFactory(mStudentId, mCourseId, studentContextManager)

    override fun onRefreshStarted() = with(binding) {
        toolbar.setGone()
        contentContainer.setGone()
        loadingView.setVisible()
        loadingView.announceForAccessibility(getString(R.string.Loading))
    }

    override fun onRefreshFinished(): Unit = with(binding) {
        loadingView.setGone()
        toolbar.setVisible()
        contentContainer.setVisible()
    }

    override fun onPresenterPrepared(presenter: StudentContextPresenter) {}

    override fun onReadySetGo(presenter: StudentContextPresenter) {
        if(!mHasLoaded) {
            presenter.refresh(mNeedToForceNetwork)
            mHasLoaded = true
        }
    }

    override fun clear() {
        binding.submissionListContainer.removeAllViewsInLayout()
    }

    override fun setData(course: AsCourse, student: User, summary: Analytics?, isStudent: Boolean) = with(binding) {
        val courseBackgroundColor = CanvasContext.emptyCourseContext(course.id.toLong()).color

        setupScrollListener()

        // Toolbar setup
        if (activity is MasterDetailInteractions) {
            toolbar.setupBackButtonWithExpandCollapseAndBack(this@StudentContextFragment) {
                toolbar.updateToolbarExpandCollapseIcon(this@StudentContextFragment)
                ViewStyler.themeToolbarColored(requireActivity(), toolbar, courseBackgroundColor, requireContext().getColor(R.color.textLightest))
                (activity as MasterDetailInteractions).toggleExpandCollapse()
            }
        } else {
            toolbar.setupBackButton(this@StudentContextFragment)
        }
        toolbar.title = Pronouns.span(student.shortName, student.pronouns)
        toolbar.subtitle = course.name
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, courseBackgroundColor, requireContext().getColor(R.color.textLightest))

        // Message FAB
        messageButton.setVisible()
        ViewStyler.themeFAB(messageButton)
        messageButton.setOnClickListener {
            val recipient = Recipient(
                stringId = student.id,
                name = student.name,
                pronouns = student.pronouns,
                avatarURL = student.avatarUrl,
            )
            val options = InboxComposeOptions.buildNewMessage().copy(
                defaultValues = InboxComposeOptionsDefaultValues(
                    contextName = course.name,
                    contextCode = "course_${course.id}",
                    recipients = listOf(recipient)
                ),
                disabledFields = InboxComposeOptionsDisabledFields(
                    isContextDisabled = true
                )
            )
            val route = InboxComposeFragment.makeRoute(options)
            RouteMatcher.route(requireActivity(), route)
        }

        studentNameView.text = Pronouns.span(student.shortName, student.pronouns)
        studentEmailView.setVisible(student.email.isValid()).text = student.email

        // Avatar
        ProfileUtils.loadAvatarForUser(avatarView, student.name, student.avatarUrl)

        // Course and section names
        courseNameView.text = course.name
        sectionNameView.text = if (isStudent) {
            student.enrollments.joinToString(" | ") { it.section?.name ?: "" }
        } else {
            student.enrollments
                .map { "${it.section?.name} (${it.type.displayText})" }
                .distinct()
                .joinToString(" | ")
        }

        // Latest activity
        student.enrollments.mapNotNull { it.lastActivityAt }.maxOrNull()?.let {
            val dateString = DateHelper.getFormattedDate(requireContext(), it)
            val timeString = DateHelper.getFormattedTime(requireContext(), it)
            lastActivityView.text = getString(R.string.latestStudentActivityAtFormatted, dateString, timeString)
        } ?: lastActivityView.setGone()

        if (isStudent) {
            val enrollmentGrades = student.enrollments.find { it.type == EnrollmentType.STUDENTENROLLMENT }?.grades

            // Grade before posting
            val gradeBeforePostingText = enrollmentGrades?.let { it.currentGrade ?: it.currentScore?.toString() } ?: "--"

            // Grade after posting
            val gradeAfterPostingText = enrollmentGrades?.let { it.unpostedCurrentGrade ?: it.unpostedCurrentScore?.toString() } ?: "--"

            if (gradeBeforePostingText == gradeAfterPostingText) {
                gradeBeforePosting.text = gradeBeforePostingText
                gradeBeforePosting.contentDescription =
                    getContentDescriptionForMinusGradeString(gradeBeforePostingText, requireContext())
                gradeBeforePostingLabel.setText(R.string.currentGrade)
                gradeAfterPostingContainer.setGone()
            } else {
                gradeBeforePosting.text = gradeBeforePostingText
                gradeBeforePosting.contentDescription =
                    getContentDescriptionForMinusGradeString(gradeBeforePostingText, requireContext())
                gradeAfterPosting.text = gradeAfterPostingText
                gradeAfterPosting.contentDescription =
                    getContentDescriptionForMinusGradeString(gradeAfterPostingText, requireContext())
            }

            // Override Grade
            val overrideText = enrollmentGrades?.let { it.overrideGrade ?: it.overrideScore?.toString() }
            if (overrideText.isValid()) {
                gradeOverride.text = overrideText
                gradeOverride.contentDescription =
                    getContentDescriptionForMinusGradeString(overrideText, requireContext())
            } else {
                gradeOverrideContainer.setGone()
            }

            val visibleGradeItems = gradeItems.children.filter { it.isVisible }
            if (visibleGradeItems.size == 1) {
                // If there is only one grade item, add a second empty child so the first doesn't stretch to the full parent width
                emptyGradeItemSpace.setVisible()
            } else {
                // Set color of last grade item
                visibleGradeItems.lastOrNull()?.apply {
                    backgroundTintList = courseBackgroundColor.asStateList()
                    children<TextView>().onEach { it.setTextColor(requireContext().getColor(R.color.textLightest)) }
                }
            }

            val onTime = summary?.tardinessBreakdown?.onTime?.toInt() ?: 0
            val late = summary?.tardinessBreakdown?.late?.toInt() ?: 0
            val missing = summary?.tardinessBreakdown?.missing?.toInt() ?: 0
            val submitted = onTime + late

            // Submitted
            submittedCount.text = if (submitted <= 0) "--" else submitted.toString()

            // Missing
            missingCount.text = if (missing <= 0) "--" else missing.toString()

            // Late
            lateCount.text = if (late <= 0) "--" else late.toString()
        } else {
            messageButton.setGone()
            val lastIdx = scrollContent.indexOfChild(additionalInfoContainer)
            scrollContent.children.forEachIndexed { idx, v -> if (idx > lastIdx) v.setGone() }
        }
    }

    private fun setupScrollListener() {
        binding.contentContainer.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    private val scrollListener = object : ViewTreeObserver.OnScrollChangedListener {

        private var triggered = false
        private val touchSlop by lazy { ViewConfiguration.get(requireContext()).scaledTouchSlop }

        override fun onScrollChanged() {
            if (view == null) return // To prevent binding crashes is split view.

            with(binding) {
                if (!isAdded || contentContainer.height == 0 || scrollContent.height == 0 || loadMoreContainer.height == 0) return
                val threshold = scrollContent.height - loadMoreContainer.top
                val bottomOffset = contentContainer.height + contentContainer.scrollY - scrollContent.bottom
                if (scrollContent.height <= contentContainer.height) {
                    presenter.loadMoreSubmissions()
                } else if (triggered && (threshold + touchSlop + bottomOffset < 0)) {
                    triggered = false
                } else if (!triggered && (threshold + bottomOffset > 0)) {
                    triggered = true
                    presenter.loadMoreSubmissions()
                }
            }
        }
    }

    override fun addSubmissions(submissions: List<Submission>, course: AsCourse, student: User) {
        val courseColor = CanvasContext.emptyCourseContext(course.id.toLong()).color
        submissions.forEach { submission ->
            val view = StudentContextSubmissionView(requireContext(), submission, courseColor)
            if (mLaunchSubmissions) view.onClick {
                val user = com.instructure.canvasapi2.models.User(
                        avatarUrl = student.avatarUrl,
                        id = student.id.toLongOrNull() ?: 0,
                        name = student.name ?: "",
                        shortName = student.shortName,
                        pronouns = student.pronouns,
                        email = student.email
                )

                val studentSubmission = GradeableStudentSubmission(StudentAssignee(user), null)
                val bundle = SpeedGraderActivity.makeBundle(
                    courseId = course.id.toLongOrNull() ?: 0,
                    assignmentId = submission.assignment?.id?.toLongOrNull() ?: 0,
                    anonymousGrading = false,
                    filteredSubmissionIds = longArrayOf(studentSubmission.id),
                    selectedIdx = 0)
                RouteMatcher.route(requireActivity(), Route(bundle, RouteContext.SPEED_GRADER))
            }
            binding.submissionListContainer.addView(view)
        }
        binding.contentContainer.post { scrollListener.onScrollChanged() }
    }

    override fun showLoadMoreIndicator(show: Boolean) {
        binding.loadMoreIndicator.setVisible(show)
    }

    override fun onErrorLoading(isDesigner: Boolean) {
        if (isDesigner) {
            toast(R.string.errorIsDesigner)
        } else {
            toast(R.string.errorLoadingStudentContextCard)
        }
        requireActivity().onBackPressed()
    }

    companion object {
        fun makeBundle(studentId: Long, courseId: Long, launchSubmissions: Boolean = false) = StudentContextFragment().apply {
            mStudentId = studentId
            mCourseId = courseId
            mLaunchSubmissions = launchSubmissions
        }.nonNullArgs

        fun newInstance(bundle: Bundle) = StudentContextFragment().apply { arguments = bundle }
    }

}
