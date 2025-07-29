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

import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.canvasapi2.utils.toDate
import com.instructure.interactions.Identity
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_QUIZ_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.pandautils.features.speedgrader.SpeedGraderFragment
import com.instructure.pandautils.features.speedgrader.SubmissionListFilter
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.loadHtmlWithIframes
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.databinding.FragmentQuizDetailsBinding
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.events.AssignmentUpdatedEvent
import com.instructure.teacher.events.QuizUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.factory.QuizDetailsPresenterFactory
import com.instructure.teacher.features.assignment.submission.SubmissionListFragment
import com.instructure.teacher.presenters.QuizDetailsPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.anonymousSubmissionsDisplayable
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.isPracticeOrUngraded
import com.instructure.teacher.utils.isUngradedSurvey
import com.instructure.teacher.utils.quizTypeDisplayable
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.shuffleAnswersDisplayable
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import com.instructure.teacher.viewinterface.QuizDetailsView
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URL
import java.util.Date

@PageView
@ScreenView(SCREEN_VIEW_EDIT_QUIZ_DETAILS)
class QuizDetailsFragment : BasePresenterFragment<
        QuizDetailsPresenter,
        QuizDetailsView,
        FragmentQuizDetailsBinding>(),
    QuizDetailsView,
    Identity {

    private var canvasContext: CanvasContext? by NullableParcelableArg(key = Const.CANVAS_CONTEXT)

    private var course: Course by ParcelableArg(default = Course())
    private var quizId: Long by LongArg(0L, QUIZ_ID)
    private var quiz: Quiz by ParcelableArg(Quiz(), QUIZ)

    private var needToForceNetwork = false

    private var loadHtmlJob: Job? = null

    @Suppress("unused")
    @PageViewUrl
    fun makePageViewUrl() = "${ApiPrefs.fullDomain}/${course.contextId.replace("_", "s/")}/quizzes/${quizId}"

    override val bindingInflater: (layoutInflater: LayoutInflater) -> FragmentQuizDetailsBinding = FragmentQuizDetailsBinding::inflate

    override fun getPresenterFactory() = QuizDetailsPresenterFactory(course, quiz)

    override fun onDestroyView() {
        super.onDestroyView()
        loadHtmlJob?.cancel()
    }

    override fun onReadySetGo(presenter: QuizDetailsPresenter) {
        if (quizId == 0L) {
            // No Quiz ID so we must have a quiz
            presenter.loadData(needToForceNetwork)
        } else {
            // No Quiz, we need to get it
            presenter.getQuiz(quizId, course, true)
        }

        setupToolbar()

        binding.swipeRefreshLayout.isRefreshing = true
    }

    override fun onPresenterPrepared(presenter: QuizDetailsPresenter) = Unit

    override fun onRefreshFinished() = Unit

    override fun onRefreshStarted() {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.toolbar.menu.clear()
        clearListeners()
    }

    override val identity: Long? get() = if(quizId != 0L) quizId else quiz.id
    override val skipCheck: Boolean get() = false

    override fun populateQuizDetails(quiz: Quiz): Unit = with(binding) {
        this@QuizDetailsFragment.quiz = quiz
        toolbar.setupMenu(R.menu.menu_assignment_details) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    openEditPage(quiz)
                }

                R.id.menu_speedGrader -> {
                    val bundle = SpeedGraderFragment.makeBundle(
                        courseId = course.id,
                        assignmentId = quiz.id
                    )
                    RouteMatcher.route(requireActivity(), Route(bundle, RouteContext.SPEED_GRADER))
                }
            }
        }
        swipeRefreshLayout.isRefreshing = false
        setupViews(quiz)
        setupListeners(quiz)
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, course.color, requireContext().getColor(R.color.textLightest))

        fullDateDetailsButton.setVisible(quiz._assignment != null)
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setupBackButtonWithExpandCollapseAndBack(this@QuizDetailsFragment) {
            toolbar.updateToolbarExpandCollapseIcon(this@QuizDetailsFragment)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, course.color, requireContext().getColor(R.color.textLightest))
            (activity as MasterDetailInteractions).toggleExpandCollapse()
        }

        toolbar.title = getString(R.string.quiz_details)
        if (!isTablet) {
            toolbar.subtitle = presenter.mCourse.name
        }
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, course.color, requireContext().getColor(R.color.textLightest))
    }

    private fun setupViews(quiz: Quiz) = with(binding) {
        swipeRefreshLayout.setOnRefreshListener {
            presenter.loadData(true)

            // Send out bus event to trigger a refresh for quiz list and submission list
            QuizUpdatedEvent(quiz.id, javaClass.simpleName).post()
            AssignmentGradedEvent(quiz.id, javaClass.simpleName).post()
        }

        availabilityLayout.setGone()
        availableFromLayout.setGone()
        availableToLayout.setGone()
        dueForLayout.setGone()
        dueDateLayout.setGone()
        otherDueDateTextView.setGone()

        // Assignment name
        quizTitleTextView.text = quiz.title

        // Points possible
        quiz.pointsPossible?.let {
            pointsTextView.text = resources.getQuantityString(
                R.plurals.quantityPointsAbbreviated,
                it.toDouble().toInt(),
                NumberHelper.formatDecimal(it.toDouble(), 1, true)
            )
        }

        pointsTextView.contentDescription = if (quiz.pointsPossible != null) {
            resources.getQuantityString(
                    R.plurals.quantityPointsFull,
                    quiz.pointsPossible?.toDouble()?.toInt() ?: 1,
                    NumberHelper.formatDecimal(quiz.pointsPossible!!.toDouble(), 1, true))
        } else getString(R.string.quiz_details_no_points_assigned)

        // Publish status
        if (quiz.published) {
            publishStatusIconView.setImageResource(R.drawable.ic_complete_solid)
            publishStatusIconView.setColorFilter(requireContext().getColorCompat(R.color.textSuccess))
            publishStatusTextView.setText(R.string.published)
            publishStatusTextView.setTextColor(requireContext().getColorCompat(R.color.textSuccess))
        } else {
            publishStatusIconView.setImageResource(R.drawable.ic_complete)
            publishStatusIconView.setColorFilter(requireContext().getColorCompat(R.color.textDark))
            publishStatusTextView.setText(R.string.not_published)
            publishStatusTextView.setTextColor(requireContext().getColorCompat(R.color.textDark))
        }

        // Lock status
        val atSeparator = getString(R.string.at)

        val allDates = quiz.allDates
        allDates.singleOrNull()?.apply {
            if (quiz.lockDate?.before(Date()) == true) {
                availabilityLayout.setVisible()
                availabilityTextView.setText(R.string.closed)
            } else {
                availableFromLayout.setVisible()
                availableToLayout.setVisible()
                availableFromTextView.text = if (quiz.unlockDate != null)
                    DateHelper.getMonthDayAtTime(requireContext(), quiz.unlockDate, atSeparator) else getString(R.string.no_date_filler)
                availableToTextView.text = if (quiz.lockDate != null)
                    DateHelper.getMonthDayAtTime(requireContext(), quiz.lockDate, atSeparator) else getString(R.string.no_date_filler)
            }
        }

        // Due date(s)
        if (allDates.size > 1) {
            otherDueDateTextView.setVisible()
            otherDueDateTextView.setText(R.string.multiple_due_dates)
        } else {
            if (allDates.isEmpty() || allDates[0].dueAt == null) {
                otherDueDateTextView.setVisible()
                otherDueDateTextView.setText(R.string.no_due_date)

                dueForLayout.setVisible()
                dueForTextView.text = if (allDates.isEmpty() || allDates[0].isBase) getString(R.string.everyone) else allDates[0].title ?: ""

            } else with(allDates[0]) {
                dueDateLayout.setVisible()
                dueDateTextView.text = DateHelper.getMonthDayAtTime(requireContext(), dueDate, atSeparator)

                dueForLayout.setVisible()
                dueForTextView.text = if (isBase) getString(com.instructure.teacher.R.string.everyone) else title ?: ""
            }
        }

        // If the user is a designer we don't want to show the submissions layout
        submissionsLayout.setVisible(!course.isDesigner)

        if (!quiz.isGradeable) {
            // Quiz is not gradeable, don't show submission dials
            submissionsLayout.setGone()
            submissionsHR.setGone()
        }

        // Description
        setupDescription(quiz)

        // Settings
        quizSettingsContainer.removeAllViews()
        Quiz.SettingTypes.values().forEach { settingType ->
            with(presenter.mQuiz) {
                val quizTypeValue: Pair<Int, String?> = when (settingType) {
                    Quiz.SettingTypes.QUIZ_TYPE -> R.string.quiz_settings_label_quiz_type to getString(quizTypeDisplayable())
                    Quiz.SettingTypes.POINTS -> R.string.quiz_settings_label_points to if (isUngradedSurvey()) " " else pointsPossible
                    Quiz.SettingTypes.ASSIGNMENT_GROUP -> R.string.quiz_settings_label_assignment_group to if (!isPracticeOrUngraded()) _assignmentGroup?.name else null
                    Quiz.SettingTypes.SHUFFLE_ANSWERS -> R.string.quiz_settings_label_shuffle_answers to getString(shuffleAnswersDisplayable())
                    Quiz.SettingTypes.TIME_LIMIT -> {
                        R.string.quiz_settings_label_time_limit to
                                if (timeLimit == 0) getString(R.string.no_time_limit)
                                else timeLimit.let { resources.getQuantityString(R.plurals.minutes, it, NumberHelper.formatInt(it.toLong())) }
                    }
                    Quiz.SettingTypes.MULTIPLE_ATTEMPTS -> R.string.quiz_settings_label_multiple_attempts to getString(if (allowedAttempts != 1) R.string.yes else R.string.no)
                    Quiz.SettingTypes.SCORE_TO_KEEP -> R.string.quiz_settings_label_score_to_keep to if (allowedAttempts != 1) getString(scoringPolicyString) else null
                    Quiz.SettingTypes.ATTEMPTS -> R.string.quiz_settings_label_attempts to if (allowedAttempts < 0) getString(R.string.unlimited) else if (allowedAttempts == 1) null else allowedAttempts.toString()
                    Quiz.SettingTypes.VIEW_RESPONSES -> R.string.quiz_settings_label_view_responses to getString(hideResultsStringResource)
                    Quiz.SettingTypes.SHOW_CORRECT_ANSWERS -> R.string.quiz_settings_label_show_correct_answers to getShowCorrectAnswersDate(showCorrectAnswers, showCorrectAnswersAt.toDate(), hideCorrectAnswersAt.toDate())
                    Quiz.SettingTypes.ACCESS_CODE -> R.string.quiz_settings_label_access_code to accessCode
                    Quiz.SettingTypes.IP_FILTER -> R.string.quiz_settings_label_ip_filter to ipFilter
                    Quiz.SettingTypes.ONE_QUESTION_AT_A_TIME -> R.string.quiz_settings_label_one_question_at_a_time to getString(if (oneQuestionAtATime) R.string.yes else R.string.no)
                    Quiz.SettingTypes.LOCK_QUESTIONS_AFTER_ANSWERING -> {
                        R.string.quiz_settings_label_lock_questions_after_answering to
                                if (oneQuestionAtATime) getString(if (isLockQuestionsAfterAnswering) R.string.yes else R.string.no)
                                else null
                    }

                    Quiz.SettingTypes.ANONYMOUS_SUBMISSIONS -> R.string.quiz_settings_label_anonymous_submissions to if (anonymousSubmissionsDisplayable()) { getString(if (allowAnonymousSubmissions) R.string.yes else R.string.no) } else null
                }

                if (quizTypeValue.second != null) {
                    val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_quiz_setting_item, null)
                    (view.findViewById<TextView>(R.id.quizSettingType)).text = getString(quizTypeValue.first)
                    (view.findViewById<TextView>(R.id.quizSettingValue)).text = quizTypeValue.second
                    quizSettingsContainer.addView(view)
                }
            }
        }
    }


    private fun getShowCorrectAnswersDate(showCorrectAnswers: Boolean, from: Date?, to: Date?): String {
        if (!showCorrectAnswers) {
            return ""
        }

        if (from == null && to == null) {
            return getString(R.string.immediately)
        }

        val fromDate: String? = DateHelper.getMonthDayTimeMaybeMinutesMaybeYear(requireActivity(), from, R.string.at)
        val toDate: String? = DateHelper.getMonthDayTimeMaybeMinutesMaybeYear(requireActivity(), to, R.string.at)

        if (fromDate != null && toDate != null) {
            return getString(R.string.quiz_details_from_to, fromDate, toDate)
        }

        if (fromDate != null) {
            return getString(R.string.quiz_details_after, fromDate)
        }

        return getString(R.string.quiz_details_until, toDate)
    }
    override fun updateSubmissionDonuts(totalStudents: Int, gradedStudents: Int, needsGradingCount: Int, notSubmitted: Int) = with(binding.donutGroup) {
        allTitle.setTextColor(course.color)
        allIcon.setColorFilter(course.color)
        // Submission section
        gradedChart.setSelected(gradedStudents)
        gradedChart.setTotal(totalStudents)
        gradedChart.setSelectedColor(course.color)
        gradedChart.setUnselectedColor(Color(course.color).copy(alpha = 0.2f).toArgb())
        gradedChart.setCenterText(gradedStudents.toString())
        gradedWrapper.contentDescription = getString(R.string.content_description_submission_donut_graded).format(gradedStudents, totalStudents)
        gradedProgressBar.setGone()
        gradedChart.invalidate()

        ungradedChart.setSelected(needsGradingCount)
        ungradedChart.setTotal(totalStudents)
        ungradedChart.setSelectedColor(course.color)
        ungradedChart.setUnselectedColor(Color(course.color).copy(alpha = 0.2f).toArgb())
        ungradedChart.setCenterText(needsGradingCount.toString())
        ungradedLabel.text = requireContext().resources.getQuantityText(R.plurals.needsGradingNoQuantity, needsGradingCount)
        ungradedWrapper.contentDescription = getString(R.string.content_description_submission_donut_needs_grading).format(needsGradingCount, totalStudents)
        ungradedProgressBar.setGone()
        ungradedChart.invalidate()

        notSubmittedChart.setSelected(notSubmitted)
        notSubmittedChart.setTotal(totalStudents)
        notSubmittedChart.setSelectedColor(course.color)
        notSubmittedChart.setUnselectedColor(Color(course.color).copy(alpha = 0.2f).toArgb())
        notSubmittedChart.setCenterText(notSubmitted.toString())
        notSubmittedWrapper.contentDescription = getString(R.string.content_description_submission_donut_unsubmitted).format(notSubmitted, totalStudents)
        notSubmittedProgressBar.setGone()
        notSubmittedChart.invalidate()
    }

    private fun setupDescription(quiz: Quiz) = with(binding) {
        noInstructionsTextView.setVisible(quiz.description.isNullOrBlank())

        // Show progress bar while loading description
        instructionsProgressBar.announceForAccessibility(getString(R.string.loading))
        instructionsProgressBar.setVisible()
        instructionsWebViewWrapper.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress >= 100) {
                    instructionsProgressBar?.setGone()
                }
            }
        }

        instructionsWebViewWrapper.webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(requireActivity(), url)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {}
            override fun onPageFinishedCallback(webView: WebView, url: String) {}
            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)

        }

        instructionsWebViewWrapper.webView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) =
                requireActivity().startActivity(InternalWebViewActivity.createIntent(requireActivity(), url, "", true))

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }

        //make the WebView background transparent
        instructionsWebViewWrapper.setBackgroundColor(0)
        instructionsWebViewWrapper.setBackgroundResource(android.R.color.transparent)

        // Load instructions
        loadHtmlJob = instructionsWebViewWrapper.webView.loadHtmlWithIframes(requireContext(), quiz.description, {
            instructionsWebViewWrapper.loadHtml(it, quiz.title, baseUrl = this@QuizDetailsFragment.quiz.htmlUrl)
        }) {
            RouteMatcher.route(requireActivity(), LtiLaunchFragment.makeSessionlessLtiUrlRoute(requireActivity(), canvasContext, it))
        }
    }

    private fun setupListeners(quiz: Quiz) = with(binding) {
        dueLayout.setOnClickListener {
            if(quiz._assignment != null) {
                val args = DueDatesFragment.makeBundle(quiz._assignment!!)
                RouteMatcher.route(requireActivity(), Route(null, DueDatesFragment::class.java, course, args))
            }
        }

        submissionsLayout.setOnClickListener {
            navigateToSubmissions(course, quiz._assignment, SubmissionListFilter.ALL)
        }
        donutGroup.viewAllSubmissions.onClick { submissionsLayout.performClick() } // Separate click listener for a11y
        donutGroup.gradedWrapper.setOnClickListener {
            navigateToSubmissions(course, quiz._assignment, SubmissionListFilter.GRADED)
        }
        donutGroup.ungradedWrapper.setOnClickListener {
            navigateToSubmissions(course, quiz._assignment, SubmissionListFilter.NOT_GRADED)
        }
        donutGroup.notSubmittedWrapper.setOnClickListener {
            navigateToSubmissions(course, quiz._assignment, SubmissionListFilter.MISSING)
        }
        noInstructionsTextView.setOnClickListener {
            openEditPage(quiz)
        }

        ViewStyler.themeButton(quizPreviewButton.quizPreviewButton)
        quizPreviewButton.quizPreviewButton.setOnClickListener {
            try {
                var urlStr = this@QuizDetailsFragment.quiz.htmlUrl +"/take?preview=1&persist_headless=1"
                val url = URL(urlStr)
                val uri = URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref)
                urlStr = uri.toASCIIString()
                val args = QuizPreviewWebviewFragment.makeBundle(urlStr, getString(R.string.quizPreview))
                RouteMatcher.route(requireActivity(), Route(QuizPreviewWebviewFragment::class.java, course, args))
            } catch (e: UnsupportedEncodingException) {}
        }
    }

    private fun navigateToSubmissions(course: Course, assignment: Assignment?, filter: SubmissionListFilter) {
        assignment ?: return // We can't navigate to the submission list if there isn't an associated assignment
        val assignmentWithAnonymousGrading = assignment.copy(anonymousGrading = quiz.allowAnonymousSubmissions)
        val args = SubmissionListFragment.makeBundle(assignmentWithAnonymousGrading, filter)
        RouteMatcher.route(requireActivity(), Route(null, SubmissionListFragment::class.java, course, args))
    }

    private fun clearListeners() = with(binding) {
        dueLayout.setOnClickListener {}
        submissionsLayout.setOnClickListener {}
        donutGroup.gradedWrapper.setOnClickListener {}
        donutGroup.ungradedWrapper.setOnClickListener {}
        donutGroup.notSubmittedWrapper.setOnClickListener {}
        noInstructionsTextView.setOnClickListener {}
    }

    private fun openEditPage(quiz: Quiz) {
        if(APIHelper.hasNetworkConnection()) {
            val args = EditQuizDetailsFragment.makeBundle(quiz, false)
            RouteMatcher.route(requireActivity(), Route(EditQuizDetailsFragment::class.java, course, args))
        } else {
            NoInternetConnectionDialog.show(requireFragmentManager())
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

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizEdited(event: QuizUpdatedEvent) {
        event.once(javaClass.simpleName) {
            if (it == presenter.mQuiz.id) {
                needToForceNetwork = true
                AssignmentUpdatedEvent(presenter.mQuiz.assignmentId, javaClass.simpleName).post()
            }
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizGraded(event: QuizSubmissionGradedEvent) {
        event.once(javaClass.simpleName) {
            if (presenter.mQuiz.assignmentId == it.assignmentId) needToForceNetwork = true
        }
    }

    companion object {
        @JvmStatic val QUIZ_ID = "quiz_details_quiz_id"
        @JvmStatic val QUIZ = "quiz_details_quiz"

        fun makeBundle(quizId: Long): Bundle = Bundle().apply { putLong(QuizDetailsFragment.QUIZ_ID, quizId) }

        fun makeBundle(quiz: Quiz): Bundle = Bundle().apply { putParcelable(QuizDetailsFragment.QUIZ, quiz) }

        fun newInstance(course: Course, args: Bundle) = QuizDetailsFragment().withArgs(args).apply { this.course = course }
    }
}
