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

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.format.Time
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ListView
import com.devspark.appmsg.AppMsg
import com.instructure.androidpolling.app.R
import com.instructure.androidpolling.app.activities.FragmentManagerActivity
import com.instructure.androidpolling.app.activities.PublishPollActivity
import com.instructure.androidpolling.app.rowfactories.PollSessionRowFactory
import com.instructure.androidpolling.app.util.ApplicationManager
import com.instructure.androidpolling.app.util.Constants
import com.instructure.androidpolling.app.util.SwipeDismissListViewTouchListener
import com.instructure.androidpolling.app.util.Utils
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.PollsManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.pandautils.utils.nonNullArgs
import kotlinx.android.synthetic.main.fragment_poll_session_list.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

class PollSessionListFragment : PaginatedListFragment<PollSession>() {

    private var pollSessionCallback: StatusCallback<PollSessionResponse>? = null
    private var sectionCallback: StatusCallback<Section>? = null
    private var pollChoiceCallback: StatusCallback<PollChoiceResponse>? = null
    private var responseCanvasCallback: StatusCallback<ResponseBody>? = null

    private var poll: Poll? = null

    private lateinit var touchListener: SwipeDismissListViewTouchListener

    private var courseMap: Map<Long, Course>? = null
    @SuppressLint("UseSparseArrays")
    private val sectionMap = HashMap<Long, Section>()
    private val pollChoiceArrayList = ArrayList<PollChoice>()
    @SuppressLint("UseSparseArrays")
    private val pollChoiceMap = HashMap<Long, PollChoice>()

    private var sessionNextUrl: String? = null
    private var fromGenerateCSV = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        touchListener = SwipeDismissListViewTouchListener(
                listView,
                object : SwipeDismissListViewTouchListener.DismissCallbacks {
                    override fun canDismiss(position: Int): Boolean = true

                    override fun onDismiss(listView: ListView, reverseSortedPositions: IntArray) {
                        for (position in reverseSortedPositions) {
                            // Set the poll that we want to remove after the api call returns successfully
                            val pollSession = getItem(position)

                            // Remove the item from the list
                            removeItem(pollSession)

                            // Delete the poll from canvas
                            PollsManager.deletePollSession(poll!!.id, pollSession.id, responseCanvasCallback!!, true)
                        }
                    }
                })

        listView.setOnTouchListener(touchListener)
        listView.setOnScrollListener(touchListener.makeScrollListener())

        // Set an animation for adding list items
        val controller = AnimationUtils.loadLayoutAnimation(
                activity, R.anim.list_layout_controller)

        listView.layoutAnimation = controller
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Constants.PUBLISH_POLL_SUCCESS || resultCode == Constants.PUBLISH_POLL_SUCCESS_MULTIPLE) {

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
            reloadData()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_upload_csv -> {
                fromGenerateCSV = true
                PollsManager.getFirstPagePollChoices(poll!!.id, pollChoiceCallback!!, true)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupClickListeners() {
        publishPoll.setOnClickListener { startActivityForResult(PublishPollActivity.createIntent(requireContext(), poll!!.id), Constants.PUBLISH_POLL_REQUEST) }
    }

    private fun generateCSV() {
        var csv = "Poll Title, Poll Session, Course Name, Section Name, User Name, Answer, Date\n"

        for (i in 0 until itemCount) {
            val pollSession = getItem(i)
            if (pollSession.pollSubmissions != null) {
                for (pollSubmission in pollSession.pollSubmissions!!) {
                    // Now add all the necessary stuff to the csv string
                    csv += poll!!.question + ","
                    csv += pollSession.id.toString() + ","
                    csv += courseMap?.get(pollSession.courseId)?.name + ","
                    csv += sectionMap[pollSession.courseSectionId]?.name + ","
                    csv += pollSubmission.userId.toString() + ","

                    // Make sure we have the poll choice information so we can include the poll choice text instead
                    // of just an id
                    csv += if (pollChoiceMap.containsKey(pollSubmission.pollChoiceId)) {
                        pollChoiceMap[pollSubmission.pollChoiceId]?.text + ","
                    } else {
                        pollSubmission.pollChoiceId.toString() + ","
                    }
                    csv += pollSubmission.createdAt.toString() + "\n"
                }
            } else {
                csv += poll!!.question + ","
                csv += pollSession.id.toString() + ","
                csv += courseMap?.get(pollSession.courseId)?.name + ","
                csv += sectionMap[pollSession.courseSectionId]?.name + ","
                csv += "" + ","
                csv += "" + ","
                csv += pollSession.createdAt.toString() + "\n"
            }

        }

        // Check to make sure there is external storage
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED != state) {
            // It's not there, so the reset of this won't work. Let the user know.
            AppMsg.makeText(activity, getString(R.string.errorGeneratingCSV), AppMsg.STYLE_ERROR).show()
            return
        }

        val path = File(Environment.getExternalStorageDirectory().absolutePath, getString(R.string.generatedCSVFolderName))
        // Make sure the directory exists.
        val success = path.mkdirs()

        if (!success) {
            // Didn't actually create the path, so now check if it's a directory (if it's already created mkdirs will
            // return false)
            if (!path.isDirectory) {
                // It's not a directory and wasn't created, so we need to return with an error
                AppMsg.makeText(activity, getString(R.string.errorGeneratingCSV), AppMsg.STYLE_ERROR).show()
                return
            }
        }
        val now = Time()
        now.setToNow()
        val file = File(path, "csv_" + now.format3339(false) + ".csv")
        try {
            // Write the string to a file
            val out = FileWriter(file)
            out.write(csv)
            out.close()
        } catch (e: IOException) {
            // Unable to create file
            AppMsg.makeText(activity, getString(R.string.errorGeneratingCSV), AppMsg.STYLE_ERROR).show()
        }

        // File is generated, not share it
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        shareIntent.type = "text/csv"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareCSV)))
    }

    override fun getEmptyViewLayoutCode(): Int = R.layout.empty_view_poll_sessions

    override fun getRootLayoutCode(): Int = R.layout.fragment_poll_session_list

    override fun configureViews(rootView: View) {
        super.configureViews(rootView)

        if (arguments != null) {
            poll = nonNullArgs.getParcelable(Constants.POLL_DATA)
            if (poll != null) {
                question!!.text = poll!!.question
            }
        }

        setupClickListeners()
    }

    override fun getRowViewForItem(item: PollSession, convertView: View?, position: Int): View {
        courseMap = Utils.createCourseMap(ApplicationManager.getCourseList(activity))
        val courseName = courseMap?.get(item.courseId)?.name ?: ""

        var sectionName = ""
        if (sectionMap.containsKey(item.courseSectionId)) {
            sectionName = sectionMap[item.courseSectionId]!!.name
        }

        return PollSessionRowFactory.buildRowView(layoutInflater(), requireActivity(), courseName, sectionName, item.isPublished, convertView)
    }

    override fun getFooterLayoutCode(): Int = 0

    override fun getTitle(): String? = null

    override fun onRowClick(item: PollSession, position: Int): Boolean {
        val pollResultsFragment = PollResultsFragment()
        val bundle = Bundle()
        bundle.putParcelable(Constants.POLL_DATA, poll)
        bundle.putParcelable(Constants.POLL_SESSION, item)
        pollResultsFragment.arguments = bundle
        (activity as FragmentManagerActivity).swapFragments(pollResultsFragment, PollResultsFragment::class.java.simpleName)
        return true
    }

    override fun areItemsSorted(): Boolean = true
    override fun loadFirstPage() = PollsManager.getFirstPagePollSessions(poll!!.id, pollSessionCallback!!, true)
    override fun loadNextPage(nextURL: String) = PollsManager.getNextPagePollSessions(nextURL, pollSessionCallback!!, true)
    override fun getNextURL(): String? = sessionNextUrl
    override fun setNextURLNull() {
        sessionNextUrl = null
    }

    override fun resetData() = Unit

    override fun setupCallbacks() {
        pollSessionCallback = object : StatusCallback<PollSessionResponse>() {
            override fun onResponse(response: Response<PollSessionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return

                val pollSessions = response.body()!!.pollSessions
                sessionNextUrl = linkHeaders.nextUrl
                if (pollSessions != null) {
                    for (pollSession in pollSessions) {
                        addItem(pollSession)
                        SectionManager.getSection(pollSession.courseId, pollSession.courseSectionId, sectionCallback!!, true)
                    }
                }
            }

            override fun onFinished(type: ApiType) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        sectionCallback = object : StatusCallback<Section>() {
            override fun onResponse(response: Response<Section>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return
                response.body()?.let {
                    sectionMap[it.id] = it
                    notifyDataSetChanged()
                }
            }
        }

        pollChoiceCallback = object : StatusCallback<PollChoiceResponse>() {
            override fun onResponse(response: Response<PollChoiceResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (activity == null || type.isCache) return
                response.body()?.let {
                    val pollChoices = it.pollChoices
                    if (pollChoices != null) {
                        pollChoiceArrayList.addAll(pollChoices)
                    }
                }

                // If linkHeaders.nextURL is null it means we have all the choices, so we can go to the edit poll page now
                // or generate the CSV, depending on which action they selected
                if (!StatusCallback.moreCallsExist(linkHeaders)) {
                    if (fromGenerateCSV) {
                        // Generate a map from the array list of poll choices
                        for (choice in pollChoiceArrayList) {
                            pollChoiceMap[choice.id] = choice
                        }
                        generateCSV()
                    } else {
                        val addQuestionFragment = AddQuestionFragment()
                        // Populate the current data with the bundle
                        val bundle = Bundle()
                        bundle.putParcelable(Constants.POLL_BUNDLE, poll)
                        bundle.putParcelableArrayList(Constants.POLL_CHOICES, pollChoiceArrayList)
                        addQuestionFragment.arguments = bundle
                        (activity as FragmentManagerActivity).swapFragments(addQuestionFragment, AddQuestionFragment::class.java.simpleName, R.anim.slide_in_from_bottom, 0, 0, R.anim.slide_out_to_bottom)
                    }
                } else {
                    // Otherwise, get the next group of poll choices.
                    PollsManager.getNextPagePollChoices(linkHeaders.nextUrl!!, pollChoiceCallback!!, false)
                }
            }

            override fun onFinished(type: ApiType) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        responseCanvasCallback = object : StatusCallback<ResponseBody>() {
            override fun onFail(call: Call<ResponseBody>?, error: Throwable, response: Response<*>?) {
                AppMsg.makeText(activity, getString(R.string.errorDeletingPollSession), AppMsg.STYLE_ERROR).show()
                // We didn't actually delete anything, but we removed the item from the list to make the animation smoother, so now
                // lets get the poll sessions again
                reloadData()
            }
        }
    }
}
