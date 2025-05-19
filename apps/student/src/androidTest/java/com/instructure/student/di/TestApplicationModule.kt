package com.instructure.student.di

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.pandautils.utils.LogoutHelper
import com.instructure.student.espresso.fakes.FakeEnabledTabs
import com.instructure.student.router.EnabledTabs
import com.instructure.student.util.StudentLogoutHelper
import com.instructure.student.widget.todo.ToDoWidgetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ApplicationModule::class]
)
class TestApplicationModule {
    @Provides
    fun provideLogoutHelper(): LogoutHelper {
        return StudentLogoutHelper()
    }

    @Provides
    @Singleton
    fun provideEnabledTabs(): EnabledTabs {
        return FakeEnabledTabs()
    }

    @Provides
    fun provideToDoWidgetRepository(
        plannerApi: PlannerAPI.PlannerInterface,
        coursesApi: CourseAPI.CoursesInterface,
        groupApi: GroupAPI.GroupInterface
    ): ToDoWidgetRepository {
        return ToDoWidgetRepository(plannerApi, coursesApi, groupApi)
    }
}
