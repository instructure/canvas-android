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

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.devspark.appmsg.AppMsg
import com.instructure.androidpolling.app.R
import com.instructure.androidpolling.app.activities.PublishPollActivity
import com.instructure.androidpolling.app.rowfactories.PollResultsRowFactory
import com.instructure.androidpolling.app.util.Constants
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.PollsManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.pandautils.utils.nonNullArgs
import kotlinx.android.synthetic.main.fragment_poll_results.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class PollResultsFragment : PaginatedListFragment<PollChoice>() {

    private var poll: Poll? = null
    private var pollSession: PollSession? = null
//
    private var sessionResults: Map<Long, Int>? = null
    private var totalSubmissions = 0

    private var timerHandler = Handler()
    private var timerRunnable: Runnable? = null
    private var createdTime: Long = 0
    private var numberFormat: NumberFormat? = null

    private var pollChoiceCallback: StatusCallback<PollChoiceResponse>? = null
    private var closePollCallback: StatusCallback<ResponseBody>? = null
    private var updatePollSessionCallback: StatusCallback<PollSessionResponse>? = null
    private var pollSessionCallback: StatusCallback<PollSessionResponse>? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setHasOptionsMenu(true)
        numberFormat = DecimalFormat("00")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // If we published just one poll session
        if (resultCode == Constants.PUBLISH_POLL_SUCCESS) {
            // Success! we just published a poll, refresh this fragment
            if (data != null) {
                pollSession = data.extras!!.getParcelable(Constants.POLL_SESSION)
                poll!!.id = data.extras!!.getLong(Constants.POLL_ID)

                // Publishing the poll was a success, so mark it as successful. Then update the views
                // so that it shows the correct status of the poll (active)
                pollSession!!.isPublished = true
                updateViews()

                reloadData()
            }
        } else if (resultCode == Constants.PUBLISH_POLL_SUCCESS_MULTIPLE) {
            activity?.onBackPressed()
        }// If we just published multiple polls from this location, go back to the poll session list
    }

    override fun updatePoll(poll: Poll) {
        // Set the current poll to be the one passed in
        this.poll = poll
        PollsManager.getFirstPagePollChoices(poll.id, pollChoiceCallback!!, true)
    }

    override fun onPause() {
        super.onPause()
        timerHandler.removeCallbacks(timerRunnable)
    }

    override fun onResume() {
        super.onResume()
        timerHandler = Handler()
        // Setup the timer to update how long the poll has been running
        timerRunnable = object : Runnable {

            override fun run() {
                setTime()

                timerHandler.postDelayed(this, 1000)
            }
        }
        if (pollSession != null) {
            timerHandler.postDelayed(timerRunnable, 0)
        }

    }

    private fun setupViews(poll: Poll, pollChoices: ArrayList<PollChoice>?) {
        clearAdapter()
        question!!.text = poll.question
        if (pollChoices != null) {
            for (choice in pollChoices) {
                addItem(choice)
            }
        }
    }

    private fun updateViews() {
        pollSession ?: return

        // Publish button
        if (pollSession!!.isPublished) {
            publishPoll!!.text = getString(R.string.closePoll)
            shareResults!!.visibility = View.GONE
        } else {
            publishPoll!!.text = getString(R.string.republishPoll)
            shareResults!!.visibility = View.VISIBLE
        }

        // Share button
        if (pollSession!!.hasPublicResults) {
            shareResults!!.text = getString(R.string.hideResults)
        } else {
            shareResults!!.text = getString(R.string.shareResults)
        }

        // Figure out how much time has elapsed since the start of the poll
        createdTime = pollSession!!.createdAt?.time ?: -1L
        if (pollSession!!.isPublished) {
            sessionStatus.text = getString(R.string.pollRunning)
            sessionStatus.setTextColor(resources.getColor(R.color.polling_aqua))
            timer.visibility = View.VISIBLE
            timer.setTextColor(resources.getColor(R.color.polling_aqua))
            timerHandler.postDelayed(timerRunnable, 0)
        } else {
            sessionStatus.text = getString(R.string.pollEnded)
            sessionStatus.setTextColor(resources.getColor(R.color.canvasRed))
            timer!!.visibility = View.INVISIBLE
        }
    }

    private fun setTime() {
        val now = Date(System.currentTimeMillis())
        val difference = now.time - createdTime

        val hours = difference.toInt() / (60 * 60 * 1000)
        val minutes = difference.toInt() / (60 * 1000) % 60
        val seconds = difference.toInt() / 1000 % 60

        timer.text = numberFormat!!.format(hours.toLong()) + ":" + numberFormat!!.format(minutes.toLong()) + ":" + numberFormat!!.format(seconds.toLong())

    }

    private fun setupClickListeners() {
        publishPoll.setOnClickListener {
            if (pollSession!!.isPublished) {
                // Close the poll session
                PollsManager.closePollSession(poll!!.id, pollSession!!.id, closePollCallback!!, true)
            } else {
                // Go to the publish screen to let the teacher select which courses and sections to use
                startActivityForResult(PublishPollActivity.createIntent(requireContext(), poll!!.id), Constants.PUBLISH_POLL_REQUEST)
            }
        }

        shareResults!!.setOnClickListener {
            // If we don't have public results, share them
            if (!pollSession!!.hasPublicResults) {
                PollsManager.updatePollSession(poll!!.id, pollSession!!.id, pollSession!!.courseId, pollSession!!.courseSectionId, true, updatePollSessionCallback!!, true)
            } else {
                // Otherwise, turn them off
                PollsManager.updatePollSession(poll!!.id, pollSession!!.id, pollSession!!.courseId, pollSession!!.courseSectionId, false, updatePollSessionCallback!!, true)
            }
        }
    }

    override fun getRootLayoutCode(): Int = R.layout.fragment_poll_results

    override fun configureViews(rootView: View) {
        setupClickListeners()
        setupCallbacks()

        // Get the poll passed in by tapping the poll on the poll list page. But we also get a poll in the updatePoll
        // function and we don't want to overwrite it with the poll from the bundle
        if (arguments != null && poll == null) {
            poll = nonNullArgs.getParcelable(Constants.POLL_DATA)
            if (poll != null) {
                PollsManager.getFirstPagePollSessions(poll!!.id, pollSessionCallback!!, true)
            }

            pollSession = nonNullArgs.getParcelable(Constants.POLL_SESSION)
        }

        updateViews()

    }

    override fun getRowViewForItem(item: PollChoice?, convertView: View?, position: Int): View {
        var result = 0f
        // SessionResults holds the student submissions for this session. Find out how many students have selected this PollChoice
        if (sessionResults != null && sessionResults!!.isNotEmpty() && item != null) {
            if (sessionResults!!.containsKey(item.id) && totalSubmissions > 0) {
                result = sessionResults!![item.id]!!.toFloat() / totalSubmissions
            }
        }
        return PollResultsRowFactory.buildRowView(layoutInflater(), requireContext(), item?.text ?: "", (result * 100).toInt(), item?.isCorrect ?: false, convertView, position)
    }

    override fun getFooterLayoutCode(): Int = 0
    override fun getTitle(): String? = null
    override fun onRowClick(item: PollChoice, position: Int): Boolean = false
    override fun areItemsSorted(): Boolean = false

    override fun loadFirstPage() {
        PollsManager.getFirstPagePollChoices(poll!!.id, pollChoiceCallback!!, true)
        PollsManager.getSinglePollSession(poll!!.id, pollSession!!.id, pollSessionCallback!!, true)
    }

    override fun loadNextPage(nextURL: String) = Unit
    override fun getNextURL(): String? = null
    override fun setNextURLNull() = Unit

    override fun resetData() {
        pollSessionCallback!!.cancel()
        totalSubmissions = 0
    }

    override fun setupCallbacks() {
        pollChoiceCallback = object : StatusCallback<PollChoiceResponse>() {
            override fun onResponse(response: Response<PollChoiceResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                val pollChoices = response.body()!!.pollChoices
                if (pollChoices != null) {
                    for (pollChoice in pollChoices) {
                        addItem(pollChoice)
                    }
                    val pollChoiceArrayList = ArrayList<PollChoice>()
                    pollChoiceArrayList.addAll(pollChoices)
                    setupViews(poll!!, pollChoiceArrayList)
                    updateViews()
                }
            }

            override fun onFinished(type: ApiType) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        closePollCallback = object : StatusCallback<ResponseBody>() {
            override fun onResponse(response: Response<ResponseBody>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                if (response.code() == 200) {
                    AppMsg.makeText(activity, getString(R.string.successfullyClosed), AppMsg.STYLE_SUCCESS).show()
                    pollSession!!.isPublished = false
                    updateViews()
                }
            }

            override fun onFinished(type: ApiType) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        updatePollSessionCallback = object : StatusCallback<PollSessionResponse>() {
            override fun onResponse(response: Response<PollSessionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                val pollSessions = response.body()!!.pollSessions
                if (pollSessions != null) {
                    pollSession = pollSessions[0]
                }
                updateViews()
            }
        }

        pollSessionCallback = object : StatusCallback<PollSessionResponse>() {
            override fun onResponse(response: Response<PollSessionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                val pollSessions = response.body()!!.pollSessions
                if (pollSessions != null) {
                    sessionResults = pollSessions[0].results
                    pollSession = pollSessions[0]
                    for (count in sessionResults!!.values) {
                        totalSubmissions += count
                    }
                    updateViews()
                    notifyDataSetChanged()
                }
            }

            override fun onFinished(type: ApiType) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    override fun getDividerHeight(): Int = 0

    override fun getDividerColor(): Int = android.R.color.transparent
}
