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
 *
 */

package com.instructure.androidpolling.app.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.devspark.appmsg.AppMsg
import com.instructure.androidpolling.app.R
import com.instructure.androidpolling.app.activities.BaseActivity
import com.instructure.androidpolling.app.model.AnswerValue
import com.instructure.androidpolling.app.rowfactories.StudentPollResultsRowFactory
import com.instructure.androidpolling.app.rowfactories.StudentPollRowFactory
import com.instructure.androidpolling.app.util.ApplicationManager
import com.instructure.androidpolling.app.util.Constants
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.PollsManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import kotlinx.android.synthetic.main.fragment_student_poll.*
import retrofit2.Call
import retrofit2.Response
import java.util.*

class StudentPollFragment : ParentFragment() {
    private var poll: Poll? = null
    private var pollSession: PollSession? = null
    private var answers: TreeSet<AnswerValue>? = null
    // Check to see if there is a correct answer
    internal var hasCorrectAnswer = false

    private var answerAdapter: AnswerAdapter? = null
    private var showResults = false
    private var hasSubmitted = false
    private var isPublished = false

    private var pollSubmission: PollSubmission? = null
    private var totalResults = 0

    private var pollChoiceCallback: StatusCallback<PollChoiceResponse>? = null
    private var pollSubmissionCallback: StatusCallback<PollSubmissionResponse>? = null
    private var pollSessionCallback: StatusCallback<PollSessionResponse>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_student_poll, container, false)
        setupViews()
        setupCallbacks()
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        handleArguments(arguments)

        if (poll != null) {
            updateViews(poll!!)
            PollsManager.getFirstPagePollChoices(poll!!.id, pollChoiceCallback!!, true)
        }

        if (pollSession != null) {
            showResults = pollSession!!.hasPublicResults
            isPublished = pollSession!!.isPublished
            getTotalResults()
        }

        if (hasSubmitted || !isPublished) {
            updateViewsSubmitted()
        }

        // Set an animation for adding list items
        val controller = AnimationUtils.loadLayoutAnimation(
                activity, R.anim.list_layout_controller)

        listView.layoutAnimation = controller
        setupListeners()

        (activity as? BaseActivity)?.setTitle(R.string.poll)
    }

    private fun setupViews() {
        answers = TreeSet()
        answerAdapter = AnswerAdapter(requireActivity(), answers!!)
        listView.adapter = answerAdapter
        swipeRefreshLayout.setColorSchemeResources(
                R.color.polling_aqua,
                R.color.polling_green,
                R.color.polling_purple,
                R.color.canvaspollingtheme_color)
    }

    private fun setupListeners() {
        submitPoll.setOnClickListener {
            // Make sure the user has something selected and get which item the user selected
            var hasSelected = false
            pollSubmission = PollSubmission()
            for (i in 0 until answerAdapter!!.count) {
                val answerValue = answerAdapter!!.getItem(i) as AnswerValue?
                if (answerValue!!.isSelected) {
                    hasSelected = true
                    // API call to submit poll
                    PollsManager.createPollSubmission(poll!!.id, pollSession!!.id, answerValue.pollChoiceId, pollSubmissionCallback!!, true)
                    break
                }
            }

            if (!hasSelected) {
                AppMsg.makeText(activity, getString(R.string.mustSelect), AppMsg.STYLE_WARNING).show()
            }
        }

        if (isPublished && !hasSubmitted) {
            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                (answerAdapter!!.getItem(position) as AnswerValue).isSelected = true
                // Uncheck all the other ones
                for (i in 0 until answerAdapter!!.count) {
                    (answerAdapter!!.getItem(i) as AnswerValue).isSelected = false
                }
                // Make sure this one is checked
                (answerAdapter!!.getItem(position) as AnswerValue).isSelected = true
                answerAdapter!!.notifyDataSetChanged()
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            // Cancel the callbacks in case the user pulls to refresh A LOT
            pollChoiceCallback!!.cancel()
            pollSessionCallback!!.cancel()
            setupCallbacks()

            totalResults = 0
            // API call to check if we can show results
            PollsManager.getSinglePollSession(poll!!.id, pollSession!!.id, pollSessionCallback!!, true)
            PollsManager.getFirstPagePollChoices(poll!!.id, pollChoiceCallback!!, true)
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun shouldShowResults() {
        if (showResults && hasSubmitted) {
            // Now show the actual results of the poll
            answerAdapter!!.notifyDataSetChanged()
        } else if (!showResults && hasSubmitted) {
            // Make all items not selected disabled
            listView.onItemClickListener = null
            answerAdapter!!.notifyDataSetChanged()
        }
    }

    private fun updateViews(poll: Poll) {
        pollQuestion!!.text = poll.question
    }

    /**
     * Update the views if the student has already submitted the poll
     */
    private fun updateViewsSubmitted() {
        // Make the submit button tell the user they've submitted the poll
        if (hasSubmitted) {
            submitPoll!!.text = getString(R.string.alreadyAnswered)
        } else {
            submitPoll!!.text = getString(R.string.closedPoll)
        }
        submitPoll!!.isEnabled = false
        submitPoll!!.isClickable = false

        // Make the background white
        rootView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

        // Remove the background on the question
        pollQuestion!!.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

        shouldShowResults()
    }

    private fun addAnswer(answer: String, id: Long, isCorrect: Boolean, position: Int) {
        val value = AnswerValue()
        value.value = answer
        value.pollChoiceId = id
        value.isCorrect = isCorrect
        value.position = position
        answerAdapter!!.addItem(value)
    }

    private fun getTotalResults() {
        totalResults = 0
        if (pollSession!!.results != null) {
            for (count in pollSession!!.results!!.values) {
                totalResults += count
            }
            answerAdapter!!.notifyDataSetChanged()
        }
    }

    private inner class AnswerAdapter(context: Activity, private val views: TreeSet<AnswerValue>) : BaseAdapter() {
        private val context: Context
        private val layoutInflater: LayoutInflater
        private var cachedItems: Array<AnswerValue>? = null
        private var invalid = false

        init {
            this.context = context
            layoutInflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        fun addItem(choice: AnswerValue) {
            invalid = true
            val added = answers!!.add(choice)
            // Replace it with the new one
            if (!added) {
                answers!!.remove(choice)
                answers!!.add(choice)
            }
            notifyDataSetChanged()
        }

        private fun itemAtPosition(position: Int): AnswerValue? {
            if (invalid || cachedItems == null) {
                cachedItems = answers!!.toTypedArray()
                invalid = false
            }
            return if (position < answers!!.size) {
                cachedItems!![position]
            } else null
        }

        override fun getViewTypeCount(): Int = 2
        override fun getItemViewType(position: Int): Int = if (showResults && hasSubmitted) 0 else 1
        override fun getCount(): Int = answers!!.size
        override fun getItem(i: Int): Any? = itemAtPosition(i)
        override fun getItemId(i: Int): Long = i.toLong()

        override fun getView(position: Int, convertView: View, viewGroup: ViewGroup): View {

            if (getItemViewType(position) == 0) {
                var selected = false
                // See if this answer is selected
                val answerValue = itemAtPosition(position)
                if (answerValue!!.pollChoiceId == ApplicationManager.getPollSubmissionId(activity, pollSession!!.id)) {
                    selected = true
                }

                // Get the number answered
                var numAnswered = 0f
                if (pollSession!!.results!!.containsKey(answerValue.pollChoiceId) && totalResults > 0) {
                    numAnswered = pollSession!!.results!![answerValue.pollChoiceId]!!.toFloat() / totalResults
                }

                return StudentPollResultsRowFactory.buildRowView(layoutInflater, requireContext(), answerValue.value, (numAnswered * 100).toInt(), answerValue.isCorrect, selected, hasCorrectAnswer, convertView, position)
            } else {
                return StudentPollRowFactory.buildRowView(layoutInflater, requireActivity(), itemAtPosition(position) ?: AnswerValue(), position, hasSubmitted, pollSession!!.isPublished, convertView)
            }
        }
    }

    private fun setupCallbacks() {
        pollChoiceCallback = object : StatusCallback<PollChoiceResponse>() {
            override fun onResponse(response: retrofit2.Response<PollChoiceResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                val pollChoices = response.body()!!.pollChoices
                if (pollChoices != null) {
                    for (pollChoice in pollChoices) {
                        addAnswer(pollChoice.text ?: "", pollChoice.id, pollChoice.isCorrect, pollChoice.position)
                        if (pollChoice.isCorrect) {
                            hasCorrectAnswer = true
                        }
                    }

                    // If this has already been submitted, we want to have the poll choice that the user selected actually selected
                    if (hasSubmitted) {
                        val checkedId = ApplicationManager.getPollSubmissionId(activity, pollSession!!.id)
                        if (checkedId != -1L) {
                            for (answerValue in answers!!) {
                                if (answerValue.pollChoiceId == checkedId) {
                                    answerValue.isSelected = true
                                    break
                                }
                            }
                        }
                    }
                }

                if (StatusCallback.moreCallsExist(linkHeaders)) {
                    PollsManager.getNextPagePollChoices(linkHeaders.nextUrl!!, pollChoiceCallback!!, true)
                }
            }

            override fun onFinished(type: ApiType) {
                swipeRefreshLayout?.isRefreshing = false
            }
        }

        pollSubmissionCallback = object : StatusCallback<PollSubmissionResponse>() {
            override fun onResponse(response: Response<PollSubmissionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                // Successful submission, let the user know
                AppMsg.makeText(activity, getString(R.string.successfullySubmitted), AppMsg.STYLE_SUCCESS).show()

                // Save the actual poll submission so we know which id they selected.
                ApplicationManager.savePollSubmission(activity, pollSession!!.id, response.body()!!.pollSubmissions[0].pollChoiceId)

                hasSubmitted = true
                updateViewsSubmitted()

                // We need to update the session so that we know how many users have submitted and their answers if the teacher has chosen to share results
                PollsManager.getSinglePollSession(poll!!.id, pollSession!!.id, pollSessionCallback!!, true)
                activity?.setResult(Constants.SUBMIT_POLL_SUCCESS)
            }

            override fun onFail(call: Call<PollSubmissionResponse>?, error: Throwable, response: Response<*>?) {
                AppMsg.makeText(activity, getString(R.string.errorSubmittingPoll), AppMsg.STYLE_ERROR).show()
            }

            override fun onFinished(type: ApiType) {
                swipeRefreshLayout?.isRefreshing = false
            }
        }

        pollSessionCallback = object : StatusCallback<PollSessionResponse>() {
            override fun onResponse(response: Response<PollSessionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                val pollSessions = response.body()!!.pollSessions
                if (pollSessions != null) {
                    showResults = pollSessions[0].hasPublicResults
                    pollSession = pollSessions[0]

                    getTotalResults()
                    shouldShowResults()
                    // Update the data so user has the most up to date results
                    answerAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onFinished(type: ApiType) {
                swipeRefreshLayout?.isRefreshing = false
            }
        }
    }

    private fun handleArguments(bundle: Bundle?) {
        bundle?.let {
            poll = it.getParcelable(Constants.POLL_DATA)
            pollSession = it.getParcelable(Constants.POLL_SESSION)
            hasSubmitted = it.getBoolean(Constants.HAS_SUBMITTED)
        }
    }
}
