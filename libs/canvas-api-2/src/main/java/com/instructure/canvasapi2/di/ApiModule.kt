package com.instructure.canvasapi2.di

import com.instructure.canvasapi2.apis.HelpLinksAPI
import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    fun provideRemoteConfigUtils(): RemoteConfigUtils {
        return RemoteConfigUtils
    }

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
    fun provideUserManager(): UserManager {
        return UserManager
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
}