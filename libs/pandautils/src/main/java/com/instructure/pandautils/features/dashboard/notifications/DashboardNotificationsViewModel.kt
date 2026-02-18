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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.AccountNotificationManager
import com.instructure.canvasapi2.managers.ConferenceManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.isValidTerm
import com.instructure.pandautils.BR
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.AnnouncementItemViewModel
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.ConferenceItemViewModel
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.InvitationItemViewModel
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.SyncProgressItemViewModel
import com.instructure.pandautils.features.dashboard.notifications.itemviewmodels.UploadItemViewModel
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.AggregateProgressViewData
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.models.ConferenceDashboardBlacklist
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.entities.DashboardFileUploadEntity
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.StudioMediaProgressDao
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
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
    private val apiPrefs: ApiPrefs,
    private val workManager: WorkManager,
    private val dashboardFileUploadDao: DashboardFileUploadDao,
    private val fileUploadInputDao: FileUploadInputDao,
    private val fileUploadUtilsHelper: FileUploadUtilsHelper,
    private val aggregateProgressObserver: AggregateProgressObserver,
    private val courseSyncProgressDao: CourseSyncProgressDao,
    private val fileSyncProgressDao: FileSyncProgressDao,
    private val studioMediaProgressDao: StudioMediaProgressDao
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

    private val runningWorkersObserver = Observer<List<DashboardFileUploadEntity>> {
        viewModelScope.launch {
            _data.value?.uploadItems?.forEach { it.clear() }
            _data.value?.uploadItems = getUploads(it)
            _data.value?.notifyPropertyChanged(BR.concatenatedItems)
        }
    }

    private val syncProgressObserver = Observer<AggregateProgressViewData?> { aggregateProgressViewData ->
        if (aggregateProgressViewData == null) {
            _data.value?.syncProgressItems = null
            _data.value?.notifyPropertyChanged(BR.concatenatedItems)
            return@Observer
        }

        if (_data.value?.syncProgressItems == null) {
            _data.value?.syncProgressItems = createSyncProgressViewModel(aggregateProgressViewData)
            _data.value?.notifyPropertyChanged(BR.concatenatedItems)
            return@Observer
        }

        if (aggregateProgressObserver.progressData.value?.progressState == ProgressState.COMPLETED) {
            _data.value?.syncProgressItems = null
            _data.value?.notifyPropertyChanged(BR.concatenatedItems)
        } else {
            _data.value?.syncProgressItems?.update(aggregateProgressViewData)
        }
    }

    private val fileUploads = dashboardFileUploadDao.getAllForUser(apiPrefs.user?.id.orDefault())

    init {
        fileUploads.observeForever(runningWorkersObserver)
        aggregateProgressObserver.progressData.observeForever(syncProgressObserver)
    }

    override fun onCleared() {
        _data.value?.uploadItems?.forEach { it.clear() }
        aggregateProgressObserver.progressData.removeObserver(syncProgressObserver)
        aggregateProgressObserver.onCleared()
        fileUploads.removeObserver(runningWorkersObserver)
        super.onCleared()
    }

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

            val uploadViewModels = getUploads(fileUploads.value)

            val syncProgress = aggregateProgressObserver.progressData.value?.let {
                createSyncProgressViewModel(it)
            }

            _data.postValue(DashboardNotificationsViewData(items, uploadViewModels, syncProgress))
        }
    }

    private fun getSyncProgress(aggregateProgressViewData: AggregateProgressViewData): SyncProgressItemViewModel {
        return SyncProgressItemViewModel(
            data = SyncProgressViewData(),
            onClick = this::openSyncProgress,
            onDismiss = this::dismissSyncProgress,
            resources = resources
        ).apply { update(aggregateProgressViewData) }
    }

    private suspend fun getAccountNotifications(forceNetwork: Boolean): List<ItemViewModel> {
        val accountNotifications =
            accountNotificationManager.getAllAccountNotificationsAsync(forceNetwork).await().dataOrNull

        return createAccountNotificationViewModels(accountNotifications)
    }

    private fun createAccountNotificationViewModels(accountNotifications: List<AccountNotification>?): List<ItemViewModel> {
        return accountNotifications?.map {

            val color = when (it.icon) {
                AccountNotification.ACCOUNT_NOTIFICATION_ERROR -> R.color.backgroundDanger
                AccountNotification.ACCOUNT_NOTIFICATION_WARNING -> R.color.backgroundWarning
                else -> R.color.backgroundInfo
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
                    color = color,
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

    private suspend fun getUploads(fileUploadEntities: List<DashboardFileUploadEntity>?) =
        fileUploadEntities?.mapNotNull { fileUploadEntity ->
            val workerId = UUID.fromString(fileUploadEntity.workerId)
            val workInfo = workManager.getWorkInfoByIdFlow(workerId).first()
            workInfo?.let {
                val icon: Int
                val background: Int
                when (it.state) {
                    WorkInfo.State.FAILED -> {
                        icon = R.drawable.ic_exclamation_mark
                        background = R.color.backgroundDanger
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        icon = R.drawable.ic_check_white_24dp
                        background = R.color.backgroundSuccess
                    }
                    else -> {
                        icon = R.drawable.ic_upload
                        background = R.color.backgroundInfo
                    }
                }
                val uploadViewData = UploadViewData(
                    fileUploadEntity.title.orEmpty(), fileUploadEntity.subtitle.orEmpty(),
                    icon, background, it.state == WorkInfo.State.RUNNING
                )
                UploadItemViewModel(
                    workerId = workerId,
                    workManager = workManager,
                    data = uploadViewData,
                    open = { uuid -> openUploadNotification(it.state, uuid, fileUploadEntity) },
                    remove = { removeUploadNotification(fileUploadEntity, workerId) }
                )
            }
        }.orEmpty()

    private fun hasValidCourseForEnrollment(enrollment: Enrollment): Boolean {
        return coursesMap[enrollment.courseId]?.let { course ->
            course.isValidTerm() && !course.accessRestrictedByDate && course.isEnrollmentBeforeEndDateOrNotRestricted()
        } ?: false
    }

    private fun openUploadNotification(state: WorkInfo.State, uuid: UUID, fileUploadEntity: DashboardFileUploadEntity) {
        if (state == WorkInfo.State.SUCCEEDED) {
            viewModelScope.launch {
                val uploadItemViewModel = _data.value?.uploadItems?.find { it.workerId == uuid }
                uploadItemViewModel?.apply {
                    loading = true
                    notifyPropertyChanged(BR.loading)
                }
                if (fileUploadEntity.courseId != null && fileUploadEntity.assignmentId != null && fileUploadEntity.attemptId != null) {
                    courseManager.getCourseAsync(fileUploadEntity.courseId, false).await().dataOrNull?.let {
                        dashboardFileUploadDao.delete(fileUploadEntity)
                        _events.postValue(
                            Event(
                                DashboardNotificationsActions.NavigateToSubmissionDetails(
                                    it,
                                    fileUploadEntity.assignmentId,
                                    fileUploadEntity.attemptId
                                )
                            )
                        )
                    }
                } else if (fileUploadEntity.folderId != null) {
                    dashboardFileUploadDao.delete(fileUploadEntity)
                    apiPrefs.user?.let {
                        _events.postValue(
                            Event(
                                DashboardNotificationsActions.NavigateToMyFiles(
                                    it,
                                    fileUploadEntity.folderId
                                )
                            )
                        )
                    }
                } else {
                    dashboardFileUploadDao.delete(fileUploadEntity)
                    _events.postValue(Event(DashboardNotificationsActions.OpenProgressDialog(uuid)))
                }
                uploadItemViewModel?.apply {
                    loading = false
                    notifyPropertyChanged(BR.loading)
                }
            }
        } else {
            _events.postValue(Event(DashboardNotificationsActions.OpenProgressDialog(uuid)))
        }
    }

    private fun removeUploadNotification(fileUploadEntity: DashboardFileUploadEntity, workerId: UUID) {
        viewModelScope.launch {
            dashboardFileUploadDao.delete(fileUploadEntity)
            fileUploadInputDao.findByWorkerId(workerId.toString())?.let {
                fileUploadUtilsHelper.deleteCachedFiles(it.filePaths)
                fileUploadInputDao.delete(it)
            }
        }
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

    private fun openSyncProgress() {
        _events.postValue(Event(DashboardNotificationsActions.OpenSyncProgress))
    }

    private fun dismissSyncProgress() {
        viewModelScope.launch {
            fileSyncProgressDao.deleteAll()
            courseSyncProgressDao.deleteAll()
            studioMediaProgressDao.deleteAll()
        }
    }

    private fun createSyncProgressViewModel(aggregateProgressViewData: AggregateProgressViewData): SyncProgressItemViewModel? {
        return if (aggregateProgressViewData.progressState != ProgressState.COMPLETED) {
            getSyncProgress(aggregateProgressViewData)
        } else {
            null
        }
    }
}