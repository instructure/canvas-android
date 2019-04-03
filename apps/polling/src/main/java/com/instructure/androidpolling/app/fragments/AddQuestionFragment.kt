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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.devspark.appmsg.AppMsg
import com.instructure.androidpolling.app.R
import com.instructure.androidpolling.app.activities.BaseActivity
import com.instructure.androidpolling.app.activities.FragmentManagerActivity
import com.instructure.androidpolling.app.activities.PublishPollActivity
import com.instructure.androidpolling.app.model.AnswerValue
import com.instructure.androidpolling.app.util.Constants
import com.instructure.androidpolling.app.util.SwipeDismissTouchListener
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.PollsManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import kotlinx.android.synthetic.main.add_answer_layout.view.*
import kotlinx.android.synthetic.main.fragment_add_question.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.*

class AddQuestionFragment : ParentFragment() {
    private var addAnswerBtn: Button? = null

    private var pollCallback: StatusCallback<PollResponse>? = null
    private var pollChoiceCallback: StatusCallback<PollChoiceResponse>? = null

    private var touchListener: SwipeDismissTouchListener? = null

    private var editPoll = false // Set when we're editing a poll that has already be created
    private var shouldPublish = false
    private var poll: Poll? = null

    private val answerMap = HashMap<View, AnswerValue>()
    private val idDeleteList = ArrayList<Long>()

    // Num polls created
    private var pollChoicesCreated = 0
    private var submittingPoll = false
    private var inflater: LayoutInflater? = null

    // Get the number of AnswerValues that actually have a value
    private val numValidEntries: Int
        get() = answerMap.values.count { value -> value.value.trim { it <= ' ' }.isNotEmpty() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_add_question, container, false)
        this.inflater = inflater
        setHasOptionsMenu(true)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupClickListeners()

        setupCallbacks()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retainInstance = true

        // Set an animation for adding list items
        val controller = AnimationUtils.loadLayoutAnimation(
                activity, R.anim.list_layout_controller)

        answerContainer!!.layoutAnimation = controller
        checkBundle(arguments)

        (activity as BaseActivity).setActionBarTitle(getString(R.string.createPoll))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Constants.PUBLISH_POLL_SUCCESS) {
            // Try to go to the poll result
            if (data != null) {
                val session = data.extras!!.getParcelable<PollSession>(Constants.POLL_SESSION)
                val pollResultsFragment = PollResultsFragment()
                val bundle = Bundle()
                bundle.putParcelable(Constants.POLL_DATA, poll)
                bundle.putParcelable(Constants.POLL_SESSION, session)
                pollResultsFragment.arguments = bundle
                (activity as FragmentManagerActivity).removeFragment(this)
                (activity as FragmentManagerActivity).swapFragments(pollResultsFragment, PollResultsFragment::class.java.simpleName)

                return
            }
            // If there are multiple poll sessions, go to the list
            activity?.onBackPressed()
        } else if (resultCode == Constants.PUBLISH_POLL_SUCCESS_MULTIPLE) {
            val pollSessionListFragment = PollSessionListFragment()
            val bundle = Bundle()
            bundle.putParcelable(Constants.POLL_DATA, poll)
            pollSessionListFragment.arguments = bundle
            (activity as FragmentManagerActivity).removeFragment(this)
            (activity as FragmentManagerActivity).swapFragments(pollSessionListFragment, PollResultsFragment::class.java.simpleName)

        }
    }

    private fun updatePollInfo() {
        poll!!.question = editQuestion!!.text.toString()
        PollsManager.updatePoll(poll!!.id, editQuestion!!.text.toString(), pollCallback!!, true)

        // Delete the ids of the removed choices
        for (id in idDeleteList) {

            PollsManager.deletePollChoice(poll!!.id, id, object : StatusCallback<ResponseBody>() {

            }, true)
        }
    }

    private fun setupViews() {
        addAnswerBtn = Button(activity)
        addAnswerBtn!!.text = getString(R.string.addAnswer)
        addAnswerBtn!!.setBackgroundResource(R.drawable.dashed_button)
        addAnswerBtn!!.setTextColor(ContextCompat.getColor(requireContext(), R.color.canvaspollingtheme_color))
        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(resources.getDimension(R.dimen.smallViewMargin).toInt(), 0, resources.getDimension(R.dimen.viewMargin).toInt(), 0)
        addAnswerBtn!!.layoutParams = layoutParams

        answerContainer.addView(addAnswerBtn)
    }

    private fun setupClickListeners() {
        addAnswerBtn!!.setOnClickListener {
            // Add another answer
            addAnswer()
        }

        publishPoll!!.setOnClickListener(View.OnClickListener {
            // Make sure they entered a question
            if (editQuestion.editableText.toString().trim { it <= ' ' }.isEmpty()) {
                AppMsg.makeText(activity, getString(R.string.msgAddQuestion), AppMsg.STYLE_WARNING).show()
                return@OnClickListener
            }
            if (checkForAnswers()) {
                shouldPublish = true
                if (editPoll) {
                    updatePollInfo()
                } else {
                    PollsManager.createPoll(editQuestion!!.text.toString(), object : StatusCallback<PollResponse>() {
                        override fun onResponse(response: retrofit2.Response<PollResponse>, linkHeaders: LinkHeaders, type: ApiType) {

                        }
                    }, true)
                }
            }
        })

        savePoll.setOnClickListener(View.OnClickListener {
            // Make sure they entered a question
            if (editQuestion.editableText.toString().trim { it <= ' ' }.isEmpty()) {
                AppMsg.makeText(activity, getString(R.string.msgAddQuestion), AppMsg.STYLE_WARNING).show()
                return@OnClickListener
            }

            // Make sure there are at least 2 answers
            if (checkForAnswers()) {
                submittingPoll = true
                // Disable the button so they can't publish multiples of the same poll
                savePoll.isEnabled = false
                if (editPoll) {
                    updatePollInfo()
                } else {
                    // API call to create poll
                    PollsManager.createPoll(editQuestion.text.toString(), pollCallback!!, true)
                }
            }
        })
    }

    private fun checkForAnswers(): Boolean {
        var validEntries = 0
        // Go through the different items, make sure there is something in at least 2 edit texts
        for (value in answerMap.values) {

            if (value.value.trim { it <= ' ' }.isNotEmpty()) {
                validEntries++
            }
            // If we have 2, we're good to go
            if (validEntries >= 2) {
                return true
            }
        }
        AppMsg.makeText(activity, getString(R.string.twoAnswersRequired), AppMsg.STYLE_WARNING).show()
        return false
    }

    private fun addAnswer(answer: String = "", isCorrect: Boolean = false, pollChoiceId: Long = 0) {
        val value = AnswerValue()
        value.value = answer
        value.isSelected = isCorrect
        value.pollChoiceId = pollChoiceId
        value.position = answerMap.size
        // Inflate the view from xml and add it to the scrollview's container
        val answerView = inflater!!.inflate(R.layout.add_answer_layout, null, false) ?: return

        // Setup the touchListener for swipe to dismiss
        touchListener = SwipeDismissTouchListener(
                answerView, null, // Optional token/cookie object
                object : SwipeDismissTouchListener.DismissCallbacks {
                    override fun canDismiss(token: Any?): Boolean = true

                    override fun onDismiss(view: View, token: Any?) {
                        answerContainer.removeView(view)

                        // If the poll has already been created, we need to delete the pollchoice with an api call too. But we don't want
                        // to delete the poll choices until they save the poll, so we'll add them to a list
                        if (editPoll && answerMap.containsKey(view) && answerMap[view]?.pollChoiceId != 0L) {
                            if (answerMap.containsKey(view)) {
                                idDeleteList.add(answerMap[view]?.pollChoiceId!!)
                            }
                        }
                        answerMap.remove(view)
                        val imm = requireActivity().getSystemService(
                                Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                })
        answerView.setOnTouchListener(touchListener)
        // Have to have an onclick listener for swipeToDismiss to work
        answerView.setOnClickListener { }

        // Get the edit text
        val answerEditText = answerView.editAnswerAdded
        answerEditText.setText(answer)
        answerEditText.setOnTouchListener(touchListener)
        answerEditText.requestFocus()

        // Add a text watcher to the edit text so we save the values as the user types
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i2: Int, i3: Int) {
                value.value = charSequence.toString()
                answerMap[answerView] = value
            }

            override fun afterTextChanged(editable: Editable) {}
        }

        answerEditText.addTextChangedListener(watcher)

        // Since the radio buttons aren't in a radioGroup, we have to manage their state by ourselves. Also, with
        // this we can let the user de-select a radio button
        val selected = answerView.correctAnswerAdded
        selected.setOnClickListener { view ->
            if (answerMap[view.parent.parent as View]?.isSelected!!) {
                selected.isSelected = false
                selected.isChecked = false
                answerMap[view.parent.parent as View]?.isSelected = false
            } else {
                selected.isSelected = true

                // Uncheck all the other ones
                for (i in 0 until answerMap.size) {
                    val container = answerContainer.getChildAt(i)
                    val selectedAnswer = container.correctAnswerAdded
                    selectedAnswer.isChecked = false
                    answerMap[container]?.isSelected = false
                }

                selected.isChecked = true
                // Make sure this one is checked
                answerMap[view.parent.parent as View]?.isSelected = true
            }
        }

        selected.isChecked = value.isSelected

        // Add the view and answerValue to a map so we can remove the value later on swipeToDismiss
        answerMap[answerView] = value

        // We want to add the view before the "add answer" button
        var index = answerMap.size
        if (index > 0) {
            index--
        }

        // Make sure we're not going to get an index out of bounds exception
        if (index > answerContainer.childCount) {
            index = answerContainer.childCount
        }

        answerContainer.addView(answerView, index)
    }

    private fun checkBundle(bundle: Bundle?) {
        if (bundle != null) {
            poll = bundle.getParcelable(Constants.POLL_BUNDLE)
            if (poll != null) {
                editPoll = true
                // We're editing a poll that has already been created

                scrollViewAnswers.visibility = View.VISIBLE
                editQuestion.setText(poll!!.question)

                val pollChoices = bundle.getParcelableArrayList<PollChoice>(Constants.POLL_CHOICES)
                if (pollChoices != null) {
                    for (pollChoice in pollChoices) {
                        addAnswer(pollChoice.text ?: "", pollChoice.isCorrect, pollChoice.id)
                    }
                }

                // Update the actionbar
                activity?.invalidateOptionsMenu()
            }
        }
    }

    private fun setupCallbacks() {

        pollCallback = object : StatusCallback<PollResponse>() {
            override fun onResponse(response: Response<PollResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                val pollList = response.body()!!.polls ?: return
                pollChoicesCreated = 0
                poll = pollList[0]
                // Now create the poll choices if we're not in edit mode
                if (!editPoll) {
                    for (answerValue in answerMap.values) {
                        PollsManager.createPollChoice(pollList[0].id, answerValue.value, answerValue.isSelected, answerValue.position, pollChoiceCallback!!, true)
                    }
                } else {
                    for (answerValue in answerMap.values) {
                        // If they added some answers we won't have a pollChoiceId
                        if (answerValue.pollChoiceId == 0L) {
                            PollsManager.createPollChoice(pollList[0].id, answerValue.value, answerValue.isSelected, answerValue.position, pollChoiceCallback!!!!, true)
                        } else {
                            PollsManager.updatePollChoice(pollList[0].id, answerValue.pollChoiceId, answerValue.value, answerValue.isSelected, answerValue.position, pollChoiceCallback!!, true)
                        }
                    }
                }
            }

            override fun onFail(call: Call<PollResponse>?, error: Throwable, response: Response<*>?) {
                submittingPoll = false
                activity?.invalidateOptionsMenu()
            }
        }

        pollChoiceCallback = object : StatusCallback<PollChoiceResponse>() {
            override fun onResponse(response: Response<PollChoiceResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                pollChoicesCreated++

                // After we've created all the polls that we need
                if (pollChoicesCreated == numValidEntries) {

                    if (editPoll || shouldPublish) {
                        if (shouldPublish) {
                            shouldPublish = false
                            startActivityForResult(PublishPollActivity.createIntent(requireContext(), poll!!.id), Constants.PUBLISH_POLL_REQUEST)
                        } else {
                            // We just came from the PollSessionsFragment
                            // Removing the fragment will make it reload it's data
                            (activity as BaseActivity).removeFragment(QuestionListFragment::class.java.simpleName)
                        }
                    } else {
                        // Now create the poll choices
                        // Removing the fragment will make it reload it's data
                        (activity as BaseActivity).removeFragment(QuestionListFragment::class.java.simpleName)
                    }
                }
            }

            override fun onFail(call: Call<PollChoiceResponse>?, error: Throwable, response: Response<*>?) {
                submittingPoll = false
                activity?.invalidateOptionsMenu()
            }
        }
    }
}
