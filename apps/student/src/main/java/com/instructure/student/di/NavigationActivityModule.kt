/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.di

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.offline.facade.AssignmentFacade
import com.instructure.pandautils.room.offline.facade.CourseFacade
import com.instructure.pandautils.room.offline.facade.EnrollmentFacade
import com.instructure.pandautils.room.offline.facade.SubmissionFacade
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.grades.datasource.GradesListLocalDataSource
import com.instructure.student.features.grades.datasource.GradesListNetworkDataSource
import com.instructure.student.features.navigation.NavigationRepository
import com.instructure.student.features.navigation.datasource.NavigationLocalDataSource
import com.instructure.student.features.navigation.datasource.NavigationNetworkDataSource
import com.instructure.student.navigation.DefaultNavigationBehavior
import com.instructure.student.navigation.ElementaryNavigationBehavior
import com.instructure.student.navigation.NavigationBehavior
import com.instructure.student.util.AppShortcutManager
import com.instructure.student.util.DefaultAppShortcutManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Named

private const val CANVAS_FOR_ELEMENTARY = "canvas_for_elementary"

@Module
@InstallIn(ActivityComponent::class)
class NavigationActivityModule {

    @Provides
    @Named(CANVAS_FOR_ELEMENTARY)
    fun providesCanvasForElementaryFeatureFlag(activity: FragmentActivity): Boolean {
        val intent = activity.intent
        return intent?.getBooleanExtra("canvas_for_elementary", false) ?: false
    }

    @Provides
    fun providesNavigationBehavior(@Named(CANVAS_FOR_ELEMENTARY) canvasForElementary: Boolean, apiPrefs: ApiPrefs): NavigationBehavior {
        return if (canvasForElementary || apiPrefs.showElementaryView) {
            ElementaryNavigationBehavior(apiPrefs)
        } else {
            DefaultNavigationBehavior(apiPrefs)
        }
    }

    @Provides
    fun provideAppShortcutManager(): AppShortcutManager {
        return DefaultAppShortcutManager()
    }

    @Provides
    fun provideNavigationRepository(
        localDataSource: NavigationLocalDataSource,
        networkDataSource: NavigationNetworkDataSource,
        networkStateProvider: NetworkStateProvider,
        featureFlagProvider: FeatureFlagProvider
    ): NavigationRepository {
        return NavigationRepository(localDataSource, networkDataSource, networkStateProvider, featureFlagProvider)
    }

    @Provides
    fun provideNavigationLocalDataSource(
        courseFacade: CourseFacade
    ): NavigationLocalDataSource {
        return NavigationLocalDataSource(courseFacade)
    }

    @Provides
    fun provideNavigationNetworkDataSource(
        courseApi: CourseAPI.CoursesInterface
    ): NavigationNetworkDataSource {
        return NavigationNetworkDataSource(courseApi)
    }
}