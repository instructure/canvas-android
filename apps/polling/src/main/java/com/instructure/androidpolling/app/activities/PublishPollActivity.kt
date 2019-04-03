/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.androidpolling.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.LinearLayout
import android.widget.TextView

import com.devspark.appmsg.AppMsg
import com.instructure.androidpolling.app.R
import com.instructure.androidpolling.app.util.ApplicationManager
import com.instructure.androidpolling.app.util.Constants
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.PollsManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PollSession
import com.instructure.canvasapi2.models.PollSessionResponse
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import kotlinx.android.synthetic.main.activity_publish_poll.*

import java.util.ArrayList
import java.util.Date

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class PublishPollActivity : AppCompatActivity() {

    private var pollID: Long = 0
    private var currentCourse: Course? = null

    private var courseAdapter: CourseSpinnerAdapter? = null
    private var sectionAdapter: SectionListAdapter? = null

    private var openPollSessionCallback: StatusCallback<PollSessionResponse>? = null

    private var sessionCount = 0
    private var sessionCreatedCount = 0

    private val openPollSessions = ArrayList<PollSession>()

    private var singlePollSession: PollSession? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_poll)

        pollID = intent.extras!!.getLong(Constants.POLL_ID)

        setupClickListeners()
        setupCallbacks()

        PollsManager.getFirstPagePollSessions(pollID, openPollSessionCallback!!, true)

        setupCourseSpinner(ApplicationManager.getCourseList(this@PublishPollActivity))
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    private fun setupCourseSpinner(courseList: MutableList<Course>) {
        // We only want courses we're a teacher for if we're trying to publish a poll
        val iterator = courseList.iterator()
        while (iterator.hasNext()) {
            if (!iterator.next().isTeacher) {
                iterator.remove()
            }
        }

        if (courseList.size == 0 || courseList[0].name != getString(R.string.selectCourse)) {
            val selectCourse = Course(
                    id = Long.MIN_VALUE,
                    name = getString(R.string.selectCourse)
            )
            courseList.add(0, selectCourse)
        }

        courseAdapter = CourseSpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, courseList.toList() as ArrayList<Course>)

        coursesSpinner!!.adapter = courseAdapter
        coursesSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val course = parent.adapter.getItem(position) as Course

                // Handle the loading cell.
                if (course?.enrollments == null) {
                    return
                }

                if (!isCourseTermActive(course)) {
                    AppMsg.makeText(this@PublishPollActivity, getString(R.string.courseTermInactive), AppMsg.STYLE_WARNING).show()
                    return
                }

                currentCourse = course

                // Unselect all the selections, if we don't it still thinks some are selected when we go to another course
                for (i in 0 until sectionListView!!.count) {
                    sectionListView.setItemChecked(i, false)
                }

                SectionManager.getAllSectionsForCourse(course.id, object : StatusCallback<List<Section>>() {
                    override fun onResponse(response: retrofit2.Response<List<Section>>, linkHeaders: com.instructure.canvasapi2.utils.LinkHeaders, type: ApiType) {
                        setupSectionAdapter(response.body()?.toMutableList())
                        ApplicationManager.saveSections(this@PublishPollActivity, response.body(), (coursesSpinner!!.selectedItem as Course).id)

                    }
                }, true)
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }
    }

    private fun setupSectionAdapter(sectionList: MutableList<Section>?) {
        sectionLabel.visibility = View.VISIBLE

        sectionAdapter = SectionListAdapter(this, android.R.layout.simple_spinner_dropdown_item, sectionList?.toList() as ArrayList<Section>?
                ?: emptyList<Section>() as ArrayList<Section>)

        if (openPollSessions.size < sectionList!!.size && sectionList.size > 1 && sectionList[0].name != getString(R.string.entireCourse)) {
            val section = Section(
                    id = Long.MIN_VALUE,
                    name = getString(R.string.entireCourse)
            )
            sectionList.add(0, section)
            sectionAdapter!!.notifyDataSetChanged()
        }

        sectionListView.adapter = sectionAdapter
        sectionListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val checked = sectionListView.checkedItemPositions
            if (!hasSectionUnpublished()) {
                // We don't want them to check anything
                sectionListView.setItemChecked(position, false)
                AppMsg.makeText(this@PublishPollActivity, getString(R.string.alreadyPublishedForSection), AppMsg.STYLE_WARNING).show()
                return@OnItemClickListener
            }
            if (checked.get(0) && position == 0) {
                // If the "entire course" is selected, select all the other items
                (0 until sectionListView!!.count)
                        .filterNot { isSectionPublished(sectionListView!!.getItemAtPosition(it) as Section) }
                        .forEach { sectionListView!!.setItemChecked(it, true) }
            } else if (!checked.get(0) && position == 0) {
                // If the "entire course" is unselected, unselect everything
                (0 until sectionListView!!.count)
                        .filterNot { isSectionPublished(sectionListView!!.getItemAtPosition(it) as Section) }
                        .forEach { sectionListView!!.setItemChecked(it, false) }
            } else if (position != 0) {
                // If we unselect one of the sections, we don't want to have the "entire course" item selected.
                // If they aren't all selected, unselect "entire course"
                for (i in 1 until sectionListView!!.count) {
                    if (!checked.get(i)) {
                        sectionListView!!.setItemChecked(0, false)
                    }
                    // If a session is already published for a section, don't let them select it again
                    if (isSectionPublished(sectionListView!!.getItemAtPosition(i) as Section)) {
                        sectionListView!!.setItemChecked(i, false)
                        if (i == position) {
                            AppMsg.makeText(this@PublishPollActivity, getString(R.string.alreadyPublishedForSection), AppMsg.STYLE_WARNING).show()
                        }
                    }
                }
            }
        }

        // If there is only one section, auto select it
        if (sectionListView!!.adapter.count == 1) {
            sectionListView!!.setItemChecked(0, true)
        }
    }

    private fun setupClickListeners() {
        publishPoll!!.setOnClickListener(View.OnClickListener {
            val sections = ArrayList<Section>()
            // Make sure we have something selected
            val course = coursesSpinner!!.selectedItem as Course
            if (course.id != java.lang.Long.MIN_VALUE) {
                // We have a valid course
                // Now, check which sections are selected
                val checked = sectionListView!!.checkedItemPositions

                if (sectionListView!!.checkedItemCount == 0) {
                    // The user didn't select a section
                    AppMsg.makeText(this@PublishPollActivity, getString(R.string.pleaseSelectSection), AppMsg.STYLE_WARNING).show()
                    return@OnClickListener
                }
                sessionCount = 0
                sessionCreatedCount = 0
                // Get how many actual sections are checked
                for (i in 0 until sectionListView!!.adapter.count) {
                    if (checked.get(i)) {
                        if (sectionAdapter!!.getItem(i)!!.id != java.lang.Long.MIN_VALUE) {
                            sessionCreatedCount++
                            sections.add(sectionAdapter!!.getItem(i)!!)

                        }
                    }
                }
                // Don't want to let the user submit multiple times
                publishPoll!!.isEnabled = false
                for (i in 0 until sessionCreatedCount) {
                    PollsManager.createPollSession(pollID, course.id, sections[i].id, object : StatusCallback<PollSessionResponse>() {
                        override fun onResponse(response: retrofit2.Response<PollSessionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                            val pollSession = response.body()!!.pollSessions
                            if (pollSession.size > 0) {
                                singlePollSession = pollSession[0]
                                // Publish all the sessions
                                PollsManager.openPollSession(pollID, pollSession[0].id, object : StatusCallback<ResponseBody>() {
                                    override fun onResponse(response: Response<ResponseBody>, linkHeaders: LinkHeaders, type: ApiType) {
                                        sessionCount++
                                        //publish all the sessions
                                        if (sessionCount == sessionCreatedCount) {

                                            //if only one session was created we want to send the user to the results screen.
                                            if (sessionCount == 1) {
                                                if (singlePollSession != null) {
                                                    val intent = intent
                                                    intent.putExtra(Constants.POLL_SESSION, singlePollSession as Parcelable?)
                                                    intent.putExtra(Constants.POLL_ID, pollID)
                                                    setResult(Constants.PUBLISH_POLL_SUCCESS, intent)
                                                }
                                            } else {
                                                //then close this screen
                                                setResult(Constants.PUBLISH_POLL_SUCCESS_MULTIPLE)
                                            }
                                            finish()
                                        }
                                    }

                                    override fun onFail(call: Call<ResponseBody>?, error: Throwable, response: Response<*>?) {
                                        publishPoll!!.isEnabled = true
                                    }
                                }, true)
                            }
                        }
                    }, true)
                }
            } else {
                //the user didn't select a course
                AppMsg.makeText(this@PublishPollActivity, getString(R.string.pleaseSelectCourse), AppMsg.STYLE_WARNING).show()
            }
        })
    }

    private fun isSectionPublished(section: Section?): Boolean = openPollSessions.any { it.isPublished && it.courseSectionId == section!!.id }

    // Check to see if there is a section that is unpublished
    private fun hasSectionUnpublished(): Boolean {

        // We'll just use the count of open polls vs. sections in the course. If there are the same
        // amount of sections and sessions, then there all the poll sessions are published
        var sectionCount = sectionAdapter!!.count
        if (sectionAdapter!!.getItem(0)!!.id == java.lang.Long.MIN_VALUE) {
            sectionCount--
        }
        // Now get the open sessions for this course
        var courseSessionCount = 0
        if (currentCourse != null) {
            openPollSessions
                    .filter { it.courseId == currentCourse!!.id }
                    .forEach { courseSessionCount++ }
        }
        return courseSessionCount < sectionCount
    }

    private fun isCourseTermActive(course: Course): Boolean = !(course.term != null && course.term!!.endDate != null && course.term!!.endDate!!.before(Date()))

    ///////////////////////////////////////////////////////////////////////////
    // Adapters
    ///////////////////////////////////////////////////////////////////////////

    inner class CourseSpinnerAdapter(context: Context, textViewResourceId: Int, courses: ArrayList<Course>) : ArrayAdapter<Course>(context, textViewResourceId, courses) {

        private var courses = ArrayList<Course>()

        init {
            this.courses = courses
        }

        fun getCourses(): Array<Course> {
            return courses.toTypedArray()
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getCustomView(position, convertView, parent)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getCustomView(position, convertView, parent)
        }

        private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            var holder: CourseViewHolder? = null

            if (convertView == null) {
                /* There is no view at this position, we create a new one.
		           In this case by inflating an xml layout */
                convertView = layoutInflater.inflate(R.layout.spinner_row_generic, null) as LinearLayout

                holder = CourseViewHolder()
                holder.courseName = convertView.findViewById<View>(R.id.name) as TextView

                convertView.tag = holder
            } else {
                /* We recycle a View that already exists */
                holder = convertView.tag as CourseViewHolder
            }

            if (courses[position] != null) {
                if (!isCourseTermActive(courses[position])) {
                    holder.courseName!!.isEnabled = false
                }

                holder.courseName!!.text = courses[position].name
            }
            return convertView
        }
    }

    private class CourseViewHolder {
        internal var courseName: TextView? = null
    }

    inner class SectionListAdapter(context: Context, textViewResourceId: Int,
                                   sectionsList: ArrayList<Section>) : ArrayAdapter<Section>(context, textViewResourceId, sectionsList) {

        private var sections = ArrayList<Section>()

        init {
            this.sections = sectionsList
        }

        fun getSections(): Array<Section> {
            return sections.toTypedArray()
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getCustomView(position, convertView, parent)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getCustomView(position, convertView, parent)
        }

        override fun getItem(position: Int): Section? {
            return sections[position]
        }

        fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView

            var holder: SectionViewHolder? = null

            if (convertView == null) {
                /* There is no view at this position, we create a new one.
		           In this case by inflating an xml layout */
                convertView = layoutInflater.inflate(android.R.layout.simple_list_item_multiple_choice, null)

                holder = SectionViewHolder()
                holder.sectionName = convertView!!.findViewById<View>(android.R.id.text1) as CheckedTextView

                convertView.tag = holder
            } else {
                /* We recycle a View that already exists */
                holder = convertView.tag as SectionViewHolder
            }

            if (sections[position] != null) {
                holder.sectionName!!.text = sections[position].name
            }

            if (isSectionPublished(getItem(position))) {
                holder.sectionName!!.isEnabled = false
                holder.sectionName!!.isChecked = false
            } else {
                holder.sectionName!!.isEnabled = true
            }
            return convertView
        }
    }

    private class SectionViewHolder {
        internal var sectionName: CheckedTextView? = null
    }

    private fun setupCallbacks() {

        openPollSessionCallback = object : StatusCallback<PollSessionResponse>() {
            override fun onResponse(response: retrofit2.Response<PollSessionResponse>, linkHeaders: LinkHeaders, type: ApiType) {
                if (type.isCache) return

                val pollSessions = response.body()!!.pollSessions
                if (pollSessions != null) {
                    for (pollSession in pollSessions) {
                        if (pollSession.isPublished) {
                            openPollSessions.add(pollSession)
                        }
                    }
                }

                if (StatusCallback.moreCallsExist(linkHeaders)) {
                    PollsManager.getNextPagePollSessions(linkHeaders.nextUrl!!, this, true)
                }
            }
        }
    }

    companion object {

        fun createIntent(context: Context, pollId: Long): Intent {
            val intent = Intent(context, PublishPollActivity::class.java)
            intent.putExtra(Constants.POLL_ID, pollId)
            return intent
        }
    }
}
