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

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ListView
import com.devspark.appmsg.AppMsg
import com.instructure.androidpolling.app.R
import com.instructure.androidpolling.app.activities.BaseActivity
import com.instructure.androidpolling.app.activities.FragmentManagerActivity
import com.instructure.androidpolling.app.rowfactories.QuestionRowFactory
import com.instructure.androidpolling.app.util.Constants
import com.instructure.androidpolling.app.util.SwipeDismissListViewTouchListener
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.PollsManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import kotlinx.android.synthetic.main.fragment_question_list.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.*

class QuestionListFragment : PaginatedExpandableListFragment<String, Poll>() {

    private var pollCallback: StatusCallback<PollResponse>? = null
    private var responseCanvasCallback: StatusCallback<ResponseBody>? = null
    private var pollSessionCallback: StatusCallback<PollSessionResponse>? = null
    private var pollChoiceCallback: StatusCallback<PollChoiceResponse>? = null

    private var hasTeacherEnrollment: Boolean = false
    private var touchListener: SwipeDismissListViewTouchListener? = null

    private val openSessions = HashMap<Long, PollSession>()
    private val closedSessions = HashMap<Long, PollSession>()

    private val pollList = ArrayList<Poll>()
    private val pollChoiceArrayList = ArrayList<PollChoice>()

    private var pollToDelete: Poll? = null
    private var selectedPoll: Poll? = null
    private var nextUrl: String? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        // Clear the list so we don't get duplicates
        pollList.clear()
        openSessions.clear()
        closedSessions.clear()

        setupClickListeners()
        touchListener = SwipeDismissListViewTouchListener(
                expandableListView,
                object : SwipeDismissListViewTouchListener.DismissCallbacks {
                    override fun canDismiss(position: Int): Boolean {
                        return true
                    }

                    override fun onDismiss(listView: ListView, reverseSortedPositions: IntArray) {
                        for (position in reverseSortedPositions) {
                            // Set the poll that we want to remove after the api call returns successfully
                            pollToDelete = expandableListView.getItemAtPosition(position) as Poll
                            confirmDelete()
                        }
                    }
                })
        expandableListView.setOnTouchListener(touchListener)
        expandableListView.setOnScrollListener(touchListener!!.makeScrollListener())

        // Set an animation for adding list items
        val controller = AnimationUtils.loadLayoutAnimation(
                activity, R.anim.list_layout_controller)

        expandableListView.layoutAnimation = controller

        (activity as BaseActivity).setActionBarTitle(getString(R.string.pollQuestions))
    }

    private fun setupClickListeners() {
        addQuestion.setOnClickListener {
            // Open the add question fragment
            val addQuestionFragment = AddQuestionFragment()
            (activity as FragmentManagerActivity).swapFragments(addQuestionFragment,
                    AddQuestionFragment::class.java.simpleName,
                    R.anim.slide_in_from_bottom, 0, 0, R.anim.slide_out_to_bottom)
        }
    }

    // We need to know if the user is a teacher in any course
    private fun checkEnrollments(courses: List<Course>) {
        for (course in courses) {
            if (course.isTeacher) {
                hasTeacherEnrollment = true
                // Update the actionbar so the icon shows if we need it
                activity?.invalidateOptionsMenu()
                return
            }
        }
        hasTeacherEnrollment = false
        // Update the actionbar so the icon shows if we need it
        activity?.invalidateOptionsMenu()
    }

    private fun displayEmptyState() {
        emptyState.visibility = View.VISIBLE
    }

    // Make the teacher confirm that they want to delete the poll
    private fun confirmDelete() {
        val confirmDeleteDialog = AlertDialog.Builder(activity)
                // Set message, title, and icon
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.confirmDelete))
                .setIcon(R.drawable.ic_cv_delete)

                .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                    // Remove the item from the list
                    removeItem(pollToDelete)

                    // Delete the poll from canvas
                    PollsManager.deletePoll(pollToDelete!!.id, responseCanvasCallback!!, true)
                    dialog.dismiss()

                    // If there are any empty groups we want to remove them
                    removeEmptyGroups()
                    // Check if all the items are gone
                    if (groupCount == 0) {
                        // Show the empty state again
                        displayEmptyState()
                    }
                }

                .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                    pollToDelete = null
                    dialog.dismiss()
                }
                .create()

        confirmDeleteDialog.show()
    }

    override fun updatePoll(poll: Poll) {
        // Add the poll to the top of the list.
        // After we have apis here we may want to just pull to refresh to get the latest data
        reloadData()
    }

    override fun configureViews(rootView: View) {

    }

    override fun getRootLayoutCode(): Int = R.layout.fragment_question_list

    override fun getRowViewForItem(item: Poll, convertView: View?, groupPosition: Int, childPosition: Int, isLastRowInGroup: Boolean, isLastRow: Boolean): View {
        var hasActiveSession = false
        if (openSessions.containsKey(item.id)) {
            hasActiveSession = true
        }
        return QuestionRowFactory.buildRowView(layoutInflater(), requireContext(), item.question!!, hasActiveSession, convertView)
    }

    override fun getGroupViewForItem(groupItem: String, convertView: View?, groupPosition: Int, isExpanded: Boolean): View {
        return QuestionRowFactory.buildGroupView(layoutInflater(), groupItem, convertView)
    }

    override fun areGroupsSorted(): Boolean = true
    override fun areGroupsReverseSorted(): Boolean = false
    override fun areGroupsCollapsible(): Boolean = true
    override fun getFooterLayoutCode(): Int = 0
    override fun getTitle(): String? = null

    override fun onRowClick(item: Poll): Boolean {
        // If the poll is in the draft section, we want to take the user to the edit poll screen
        if (!openSessions.containsKey(item.id) && !closedSessions.containsKey(item.id)) {
            selectedPoll = item
            pollChoiceArrayList.clear()
            PollsManager.getFirstPagePollChoices(selectedPoll!!.id, pollChoiceCallback!!, true)
            return true
        }

        // Send the poll data to the results screen
        val pollSessionListFragment = PollSessionListFragment()
        val bundle = Bundle()
        bundle.putParcelable(Constants.POLL_DATA, item)
        pollSessionListFragment.arguments = bundle
        (activity as FragmentManagerActivity).swapFragments(pollSessionListFragment, PollSessionListFragment::class.java.simpleName)

        return true
    }

    override fun areItemsSorted(): Boolean = true
    override fun loadFirstPage() = PollsManager.getFirstPagePolls(pollCallback!!, true)
    override fun loadNextPage(nextURL: String) = PollsManager.getNextPagePolls(nextURL, pollCallback!!, true)
    override fun getNextURL(): String? = nextUrl
    override fun setNextURLNull() {
        nextUrl = null
    }

    override fun resetData() {
        pollSessionCallback!!.cancel()
        pollCallback!!.cancel()
        openSessions.clear()
        pollList.clear()
    }

    override fun setupCallbacks() {
        pollCallback = object : StatusCallback<PollResponse>() {
            override fun onResponse(response: Response<PollResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                nextUrl = linkHeaders.nextUrl
                if (response.body()!!.polls.isEmpty()) {
                    displayEmptyState()
                } else {
                    val polls = response.body()!!.polls
                    for (poll in polls) {
                        //add all the polls to a list. we'll use the list later to populate the
                        //different groups after we get some session information about each poll
                        pollList.add(poll)
                        PollsManager.getFirstPagePollSessions(poll.id, pollSessionCallback!!, true)
                    }
                }
            }

            override fun onFinished(type: ApiType) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        responseCanvasCallback = object : StatusCallback<ResponseBody>() {
            override fun onResponse(response: Response<ResponseBody>, linkHeaders: LinkHeaders, type: ApiType) {
                if (response.code() == 204) {
                    if (pollToDelete != null) {
                        //reset it so we don't try to remove it from the list again
                        pollToDelete = null
                    }
                }
            }

            override fun onFail(call: Call<ResponseBody>?, error: Throwable, response: Response<*>?) {
                AppMsg.makeText(activity, getString(R.string.errorDeletingPoll), AppMsg.STYLE_ERROR).show()
                // We didn't actually delete anything, but we removed the item from the list to make the animation smoother, so now
                //l ets get the polls again
                reloadData()
            }
        }

        pollSessionCallback = object : StatusCallback<PollSessionResponse>() {
            override fun onResponse(response: Response<PollSessionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return
                val pollSessions = response.body()!!.pollSessions
                for (session in pollSessions) {
                    if (session.isPublished) {
                        openSessions[session.pollId] = session
                        // We only care about there being one active poll session
                        break
                    } else {
                        closedSessions[session.pollId] = session
                    }
                }
                // If the poll has an active session, remove it from the list (from the "inactive" group)
                // and add it to the "active" group
                for (poll in pollList) {
                    if (openSessions.containsKey(poll.id)) {
                        removeItem(poll)
                        addItem(getString(R.string.active), poll)
                    } else if (!closedSessions.containsKey(poll.id)) {
                        removeItem(poll)
                        addItem(getString(R.string.draft), poll)
                    } else {
                        removeItem(poll)
                        addItem(getString(R.string.inactive), poll)
                    } // If the poll doesn't have an open session or any closed sessions, it is still in the draft state
                }
                expandAllGroups()
                if (linkHeaders.nextUrl != null) {
                    PollsManager.getNextPagePollSessions(linkHeaders.nextUrl!!, pollSessionCallback!!, true)
                }
                notifyDataSetChanged()
            }

            override fun onFinished(type: ApiType) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        pollChoiceCallback = object : StatusCallback<PollChoiceResponse>() {
            override fun onResponse(response: Response<PollChoiceResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                val pollChoices = response.body()!!.pollChoices
                if (pollChoices != null) {
                    pollChoiceArrayList.addAll(pollChoices)
                }

                // If linkHeaders.nextURL is null it means we have all the choices, so we can go to the edit poll page now
                // or generate the CSV, depending on which action they selected
                if (!StatusCallback.moreCallsExist(linkHeaders)) {

                    val addQuestionFragment = AddQuestionFragment()
                    // Populate the current data with the bundle
                    val bundle = Bundle()
                    bundle.putParcelable(Constants.POLL_BUNDLE, selectedPoll)
                    bundle.putParcelableArrayList(Constants.POLL_CHOICES, pollChoiceArrayList)
                    addQuestionFragment.arguments = bundle
                    (activity as FragmentManagerActivity).swapFragments(addQuestionFragment, AddQuestionFragment::class.java.simpleName)

                } else {
                    // Otherwise, get the next group of poll choices.
                    PollsManager.getNextPagePollChoices(linkHeaders.nextUrl!!, pollChoiceCallback!!, true)
                }
            }

            override fun onFinished(type: ApiType) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }
}
