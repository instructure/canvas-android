package com.instructure.canvasapi2.di

import com.instructure.canvasapi2.apis.*
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
    fun provideQuizManager(): QuizManager {
        return QuizManager
    }

    @Provides
    fun provideSubmissionManager(): SubmissionManager {
        return SubmissionManager
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

    @Provides
    fun provideTabApi(): TabAPI.TabsInterface {
        return RestBuilder().build(TabAPI.TabsInterface::class.java, RestParams())
    }

    @Provides
    fun providesUserApi(): UserAPI.UsersInterface {
        return RestBuilder().build(UserAPI.UsersInterface::class.java, RestParams())
    }

    @Provides
    fun providePageApi(): PageAPI.PagesInterface {
        return RestBuilder().build(PageAPI.PagesInterface::class.java, RestParams())
    }

    @Provides
    fun provideAssignmentApi(): AssignmentAPI.AssignmentInterface {
        return RestBuilder().build(AssignmentAPI.AssignmentInterface::class.java, RestParams())
    }

    @Provides
    fun provideFileFolderApi(): FileFolderAPI.FilesFoldersInterface {
        return RestBuilder().build(FileFolderAPI.FilesFoldersInterface::class.java, RestParams())
    }

    @Provides
    fun provideQuizApi(): QuizAPI.QuizInterface {
        return RestBuilder().build(QuizAPI.QuizInterface::class.java, RestParams())
    }

    @Provides
    fun provideSubmissionApi(): SubmissionAPI.SubmissionInterface {
        return RestBuilder().build(SubmissionAPI.SubmissionInterface::class.java, RestParams())
    }

    @Provides
    fun provideCalendarEventApi(): CalendarEventAPI.CalendarEventInterface {
        return RestBuilder().build(CalendarEventAPI.CalendarEventInterface::class.java, RestParams())
    }

    @Provides
    fun provideEnrollmentApi(): EnrollmentAPI.EnrollmentInterface {
        return RestBuilder().build(EnrollmentAPI.EnrollmentInterface::class.java, RestParams())
    }

    @Provides
    fun providesConferencesApi(): ConferencesApi.ConferencesInterface {
        return RestBuilder().build(ConferencesApi.ConferencesInterface::class.java, RestParams())
    }

    @Provides
    fun providesOAuthApi(): OAuthAPI.OAuthInterface {
        return RestBuilder().build(OAuthAPI.OAuthInterface::class.java, RestParams())
    }

    @Provides
    fun provideDiscussionApi(): DiscussionAPI.DiscussionInterface {
        return RestBuilder().build(DiscussionAPI.DiscussionInterface::class.java, RestParams())
    }

    @Provides
    fun provideAnnouncementApi(): AnnouncementAPI.AnnouncementInterface {
        return RestBuilder().build(AnnouncementAPI.AnnouncementInterface::class.java, RestParams())
    }

    @Provides
    fun provideModuleApi(): ModuleAPI.ModuleInterface {
        return RestBuilder().build(ModuleAPI.ModuleInterface::class.java, RestParams())
    }

    @Provides
    fun provideFeaturesApi(): FeaturesAPI.FeaturesInterface {
        return RestBuilder().build(FeaturesAPI.FeaturesInterface::class.java, RestParams())
    }

    @Provides
    fun provideFileDownloadApi(): FileDownloadAPI {
        return RestBuilder().build(FileDownloadAPI::class.java, RestParams())
    }
}