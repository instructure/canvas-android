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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.AccountNotificationManager
import com.instructure.canvasapi2.managers.ConferenceManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.InvitationItemViewModel
import com.instructure.pandautils.models.ConferenceDashboardBlacklist
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.notifyAll
import org.threeten.bp.OffsetDateTime
import java.lang.Exception
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardNotificationsViewModel @Inject constructor(
    private val resources: Resources,
    private val courseManager: CourseManager,
    private val enrollmentManager: EnrollmentManager,
    private val accountNotificationManager: AccountNotificationManager,
    private val conferenceDashboardBlacklist: ConferenceDashboardBlacklist
) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<DashboardNotificationsViewData>
        get() = _data
    private val _data = MutableLiveData<DashboardNotificationsViewData>()

    val events: LiveData<Event<DashboardNotificationsActions>>
        get() = _events
    private val _events = MutableLiveData<Event<DashboardNotificationsActions>>()

    private var coursesMap: Map<Long, Course> = emptyMap()

    fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {

            val courses = courseManager.getCoursesAsync(forceNetwork).await().dataOrNull

            coursesMap = courses
                ?.associateBy { it.id } ?: emptyMap()

            val invites = enrollmentManager.getSelfEnrollmentsAsync(
                null,
                listOf(EnrollmentAPI.STATE_INVITED, EnrollmentAPI.STATE_CURRENT_AND_FUTURE),
                forceNetwork
            ).await()
                .dataOrNull
                ?.filter { it.enrollmentState == EnrollmentAPI.STATE_INVITED && hasValidCourseForEnrollment(it) }

            val invitationViewModels = createInvitationViewModels(invites)

            val accountNotifications =
                accountNotificationManager.getAllAccountNotificationsAsync(true).await().dataOrNull

            val blackList = conferenceDashboardBlacklist.blacklist
            val conferences = ConferenceManager.getLiveConferencesAsync(forceNetwork).await().dataOrNull
                ?.filter { conference ->
                    // Remove blacklisted (i.e. 'dismissed') conferences
                    blackList?.contains(conference.id.toString())?.not() ?: false
                }
                ?.onEach { conference ->
                    // Attempt to add full canvas context to conference items, fall back to generic built context
                    val contextType = conference.contextType.toLowerCase(Locale.US)
                    val contextId = conference.contextId
                    val genericContext = CanvasContext.fromContextCode("${contextType}_${contextId}")!!
                    conference.canvasContext = when (genericContext) {
                        is Course -> coursesMap[contextId] ?: genericContext
                        else -> genericContext
                    }
                } ?: emptyList()

            _data.postValue(DashboardNotificationsViewData(invitationViewModels))
        }
    }

    private fun createInvitationViewModels(invites: List<Enrollment>?): List<ItemViewModel>? {
        return invites?.map { enrollment ->
            val course = coursesMap[enrollment.courseId]!!
            val section = course.sections.find { it.id == enrollment.courseSectionId }
            InvitationItemViewModel(
                InvitationViewData(
                    title = resources.getString(R.string.courseInviteTitle),
                    description = listOfNotNull(course.name, section?.name).distinct().joinToString(", "),
                    enrollmentId = enrollment.id,
                    courseId = enrollment.courseId
                ),
                this@DashboardNotificationsViewModel::handleInvitation
            )
        }

    }

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

    private fun handleInvitation(
        enrollmentId: Long,
        courseId: Long,
        itemViewModel: InvitationItemViewModel,
        accepted: Boolean
    ) {
        itemViewModel.inProgress = true
        itemViewModel.notifyPropertyChanged(BR.inProgress)
        viewModelScope.launch {
            try {
                enrollmentManager.handleInviteAsync(courseId, enrollmentId, accepted).await().dataOrThrow
                itemViewModel.accepted = accepted
                itemViewModel.inProgress = false
                itemViewModel.notifyChange()
                delay(2000)
                loadData(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _events.postValue(Event(DashboardNotificationsActions.ShowToast(resources.getString(R.string.errorOccurred))))
                itemViewModel.inProgress = false
                itemViewModel.notifyPropertyChanged(BR.inProgress)
            }
        }
    }
}