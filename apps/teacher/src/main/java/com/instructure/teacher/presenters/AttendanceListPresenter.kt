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

package com.instructure.teacher.presenters

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.AttendanceManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.SectionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ApiType
import com.instructure.canvasapi2.utils.LinkHeaders
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.teacher.viewinterface.AttendanceListView
import com.instructure.pandautils.blueprint.SyncPresenter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AttendanceListPresenter(private val mCanvasContext: CanvasContext, private val mTab: Tab) :
        com.instructure.pandautils.blueprint.SyncPresenter<Attendance, AttendanceListView>(Attendance::class.java) {

    private var mToken: String? = null
    private var mCookie: String? = null
    private var mForceNetwork: Boolean = false
    private var mSelectedDate: Calendar = Calendar.getInstance()
    private var mMarkAllJob: Job? = null
    private var mApiCalls: Job? = null
    private var mJobList: HashMap<Long, Job?> = HashMap()
    private var mAvailableSections: List<Section>? = null
    private var mAttendanceBySectionJob: Job? = null
    private var mSelectedSection: Section? = null

    override fun loadData(forceNetwork: Boolean) {
        if(data.size() == 0) {
            mForceNetwork = forceNetwork
            viewCallback?.launchLTI(mTab)
        }
    }

    override fun refresh(forceNetwork: Boolean) {
        mForceNetwork = true
        onRefreshStarted()
        clearData()
        loadData(forceNetwork)
    }

    private fun partialRefresh() {
        onRefreshStarted()
        clearData()
        fetchAttendance(mToken, mCookie)
    }

    /**
     * Fetches (GET) the list of attendance objects and enrollments
     */
    fun fetchAttendance(token: String?, cookie: String?) {
        if(token == null || cookie == null) {
            viewCallback?.unableToLoad()
        } else {
            mToken = token
            mCookie = cookie
            mApiCalls = tryWeave {
                val userId = ApiPrefs.user?.id ?: 0
                val enrollmentsList = awaitApi<List<Enrollment>> {
                    EnrollmentManager.getAllEnrollmentsForUserInCourse(mCanvasContext.id, userId, mForceNetwork, it)
                }

                var atLeastOneUnlimitedEnrollment : Boolean
                try { enrollmentsList.first { !it.limitPrivilegesToCourseSection }
                    atLeastOneUnlimitedEnrollment = true
                } catch (e: NoSuchElementException) {
                    atLeastOneUnlimitedEnrollment = false
                }

                if(enrollmentsList.isEmpty()) {
                    updateAsFinished()
                    return@tryWeave
                }

                val sectionIds = ArrayList<Long>()
                enrollmentsList.forEach { sectionIds.add(it.courseSectionId) }

                val sections = awaitApi<List<Section>> { SectionManager.getAllSectionsForCourse(mCanvasContext.id, it, mForceNetwork) }

                //Probably a better way to do this...
                val availableSections = sections.filter { atLeastOneUnlimitedEnrollment || sectionIds.contains(it.id) }

                if(availableSections.isEmpty()) {
                    updateAsFinished()
                    return@tryWeave
                }

                mAvailableSections = availableSections
                if(mSelectedSection == null) {
                    mSelectedSection = availableSections.first()
                }

                val attendanceList = awaitApi<List<Attendance>> {
                    AttendanceManager.getAttendance(mSelectedSection!!.id, getSelectedDate(), mToken!!, mCookie!!, it, true)
                }

                data.addOrUpdate(attendanceList)
                viewCallback?.addSectionMenu(mSelectedSection, mAvailableSections)
                viewCallback?.updateSectionPicked(mSelectedSection)
                updateAsFinished()
            } catch {
                Logger.e("Error fetching attendance and enrollments: " + it.message)
            }
        }
    }

    private fun fetchAttendanceForSection(sectionId: Long) {
        mAttendanceBySectionJob = tryWeave {
            viewCallback?.onRefreshStarted()
            val attendanceList = awaitApi<List<Attendance>> {
                AttendanceManager.getAttendance(sectionId, getSelectedDate(), mToken!!, mCookie!!, it, true)
            }
            data.clear()
            data.addOrUpdate(attendanceList)
            mAvailableSections?.forEach {
                if(it.id == sectionId) {
                    mSelectedSection = it
                    viewCallback?.updateSectionPicked(it)
                    return@forEach
                }
            }
            updateAsFinished()
        } catch {
            Logger.e("Error fetching attendance for section (" + sectionId + "): " + it.message)
        }
    }

    private fun updateAsFinished() {
        viewCallback?.onRefreshFinished()
        viewCallback?.checkIfEmpty()
        checkMarkAllVisibility()
    }

    private fun selectSection(sectionId: Long) {
        mAvailableSections?.filter { it.id == sectionId }?.let {
            fetchAttendanceForSection(sectionId)
        }
    }

    //We have to select by position because the MenuItem object only supports Integers where our IDs are Longs.
    fun selectSectionByPosition(position: Int) {
        try {
            val section = mAvailableSections?.get(position)
            if(section != null) {
                selectSection(section.id)
            }
        } catch (e: IndexOutOfBoundsException) {
            //Do nothing, don't show an error.
        }
    }

    /**
     * Marks attendance in a cycled fashion going from: Present -> Absent -> Late -> Unmarked
     */
    fun markAttendance(attendance: Attendance) {
        val statusTo = cycleAttendance(attendance)
        markAttendance(attendance, statusTo)
    }

    /**
     * Marks attendance to a status as passed in this function
     */
    fun markAttendance(attendance: Attendance, statusTo: Attendance.Attendance, fromMarkAllJobs: Boolean = false) {
        if(attendance.attendanceStatus() == statusTo) return
        if(!fromMarkAllJobs && mMarkAllJob?.isActive ?: false) return //If in the process of marking all, don't allow changes

        mJobList[attendance.studentId]?.cancel()
        mJobList[attendance.studentId] = tryWeave {
            attendance.setDate(mSelectedDate)
            attendance.setAttendanceStatus(statusTo)
            viewCallback?.notifyAttendanceAsMarked(attendance)

            delay(600)

            attendance._postingAttendance = attendance.attendance
            AttendanceManager.markAttendance(attendance, mToken!!, mCookie!!, object : StatusCallback<Attendance>() {
                override fun onResponse(response: Response<Attendance>, linkHeaders: LinkHeaders, type: ApiType) {
                    if(type.isAPI) {
                        response.body()?.let {
                            if (response.code() in 200..299) {
                                when (attendance.attendanceStatus()) {
                                    Attendance.Attendance.UNMARKED -> attendance.statusId = null // Successful DELETE
                                    else -> { attendance.statusId = it.statusId }
                                }
                            }
                            mJobList.remove(attendance.studentId)
                        }
                    }
                }

                override fun onFail(call: Call<Attendance>?, error: Throwable, response: Response<*>?) {
                    mJobList.remove(attendance.studentId)
                    attendance.attendance = attendance._postingAttendance //Revert status
                    viewCallback?.notifyAttendanceAsMarked(attendance)
                }

                override fun onFinished(type: ApiType) {
                    if(type.isAPI) {
                        checkMarkAllVisibility()
                    }
                }
            }, mForceNetwork)
        } catch {
            Logger.d("Could not mark attendance: " + it.message)
        }
    }

    /**
     * Cycles the attendance status
     */
    private fun cycleAttendance(attendance: Attendance): Attendance.Attendance {
        //Order of updating = Present -> Absent -> Late -> Unmarked
        when(attendance.attendanceStatus()) {
            Attendance.Attendance.PRESENT -> return Attendance.Attendance.ABSENT
            Attendance.Attendance.ABSENT -> return Attendance.Attendance.LATE
            Attendance.Attendance.LATE -> return Attendance.Attendance.UNMARKED
            Attendance.Attendance.UNMARKED -> return Attendance.Attendance.PRESENT
        }
    }

    /**
     * Marks attendance for all the remaining unmarked persons
     */
    fun bulkMarkAttendance() {
        if(mMarkAllJob?.isActive ?: false) return

        mMarkAllJob = tryWeave {
            (0..data.size() - 1)
                    .map { data.get(it) }
                    .filter { Attendance.Attendance.UNMARKED == it.attendanceStatus() }
                    .forEach {
                        markAttendance(it, Attendance.Attendance.PRESENT, fromMarkAllJobs = true)
                    }
        } catch {
            Logger.d("Could not mark rest as Present: " + it.message)
        }
    }

    fun getSelectedDate(): Calendar {
        return mSelectedDate
    }

    fun setSelectedDate(date: Calendar) {
        mSelectedDate = date
        partialRefresh()
    }

    private fun checkMarkAllVisibility() {
        try {
            val items = ArrayList<Attendance>(data.size())
            (0..data.size() -1).mapTo(items) { data.get(it) }

            if(items.size == 0) {
                viewCallback?.updateMarkAllButtonVisibility(false)
                return
            }

            //Check to see if we have at least one that is marked as Present, Late, or Absent
            items.first { Attendance.Attendance.UNMARKED != it.attendanceStatus() }
            viewCallback?.updateMarkAllButton(true)

            //If all of them are marked as Present, Late, or Absent then hide the button
            try {
                //Check to see if we have at least one that is unmarked
                items.first { Attendance.Attendance.UNMARKED == it.attendanceStatus() }
                viewCallback?.updateMarkAllButtonVisibility(true)
            } catch (e: NoSuchElementException) {
                viewCallback?.updateMarkAllButtonVisibility(false)
            }
        } catch (e: NoSuchElementException) {
            viewCallback?.updateMarkAllButton(false)
            viewCallback?.updateMarkAllButtonVisibility(true)
        }
    }

    override fun compare(item1: Attendance, item2: Attendance): Int {
        if(item1.student != null && item2.student != null && item1.student?.sortableName != null && item2.student?.sortableName != null) {
            return item1.student!!.sortableName?.compareTo(item2.student!!.sortableName!!) ?: -1
        }
        return -1
    }

    override fun areContentsTheSame(item1: Attendance, item2: Attendance): Boolean {
        return false
    }

    override fun areItemsTheSame(item1: Attendance, item2: Attendance): Boolean {
        return item1.studentId == item2.studentId
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mMarkAllJob?.cancel()
        mApiCalls?.cancel()
        mAttendanceBySectionJob?.cancel()
        mJobList.forEach { (_, job) -> job?.cancel() }
    }
}
