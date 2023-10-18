package com.instructure.teacher.di

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.teacher.features.discussion.routing.TeacherDiscussionRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class DiscussionModule {

    @Provides
    fun provideDiscussionRouter(activity: FragmentActivity): DiscussionRouter {
        return TeacherDiscussionRouter(activity)
    }
}