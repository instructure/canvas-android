package com.instructure.student.di

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.instructure.canvas.espresso.TestAppManager
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
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        Log.d("WorkManagerTest", "TestWorkManagerModule.provideWorkManager() called")

        val application = context.applicationContext as? TestAppManager
        if (application != null && !application.workManagerInitialized) {
            Log.d("WorkManagerTest", "WorkManager not yet initialized, initializing with WorkManagerTestInitHelper")
            WorkManagerTestInitHelper.initializeTestWorkManager(context)
            Log.d("WorkManagerTest", "WorkManager initialized, will be overridden in @Before with HiltWorkerFactory")
        } else {
            Log.d("WorkManagerTest", "WorkManager already initialized by @Before, skipping")
        }

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