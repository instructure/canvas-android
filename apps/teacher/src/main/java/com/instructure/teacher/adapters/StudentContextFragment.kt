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
import android.view.*
import android.widget.TextView
import com.instructure.canvasapi2.StudentContextCardQuery.*
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
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.databinding.FragmentStudentContextBinding
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.factory.StudentContextPresenterFactory
import com.instructure.teacher.fragments.AddMessageFragment
import com.instructure.teacher.holders.StudentContextSubmissionView
import com.instructure.teacher.presenters.StudentContextPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.displayText
import com.instructure.teacher.utils.setupBackButton
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import com.instructure.teacher.viewinterface.StudentContextView
import instructure.androidblueprint.PresenterFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class StudentContextFragment : PresenterFragment<StudentContextPresenter, StudentContextView>(), StudentContextView {

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

    override fun getPresenterFactory() = StudentContextPresenterFactory(mStudentId, mCourseId)

    override fun onRefreshStarted() {
        binding.toolbar.setGone()
        binding.contentContainer.setGone()
        binding.loadingView.setVisible()
        binding.loadingView.announceForAccessibility(getString(R.string.Loading))
    }

    override fun onRefreshFinished() {
        binding.loadingView.setGone()
        binding.toolbar.setVisible()
        binding.contentContainer.setVisible()
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

    override fun setData(course: AsCourse, student: User, summary: Analytics?, isStudent: Boolean) {
        val courseBackgroundColor = CanvasContext.emptyCourseContext(course.id.toLong()).backgroundColor

        setupScrollListener()

        // Toolbar setup
        if (activity is MasterDetailInteractions) {
            binding.toolbar.setupBackButtonWithExpandCollapseAndBack(this) {
                binding.toolbar.updateToolbarExpandCollapseIcon(this)
                ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, courseBackgroundColor, requireContext().getColor(R.color.white))
                (activity as MasterDetailInteractions).toggleExpandCollapse()
            }
        } else {
            binding.toolbar.setupBackButton(this)
        }
        binding.toolbar.title = Pronouns.span(student.shortName, student.pronouns)
        binding.toolbar.subtitle = course.name
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, courseBackgroundColor, requireContext().getColor(R.color.white))

        // Message FAB
        binding.messageButton.setVisible()
        ViewStyler.themeFAB(binding.messageButton)
        binding.messageButton.setOnClickListener {
            val recipient = Recipient(
                stringId = student.id,
                name = student.name,
                pronouns = student.pronouns,
                avatarURL = student.avatarUrl,
            )
            val args = AddMessageFragment.createBundle(listOf(recipient), "", "course_${course.id}", true)
            RouteMatcher.route(requireContext(), Route(AddMessageFragment::class.java, null, args))
        }

        binding.studentNameView.text = Pronouns.span(student.shortName, student.pronouns)
        binding.studentEmailView.setVisible(student.email.isValid()).text = student.email

        // Avatar
        ProfileUtils.loadAvatarForUser(binding.avatarView, student.name, student.avatarUrl)

        // Course and section names
        binding.courseNameView.text = course.name
        binding.sectionNameView.text = if (isStudent) {
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
            binding.lastActivityView.text = getString(R.string.latestStudentActivityAtFormatted, dateString, timeString)
        } ?: binding.lastActivityView.setGone()

        if (isStudent) {
            val enrollmentGrades = student.enrollments.find { it.type == EnrollmentType.STUDENTENROLLMENT }?.grades

            // Grade before posting
            val gradeBeforePostingText = enrollmentGrades?.let { it.currentGrade ?: it.currentScore?.toString() } ?: "--"

            // Grade after posting
            val gradeAfterPostingText = enrollmentGrades?.let { it.unpostedCurrentGrade ?: it.unpostedCurrentScore?.toString() } ?: "--"

            if (gradeBeforePostingText == gradeAfterPostingText) {
                binding.gradeBeforePosting.text = gradeBeforePostingText
                binding.gradeBeforePosting.contentDescription =
                    getContentDescriptionForMinusGradeString(gradeBeforePostingText, requireContext())
                binding.gradeBeforePostingLabel.setText(R.string.currentGrade)
                binding.gradeAfterPostingContainer.setGone()
            } else {
                binding.gradeBeforePosting.text = gradeBeforePostingText
                binding.gradeBeforePosting.contentDescription =
                    getContentDescriptionForMinusGradeString(gradeBeforePostingText, requireContext())
                binding.gradeAfterPosting.text = gradeAfterPostingText
                binding.gradeAfterPosting.contentDescription =
                    getContentDescriptionForMinusGradeString(gradeAfterPostingText, requireContext())
            }

            // Override Grade
            val overrideText = enrollmentGrades?.let { it.overrideGrade ?: it.overrideScore?.toString() }
            if (overrideText.isValid()) {
                binding.gradeOverride.text = overrideText
                binding.gradeOverride.contentDescription =
                    getContentDescriptionForMinusGradeString(overrideText, requireContext())
            } else {
                binding.gradeOverrideContainer.setGone()
            }

            val visibleGradeItems = binding.gradeItems.children.filter { it.isVisible }
            if (visibleGradeItems.size == 1) {
                // If there is only one grade item, add a second empty child so the first doesn't stretch to the full parent width
                binding.emptyGradeItemSpace.setVisible()
            } else {
                // Set color of last grade item
                visibleGradeItems.lastOrNull()?.apply {
                    backgroundTintList = courseBackgroundColor.asStateList()
                    children<TextView>().onEach { it.setTextColor(requireContext().getColor(R.color.white)) }
                }
            }

            val onTime = summary?.tardinessBreakdown?.onTime?.toInt() ?: 0
            val late = summary?.tardinessBreakdown?.late?.toInt() ?: 0
            val missing = summary?.tardinessBreakdown?.missing?.toInt() ?: 0
            val submitted = onTime + late

            // Submitted
            binding.submittedCount.text = if (submitted <= 0) "--" else submitted.toString()

            // Missing
            binding.missingCount.text = if (missing <= 0) "--" else missing.toString()

            // Late
            binding.lateCount.text = if (late <= 0) "--" else late.toString()
        } else {
            binding.messageButton.setGone()
            val lastIdx = binding.scrollContent.indexOfChild(binding.additionalInfoContainer)
            binding.scrollContent.children.forEachIndexed { idx, v -> if (idx > lastIdx) v.setGone() }
        }
    }

    private fun setupScrollListener() {
        binding.contentContainer.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    private val scrollListener = object : ViewTreeObserver.OnScrollChangedListener {

        private var triggered = false
        private val touchSlop by lazy { ViewConfiguration.get(requireContext()).scaledTouchSlop }

        override fun onScrollChanged() {
            if (!isAdded || binding.contentContainer.height == 0 || binding.scrollContent.height == 0 || binding.loadMoreContainer.height == 0) return
            val threshold = binding.scrollContent.height - binding.loadMoreContainer.top
            val bottomOffset = binding.contentContainer.height + binding.contentContainer.scrollY - binding.scrollContent.bottom
            if (binding.scrollContent.height <= binding.contentContainer.height) {
                presenter.loadMoreSubmissions()
            } else if (triggered && (threshold + touchSlop + bottomOffset < 0)) {
                triggered = false
            } else if (!triggered && (threshold + bottomOffset > 0)) {
                triggered = true
                presenter.loadMoreSubmissions()
            }
        }

    }

    override fun addSubmissions(submissions: List<Submission>, course: AsCourse, student: User) {
        val courseColor = CanvasContext.emptyCourseContext(course.id.toLong()).textAndIconColor
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
                    course.id.toLongOrNull() ?: 0,
                    submission.assignment?.id?.toLongOrNull() ?: 0,
                    listOf(studentSubmission), 0)
                RouteMatcher.route(requireContext(), Route(bundle, RouteContext.SPEED_GRADER))
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
