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
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.homeroom.itemviewmodels.CourseCardItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.utils.ColorApiHelper
import com.instructure.pandautils.utils.ColorKeeper
import kotlinx.coroutines.awaitAll
import org.threeten.bp.LocalDate

class CourseCardCreator(
    private val plannerManager: PlannerManager,
    private val userManager: UserManager,
    private val announcementManager: AnnouncementManager,
    private val resources: Resources,
    private val colorKeeper: ColorKeeper
) {

    suspend fun createCourseCards(dashboardCourses: List<Course>, forceNetwork: Boolean,
                                  updateAssignments: Boolean, events: MutableLiveData<Event<HomeroomAction>>): List<CourseCardItemViewModel> {
        val announcements = dashboardCourses
            .map { announcementManager.getLatestAnnouncementAsync(it, forceNetwork) }
            .awaitAll()
            .map { it.dataOrNull?.firstOrNull() }

        val now = LocalDate.now()
        val tomorrow = now.plusDays(1).toApiString()

        val forceNetworkAssignments = forceNetwork || updateAssignments

        val plannerItems = plannerManager.getPlannerItemsAsync(forceNetworkAssignments, now.toApiString(), tomorrow).await().dataOrNull
        val missingSubmissions = userManager.getAllMissingSubmissionsAsync(forceNetworkAssignments).await().dataOrNull

        val assignmentsDueTexts = createDueTexts(dashboardCourses, plannerItems ?: emptyList())
        val assignmentsMissingTexts = createMissingTexts(dashboardCourses, missingSubmissions
            ?: emptyList())

        return dashboardCourses
            .mapIndexed { index, course ->
                val viewData = CourseCardViewData(
                    course.name,
                    assignmentsDueTexts[course.id].orEmpty(),
                    assignmentsMissingTexts[course.id].orEmpty(),
                    announcements[index]?.title.orEmpty(),
                    ColorKeeper.getOrGenerateColor(course),
                    course.imageUrl.orEmpty())

                CourseCardItemViewModel(
                    viewData,
                    { events.postValue(Event(HomeroomAction.OpenCourse(course))) },
                    { events.postValue(Event(HomeroomAction.OpenAssignments(course))) },
                    { openAnnouncementDetails(events, course, announcements[index]) }
                )
            }
    }

    private fun createDueTexts(courses: List<Course>, plannerItems: List<PlannerItem>): Map<Long, String> {
        val dueTodayCountByCourses = courses
            .associate { Pair(it.id, 0) }
            .toMutableMap()

        plannerItems
            .filter { isNotSubmittedAssignment(it) }
            .forEach { item ->
                val dueTodayCount = dueTodayCountByCourses[item.courseId!!]
                if (dueTodayCount != null) {
                    dueTodayCountByCourses[item.courseId!!] = dueTodayCount + 1
                }
            }

        return courses.associate {
            Pair(it.id, createDueTextForCourse(dueTodayCountByCourses[it.id] ?: 0))
        }
    }

    private fun isNotSubmittedAssignment(it: PlannerItem) =
        it.courseId != null && it.submissionState?.submitted == false && it.plannableType == PlannableType.ASSIGNMENT && it.submissionState?.missing == false

    private fun createDueTextForCourse(dueCount: Int): String {
        return if (dueCount == 0) {
            resources.getString(R.string.nothingDueToday)
        } else {
            resources.getString(R.string.dueToday, dueCount)
        }
    }

    private fun createMissingTexts(courses: List<Course>, missingAssignments: List<Assignment>): Map<Long, String> {
        val missingCountByCourses = courses
            .associate { Pair(it.id, 0) }
            .toMutableMap()

        missingAssignments
            .filter { it.plannerOverride?.dismissed != true }
            .forEach { assignment ->
                val missingCount = missingCountByCourses[assignment.courseId]
                if (missingCount != null) {
                    missingCountByCourses[assignment.courseId] = missingCount + 1
                }
            }

        return courses
            .associate {
                Pair(it.id, createMissingTextForCourse(missingCountByCourses[it.id] ?: 0))
            }
    }

    private fun createMissingTextForCourse(missingCount: Int): String {
        return if (missingCount == 0) {
            ""
        } else {
            resources.getString(R.string.missing, missingCount)
        }
    }

    private fun openAnnouncementDetails(events: MutableLiveData<Event<HomeroomAction>>, course: Course, announcement: DiscussionTopicHeader?) {
        if (announcement != null) {
            events.postValue(Event(HomeroomAction.OpenAnnouncementDetails(course, announcement)))
        }
    }
}