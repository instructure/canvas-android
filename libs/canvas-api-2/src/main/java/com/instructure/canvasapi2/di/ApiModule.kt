package com.instructure.canvasapi2.di

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.HelpLinksAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.NotificationPreferencesAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.utils.ApiPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    fun provideCourseManager(): CourseManager {
        return CourseManager
    }

    @Provides
    fun provideGroupManager(): GroupManager {
        return GroupManager
    }

    @Provides
    fun provideHelpLinksManager(helpLinksApi: HelpLinksAPI): HelpLinksManager {
        return HelpLinksManager(helpLinksApi)
    }

    @Provides
    fun provideFeaturesManager(): FeaturesManager {
        return FeaturesManager
    }

    @Provides
    fun provideAnnouncementManager(): AnnouncementManager {
        return AnnouncementManager
    }

    @Provides
    fun provideOAuthManager(): OAuthManager {
        return OAuthManager
    }

    @Provides
    fun providePlannerManager(plannerApi: PlannerAPI): PlannerManager {
        return PlannerManager(plannerApi)
    }

    @Provides
    fun provideUserManager(): UserManager {
        return UserManager
    }

    @Provides
    fun provideToDoManager(): ToDoManager {
        return ToDoManager
    }

    @Provides
    fun provideEnrollmentManager(): EnrollmentManager {
        return EnrollmentManager
    }

    @Provides
    fun provideExternalToolManager(): ExternalToolManager {
        return ExternalToolManager
    }

    @Provides
    fun provideCanvaDocsManager(): CanvaDocsManager {
        return CanvaDocsManager
    }

    @Provides
    @Singleton
    fun provideHelpLinksApi(): HelpLinksAPI {
        return HelpLinksAPI
    }

    @Provides
    @Singleton
    fun provideApiPrefs(): ApiPrefs {
        return ApiPrefs
    }

    @Provides
    @Singleton
    fun providePlannerApi(): PlannerAPI {
        return PlannerAPI
    }

    @Provides
    fun provideAssignmentManager(): AssignmentManager {
        return AssignmentManager
    }

    @Provides
    fun provideCalendarEventManager(): CalendarEventManager {
        return CalendarEventManager
    }

    @Provides
    fun provideTabManager(): TabManager {
        return TabManager
    }

    @Provides
    fun provideAccountNotificationManager(): AccountNotificationManager {
        return AccountNotificationManager
    }

    @Provides
    fun provideConferenceManager(): ConferenceManager {
        return ConferenceManager
    }

    @Provides
    fun provideCommunicationChannelsManager(): CommunicationChannelsManager {
        return CommunicationChannelsManager
    }

    @Provides
    fun provideNotificationPreferencesManager(): NotificationPreferencesManager {
        return NotificationPreferencesManager(NotificationPreferencesAPI)
    }

    @Provides
    fun provideDiscussionManager(): DiscussionManager {
        return DiscussionManager
    }

    @Provides
    fun provideInboxApi(): InboxApi.InboxInterface {
        return RestBuilder().build(InboxApi.InboxInterface::class.java, RestParams())
    }

    @Provides
    fun provideCourseApi(): CourseAPI.CoursesInterface {
        return RestBuilder().build(CourseAPI.CoursesInterface::class.java, RestParams())
    }

    @Provides
    fun provideGroupApi(): GroupAPI.GroupInterface {
        return RestBuilder().build(GroupAPI.GroupInterface::class.java, RestParams())
    }

    @Provides
    fun provideProgressApi(): ProgressAPI.ProgressInterface {
        return RestBuilder().build(ProgressAPI.ProgressInterface::class.java, RestParams())
    }
}