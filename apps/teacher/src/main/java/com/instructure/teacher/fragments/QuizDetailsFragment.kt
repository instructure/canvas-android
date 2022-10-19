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
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.*
import com.instructure.interactions.Identity
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_EDIT_QUIZ_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BasePresenterFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.dialog.NoInternetConnectionDialog
import com.instructure.teacher.events.AssignmentGradedEvent
import com.instructure.teacher.events.QuizUpdatedEvent
import com.instructure.teacher.events.post
import com.instructure.teacher.factory.QuizDetailsPresenterFactory
import com.instructure.teacher.presenters.AssignmentSubmissionListPresenter.SubmissionListFilter
import com.instructure.teacher.presenters.QuizDetailsPresenter
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.*
import com.instructure.teacher.view.QuizSubmissionGradedEvent
import com.instructure.teacher.viewinterface.QuizDetailsView
import kotlinx.android.synthetic.main.fragment_quiz_details.*
import kotlinx.android.synthetic.main.view_quiz_preview_button.*
import kotlinx.android.synthetic.main.view_submissions_donut_group.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URL
import java.util.*

@ScreenView(SCREEN_VIEW_EDIT_QUIZ_DETAILS)
class QuizDetailsFragment : BasePresenterFragment<
        QuizDetailsPresenter,
        QuizDetailsView>(),
        QuizDetailsView, Identity {

    private var mCourse: Course by ParcelableArg(default = Course())
    private var mQuizId: Long by LongArg(0L, QUIZ_ID)
    private var mQuiz: Quiz by ParcelableArg(Quiz(), QUIZ)

    private var mNeedToForceNetwork = false

    private var loadHtmlJob: Job? = null

    override fun layoutResId(): Int = R.layout.fragment_quiz_details

    override fun getPresenterFactory() = QuizDetailsPresenterFactory(mCourse, mQuiz)

    override fun onDestroyView() {
        super.onDestroyView()
        loadHtmlJob?.cancel()
    }

    override fun onReadySetGo(presenter: QuizDetailsPresenter) {
        if (mQuizId == 0L) {
            // No Quiz ID so we must have a quiz
            presenter.loadData(mNeedToForceNetwork)
        } else {
            // No Quiz, we need to get it
            presenter.getQuiz(mQuizId, mCourse, true)
        }

        setupToolbar()
    }

    override fun onPresenterPrepared(presenter: QuizDetailsPresenter) = Unit

    override fun onRefreshFinished() = Unit

    override fun onRefreshStarted() {
        toolbar.menu.clear()
        clearListeners()
    }

    override val identity: Long? get() = if(mQuizId != 0L) mQuizId else mQuiz.id
    override val skipCheck: Boolean get() = false

    override fun populateQuizDetails(quiz: Quiz) {
        mQuiz = quiz
        toolbar.setupMenu(R.menu.menu_edit_generic) { openEditPage(quiz) }
        swipeRefreshLayout.isRefreshing = false
        setupViews(quiz)
        setupListeners(quiz)
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, mCourse.backgroundColor, requireContext().getColor(R.color.white))

        fullDateDetailsButton.setVisible(quiz._assignment != null)
    }

    private fun setupToolbar() {
        toolbar.setupBackButtonWithExpandCollapseAndBack(this) {
            toolbar.updateToolbarExpandCollapseIcon(this)
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, mCourse.backgroundColor, requireContext().getColor(R.color.white))
            (activity as MasterDetailInteractions).toggleExpandCollapse()
        }

        toolbar.title = getString(R.string.quiz_details)
        if (!isTablet) {
            toolbar.subtitle = presenter.mCourse.name
        }
        ViewStyler.themeToolbarColored(requireActivity(), toolbar, mCourse.backgroundColor, requireContext().getColor(R.color.white))
    }

    private fun setupViews(quiz: Quiz) = with(quiz) {
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
        quizTitleTextView.text = title

        // Points possible
        pointsPossible?.let {
            pointsTextView.text = resources.getQuantityString(
                    R.plurals.quantityPointsAbbreviated,
                    pointsPossible?.toDouble()?.toInt() ?: 1,
                    NumberHelper.formatDecimal(pointsPossible!!.toDouble(), 1, true)
            )
        }

        pointsTextView.contentDescription = if (pointsPossible != null) {
            resources.getQuantityString(
                    R.plurals.quantityPointsFull,
                    pointsPossible?.toDouble()?.toInt() ?: 1,
                    NumberHelper.formatDecimal(pointsPossible!!.toDouble(), 1, true))
        } else getString(R.string.quiz_details_no_points_assigned)

        // Publish status
        if (published) {
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

        allDates.singleOrNull()?.apply {
            if (lockDate?.before(Date()) == true) {
                availabilityLayout.setVisible()
                availabilityTextView.setText(R.string.closed)
            } else {
                availableFromLayout.setVisible()
                availableToLayout.setVisible()
                availableFromTextView.text = if (unlockDate != null)
                    DateHelper.getMonthDayAtTime(requireContext(), unlockDate, atSeparator) else getString(R.string.no_date_filler)
                availableToTextView.text = if (lockDate != null)
                    DateHelper.getMonthDayAtTime(requireContext(), lockDate, atSeparator) else getString(R.string.no_date_filler)
            }
        }

        // Due date(s)
        if (allDates.size > 1) {
            otherDueDateTextView.setVisible()
            otherDueDateTextView.setText(R.string.multiple_due_dates)
        } else {
            if (allDates.size == 0 || allDates[0].dueAt == null) {
                otherDueDateTextView.setVisible()
                otherDueDateTextView.setText(R.string.no_due_date)

                dueForLayout.setVisible()
                dueForTextView.text = if (allDates.size == 0 || allDates[0].isBase) getString(R.string.everyone) else allDates[0].title ?: ""

            } else with(allDates[0]) {
                dueDateLayout.setVisible()
                dueDateTextView.text = DateHelper.getMonthDayAtTime(requireContext(), dueDate, atSeparator)

                dueForLayout.setVisible()
                dueForTextView.text = if (isBase) getString(com.instructure.teacher.R.string.everyone) else title ?: ""
            }
        }

        // If the user is a designer we don't want to show the submissions layout
        submissionsLayout.setVisible(!mCourse.isDesigner)


        if (!isGradeable) {
            // Quiz is not gradeable, don't show submission dials
            submissionsLayout.setGone()
            submissionsHR.setGone()
        }

        // Description
        setupDescription(this)

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
    override fun updateSubmissionDonuts(totalStudents: Int, gradedStudents: Int, needsGradingCount: Int, notSubmitted: Int) {

        // Submission section
        gradedChart.setSelected(gradedStudents)
        gradedChart.setTotal(totalStudents)
        gradedChart.setSelectedColor(ThemePrefs.brandColor)
        gradedChart.setCenterText(gradedStudents.toString())
        gradedWrapper.contentDescription = getString(R.string.content_description_submission_donut_graded).format(gradedStudents, totalStudents)
        gradedProgressBar.setGone()
        gradedChart.invalidate()

        ungradedChart.setSelected(needsGradingCount)
        ungradedChart.setTotal(totalStudents)
        ungradedChart.setSelectedColor(ThemePrefs.brandColor)
        ungradedChart.setCenterText(needsGradingCount.toString())
        ungradedLabel.text = requireContext().resources.getQuantityText(R.plurals.needsGradingNoQuantity, needsGradingCount)
        ungradedWrapper.contentDescription = getString(R.string.content_description_submission_donut_needs_grading).format(needsGradingCount, totalStudents)
        ungradedProgressBar.setGone()
        ungradedChart.invalidate()

        notSubmittedChart.setSelected(notSubmitted)
        notSubmittedChart.setTotal(totalStudents)
        notSubmittedChart.setSelectedColor(ThemePrefs.brandColor)
        notSubmittedChart.setCenterText(notSubmitted.toString())
        notSubmittedWrapper.contentDescription = getString(R.string.content_description_submission_donut_unsubmitted).format(notSubmitted, totalStudents)
        notSubmittedProgressBar.setGone()
        notSubmittedChart.invalidate()
    }

    private fun setupDescription(quiz: Quiz) {
        // Show "No description" layout if there is no description
        if (quiz.description.isNullOrBlank()) {
            noInstructionsTextView.setVisible()
            return
        }

        // Show progress bar while loading description
        instructionsProgressBar.announceForAccessibility(getString(R.string.loading))
        instructionsProgressBar.setVisible()
        instructionsWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress >= 100) {
                    instructionsProgressBar?.setGone()
                }
            }
        }

        instructionsWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
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

        instructionsWebView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) =
                requireActivity().startActivity(InternalWebViewActivity.createIntent(requireActivity(), url, "", true))

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }

        //make the WebView background transparent
        instructionsWebView.setBackgroundColor(0)
        instructionsWebView.setBackgroundResource(android.R.color.transparent)

        // Load instructions
        loadHtmlJob = instructionsWebView.loadHtmlWithIframes(requireContext(), quiz.description, {
            instructionsWebView.loadHtml(it, quiz.title, baseUrl = mQuiz.htmlUrl)
        }) {
            LtiLaunchFragment.routeLtiLaunchFragment(requireContext(), canvasContext, it)
        }
    }

    private fun setupListeners(quiz: Quiz) {
        dueLayout.setOnClickListener {
            if(quiz._assignment != null) {
                val args = DueDatesFragment.makeBundle(quiz._assignment!!)
                RouteMatcher.route(requireContext(), Route(null, DueDatesFragment::class.java, mCourse, args))
            }
        }

        submissionsLayout.setOnClickListener {
            navigateToSubmissions(mCourse, quiz._assignment, SubmissionListFilter.ALL)
        }
        viewAllSubmissions.onClick { submissionsLayout.performClick() } // Separate click listener for a11y
        gradedWrapper.setOnClickListener {
            navigateToSubmissions(mCourse, quiz._assignment, SubmissionListFilter.GRADED)
        }
        ungradedWrapper.setOnClickListener {
            navigateToSubmissions(mCourse, quiz._assignment, SubmissionListFilter.NOT_GRADED)
        }
        notSubmittedWrapper.setOnClickListener {
            navigateToSubmissions(mCourse, quiz._assignment, SubmissionListFilter.MISSING)
        }
        noInstructionsTextView.setOnClickListener {
            openEditPage(quiz)
        }

        ViewStyler.themeButton(quizPreviewButton)
        quizPreviewButton.setOnClickListener {
            try {
                var urlStr = mQuiz.htmlUrl +"/take?preview=1&persist_headless=1"
                val url = URL(urlStr)
                val uri = URI(url.protocol, url.userInfo, url.host, url.port, url.path, url.query, url.ref)
                urlStr = uri.toASCIIString()
                val args = QuizPreviewWebviewFragment.makeBundle(urlStr, getString(R.string.quizPreview))
                RouteMatcher.route(requireContext(), Route(QuizPreviewWebviewFragment::class.java, mCourse, args))
            } catch (e: UnsupportedEncodingException) {}
        }
    }

    private fun navigateToSubmissions(course: Course, assignment: Assignment?, filter: SubmissionListFilter) {
        assignment ?: return // We can't navigate to the submission list if there isn't an associated assignment
        val assignmentWithAnonymousGrading = assignment.copy(anonymousGrading = mQuiz.allowAnonymousSubmissions)
        val args = AssignmentSubmissionListFragment.makeBundle(assignmentWithAnonymousGrading, filter)
        RouteMatcher.route(requireContext(), Route(null, AssignmentSubmissionListFragment::class.java, course, args))
    }

    private fun clearListeners() {
        dueLayout.setOnClickListener {}
        submissionsLayout.setOnClickListener {}
        gradedWrapper.setOnClickListener {}
        ungradedWrapper.setOnClickListener {}
        notSubmittedWrapper.setOnClickListener {}
        noInstructionsTextView.setOnClickListener {}
    }


    fun openEditPage(quiz: Quiz) {
        if(APIHelper.hasNetworkConnection()) {
            val args = EditQuizDetailsFragment.makeBundle(quiz, false)
            RouteMatcher.route(requireContext(), Route(EditQuizDetailsFragment::class.java, mCourse, args))
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
            if (it == presenter.mQuiz.id) mNeedToForceNetwork = true
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onQuizGraded(event: QuizSubmissionGradedEvent) {
        event.once(javaClass.simpleName) {
            if (presenter.mQuiz.assignmentId == it.assignmentId) mNeedToForceNetwork = true
        }
    }

    companion object {
        @JvmStatic val QUIZ_ID = "quiz_details_quiz_id"
        @JvmStatic val QUIZ = "quiz_details_quiz"

        fun makeBundle(quizId: Long): Bundle = Bundle().apply { putLong(QuizDetailsFragment.QUIZ_ID, quizId) }

        fun makeBundle(quiz: Quiz): Bundle = Bundle().apply { putParcelable(QuizDetailsFragment.QUIZ, quiz) }

        fun newInstance(course: Course, args: Bundle) = QuizDetailsFragment().withArgs(args).apply { mCourse = course }
    }
}
