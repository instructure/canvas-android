package com.instructure.teacher.di

import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.teacher.features.discussion.TeacherDiscussionRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class DiscussionModule {

    @Provides
    fun provideDiscussionRouter(): DiscussionRouter {
        return TeacherDiscussionRouter()
    }
}