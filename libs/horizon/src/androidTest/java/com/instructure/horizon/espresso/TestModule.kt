package com.instructure.horizon.espresso

import android.content.Intent
import com.instructure.canvasapi2.LoginRouter
import com.instructure.canvasapi2.utils.pageview.PandataInfo
import com.instructure.pandautils.features.about.AboutRepository
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsBehaviour
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsColorProvider
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRepository
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler
import com.instructure.pandautils.features.assignments.list.AssignmentListBehavior
import com.instructure.pandautils.features.assignments.list.AssignmentListRepository
import com.instructure.pandautils.features.assignments.list.AssignmentListRouter
import com.instructure.pandautils.features.calendar.CalendarBehavior
import com.instructure.pandautils.features.calendar.CalendarRepository
import com.instructure.pandautils.features.calendar.CalendarRouter
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventRepository
import com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventViewModelBehavior
import com.instructure.pandautils.features.calendarevent.details.EventRouter
import com.instructure.pandautils.features.calendarevent.details.EventViewModelBehavior
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoRepository
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoViewModelBehavior
import com.instructure.pandautils.features.calendartodo.details.ToDoRouter
import com.instructure.pandautils.features.calendartodo.details.ToDoViewModelBehavior
import com.instructure.pandautils.features.dashboard.edit.EditDashboardRepository
import com.instructure.pandautils.features.dashboard.edit.EditDashboardRouter
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragmentBehavior
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.pandautils.features.elementary.grades.GradesRouter
import com.instructure.pandautils.features.elementary.homeroom.HomeroomRouter
import com.instructure.pandautils.features.elementary.importantdates.ImportantDatesRouter
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ResourcesRouter
import com.instructure.pandautils.features.elementary.schedule.ScheduleRouter
import com.instructure.pandautils.features.grades.GradesBehaviour
import com.instructure.pandautils.features.grades.GradesRepository
import com.instructure.pandautils.features.help.HelpDialogFragmentBehavior
import com.instructure.pandautils.features.help.HelpLinkFilter
import com.instructure.pandautils.features.inbox.compose.InboxComposeBehavior
import com.instructure.pandautils.features.inbox.compose.InboxComposeRepository
import com.instructure.pandautils.features.inbox.details.InboxDetailsBehavior
import com.instructure.pandautils.features.inbox.list.InboxRepository
import com.instructure.pandautils.features.inbox.list.InboxRouter
import com.instructure.pandautils.features.legal.LegalRouter
import com.instructure.pandautils.features.lti.LtiLaunchFragmentBehavior
import com.instructure.pandautils.features.offline.sync.SyncRouter
import com.instructure.pandautils.features.settings.SettingsBehaviour
import com.instructure.pandautils.features.settings.SettingsRouter
import com.instructure.pandautils.features.shareextension.ShareExtensionRouter
import com.instructure.pandautils.features.smartsearch.SmartSearchRouter
import com.instructure.pandautils.features.speedgrader.SpeedGraderPostPolicyRouter
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentRouter
import com.instructure.pandautils.features.speedgrader.grade.comments.SpeedGraderCommentsAttachmentRouter
import com.instructure.pandautils.features.todolist.ToDoListRouter
import com.instructure.pandautils.features.todolist.ToDoListViewModelBehavior
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.utils.LogoutHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object HorizonTestModule {
    @Provides
    fun provideSpeedGraderContentRouter(): SpeedGraderContentRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideSpeedGraderCommentsAttachmentRouter(): SpeedGraderCommentsAttachmentRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAlarmReceiverNotificationHandler(): AlarmReceiverNotificationHandler {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideLoginRouter(): LoginRouter {
        return object : LoginRouter {
            override fun loginIntent(): Intent {
                return Intent()
            }
        }
    }

    @Provides
    fun provideAppDatabase(): AppDatabase {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideSyncRouter(): SyncRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideLogoutHelper(): LogoutHelper {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun providePandataInfoAppKey(): PandataInfo.AppKey {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideDiscussionRouteHelperRepository(): DiscussionRouteHelperRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentDetailsRouter(): AssignmentDetailsRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideWebViewRouter(): WebViewRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentDetailsBehaviour(): AssignmentDetailsBehaviour {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentListRouter(): AssignmentListRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCalendarRouter(): CalendarRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideEventRouter(): EventRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideToDoRouter(): ToDoRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideEditDashboardRouter(): EditDashboardRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideShareExtensionRouter(): ShareExtensionRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideDashboardRouter(): DashboardRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideDiscussionRouter(): DiscussionRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideDiscussionDetailsWebViewFragmentBehavior(): DiscussionDetailsWebViewFragmentBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideGradesRouter(): GradesRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideHomeroomRouter(): HomeroomRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideImportantDatesRouter(): ImportantDatesRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideResourcesRouter(): ResourcesRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideScheduleRouter(): ScheduleRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideHelpDialogFragmentBehavior(): HelpDialogFragmentBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideInboxRouter(): InboxRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideLegalRouter(): LegalRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideLtiLaunchFragmentBehavior(): LtiLaunchFragmentBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideSettingsRouter(): SettingsRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideSmartSearchRouter(): SmartSearchRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAboutRepository(): AboutRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentDetailsRepository(): AssignmentDetailsRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentDetailsSubmissionHandler(): AssignmentDetailsSubmissionHandler {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentDetailsColorProvider(): AssignmentDetailsColorProvider {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentListRepository(): AssignmentListRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentListBehavior(): AssignmentListBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCalendarRepository(): CalendarRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCalendarBehavior(): CalendarBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCreateUpdateEventRepository(): CreateUpdateEventRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCreateUpdateEventViewModelBehavior(): CreateUpdateEventViewModelBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCreateUpdateToDoRepository(): CreateUpdateToDoRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCreateUpdateToDoViewModelBehavior(): CreateUpdateToDoViewModelBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideEditDashboardRepository(): EditDashboardRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideEventViewModelBehavior(): EventViewModelBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideGradesBehaviour(): GradesBehaviour {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideGradesRepository(): GradesRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideHelpLinkFilter(): HelpLinkFilter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideInboxComposeRepository(): InboxComposeRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideInboxComposeBehavior(): InboxComposeBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideInboxDetailsBehavior(): InboxDetailsBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideInboxRepository(): InboxRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideSettingsBehaviour(): SettingsBehaviour {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideSpeedGraderPostPolicyRouter(): SpeedGraderPostPolicyRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideToDoViewModelBehavior(): ToDoViewModelBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideToDoListRouter(): ToDoListRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideToDoListViewModelBehavior(): ToDoListViewModelBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }
}