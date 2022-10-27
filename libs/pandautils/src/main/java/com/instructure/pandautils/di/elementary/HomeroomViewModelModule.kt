package com.instructure.pandautils.di.elementary

import android.content.res.Resources
import com.instructure.canvasapi2.managers.AnnouncementManager
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.pandautils.features.elementary.homeroom.CourseCardCreator
import com.instructure.pandautils.utils.ColorKeeper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class HomeroomViewModelModule {

    @Provides
    fun provideCourseCardCreator(plannerManager: PlannerManager, userManager: UserManager,
                                 announcementManager: AnnouncementManager, resources: Resources): CourseCardCreator {
        return CourseCardCreator(plannerManager, userManager, announcementManager, resources, ColorKeeper)
    }
}