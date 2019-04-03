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
package com.instructure.parentapp.presenters

import android.text.TextUtils

import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.WeaveJob
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.parentapp.models.WeekHeaderItem
import com.instructure.parentapp.models.isValidForParent
import com.instructure.parentapp.util.AnalyticUtils
import com.instructure.parentapp.viewinterface.WeekView

import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.HashMap

import instructure.androidblueprint.SyncExpandablePresenter

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class WeekPresenter(student: User, course: Course?) : SyncExpandablePresenter<WeekHeaderItem, ScheduleItem, WeekView>(WeekHeaderItem::class.java, ScheduleItem::class.java) {

    private var courseJob: WeaveJob? = null
    private var calendarJob: WeaveJob? = null

    var student: User? = null
    var course: Course? = null

    private var startDate = GregorianCalendar()
    private val endDate = GregorianCalendar()

    private var courseMap = HashMap<Long, Course>()
    private var headers = HashMap<Int, WeekHeaderItem>(7)

    private val contextCodes: ArrayList<String>
        get() {
            val contextCodes = ArrayList<String>()
            for (course in courses) {
                contextCodes.add(course.contextId)
            }
            return contextCodes
        }

    val courses: ArrayList<Course>
        get() {
            if (course != null) {
                val courses = ArrayList<Course>()
                courses.add(course!!)
                return courses
            } else {
                return ArrayList(courseMap.values)
            }
        }

    val coursesMap: Map<Long, Course>
        get() {
            if (course != null) {
                val map = HashMap<Long, Course>()
                map[course!!.id] = course!!
                return map
            } else {
                return courseMap
            }
        }

    init {
        this.student = student
        this.course = course

        initCalendars()
        initHeaders()
    }

    override fun onDestroyed() {
        super.onDestroyed()
        courseJob?.cancel()
        calendarJob?.cancel()
    }

    private fun publishScheduleItem(items: List<ScheduleItem>) {
        for (item in items) {
            if (item.hasAssignmentOverrides() && item.assignmentOverrides!![0]?.dueAt != null) {
                val date = dateToCalendar(item.assignmentOverrides!![0]!!.dueAt!!)
                amendDateAndUpdate(item, date)
            } else if (item.isAllDay && item.allDayAt != null) {
                val date = dateToCalendar(item.allDayDate!!)
                amendDateAndUpdate(item, date)
            } else if (item.startAt != null) {
                val date = dateToCalendar(item.startDate!!)
                amendDateAndUpdate(item, date)
            } else {
                Logger.e("Could not parse schedule item, invalid date: " + item.id)
            }
        }
    }

    private fun amendDateAndUpdate(item: ScheduleItem, date: Calendar) {
        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
        val group = headers[dayOfWeek]
        group?.let {
            it.date = date
        data.addOrUpdateItem(it, item)
        }
    }


    override fun compare(group1: WeekHeaderItem, group2: WeekHeaderItem): Int {
        return if (group1.comparisonDate != null && group2.comparisonDate != null) {
            group1.comparisonDate!!.compareTo(group2.comparisonDate)
        } else super.compare(group1, group2)
    }

    override fun loadData(forceNetwork: Boolean) {
        onRefreshStarted()

        setWeekText()
        if (courseMap.isEmpty()) {
            getCourses(forceNetwork)
        } else {
            getCalendarEvents(forceNetwork)
        }
    }

    override fun refresh(forceNetwork: Boolean) {
        clearData()
        courseJob?.cancel()
        calendarJob?.cancel()
        
        // Additionally, courses need to be cleared, as they could be leftover from a previously selected student
        courseMap.clear()
        loadData(forceNetwork)
    }

    private fun getCourses(forceNetwork: Boolean) {
        viewCallback?.let {
            courseJob = tryWeave {
                val courses = awaitApi<List<Course>> { CourseManager.getCoursesWithSyllabus(forceNetwork, it) }.filter {
                    for (enrollment in it.enrollments!!) {
                        if (enrollment.userId == student?.id) {
                            // If the enrollment is valid, let's check the other aspects of the course
                            return@filter it.isValidForParent()
                        }
                    }
                    false
                }

                addToMap(courses)
                getCalendarEvents(forceNetwork)
            } catch {

            }

        }
    }


    private fun getCalendarEvents(forceNetwork: Boolean) {
        viewCallback?.let {
            calendarJob = tryWeave {
                val scheduleItems = awaitApi<List<ScheduleItem>> { CalendarEventManager.getCalendarEventsExhaustive(
                        false,
                        CalendarEventAPI.CalendarEventType.ASSIGNMENT,
                        APIHelper.dateToString(startDate),
                        APIHelper.dateToString(endDate),
                        contextCodes,
                        it,
                        forceNetwork) }

                val courseAssignmentMap = HashMap<Long, List<Long>>()

                courses.forEach { course ->
                    courseAssignmentMap[course.id] = scheduleItems.filter { course.id == it.courseId }.map{ it.assignment?.id!!  }
                }

                val assignmentSubmissions = ArrayList<Submission>()

                if (courseAssignmentMap.isNotEmpty()) {
                    courses.forEach { course ->
                        // We want to make sure this course has an assignment for this time period
                        if(courseAssignmentMap[course.id]?.isNotEmpty() == true) {
                            assignmentSubmissions.addAll(awaitApi<List<Submission>> {
                                SubmissionManager.getSubmissionsForMultipleAssignments(
                                        student?.id ?: 0,
                                        course.id,
                                        courseAssignmentMap[course.id]!!,
                                        it,
                                        forceNetwork)
                            })
                        }
                    }
                }
                val events = awaitApi<List<ScheduleItem>> { CalendarEventManager.getCalendarEventsExhaustive(
                        false,
                        CalendarEventAPI.CalendarEventType.CALENDAR,
                        APIHelper.dateToString(startDate),
                        APIHelper.dateToString(endDate),
                        contextCodes,
                        it,
                        forceNetwork) }

                // At this point the assignments variable should have all the submissions and their assignments, now
                // we just need to convert them to scheduleItems
                assignmentSubmissions.forEach {
                    // Need to null out the assignment so it's not a stackoverflow error when it tries to parcel it
                    val submission = it.parcelCopy().copy(assignment = null)
                    it.assignment?.submission = submission
                }
                val assignmentScheduleItems = assignmentSubmissions.mapNotNull{ it.assignment?.toScheduleItem() }
                publishScheduleItem(assignmentScheduleItems)
                publishScheduleItem(events)

                viewCallback?.let {
                    it.onRefreshFinished()
                    it.checkIfEmpty()
                }
            } catch {
                viewCallback?.let {
                    it.onRefreshFinished()
                    it.checkIfEmpty()
                }
            }
        }
    }

    fun setStudent(student: User, refresh: Boolean) {
        this.student = student
        if (refresh) {
            refresh(false)
        }
    }

    //region Calendar Management

    private fun dateToCalendar(date: Date): GregorianCalendar {
        val calendar = GregorianCalendar()
        calendar.timeInMillis = date.time
        return calendar
    }

    //endregion

    //region Next, Prev, Date Text

    private fun addToMap(courses: List<Course>) {
        for (course in courses) {

            // update the current course if it exists
            if (this.course != null && course.id == this.course!!.id) {
                this.course = course
            }
            // We won't be able to get the name if the course hasn't started yet or if the user doesn't have
            // access to the course. So we don't want to add the course to the list
            if (!TextUtils.isEmpty(course.name)) {
                courseMap[course.id] = course
            }
        }
    }

    fun nextWeekClicked() {
        if (course == null) {
            AnalyticUtils.trackButtonPressed(AnalyticUtils.WEEK_NAV_NEXT)
        } else {
            AnalyticUtils.trackFlow(AnalyticUtils.COURSE_FLOW, AnalyticUtils.WEEK_NAV_NEXT)
        }

        startDate.add(TIME_SPAN, 1)
        adjustEndTime()
        setWeekText()
        refresh(true)
    }

    fun prevWeekClicked() {
        if (course == null) {
            AnalyticUtils.trackButtonPressed(AnalyticUtils.WEEK_NAV_PREVIOUS)
        } else {
            AnalyticUtils.trackFlow(AnalyticUtils.COURSE_FLOW, AnalyticUtils.WEEK_NAV_PREVIOUS)
        }

        startDate.add(TIME_SPAN, -1)
        adjustEndTime()
        setWeekText()
        refresh(true)
    }

    private fun adjustEndTime() {
        endDate.timeInMillis = startDate.timeInMillis
        endDate.add(TIME_SPAN, 1)
    }

    private fun setWeekText() {
        val dates = ArrayList<GregorianCalendar>(2)
        dates.add(0, startDate)
        endDate.add(Calendar.SECOND, -1)
        dates.add(1, endDate)
        if (viewCallback != null) {
            viewCallback!!.updateWeekText(dates)
        }
        endDate.add(Calendar.SECOND, 1)
    }

    fun onNewDatePicked(datePicked: GregorianCalendar) {
        startDate = datePicked
        initCalendars()
        refresh(false)
    }

    //endregion

    //region Setup

    private fun initCalendars() {
        cleanCalendar(startDate)
        adjustEndTime()
    }

    private fun initHeaders() {
        headers[Calendar.SUNDAY] = WeekHeaderItem(Calendar.SUNDAY)
        headers[Calendar.MONDAY] = WeekHeaderItem(Calendar.MONDAY)
        headers[Calendar.TUESDAY] = WeekHeaderItem(Calendar.TUESDAY)
        headers[Calendar.WEDNESDAY] = WeekHeaderItem(Calendar.WEDNESDAY)
        headers[Calendar.THURSDAY] = WeekHeaderItem(Calendar.THURSDAY)
        headers[Calendar.FRIDAY] = WeekHeaderItem(Calendar.FRIDAY)
        headers[Calendar.SATURDAY] = WeekHeaderItem(Calendar.SATURDAY)
    }

    private fun cleanCalendar(calendar: GregorianCalendar) {
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    companion object {
        private const val TIME_SPAN = Calendar.WEEK_OF_MONTH
    }
    //endregion
}
