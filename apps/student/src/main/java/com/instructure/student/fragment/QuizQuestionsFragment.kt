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

import android.animation.ValueAnimator
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.Chronometer
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.router.Route
import com.instructure.pandautils.dialogs.InstAlertDialog
import com.instructure.pandautils.dialogs.UploadFilesDialog
import com.instructure.pandautils.services.FileUploadService
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.adapter.QuizSubmissionQuestionListRecyclerAdapter
import com.instructure.student.dialog.QuizQuestionDialog
import com.instructure.student.interfaces.QuizFileUploadListener
import com.instructure.student.util.PreCachingLayoutManager
import com.instructure.student.view.ViewUtils
import kotlinx.android.synthetic.main.fragment_quiz_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Response
import java.util.*

@PageView(url = "{canvasContext}/quizzes/{quizId}/take")
class QuizQuestionsFragment : ParentFragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var quiz: Quiz? by NullableParcelableArg(key = Const.QUIZ)
    private var quizSubmission: QuizSubmission? by NullableParcelableArg(key = Const.QUIZ_SUBMISSION)
    private var shouldLetAnswer: Boolean by BooleanArg(key = Const.QUIZ_SHOULD_LET_ANSWER)

    private val course: Course
        get() = canvasContext as Course

    private var autoSubmitReason: AUTO_SUBMIT_REASON? = null

    private var quizQuestionAdapter: QuizSubmissionQuestionListRecyclerAdapter? = null

    private var shouldShowTimer = true

    private var anim: ValueAnimator? = null
    private var countDownTimer: CountDownTimer? = null
    private var mQuizSubmissionTime: QuizSubmissionTime? = null

    //callback
    private var quizSubmissionQuestionResponseCanvasCallback: StatusCallback<QuizSubmissionQuestionResponse>? = null
    private var submitQuizCallback: StatusCallback<QuizSubmissionResponse>? = null
    private var quizSubmissionTimeCanvasCallback: StatusCallback<QuizSubmissionTime>? = null
    private var quizFileUploadListener: QuizFileUploadListener? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network?) {
            if (!APIHelper.hasNetworkConnection()) {
                InstAlertDialog.newInstance(requireContext(), R.string.quizNetworkErrorTitle, R.string.quizNetworkErrorMessage, R.string.dismiss)
                        .apply { isCancelable = false }
                        .show(requireFragmentManager()) {
                            requireActivity().onBackPressed()
                        }
            }
        }
    }

    @PageViewUrlParam(name = "quizId")
    private fun quizIdValue(): Long = if (quiz != null) quiz!!.id else 0

    private enum class AUTO_SUBMIT_REASON {
        TIMED_QUIZ, DUE_DATE, LOCK_DATE
    }

    override fun title(): String = quiz?.title ?: getString(R.string.quizzes)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (quizQuestionAdapter != null) {
            //we might be coming back from a link that opened a webview, and we don't want to add duplicates.
            quizQuestionAdapter!!.clear()
        }

        if (quizSubmission != null && !quiz!!.requireLockdownBrowserForResults) {
            QuizManager.getFirstPageSubmissionQuestions(quizSubmission!!.id, true, quizSubmissionQuestionResponseCanvasCallback!!)
        } else {
            showToast(R.string.cantStartQuiz)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater?.inflate(R.layout.fragment_quiz_list, container, false)

    override fun onDestroyView() {
        super.onDestroyView()
        quizSubmissionTimeCanvasCallback?.cancel()
        submitQuizCallback?.cancel()
        quizSubmissionQuestionResponseCanvasCallback?.cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.title = title()

        view?.let {
            setupViews()
            setupCallbacks()
        }
    }

    override fun applyTheme() {
        toolbar.setupAsBackButton(this)
        setupToolbarMenu(toolbar)
        toolbar.menu.clear()

        toolbar.menu.add(Menu.NONE, R.id.toggleTimer, 0, R.string.toggleTimer)
                .setIcon(R.drawable.vd_timer)
                .setTitle(getString(R.string.toggleTimer))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        toolbar.menu.add(Menu.NONE, R.id.showFlaggedQuestions, 1, R.string.showFlaggedQuestions)
                .setIcon(R.drawable.vd_navigation_bookmarks)
                .setTitle(getString(R.string.showFlaggedQuestions))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }


    override fun onPause() {
        super.onPause()
        countDownTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        QuizManager.getQuizSubmissionTime(canvasContext, quizSubmission!!, true, quizSubmissionTimeCanvasCallback!!)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        APIHelper.registerNetworkCallback(networkCallback)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        APIHelper.unregisterNetworkCallback(networkCallback)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFileUploadStarted(event: QuizFileUploadStarted) {
        //update the adapter item
        event.get { (first, second) ->
            quizQuestionAdapter!!.setIsLoading(first, true, second)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFileUploadEvent(event: FileUploadEvent) {
        //update the adapter item
        event.get { notification ->
            val statusIntent = notification.intent
            if (statusIntent != null) {
                if (FileUploadService.QUIZ_UPLOAD_COMPLETE == statusIntent.action) {
                    if (statusIntent.hasExtra(Const.ATTACHMENT)) {
                        val questionId = statusIntent.getLongExtra(Const.QUIZ_ANSWER_ID, -1L)
                        val position = statusIntent.getIntExtra(Const.POSITION, -1)
                        val attachment = statusIntent.extras!!.get(Const.ATTACHMENT) as? Attachment
                        if (attachment != null && questionId != -1L) {
                            quizQuestionAdapter!!.setFileUploadForQuiz(questionId, attachment, position)
                        }
                    }
                } else if (FileUploadService.UPLOAD_ERROR == statusIntent.action) {
                    val bundle = statusIntent.extras
                    if (bundle != null && bundle.containsKey(Const.MESSAGE)) {
                        var errorMessage = bundle.getString(Const.MESSAGE)
                        if (null == errorMessage || "" == errorMessage) {
                            errorMessage = getString(R.string.errorUploadingFile)
                        }
                        showToast(errorMessage!!)
                    }

                    val questionId = statusIntent.getLongExtra(Const.QUIZ_ANSWER_ID, -1L)
                    if (questionId != -1L) {
                        val position = statusIntent.getIntExtra(Const.POSITION, -1)
                        quizQuestionAdapter!!.setIsLoading(questionId, false, position)
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.toggleTimer) {
            if (shouldShowTimer) {
                shouldShowTimer = false

                //setup animation
                //hide the timer by making the height 0
                if (anim == null) {
                    anim = ValueAnimator.ofInt(timerLayout.measuredHeight, 0)
                    anim!!.addUpdateListener { valueAnimator ->
                        val `val` = valueAnimator.animatedValue as Int
                        val layoutParams = timerLayout.layoutParams
                        layoutParams.height = `val`
                        timerLayout.layoutParams = layoutParams
                    }
                    anim!!.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                }
                anim!!.start()


            } else {
                shouldShowTimer = true

                //show the timer again by increasing the height
                anim!!.reverse()
            }
        } else if (item.itemId == R.id.showFlaggedQuestions) {
            if (quizQuestionAdapter == null) {
                showToast(R.string.errorOccurred)
                return true
            }
            val questions = ArrayList<QuizSubmissionQuestion>()

            for (i in 0 until quizQuestionAdapter!!.size()) {
                val quizQuestion = quizQuestionAdapter!!.getItemAtPosition(i)
                quizQuestion?.let { questions.add(it) }
            }

            val dialog = QuizQuestionDialog.newInstance(quizQuestionAdapter!!.answeredQuestions, questions, course)
            dialog.setLayoutManager(recyclerView.layoutManager)
            dialog.show(requireActivity().supportFragmentManager, QuizQuestionDialog.TAG)

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViews() {
        //Setup layout manager
        val layoutManager = PreCachingLayoutManager(requireActivity())
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.setExtraLayoutSpace(ViewUtils.getWindowHeight(activity))

        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun setupCallbacks() {
        quizFileUploadListener = QuizFileUploadListener { quizQuestionId, position ->
            val bundle = UploadFilesDialog.createQuizFileBundle(quizQuestionId, course.id, quiz!!.id, position)
            UploadFilesDialog.show(fragmentManager, bundle, { _ -> })
        }

        quizSubmissionQuestionResponseCanvasCallback = object : StatusCallback<QuizSubmissionQuestionResponse>() {

            override fun onResponse(response: Response<QuizSubmissionQuestionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type == ApiType.CACHE) return

                val questions = response.body()!!.quizSubmissionQuestions

                //sort the questions based on position that is part of the model object
                if (questions != null) {
                    Collections.sort(questions, Comparator { lh, rh ->
                        if (lh != null && rh != null) {
                            if (lh.position < rh.position) {
                                return@Comparator -1
                            } else if (lh.position > rh.position) {
                                return@Comparator 1
                            }
                        }
                        0
                    })
                }
                if (quizQuestionAdapter == null) {
                    quizQuestionAdapter = QuizSubmissionQuestionListRecyclerAdapter(requireActivity(),
                            questions!!, course, shouldLetAnswer, quizSubmission, quizFileUploadListener)
                } else {
                    quizQuestionAdapter!!.addAll(questions)
                }
                //cache the views after we view them
                recyclerView.setItemViewCacheSize(response.body()!!.quizSubmissionQuestions!!.size)
                recyclerView.adapter = quizQuestionAdapter

                if (linkHeaders.nextUrl != null) {
                    QuizManager.getNextPageSubmissionQuestions(linkHeaders.nextUrl!!, false, quizSubmissionQuestionResponseCanvasCallback!!)
                }
            }
        }

        submitQuizCallback = object : StatusCallback<QuizSubmissionResponse>() {
            override fun onResponse(response: Response<QuizSubmissionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type == ApiType.CACHE) return

                when (autoSubmitReason) {
                    QuizQuestionsFragment.AUTO_SUBMIT_REASON.TIMED_QUIZ -> showToast(R.string.submitReasonTimedQuiz)
                    QuizQuestionsFragment.AUTO_SUBMIT_REASON.DUE_DATE -> showToast(R.string.quizSubmittedSuccessfully)
                    QuizQuestionsFragment.AUTO_SUBMIT_REASON.LOCK_DATE -> showToast(R.string.submitReasonLockAt)
                    else -> showToast(R.string.quizSubmittedSuccessfully)
                }

                chronometer.stop()
                countDownTimer?.cancel()
                requireActivity().onBackPressed()
            }
        }

        quizSubmissionTimeCanvasCallback = object : StatusCallback<QuizSubmissionTime>() {
            override fun onResponse(response: Response<QuizSubmissionTime>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type == ApiType.CACHE) return

                mQuizSubmissionTime = response.body()
                if (shouldLetAnswer && isAdded) {

                    if (quiz!!.timeLimit == 0 && quiz!!.dueAt == null && quiz!!.lockAt == null) {
                        normalTimer()

                    } else if (quiz!!.timeLimit > 0) {
                        timeLimitCountDown(mQuizSubmissionTime!!.timeLeft)
                    } else if (quiz!!.dueAt != null && (quiz!!.lockAt == null || quiz!!.dueDate!!.before(quiz!!.lockDate))) {
                        //if we have a due date, we want to give them an option to submit it when it gets to that point, but only if the due date is before the lock date
                        autoSubmitReason = AUTO_SUBMIT_REASON.DUE_DATE

                        countDownForSubmit((mQuizSubmissionTime!!.timeLeft * MILLISECOND).toLong())

                    } else if (quiz!!.lockAt != null) {
                        autoSubmitReason = AUTO_SUBMIT_REASON.LOCK_DATE

                        countDownForSubmit((mQuizSubmissionTime!!.timeLeft * MILLISECOND).toLong())
                    }
                } else {
                    val minutes = Math.ceil(quizSubmission!!.timeSpent.toDouble() / 60).toInt()

                    chronometer.visibility = View.GONE
                    timer.text = String.format(Locale.getDefault(), getString(R.string.timeSpentFormat), minutes)
                }
            }
        }
    }

    //just count up, no due date, no lock date
    private fun normalTimer() {
        timer.visibility = View.GONE
        //count up if they haven't turned it in

        chronometer.visibility = View.VISIBLE

        chronometer.onChronometerTickListener = Chronometer.OnChronometerTickListener {
            val elapsed = System.currentTimeMillis() - quizSubmission!!.startedDate!!.time
            // if the user has set a custom time the date on their device could be set to before the started time of the quiz.
            // if this is the case then just start from 0.
            if (elapsed >= 0) {
                val seconds = elapsed / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24
                val timeValue: String
                if (days == 1L) {
                    timeValue = String.format("%d %s %02d:%02d:%02d", days, getString(R.string.calendarDay), hours % 24, minutes % SECONDS_IN_MINUTE, seconds % SECONDS_IN_MINUTE)
                } else if (days > 1) {
                    timeValue = String.format("%d %s %02d:%02d:%02d", days, getString(R.string.days), hours % 24, minutes % SECONDS_IN_MINUTE, seconds % SECONDS_IN_MINUTE)
                } else {
                    timeValue = String.format("%02d:%02d:%02d", hours, minutes % SECONDS_IN_MINUTE, seconds % SECONDS_IN_MINUTE)
                }
                chronometer.text = timeValue
            }
        }
        chronometer.start()
    }

    //quiz has a time limit, count down
    private fun timeLimitCountDown(secondsVal: Int) {
        var seconds = secondsVal
        chronometer.visibility = View.GONE
        timer.visibility = View.VISIBLE
        //need to start a countdown

        //a quiz might be overdue, but they can still take it again. In this case we'll set the timer to be
        //the time limit
        if (seconds < 0) {
            seconds = quiz!!.timeLimit * MILLISECOND
        } else {
            seconds *= MILLISECOND
        }
        countDownTimer = object : CountDownTimer(seconds.toLong(), MILLISECOND.toLong()) {

            override fun onTick(millisUntilFinished: Long) {

                //subtract the warning seconds. We try to submit the quiz at 5 seconds, but the user might try to wait until the last second
                //to submit it, and this would lead to unexpected behavior. So we'll subtract 5 seconds from the timer so it will submit when
                //the timer reaches 0
                val secondsTick = (millisUntilFinished / MILLISECOND).toInt() - LAST_WARNING_SECONDS
                val minutesString = String.format("%02d:%02d", secondsTick / SECONDS_IN_MINUTE, secondsTick % SECONDS_IN_MINUTE)
                timer.text = minutesString

                //if we're at 30 seconds, warn the user
                if (secondsTick == FIRST_WARNING_SECONDS) {
                    showToast(R.string.thirtySecondWarning)
                }
                //there are actually 5 seconds left, but the timer will show 0 seconds because we subtract it when we set the variable "seconds"
                if (secondsTick == 0) {
                    //auto-submit the quiz.
                    QuizManager.postQuizSubmit(course, quizSubmission!!, true, submitQuizCallback!!)
                    autoSubmitReason = AUTO_SUBMIT_REASON.TIMED_QUIZ
                    showToast(R.string.autoSubmitting)
                    countDownTimer?.cancel()
                }
            }

            override fun onFinish() {
                timer.text = getString(R.string.done)
            }
        }.start()
    }

    private fun countDownForSubmit(minutesMS: Long) {
        //count up
        normalTimer()

        countDownTimer = object : CountDownTimer(minutesMS, MILLISECOND.toLong()) {

            override fun onTick(millisUntilFinished: Long) {

                val seconds = (millisUntilFinished / MILLISECOND).toInt() - LAST_WARNING_SECONDS

                //if we're at 30 seconds, warn the user
                if (seconds == FIRST_WARNING_SECONDS - LAST_WARNING_SECONDS) {
                    showToast(R.string.thirtySecondWarning)
                    // if it's a due date, let them decide if they want to submit, so show a dialog
                    if (autoSubmitReason == AUTO_SUBMIT_REASON.DUE_DATE) {
                        AlertDialog.Builder(requireContext())
                                .setTitle(R.string.almostDue)
                                .setMessage(R.string.almostDueMsg)
                                .setPositiveButton(android.R.string.yes) { _, _ -> QuizManager.postQuizSubmit(course, quizSubmission!!, false, submitQuizCallback!!) }
                                .setNegativeButton(android.R.string.no) { _, _ -> }
                                .create()
                                .show()
                    }
                }
                if (seconds == 0) {

                    //auto-submit the quiz if it's a lock_date type. We don't auto submit at the due date
                    if (autoSubmitReason == AUTO_SUBMIT_REASON.LOCK_DATE) {
                        QuizManager.postQuizSubmit(course, quizSubmission!!, false, submitQuizCallback!!)

                        showToast(R.string.autoSubmitting)
                        countDownTimer?.cancel()
                    }
                }
            }

            override fun onFinish() {
                timer.text = getString(R.string.done)
            }
        }.start()
    }

    companion object {
        private const val MILLISECOND = 1000
        private const val SECONDS_IN_MINUTE = 60
        private const val FIRST_WARNING_SECONDS = 30
        private const val LAST_WARNING_SECONDS = 5

        @JvmStatic
        fun newInstance(route: Route) : QuizQuestionsFragment? {
            return if(validRoute(route)) QuizQuestionsFragment().apply {
                arguments = route.arguments
                canvasContext = route.canvasContext!!
            } else null
        }

        @JvmStatic
        private fun validRoute(route: Route): Boolean {
            return route.canvasContext != null && route.canvasContext is Course
                    && route.arguments.containsKey(Const.QUIZ)
                    && route.arguments.containsKey(Const.QUIZ_SHOULD_LET_ANSWER)
        }

        @JvmStatic
        fun makeRoute(canvasContext: CanvasContext, quiz: Quiz, quizSubmission: QuizSubmission?, shouldLetAnswer: Boolean): Route {
            return Route(null, QuizQuestionsFragment::class.java, canvasContext, canvasContext.makeBundle(Bundle().apply {
                putParcelable(Const.QUIZ, quiz)
                if(quizSubmission != null) putParcelable(Const.QUIZ_SUBMISSION, quizSubmission)
                putBoolean(Const.QUIZ_SHOULD_LET_ANSWER, shouldLetAnswer)
            }))
        }
    }
}