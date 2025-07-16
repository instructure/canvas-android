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
package com.instructure.teacher.fragments

import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.Assignee
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.GroupAssignee
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.analytics.SCREEN_VIEW_SPEED_GRADER_GRADE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentSpeedgraderGradeBinding
import com.instructure.teacher.dialog.CustomizeGradeDialog
import com.instructure.teacher.dialog.PassFailGradeDailog
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.factory.SpeedGraderGradePresenterFactory
import com.instructure.teacher.presenters.SpeedGraderGradePresenter
import com.instructure.teacher.utils.getDisplayGrade
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import com.instructure.teacher.viewinterface.SpeedGraderGradeView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DecimalFormat

@ScreenView(SCREEN_VIEW_SPEED_GRADER_GRADE)
class SpeedGraderGradeFragment : BasePresenterFragment<
        SpeedGraderGradePresenter,
        SpeedGraderGradeView,
        FragmentSpeedgraderGradeBinding>(),
    SpeedGraderGradeView {

    private var mSubmission: Submission? by NullableParcelableArg(default = Submission())
    private var mAssignment: Assignment by ParcelableArg(default = Assignment())
    private var mAssignee: Assignee by ParcelableArg(default = StudentAssignee(User()))
    private var mCourse: Course by ParcelableArg(default = Course())

    override val bindingInflater: (layoutInflater: LayoutInflater) -> FragmentSpeedgraderGradeBinding = FragmentSpeedgraderGradeBinding::inflate

    override fun getPresenterFactory() = SpeedGraderGradePresenterFactory(mSubmission, mAssignment, mCourse, mAssignee)
    override fun onReadySetGo(presenter: SpeedGraderGradePresenter) {}

    override fun onPresenterPrepared(presenter: SpeedGraderGradePresenter) {
        setupViews()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizSaved(event: QuizSubmissionGradedEvent) {
        event.once("GradeFragment|${mCourse.id}|${mAssignment.id}|${mSubmission?.id}|${mAssignee.id}") {
            if (it.id == mSubmission?.id) {
                presenter.submission = it
                setupViews()
            }
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAssignmentGraded(event: AssignmentGradedEvent) {
        val submissionId = presenter.submission?.id ?: return
        event.once(javaClass.simpleName + submissionId) {
            if (mAssignment.id == it) {
                presenter.refreshSubmission()
            }
        }
    }

    companion object {
        fun newInstance(
                submission: Submission?,
                assignment: Assignment,
                course: Course,
                assignee: Assignee
        ) = SpeedGraderGradeFragment().apply {
            mSubmission = submission
            mAssignment = assignment
            mCourse = course
            mAssignee = assignee
        }
    }

    override fun updateGradeText() {
        // Show 'grade hidden' icon if the submission is graded but there is no postAt date
        val showHiddenIcon = presenter.submission?.let { (it.isGraded || it.excused) && it.postedAt == null }
                ?: false
        binding.hiddenIcon.setVisible(showHiddenIcon)

        presenter.submission?.let {
            if (it.score > presenter.assignment.pointsPossible) {
                val numberFormatter = DecimalFormat("##.##")
                binding.gradeText.text = getString(R.string.speed_grader_overgraded_by, numberFormatter.format(it.score - presenter.assignment.pointsPossible))
                binding.gradeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.textWarning))
            } else {
                binding.gradeText.setText(R.string.grade)
                binding.gradeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.textDarkest))
            }
        }

        val displayGrade = presenter.assignment.getDisplayGrade(presenter.submission, requireContext())
        // Toggle visibility and set text
        if (displayGrade.text.isBlank()) {
            binding.gradeValueText.setVisible(false)
            binding.addGradeIcon.setVisible(true)
            binding.gradeValueText.text = ""
            binding.editGradeIcon.setVisible(false)
        } else {
            binding.gradeValueText.setVisible(true)
            binding.addGradeIcon.setVisible(false)
            binding.editGradeIcon.setVisible(true)

            var currentGrade = displayGrade
            // Check to see if this submission has a late penalty
            if (presenter.submission?.pointsDeducted != null && presenter.submission?.pointsDeducted as Double > 0.0) {
                // Make the late policy information visible
                binding.finalGradeValueText.setVisible(true)
                binding.latePenaltyValue.setVisible(true)
                binding.latePolicy.setVisible(true)
                binding.finalGradeContainer.setVisible(true)

                binding.latePenalty.setTextColor(ThemePrefs.brandColor)
                binding.latePenaltyValue.setTextColor(ThemePrefs.brandColor)

                binding.finalGradeValueText.text = displayGrade.text
                binding.finalGradeValueText.contentDescription = displayGrade.contentDescription
                binding.latePenaltyValue.text = requireContext().resources.getQuantityString(R.plurals.latePolicyPenalty, if (presenter.submission?.pointsDeducted as Double == 1.0) 1 else 2, NumberHelper.formatDecimal(presenter.submission?.pointsDeducted as Double, 2, true))
                binding.latePenaltyValue.contentDescription = requireContext().resources.getQuantityString(R.plurals.latePolicyPenaltyFull, if (presenter.submission?.pointsDeducted as Double == 1.0) 1 else 2, NumberHelper.formatDecimal(presenter.submission?.pointsDeducted as Double, 2, true))

                // Change the currentGrade variable to the entered grade because the actual grade on the submission applies the late penalty
                currentGrade = presenter.assignment.getDisplayGrade(presenter.submission, requireContext(), true, true)
            }
            binding.gradeValueText.text = currentGrade.text
            binding.gradeValueText.contentDescription = currentGrade.contentDescription
        }
    }

    override fun updateGradeError() {
        toast(R.string.error_occurred)
    }

    private fun setupViews() = with(binding) {
        /* Mobile doesn't support moderated grading yet. If moderated grading is enabled
        for this assignment, hide all grading options and display a message to the user */
        if (presenter.assignment.moderatedGrading) {
            gradeContainer.setGone()
            rubricEditView.setGone()
            speedGraderSlider.setGone()
            moderatedGradingMessage.setVisible()
            return
        }

        updateGradeText()

        if (presenter.assignment.isUseRubricForGrading) {
            gradeSubtext.setVisible(true)
            gradeSubtext.text = getString(R.string.calculated_by_rubric)
        } else {
            gradeSubtext.setVisible(false)
        }

        gradeTextContainer.onClickWithRequireNetwork {
            // Scores for submitted quizzes must be edited in the WebView, so we disallow editing here
            if (presenter.assignment.quizId > 0 && presenter.assignment.getDisplayGrade(presenter.submission, requireContext()).text.isNotBlank()) {
                return@onClickWithRequireNetwork
            }

            // Launch grading dialog; no grading if it's not a graded type
            if (gradeValueText.text != getString(R.string.not_graded)) {
                if (Assignment.getGradingTypeFromAPIString(presenter.assignment.gradingType!!) == Assignment.GradingType.PASS_FAIL) {
                    showPassFailGradeDialog()
                } else {
                    showCustomizeGradeDialog()
                }
            }
        }

        if (shouldShowRubricView(presenter.assignment)) {
            rubricEditView.setData(presenter.assignment, presenter.submission, presenter.assignee)
            rubricEditView.onAssessmentSaved = { presenter.updateSubmission(it) }
            rubricEditView.setVisible()
        }

        if (shouldShowSliderView(presenter.assignment)) {
            speedGraderSlider.setData(presenter.assignment, presenter.submission, presenter.assignee)
            speedGraderSlider.onGradeChanged = { grade, isExcused -> presenter.updateGrade(grade, isExcused) }
            speedGraderSlider.setVisible()
        }

        editGradeIcon.onClick { showCustomizeGradeDialog() }
    }

    private fun shouldShowSliderView(assignment: Assignment): Boolean = (assignment.rubric == null || assignment.rubric!!.isEmpty())
            && (assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.POINTS || assignment.gradingType?.let { Assignment.getGradingTypeFromAPIString(it) } == Assignment.GradingType.PERCENT)

    private fun shouldShowRubricView(assignment: Assignment): Boolean = assignment.rubric != null && assignment.rubric!!.isNotEmpty()

    private fun showCustomizeGradeDialog() {
        val pointsPossible: String = NumberHelper.formatDecimal(presenter.assignment.pointsPossible, 2, true)
        var grade: String? = ""
        if (presenter.submission != null) {
            var gradeInput = presenter.submission?.grade
            if(!gradeInput.isNullOrEmpty() && gradeInput.last().toString() == "%") {
                gradeInput = gradeInput.dropLast(1)
            }
            grade = gradeInput
        }

        val dialog = CustomizeGradeDialog.getInstance(requireActivity().supportFragmentManager,
                pointsPossible, grade, presenter.assignment.gradingType!!, presenter.assignee is GroupAssignee, !shouldShowSliderView(presenter.assignment)) { currentGrade, isExcused ->

            presenter.updateGrade(currentGrade, isExcused)
        }

        dialog.show(requireActivity().supportFragmentManager, CustomizeGradeDialog::class.java.simpleName)
    }

    private fun showPassFailGradeDialog() {
        val dialog = PassFailGradeDailog.getInstance(requireActivity().supportFragmentManager, presenter.submission?.grade) { grade, isExcused ->
            presenter.updateGrade(grade, isExcused)
        }
        dialog.show(requireActivity().supportFragmentManager, PassFailGradeDailog::class.java.simpleName)
    }

    override fun onRefreshStarted(): Unit = with(binding) {
        gradeValueText.setGone()
        addGradeIcon.setGone()
        editGradeIcon.setGone()
        gradeProgressSpinner.announceForAccessibility(getString(R.string.loading))
        gradeProgressSpinner.setVisible()
        hiddenIcon.setGone()
    }

    val hasUnsavedChanges: Boolean get() = if (view != null) binding.rubricEditView.hasUnsavedChanges else false

    override fun onRefreshFinished() = with(binding) {
        gradeProgressSpinner.setGone()
        speedGraderSlider.setData(presenter.assignment, presenter.submission, presenter.assignee)
    }
}
