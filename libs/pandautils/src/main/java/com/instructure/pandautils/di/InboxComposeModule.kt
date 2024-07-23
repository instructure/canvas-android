package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.RecipientAPI
import com.instructure.pandautils.features.inbox.coursepicker.CoursePickerRepository
import com.instructure.pandautils.features.inbox.coursepicker.CoursePickerRepositoryImpl
import com.instructure.pandautils.features.inbox.recipientpicker.RecipientPickerRepository
import com.instructure.pandautils.features.inbox.recipientpicker.RecipientPickerRepositoryImpl
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
        groupAPI: GroupAPI.GroupInterface
    ): CoursePickerRepository {
        return CoursePickerRepositoryImpl(courseAPI, groupAPI)
    }

    @Provides
    fun provideRecipientPickerRepository(
        recipientAPI: RecipientAPI.RecipientInterface,
    ): RecipientPickerRepository {
        return RecipientPickerRepositoryImpl(recipientAPI)
    }
}