/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.homeroom

import android.content.res.Resources
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.CourseCardViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.ColorApiHelper
import kotlinx.coroutines.awaitAll
import org.threeten.bp.LocalDate

private const val PLANNABLE_TYPE_ASSIGNMENT = "assignment"

class CourseCardCreator(
    private val plannerManager: PlannerManager,
    private val userManager: UserManager,
    private val announcementManager: AnnouncementManager,
    private val resources: Resources
) {

    suspend fun createCourseCards(dashboardCourses: List<Course>, forceNetwork: Boolean,
                                  updateAssignments: Boolean, events: MutableLiveData<Event<HomeroomAction>>): List<CourseCardViewModel> {
        val announcements = dashboardCourses
            .map { announcementManager.getLatestAnnouncementAsync(it, forceNetwork) }
            .awaitAll()
            .map { it.dataOrNull?.firstOrNull() }

        val now = LocalDate.now()
        val tomorrow = now.plusDays(1).toApiString()

        val forceNetworkAssignments = forceNetwork || updateAssignments

        val plannerItems = plannerManager.getPlannerItemsAsync(forceNetworkAssignments, now.toApiString(), tomorrow).await().dataOrNull
        val missingSubmissions = userManager.getAllMissingSubmissionsAsync(forceNetworkAssignments).await().dataOrNull

        val assignmentsDueStrings = createDueTexts(dashboardCourses,
            plannerItems ?: emptyList(),
            missingSubmissions ?: emptyList())

        return dashboardCourses
            .mapIndexed { index, course ->
                val viewData = CourseCardViewData(
                    course.name,
                    assignmentsDueStrings[course.id]
                        ?: SpannableString(resources.getString(R.string.nothingDueToday)),
                    announcements[index]?.title ?: "",
                    getCourseColor(course),
                    course.imageUrl ?: "")

                CourseCardViewModel(
                    viewData,
                    { events.postValue(Event(HomeroomAction.OpenCourse(course))) },
                    { events.postValue(Event(HomeroomAction.OpenAssignments(course))) },
                    { openAnnouncementDetails(events, course, announcements[index]) }
                )
            }
    }

    private fun createDueTexts(courses: List<Course>, plannerItems: List<PlannerItem>, missingAssignments: List<Assignment>): Map<Long, SpannableString> {
        val dueTodayCountByCourses = courses
            .associate { Pair(it.id, 0) }
            .toMutableMap()

        plannerItems
            .filter { isNotSubmittedAssignment(it) }
            .forEach { item ->
                dueTodayCountByCourses.computeIfPresent(item.courseId!!) { _, value -> value + 1 }
            }

        val missingCountByCourses = courses
            .associate { Pair(it.id, 0) }
            .toMutableMap()

        missingAssignments
            .filter { it.plannerOverride?.dismissed != true }
            .forEach { item ->
                missingCountByCourses.computeIfPresent(item.courseId) { _, value -> value + 1 }
            }

        return courses
            .associate {
                Pair(it.id, createDueTextForCourse(dueTodayCountByCourses[it.id]
                    ?: 0, missingCountByCourses[it.id] ?: 0))
            }
    }

    private fun isNotSubmittedAssignment(it: PlannerItem) =
        it.courseId != null && it.submissions?.submitted == false && it.plannableType == PLANNABLE_TYPE_ASSIGNMENT && it.submissions?.missing == false

    private fun createDueTextForCourse(dueToday: Int, missing: Int): SpannableString {
        val dueTodayString = if (dueToday == 0) {
            resources.getString(R.string.nothingDueToday)
        } else {
            resources.getString(R.string.dueToday, dueToday)
        }

        return if (missing == 0) {
            SpannableString(dueTodayString)
        } else {
            val missingString = resources.getString(R.string.missing, missing)
            val separator = " | "
            val completeString = SpannableString(dueTodayString + separator + missingString)
            val spanColor = resources.getColor(R.color.destructive, null)
            completeString.setSpan(ForegroundColorSpan(spanColor), dueTodayString.length + separator.length, completeString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return completeString
        }
    }

    private fun getCourseColor(course: Course): String {
        return if (!course.courseColor.isNullOrEmpty()) {
            course.courseColor!!
        } else {
            ColorApiHelper.K5_DEFAULT_COLOR
        }
    }

    private fun openAnnouncementDetails(events: MutableLiveData<Event<HomeroomAction>>, course: Course, announcement: DiscussionTopicHeader?) {
        if (announcement != null) {
            events.postValue(Event(HomeroomAction.OpenAnnouncementDetails(course, announcement)))
        }
    }
}