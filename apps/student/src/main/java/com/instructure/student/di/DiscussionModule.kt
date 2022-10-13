package com.instructure.student.di

import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.student.features.discussion.StudentDiscussionRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class DiscussionModule {

    @Provides
    fun provideDiscussionRouter(): DiscussionRouter {
        return StudentDiscussionRouter()
    }
}