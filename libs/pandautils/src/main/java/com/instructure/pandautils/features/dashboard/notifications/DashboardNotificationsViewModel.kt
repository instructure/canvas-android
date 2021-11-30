/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.notifications

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import com.instructure.canvasapi2.managers.AccountNotificationManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.pandautils.models.ConferenceDashboardBlacklist
import dagger.hilt.android.lifecycle.HiltViewModel
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class DashboardNotificationsViewModel @Inject constructor(
    private val resources: Resources,
    private val courseManager: CourseManager,
    private val enrollmentManager: EnrollmentManager,
    private val accountNotificationManager: AccountNotificationManager,
    private val conferenceDashboardBlacklist: ConferenceDashboardBlacklist
) : ViewModel() {

    private var coursesMap: Map<Long, Course> = emptyMap()

//    private suspend fun getNotifications(forceNetwork: Boolean): List<ItemViewModel>? {
//        val invites = enrollmentManager.getSelfEnrollmentsAsync(
//            null,
//            listOf(EnrollmentAPI.STATE_INVITED, EnrollmentAPI.STATE_CURRENT_AND_FUTURE),
//            forceNetwork
//        ).await()
//            .dataOrNull
//            ?.filter { it.enrollmentState == EnrollmentAPI.STATE_INVITED && hasValidCourseForEnrollment(it) }
//
//        val invitationViewModels = createInvitationViewModels(invites)
//
//        val accountNotifications = accountNotificationManager.getAllAccountNotificationsAsync(true).await().dataOrNull
//
//        val blackList = conferenceDashboardBlacklist.blacklist
//        val conferences = ConferenceManager.getLiveConferencesAsync(forceNetwork).await().dataOrNull
//            ?.filter { conference ->
//                // Remove blacklisted (i.e. 'dismissed') conferences
//                blackList?.contains(conference.id.toString())?.not() ?: false
//            }
//            ?.onEach { conference ->
//                // Attempt to add full canvas context to conference items, fall back to generic built context
//                val contextType = conference.contextType.toLowerCase(Locale.US)
//                val contextId = conference.contextId
//                val genericContext = CanvasContext.fromContextCode("${contextType}_${contextId}")!!
//                conference.canvasContext = when (genericContext) {
//                    is Course -> coursesMap[contextId] ?: genericContext
//                    else -> genericContext
//                }
//            } ?: emptyList()
//
//        return invitationViewModels
//    }
//
//    private fun createInvitationViewModels(invites: List<Enrollment>?): List<ItemViewModel>? {
//        return invites?.map { enrollment ->
//            val course = coursesMap[enrollment.courseId]!!
//            val section = course.sections.find { it.id == enrollment.courseSectionId }
//            InvitationItemViewModel(
//                InvitationViewData(
//                    title = resources.getString(R.string.courseInviteTitle),
//                    description = listOfNotNull(course.name, section?.name).distinct().joinToString(", ")
//                ),
//                this@HomeroomViewModel::handleInvitation
//            )
//        }
//    }
//
//    private fun handleInvitation(accepted: Boolean) {
//
//    }

    private fun hasValidCourseForEnrollment(enrollment: Enrollment): Boolean {
        return coursesMap[enrollment.courseId]?.let { course ->
            course.isValidTerm() && !course.accessRestrictedByDate && isEnrollmentBeforeEndDateOrNotRestricted(course)
        } ?: false
    }

    private fun isEnrollmentBeforeEndDateOrNotRestricted(course: Course): Boolean {
        val isBeforeEndDate = course.endAt?.let {
            val now = OffsetDateTime.now()
            val endDate = OffsetDateTime.parse(it).withOffsetSameInstant(OffsetDateTime.now().offset)
            now.isBefore(endDate)
        } ?: true // Case when the course has no end date

        return !course.restrictEnrollmentsToCourseDate || isBeforeEndDate
    }
}