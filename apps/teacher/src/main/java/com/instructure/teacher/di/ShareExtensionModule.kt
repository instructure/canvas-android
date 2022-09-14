package com.instructure.teacher.di

import com.instructure.pandautils.features.shareextension.ShareExtensionRouter
import com.instructure.teacher.features.shareextension.TeacherShareExtensionRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ShareExtensionModule {

    @Provides
    @Singleton
    fun provideShareExtensionRouter() : ShareExtensionRouter {
        return TeacherShareExtensionRouter()
    }
}