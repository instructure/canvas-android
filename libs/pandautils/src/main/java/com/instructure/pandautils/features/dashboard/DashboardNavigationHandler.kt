/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.instructure.pandautils.features.dashboard

interface DashboardNavigationHandler {
    fun handleCoursesNavigation(event: DashboardNavigationEvent.Courses)
    fun handleTodoNavigation(event: DashboardNavigationEvent.Todo)
    fun handleForecastNavigation(event: DashboardNavigationEvent.Forecast)
    fun handleProgressNavigation(event: DashboardNavigationEvent.Progress)
    fun handleConferencesNavigation(event: DashboardNavigationEvent.Conferences)
    fun handleDashboardNavigation(event: DashboardNavigationEvent.Dashboard)
}
