/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.QuizSubmission.Companion.parseWorkflowState
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_quiz_start.*
import kotlinx.coroutines.Job
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.net.URLDecoder
import java.util.*

@PageView(url = "{canvasContext}/quizzes/{quizId}")
class QuizStartFragment : ParentFragment(), Bookmarkable {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var quiz: Quiz by ParcelableArg(key = Const.QUIZ)

    private val course: Course
        get() = canvasContext as Course

    private var quizSubmission: QuizSubmission? = null
    private var shouldStartQuiz = false
    private var shouldLetAnswer = true
    private var quizSubmissionTime: QuizSubmissionTime? = null

    private var quizSubmissionResponseCanvasCallback: StatusCallback<QuizSubmissionResponse>? = null
    private var quizStartResponseCallback: StatusCallback<QuizSubmissionResponse>? = null
    private var quizStartSessionCallback: StatusCallback<ResponseBody>? = null
    private var quizSubmissionTimeCanvasCallback: StatusCallback<QuizSubmissionTime>? = null

    private var webViewClientCallback: CanvasWebView.CanvasWebViewClientCallback? = null
    private var embeddedWebViewCallback: CanvasWebView.CanvasEmbeddedWebViewCallback? = null
    private var loadHtmlJob: Job? = null

    @PageViewUrlParam(name = "quizId")
    fun getQuizIdentifier(): Long = quiz.id

    override fun title(): String = quiz.title ?: getString(R.string.quizzes)

    // Currently there isn't a way to know how to decide if we want to route
    // to this fragment or the BasicQuizViewFragment.
    override val bookmark: Bookmarker
        get() = Bookmarker(true, canvasContext, quiz.htmlUrl).withParam(RouterParams.QUIZ_ID, quiz.id.toString())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater!!.inflate(R.layout.fragment_quiz_start, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupCallbacks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        quizSubmissionResponseCanvasCallback?.cancel()
        quizStartResponseCallback?.cancel()
        quizSubmissionTimeCanvasCallback?.cancel()
        quizSubmissionTimeCanvasCallback?.cancel()
        loadHtmlJob?.cancel()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loading.setVisible()
        QuizManager.getQuizSubmissions(course, quiz.id, true, quizSubmissionResponseCanvasCallback!!)
    }

    override fun applyTheme() {
        toolbar.setupAsBackButton(this)
        setupToolbarMenu(toolbar)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    // Called after submitting a quiz
    fun updateQuizInfo() {
        loading.setVisible()
        // Don't let them try to start the quiz until the data loads
        next.isEnabled = false
        quizSubmissionResponseCanvasCallback!!.reset() // Reset to clear out any link headers
        QuizManager.getQuizSubmissions(course, quiz.id, true, quizSubmissionResponseCanvasCallback!!)
    }

    private fun setupViews() {
        next.setOnClickListener(View.OnClickListener {
            if (quiz.lockedForUser) {
                if (quiz.lockExplanation != null) {
                    showToast(quiz.lockExplanation!!)
                }
                return@OnClickListener
            }

            if (shouldStartQuiz) {
                QuizManager.startQuiz(course, quiz.id, true, quizStartResponseCallback!!)
                // If the user hits the back button, we don't want them to try to start the quiz again
                shouldStartQuiz = false
            } else if (quizSubmission != null) {
                showQuiz()
            } else {
                getLockedMessage()
            }
        })

        quiz_results.setOnClickListener {
            // We don't want it to route internally, it will pop open the sliding drawer and route back the to same place
            RouteMatcher.route(requireActivity(), InternalWebviewFragment.makeRoute(course, quiz.url!!, false, false, true))
        }
    }

    fun populateQuizInfo() {
        quiz_title.text = quiz.title
        toolbar.title = title()

        loadHtmlJob = quiz_details.loadHtmlWithIframes(requireContext(), isTablet,
                quiz.description.orEmpty(), ::loadQuizHtml, {
            val args = LTIWebViewFragment.makeLTIBundle(
                    URLDecoder.decode(it, "utf-8"), getString(R.string.utils_externalToolTitle), true)
            RouteMatcher.route(requireContext(), Route(LTIWebViewFragment::class.java, canvasContext, args))
        }, quiz.title)

        quiz_details.setBackgroundColor(Color.TRANSPARENT)
        // Set some callbacks in case there is a link in the quiz description. We want it to open up in a new InternalWebViewFragment
        quiz_details.canvasEmbeddedWebViewCallback = embeddedWebViewCallback
        quiz_details.canvasWebViewClientCallback = webViewClientCallback
        quiz_question_count_details.text = NumberHelper.formatInt(quiz.questionCount.toLong())
        quiz_points_details.text = NumberHelper.formatDecimal(quiz.pointsPossible?.toDoubleOrNull() ?: 0.0, 2, true)

        if (quiz.allowedAttempts == -1) {
            quiz_attempt_details.text = getString(R.string.unlimited)
        } else {
            quiz_attempt_details.text = NumberHelper.formatInt(quiz.allowedAttempts.toLong())
        }

        if (quiz.dueAt != null) {
            quiz_due_details.text = DateHelper.getDateTimeString(requireActivity(), quiz.dueDate)
        } else {
            quiz_due_details.text = getString(R.string.toDoNoDueDate)
        }

        if (quiz.unlockAt != null) {
            quiz_unlocked.text = getString(R.string.unlockedAt)
            quiz_unlocked_details.text = DateHelper.getDateTimeString(requireActivity(), quiz.unlockDate)
        } else {
            quiz_unlocked_container.setGone()
        }

        if (quiz.timeLimit != 0) {
            quiz_time_limit.text = getString(R.string.timeLimit)
            quiz_time_limit_details.text = NumberHelper.formatInt(quiz.timeLimit.toLong())
        } else {
            quiz_time_limit_container.setGone()
        }
    }

    private fun loadQuizHtml(html: String, contentDescription: String?) {
        quiz_details.loadHtml(html, contentDescription)
    }

    private fun setupCallbacks() {
        webViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                openMedia(mime, url, filename, canvasContext)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {}
            override fun onPageStartedCallback(webView: WebView, url: String) {}

            override fun canRouteInternallyDelegate(url: String): Boolean {
                return RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, false)
            }

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, true)
            }
        }

        embeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) {
                InternalWebviewFragment.loadInternalWebView(activity, InternalWebviewFragment.makeRoute(course, url, false))
            }
            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }

        quizSubmissionTimeCanvasCallback = object : StatusCallback<QuizSubmissionTime>() {
            override fun onResponse(response: Response<QuizSubmissionTime>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type == ApiType.CACHE) return
                this@QuizStartFragment.quizSubmissionTime = response.body()
                QuizManager.getQuizSubmissions(course, quiz.id, true, quizSubmissionResponseCanvasCallback!!)
            }
        }
        quizSubmissionResponseCanvasCallback = object : StatusCallback<QuizSubmissionResponse>() {
            override fun onResponse(response: Response<QuizSubmissionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type == ApiType.CACHE) return


                // Since this is a student app, make sure they only have their own submissions (if they're siteadmin it'll be different)
                val submissions = ArrayList<QuizSubmission>()
                val user = ApiPrefs.user
                if (user != null) {
                    for (submission in response.body()!!.quizSubmissions) {
                        if (submission.userId == user.id) {
                            submissions.add(submission)
                        }
                    }
                }

                val quizSubmissionResponse = response.body()?.copy(quizSubmissions = submissions)
                if (quizSubmissionResponse?.quizSubmissions == null || quizSubmissionResponse.quizSubmissions.isEmpty()) {
                    // No quiz submissions, let the user start the quiz.

                    // They haven't turned it in yet, so don't show the turned-in view
                    quiz_turned_in_container.setGone()
                    shouldStartQuiz = true
                    next.setVisible()
                    next.isEnabled = true
                } else {
                    // We should have at least 1 submission
                    quizSubmission = quizSubmissionResponse.quizSubmissions[quizSubmissionResponse.quizSubmissions.size - 1]
                    next?.isEnabled = true

                    val hasUnlimitedAttempts = quiz.allowedAttempts == -1
                    val teacherUnlockedQuizAttempts = quizSubmission!!.manuallyUnlocked // Teacher can manually unlock a quiz for an individual student
                    val hasMoreAttemptsLeft = quizSubmission!!.attemptsLeft > 0

                    val canTakeQuizAgain = hasUnlimitedAttempts or teacherUnlockedQuizAttempts or hasMoreAttemptsLeft

                    if (quiz.hideResults == Quiz.HideResultsType.ALWAYS.apiString && !canTakeQuizAgain) {
                        // Don't let the user see the questions if they've exceeded their attempts
                        next.setGone()
                    } else if (quiz.hideResults == Quiz.HideResultsType.AFTER_LAST_ATTEMPT.apiString && !canTakeQuizAgain) {
                        // They can only see the results after their last attempt, and that hasn't happened yet
                        next.setGone()
                    }

                    // They can -take- the quiz if there's no finished time and they have attempts left, OR the teacher has unlocked the quiz for them

                    // If they've finished the quiz and have no more attempt chances, or the teacher has locked the quiz, then they're done
                    // -1 allowed attempts == unlimited
                    if (quizSubmission!!.finishedAt != null && !canTakeQuizAgain) {
                        // They've finished the quiz and they can't take it anymore; let them see results
                        next.setVisible()
                        next.text = getString(R.string.viewQuestions)
                        shouldLetAnswer = false
                    } else {
                        // They are allowed to take the quiz...
                        next.setVisible()

                        if (quizSubmission!!.finishedAt != null) {
                            shouldStartQuiz = true
                            next.text = getString(R.string.takeQuizAgain)
                        } else {
                            // Let the user resume their quiz
                            next.text = getString(R.string.resumeQuiz)
                        }
                    }

                    if (quizSubmission!!.finishedAt != null) {
                        quiz_turned_in.text = getString(R.string.turnedIn)
                        quiz_turned_in_details.text = DateHelper.getDateTimeString(requireActivity(), quizSubmission!!.finishedDate)
                        // The user has turned in the quiz, let them see the results
                        quiz_results.setVisible()
                    } else {
                        quiz_turned_in_container.setGone()
                    }

                    // Weird hack where if the time expires and the user hasn't submitted it doesn't let you start the quiz
                    if (parseWorkflowState(quizSubmission!!.workflowState!!) == QuizSubmission.WorkflowState.UNTAKEN && quizSubmission!!.endAt != null && quizSubmissionTime != null && quizSubmissionTime!!.timeLeft > 0) {
                        next.isEnabled = false
                        // Submit the quiz for them
                        QuizManager.submitQuiz(course, quizSubmission!!, true, object : StatusCallback<QuizSubmissionResponse>() {
                            override fun onResponse(response: Response<QuizSubmissionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                                if (type == ApiType.CACHE) return
                                // The user has turned in the quiz, let them see the results
                                quiz_results.setVisible()
                                next.isEnabled = true
                                shouldStartQuiz = true
                                next.text = getString(R.string.takeQuizAgain)


                                // Since this is a student app, make sure they only have their own submissions (if they're siteadmin it'll be different)
                                val submissionsVal = ArrayList<QuizSubmission>()
                                val userVal = ApiPrefs.user
                                if (userVal != null) {
                                    for (submission in response.body()!!.quizSubmissions) {
                                        if (submission.userId == userVal.id) {
                                            submissions.add(submission)
                                        }
                                    }
                                }

                                val quizResponse = response.body()?.copy(quizSubmissions = submissionsVal)

                                if (quizResponse?.quizSubmissions != null && quizResponse.quizSubmissions.isNotEmpty() == true) {
                                    quizSubmission = quizResponse.quizSubmissions[quizResponse.quizSubmissions.size - 1]
                                }
                            }
                        })
                    }

                    // If the user can only see results once and they have seen it, don't let them view the questions
                    if (quiz.oneTimeResults && quizSubmission!!.hasSeenResults && !canTakeQuizAgain) {
                        next.setGone()
                    }

                    if (quiz.lockedForUser) {
                        shouldStartQuiz = false
                        next.text = getString(R.string.assignmentLocked)
                    }
                }

                populateQuizInfo()

                loading.setGone()
            }

            override fun onFail(call: Call<QuizSubmissionResponse>?, error: Throwable, response: Response<*>?) {
                loading.setGone()
                // If a quiz is excused we get a 401 error when trying to get the submissions. This is a workaround until we have an excused field
                // on quizzes.
                if (response != null && response.code() == 401) {
                    populateQuizInfo()
                    // There is a not authorized error, so don't let them start the quiz
                    next.setGone()
                }
            }
        }

        quizStartResponseCallback = object : StatusCallback<QuizSubmissionResponse>() {

            override fun onResponse(response: Response<QuizSubmissionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (response.code() == 200 && type == ApiType.API) {
                    // We want to show the quiz here, but we need to get the quizSubmissionId first so our
                    // api call for the QuizQuestionsFragment knows which questions to get
                    val quizSubmissionResponseCallback = object : StatusCallback<QuizSubmissionResponse>() {

                        override fun onResponse(response: Response<QuizSubmissionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                            val quizSubmissionResponse = response.body()
                            if (quizSubmissionResponse != null && quizSubmissionResponse.quizSubmissions != null &&
                                    quizSubmissionResponse.quizSubmissions.size > 0) {
                                quizSubmission = quizSubmissionResponse.quizSubmissions[quizSubmissionResponse.quizSubmissions.size - 1]
                                if (quizSubmission != null) {
                                    showQuiz()
                                } else {
                                    getLockedMessage()
                                }
                            }
                        }
                    }

                    QuizManager.getFirstPageQuizSubmissions(course, quiz.id, false, quizSubmissionResponseCallback)
                }
            }

            override fun onFail(call: Call<QuizSubmissionResponse>?, error: Throwable, response: Response<*>?) {
                if (response != null && response.code() == 403) {
                    // Forbidden
                    // Check to see if it's because of IP restriction or bad access code or either
                    getLockedMessage()
                }
            }
        }

        quizStartSessionCallback = object : StatusCallback<ResponseBody>() {

            // Alerting the user that we couldn't post the start session event doesn't really make sense. If something went wrong the logs will
            // be off on the admin/teacher side
        }
    }

    private fun getLockedMessage() {
        // Check to see if it's because of IP restriction or bad access code or either
        if (quiz.ipFilter != null && quiz.accessCode == null) {
            showToast(R.string.lockedIPAddress)
        } else if (quiz.ipFilter == null && quiz.accessCode != null) {
            showToast(R.string.lockedInvalidAccessCode)
        } else {
            // Something went wrong (no data possibly)
            showToast(R.string.cantStartQuiz)
        }
    }

    private fun showQuiz() {
        val navigation = navigation
        if (navigation != null) {
            // Post the android session started event
            QuizManager.postQuizStartedEvent(canvasContext, quizSubmission!!.quizId, quizSubmission!!.id, true, quizStartSessionCallback!!)
            RouteMatcher.route(requireActivity(), QuizQuestionsFragment.makeRoute(canvasContext, quiz, quizSubmission, shouldLetAnswer))
        }
    }

    companion object {

        fun newInstance(route: Route): QuizStartFragment? {
            return if(validRoute(route)) QuizStartFragment().apply {
                arguments = route.arguments

                with(nonNullArgs) {
                    if (containsKey(Const.QUIZ)) quiz = getParcelable(Const.QUIZ)
                }

                this.canvasContext = route.canvasContext!!
            } else null
        }

        private fun validRoute(route: Route): Boolean {
            return route.canvasContext != null && route.arguments.containsKey(Const.QUIZ)
        }

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, quiz: Quiz): Route {
            return Route(null, QuizStartFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply { putParcelable(Const.QUIZ, quiz) }))
        }
    }
}
