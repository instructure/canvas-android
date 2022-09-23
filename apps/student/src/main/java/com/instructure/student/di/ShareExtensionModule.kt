package com.instructure.student.di

import com.instructure.pandautils.features.shareextension.ShareExtensionRouter
import com.instructure.student.features.shareextension.StudentShareExtensionRouter
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
    fun provideShareExtensionRouter(): ShareExtensionRouter {
        return StudentShareExtensionRouter()
    }
}