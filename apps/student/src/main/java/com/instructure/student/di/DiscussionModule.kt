package com.instructure.student.di

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragmentBehavior
import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.details.StudentDiscussionDetailsWebViewFragmentBehavior
import com.instructure.student.features.discussion.routing.StudentDiscussionRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
class DiscussionModule {

    @Provides
    fun provideDiscussionRouter(fragmentActivity: FragmentActivity, networkStateProvider: NetworkStateProvider): DiscussionRouter {
        return StudentDiscussionRouter(fragmentActivity, networkStateProvider)
    }

    @Provides
    fun provideDiscussionWebViewFragmentBehavior(): DiscussionDetailsWebViewFragmentBehavior {
        return StudentDiscussionDetailsWebViewFragmentBehavior()
    }
}
