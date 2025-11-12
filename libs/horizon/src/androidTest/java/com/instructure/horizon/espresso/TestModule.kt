package com.instructure.horizon.espresso

import android.content.Intent
import com.instructure.canvasapi2.LoginRouter
import com.instructure.pandautils.features.offline.sync.SyncRouter
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentRouter
import com.instructure.pandautils.features.speedgrader.grade.comments.SpeedGraderCommentsAttachmentRouter
import com.instructure.pandautils.receivers.alarm.AlarmReceiverNotificationHandler
import com.instructure.pandautils.room.appdatabase.AppDatabase
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
    fun provideLogoutHelper(): com.instructure.pandautils.utils.LogoutHelper {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun providePandataInfoAppKey(): com.instructure.canvasapi2.utils.pageview.PandataInfo.AppKey {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideDiscussionRouteHelperRepository(): com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentDetailsRouter(): com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideWebViewRouter(): com.instructure.pandautils.navigation.WebViewRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentDetailsBehaviour(): com.instructure.pandautils.features.assignments.details.AssignmentDetailsBehaviour {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentListRouter(): com.instructure.pandautils.features.assignments.list.AssignmentListRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCalendarRouter(): com.instructure.pandautils.features.calendar.CalendarRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideEventRouter(): com.instructure.pandautils.features.calendarevent.details.EventRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideToDoRouter(): com.instructure.pandautils.features.calendartodo.details.ToDoRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideEditDashboardRouter(): com.instructure.pandautils.features.dashboard.edit.EditDashboardRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideShareExtensionRouter(): com.instructure.pandautils.features.shareextension.ShareExtensionRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideDashboardRouter(): com.instructure.pandautils.features.dashboard.notifications.DashboardRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideDiscussionRouter(): com.instructure.pandautils.features.discussion.router.DiscussionRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideDiscussionDetailsWebViewFragmentBehavior(): com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragmentBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideGradesRouter(): com.instructure.pandautils.features.elementary.grades.GradesRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideHomeroomRouter(): com.instructure.pandautils.features.elementary.homeroom.HomeroomRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideImportantDatesRouter(): com.instructure.pandautils.features.elementary.importantdates.ImportantDatesRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideResourcesRouter(): com.instructure.pandautils.features.elementary.resources.itemviewmodels.ResourcesRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideScheduleRouter(): com.instructure.pandautils.features.elementary.schedule.ScheduleRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideHelpDialogFragmentBehavior(): com.instructure.pandautils.features.help.HelpDialogFragmentBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideInboxRouter(): com.instructure.pandautils.features.inbox.list.InboxRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideLegalRouter(): com.instructure.pandautils.features.legal.LegalRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideLtiLaunchFragmentBehavior(): com.instructure.pandautils.features.lti.LtiLaunchFragmentBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideSettingsRouter(): com.instructure.pandautils.features.settings.SettingsRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideSmartSearchRouter(): com.instructure.pandautils.features.smartsearch.SmartSearchRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAboutRepository(): com.instructure.pandautils.features.about.AboutRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentDetailsRepository(): com.instructure.pandautils.features.assignments.details.AssignmentDetailsRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentDetailsSubmissionHandler(): com.instructure.pandautils.features.assignments.details.AssignmentDetailsSubmissionHandler {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentDetailsColorProvider(): com.instructure.pandautils.features.assignments.details.AssignmentDetailsColorProvider {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentListRepository(): com.instructure.pandautils.features.assignments.list.AssignmentListRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideAssignmentListBehavior(): com.instructure.pandautils.features.assignments.list.AssignmentListBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCalendarRepository(): com.instructure.pandautils.features.calendar.CalendarRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCalendarBehavior(): com.instructure.pandautils.features.calendar.CalendarBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCreateUpdateEventRepository(): com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCreateUpdateEventViewModelBehavior(): com.instructure.pandautils.features.calendarevent.createupdate.CreateUpdateEventViewModelBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCreateUpdateToDoRepository(): com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideCreateUpdateToDoViewModelBehavior(): com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoViewModelBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideEditDashboardRepository(): com.instructure.pandautils.features.dashboard.edit.EditDashboardRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideEventViewModelBehavior(): com.instructure.pandautils.features.calendarevent.details.EventViewModelBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideGradesBehaviour(): com.instructure.pandautils.features.grades.GradesBehaviour {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideGradesRepository(): com.instructure.pandautils.features.grades.GradesRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideHelpLinkFilter(): com.instructure.pandautils.features.help.HelpLinkFilter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideInboxComposeRepository(): com.instructure.pandautils.features.inbox.compose.InboxComposeRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideInboxComposeBehavior(): com.instructure.pandautils.features.inbox.compose.InboxComposeBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideInboxDetailsBehavior(): com.instructure.pandautils.features.inbox.details.InboxDetailsBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideInboxRepository(): com.instructure.pandautils.features.inbox.list.InboxRepository {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideSettingsBehaviour(): com.instructure.pandautils.features.settings.SettingsBehaviour {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideSpeedGraderPostPolicyRouter(): com.instructure.pandautils.features.speedgrader.SpeedGraderPostPolicyRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideToDoViewModelBehavior(): com.instructure.pandautils.features.calendartodo.details.ToDoViewModelBehavior {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }

    @Provides
    fun provideToDoListRouter(): com.instructure.pandautils.features.todolist.ToDoListRouter {
        throw NotImplementedError("This is a test module. Implementation not required.")
    }
}