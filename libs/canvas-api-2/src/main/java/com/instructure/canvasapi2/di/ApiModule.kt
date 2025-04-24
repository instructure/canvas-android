package com.instructure.canvasapi2.di

import com.instructure.canvasapi2.apis.AccountNotificationAPI
import com.instructure.canvasapi2.apis.AnnouncementAPI
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CalendarEventAPI
import com.instructure.canvasapi2.apis.CommunicationChannelsAPI
import com.instructure.canvasapi2.apis.ConferencesApi
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.apis.FileDownloadAPI
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.HelpLinksAPI
import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.apis.NotificationPreferencesAPI
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.apis.ObserverApi
import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.apis.ProgressAPI
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.canvasapi2.apis.SectionAPI
import com.instructure.canvasapi2.apis.SmartSearchApi
import com.instructure.canvasapi2.apis.StudioApi
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.apis.TabAPI
import com.instructure.canvasapi2.apis.ThemeAPI
import com.instructure.canvasapi2.apis.UnreadCountAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.AccountNotificationManager
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.managers.CanvaDocsManager
import com.instructure.canvasapi2.managers.CommunicationChannelsManager
import com.instructure.canvasapi2.managers.ConferenceManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.DiscussionManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.ExternalToolManager
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.managers.HelpLinksManager
import com.instructure.canvasapi2.managers.NotificationPreferencesManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.managers.TabManager
import com.instructure.canvasapi2.managers.ToDoManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PandataApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

const val PLANNER_API_SERIALIZE_NULLS = "PLANNER_API_SERIALIZE_NULLS"

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
    fun provideAccountNotificationApi(): AccountNotificationAPI.AccountNotificationInterface {
        return RestBuilder().build(AccountNotificationAPI.AccountNotificationInterface::class.java, RestParams())
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

    @Provides
    fun providePlannerApiInterface(): PlannerAPI.PlannerInterface {
        return RestBuilder().build(PlannerAPI.PlannerInterface::class.java, RestParams())
    }

    @Provides
    @Named(PLANNER_API_SERIALIZE_NULLS)
    fun providePlannerApiInterfaceSerializeNulls(): PlannerAPI.PlannerInterface {
        return RestBuilder().buildSerializeNulls(PlannerAPI.PlannerInterface::class.java, RestParams())
    }

    @Provides
    fun provideThemeApi(): ThemeAPI.ThemeInterface {
        return RestBuilder().build(ThemeAPI.ThemeInterface::class.java, RestParams())
    }

    @Provides
    fun provideObserverApi(): ObserverApi {
        return RestBuilder().build(ObserverApi::class.java, RestParams())
    }

    @Provides
    fun provideUnreadCountApi(): UnreadCountAPI.UnreadCountsInterface {
        return RestBuilder().build(UnreadCountAPI.UnreadCountsInterface::class.java, RestParams())
    }

    @Provides
    fun provideRecipientApi(): RecipientAPI.RecipientInterface {
        return RestBuilder().build(RecipientAPI.RecipientInterface::class.java, RestParams())
    }

    @Provides
    fun provideLaunchDefinitionsApi(): LaunchDefinitionsAPI.LaunchDefinitionsInterface {
        return RestBuilder().build(LaunchDefinitionsAPI.LaunchDefinitionsInterface::class.java, RestParams())
    }

    @Provides
    fun provideStudioApi(): StudioApi {
        return RestBuilder().build(StudioApi::class.java, RestParams())
    }

    @Provides
    fun provideSmartSearchApi(): SmartSearchApi {
        return RestBuilder().build(SmartSearchApi::class.java, RestParams())
    }

    @Provides
    fun providePandataApi(): PandataApi.PandataInterface {
        return RestBuilder().build(PandataApi.PandataInterface::class.java, RestParams())
    }

    @Provides
    fun provideSectionApi(): SectionAPI.SectionsInterface {
        return RestBuilder().build(SectionAPI.SectionsInterface::class.java, RestParams())
    }

    @Provides
    fun provideNotificationPreferencesApi(): NotificationPreferencesAPI.NotificationPreferencesInterface {
        return RestBuilder().build(NotificationPreferencesAPI.NotificationPreferencesInterface::class.java, RestParams())
    }

    @Provides
    fun provideCommunicationChannelsApi(): CommunicationChannelsAPI.CommunicationChannelInterface {
        return RestBuilder().build(CommunicationChannelsAPI.CommunicationChannelInterface::class.java, RestParams())
    }
}