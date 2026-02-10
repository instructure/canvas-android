/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.di

import com.instructure.canvasapi2.utils.pageview.PandataInfo
import com.instructure.pandautils.features.dashboard.edit.EditDashboardRepository
import com.instructure.pandautils.features.dashboard.edit.EditDashboardRouter
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.pandautils.features.dashboard.widget.conferences.ConferencesWidgetRouter
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetBehavior
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetRouter
import com.instructure.pandautils.features.dashboard.widget.forecast.ForecastWidgetRouter
import com.instructure.pandautils.features.dashboard.widget.todo.TodoWidgetBehavior
import com.instructure.pandautils.features.dashboard.widget.todo.TodoWidgetRouter
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragmentBehavior
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository
import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.pandautils.features.elementary.grades.GradesRouter
import com.instructure.pandautils.features.elementary.homeroom.HomeroomRouter
import com.instructure.pandautils.features.elementary.importantdates.ImportantDatesRouter
import com.instructure.pandautils.features.elementary.resources.itemviewmodels.ResourcesRouter
import com.instructure.pandautils.features.elementary.schedule.ScheduleRouter
import com.instructure.pandautils.features.offline.sync.SyncRouter
import com.instructure.pandautils.features.shareextension.ShareExtensionRouter
import com.instructure.pandautils.features.smartsearch.SmartSearchRouter
import com.instructure.pandautils.features.speedgrader.SpeedGraderPostPolicyRouter
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
class DefaultBindingsModule {

    @Provides
    fun provideDashboardRouter(): DashboardRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideEditDashboardRouter(): EditDashboardRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideEditDashboardRepository(): EditDashboardRepository {
        throw NotImplementedError()
    }

    @Provides
    fun provideDiscussionRouteHelperStudentRepository(): DiscussionRouteHelperRepository {
        throw NotImplementedError()
    }

    @Provides
    fun provideDiscussionRouter(): DiscussionRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideGradesRouter(): GradesRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideHomeroomRouter(): HomeroomRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideImportantDatesRouter(): ImportantDatesRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideResourcesRouter(): ResourcesRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideScheduleRouter(): ScheduleRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideShareExtensionRouter(): ShareExtensionRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideSyncRouter(): SyncRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideDiscussionDetailsWebViewFragmentBehavior(): DiscussionDetailsWebViewFragmentBehavior {
        throw NotImplementedError()
    }

    @Provides
    fun provideSmartSearchRouter(): SmartSearchRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideAppInfo(): PandataInfo.AppKey {
        throw NotImplementedError()
    }

    @Provides
    fun provideSpeedGraderRouter(): SpeedGraderContentRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideSpeedGraderPostPolicyRouter(): SpeedGraderPostPolicyRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideCoursesWidgetRouter(): CoursesWidgetRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideCoursesWidgetBehavior(): CoursesWidgetBehavior {
        throw NotImplementedError()
    }

    @Provides
    fun provideForecastWidgetRouter(): ForecastWidgetRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideConferencesWidgetRouter(): ConferencesWidgetRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideTodoWidgetRouter(): TodoWidgetRouter {
        throw NotImplementedError()
    }

    @Provides
    fun provideTodoWidgetBehavior(): TodoWidgetBehavior {
        throw NotImplementedError()
    }
}