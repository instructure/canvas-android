package com.instructure.student.di

import android.content.Context
import androidx.work.WorkManager
import com.instructure.pandautils.di.WorkManagerModule
import com.instructure.pandautils.utils.LogoutHelper
import com.instructure.student.espresso.fakes.FakeEnabledTabs
import com.instructure.student.router.EnabledTabs
import com.instructure.student.util.StudentLogoutHelper
import com.instructure.student.util.StudentPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [WorkManagerModule::class]
)
class TestWorkManagerModule {
    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        // Just return the instance - CanvasTest @Before will handle initialization
        return WorkManager.getInstance(context)
    }
}

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
    @Singleton
    fun provideStudentPrefs(): StudentPrefs {
        return StudentPrefs
    }
}