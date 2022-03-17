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
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.AnnouncementItemViewModel
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.ConferenceItemViewModel
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.InvitationItemViewModel
import com.instructure.pandautils.models.ConferenceDashboardBlacklist
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorKeeper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.toHexString
import org.threeten.bp.OffsetDateTime
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardNotificationsViewModel @Inject constructor(
    private val resources: Resources,
    private val courseManager: CourseManager,
    private val groupManager: GroupManager,
    private val enrollmentManager: EnrollmentManager,
    private val conferenceManager: ConferenceManager,
    private val accountNotificationManager: AccountNotificationManager,
    private val oauthManager: OAuthManager,
    private val conferenceDashboardBlacklist: ConferenceDashboardBlacklist,
    private val colorKeeper: ColorKeeper,
    private val apiPrefs: ApiPrefs
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
    private var groupMap: Map<Long, Group> = emptyMap()

    fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {

            val items = mutableListOf<ItemViewModel>()

            val courses = courseManager.getCoursesAsync(forceNetwork).await().dataOrNull
            val groups = groupManager.getAllGroupsAsync(forceNetwork).await().dataOrNull

            coursesMap = courses?.associateBy { it.id } ?: emptyMap()

            groupMap = groups?.associateBy { it.id } ?: emptyMap()

            val invitationViewModels = getInvitations(forceNetwork)
            items.addAll(invitationViewModels)

            val accountNotificationViewModels = getAccountNotifications(forceNetwork)
            items.addAll(accountNotificationViewModels)

            val conferenceViewModels = getConferences(forceNetwork)
            items.addAll(conferenceViewModels)

            _data.postValue(DashboardNotificationsViewData(items))
        }
    }

    private suspend fun getAccountNotifications(forceNetwork: Boolean): List<ItemViewModel> {
        val accountNotifications =
            accountNotificationManager.getAllAccountNotificationsAsync(forceNetwork).await().dataOrNull

        return createAccountNotificationViewModels(accountNotifications)
    }

    private fun createAccountNotificationViewModels(accountNotifications: List<AccountNotification>?): List<ItemViewModel> {
        return accountNotifications?.map {

            val color = when (it.icon) {
                AccountNotification.ACCOUNT_NOTIFICATION_ERROR -> resources.getColor(R.color.notificationTintError)
                AccountNotification.ACCOUNT_NOTIFICATION_WARNING -> resources.getColor(R.color.notificationTintWarning)
                else -> colorKeeper.defaultColor
            }

            val icon = when (it.icon) {
                AccountNotification.ACCOUNT_NOTIFICATION_ERROR,
                AccountNotification.ACCOUNT_NOTIFICATION_WARNING -> R.drawable.ic_warning
                AccountNotification.ACCOUNT_NOTIFICATION_CALENDAR -> R.drawable.ic_calendar
                AccountNotification.ACCOUNT_NOTIFICATION_QUESTION -> R.drawable.ic_question_mark
                else -> R.drawable.ic_info
            }

            AnnouncementItemViewModel(
                AnnouncementViewData(
                    id = it.id,
                    subject = it.subject,
                    message = it.message,
                    color = "#${color.toHexString()}",
                    icon = icon
                ),
                this@DashboardNotificationsViewModel::dismissAnnouncement,
                this@DashboardNotificationsViewModel::openAnnouncement
            )
        } ?: emptyList()
    }

    private suspend fun getConferences(forceNetwork: Boolean): List<ItemViewModel> {
        val blackList = conferenceDashboardBlacklist.conferenceDashboardBlacklist
        val conferences = conferenceManager.getLiveConferencesAsync(forceNetwork).await().dataOrNull
            ?.filter { conference ->
                // Remove blacklisted (i.e. 'dismissed') conferences
                blackList.contains(conference.id.toString()).not()
            }
            ?.onEach { conference ->
                // Attempt to add full canvas context to conference items, fall back to generic built context
                val contextType = conference.contextType.lowercase(Locale.US)
                val contextId = conference.contextId
                val genericContext = CanvasContext.fromContextCode("${contextType}_${contextId}")!!
                conference.canvasContext = when (genericContext) {
                    is Course -> coursesMap[contextId] ?: genericContext
                    is Group -> groupMap[contextId] ?: genericContext
                    else -> genericContext
                }
            }

        return createConferenceViewModels(conferences) ?: emptyList()
    }

    private fun createConferenceViewModels(conferences: List<Conference>?): List<ConferenceItemViewModel>? {
        return conferences?.map {
            ConferenceItemViewModel(
                ConferenceViewData(subtitle = it.canvasContext.name ?: it.title, conference = it),
                handleJoin = this@DashboardNotificationsViewModel::handleConferenceJoin,
                handleDismiss = this@DashboardNotificationsViewModel::handleConferenceDismiss
            )
        }
    }

    private suspend fun getInvitations(forceNetwork: Boolean): List<ItemViewModel> {
        val invites = enrollmentManager.getSelfEnrollmentsAsync(
            null,
            listOf(EnrollmentAPI.STATE_INVITED, EnrollmentAPI.STATE_CURRENT_AND_FUTURE),
            forceNetwork
        ).await()
            .dataOrNull
            ?.filter { it.enrollmentState == EnrollmentAPI.STATE_INVITED && hasValidCourseForEnrollment(it) }

        return createInvitationViewModels(invites) ?: emptyList()
    }

    private fun createInvitationViewModels(invites: List<Enrollment>?): List<InvitationItemViewModel>? {
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

    private fun handleConferenceJoin(itemViewModel: ConferenceItemViewModel, conference: Conference) {
        itemViewModel.isJoining = true
        itemViewModel.notifyPropertyChanged(BR.joining)

        viewModelScope.launch {
            var url: String = conference.joinUrl
                ?: "${apiPrefs.fullDomain}${conference.canvasContext.toAPIString()}/conferences/${conference.id}/join"

            if (url.startsWith(apiPrefs.fullDomain)) {
                try {
                    val authSession = oauthManager.getAuthenticatedSessionAsync(url).await().dataOrThrow
                    url = authSession.sessionUrl
                } catch (e: Throwable) {
                    // Try launching without authenticated URL
                }
            }
            _events.postValue(Event(DashboardNotificationsActions.LaunchConference(conference.canvasContext, url)))

            delay(3000)
            itemViewModel.isJoining = false
            itemViewModel.notifyPropertyChanged(BR.joining)
        }
    }

    private fun handleConferenceDismiss(conference: Conference) {
        val blacklist = conferenceDashboardBlacklist.conferenceDashboardBlacklist + conference.id.toString()
        conferenceDashboardBlacklist.conferenceDashboardBlacklist = blacklist
        loadData(false)
    }

    private fun dismissAnnouncement(itemViewModel: AnnouncementItemViewModel, announcementId: Long) {
        itemViewModel.inProgress = true
        itemViewModel.notifyPropertyChanged(BR.inProgress)
        viewModelScope.launch {
            try {
                accountNotificationManager.deleteAccountNotificationsAsync(announcementId).await().dataOrThrow
                loadData(true)
            } catch (e: Exception) {
                e.printStackTrace()
                _events.postValue(Event(DashboardNotificationsActions.ShowToast(resources.getString(R.string.errorOccurred))))
                itemViewModel.inProgress = false
                itemViewModel.notifyPropertyChanged(BR.inProgress)
            }
        }
    }

    private fun openAnnouncement(subject: String, message: String) {
        _events.postValue(Event(DashboardNotificationsActions.OpenAnnouncement(subject, message)))
    }
}