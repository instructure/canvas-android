package com.emeritus.student.di

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.emeritus.student.features.discussion.StudentDiscussionRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class DiscussionModule {

    @Provides
    fun provideDiscussionRouter(fragmentActivity: FragmentActivity): DiscussionRouter {
        return StudentDiscussionRouter(fragmentActivity)
    }
}