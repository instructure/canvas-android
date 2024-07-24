package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.pandautils.features.inbox.compose.InboxComposeRepository
import com.instructure.pandautils.features.inbox.compose.InboxComposeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class InboxComposeModule {
    @Provides
    fun provideCoursePickerRepository(
        courseAPI: CourseAPI.CoursesInterface,
        groupAPI: GroupAPI.GroupInterface,
        recipientAPI: RecipientAPI.RecipientInterface,
    ): InboxComposeRepository {
        return InboxComposeRepositoryImpl(courseAPI, groupAPI, recipientAPI)
    }
}