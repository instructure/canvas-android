/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.calendar

import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.withArgs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalendarFragment : BaseCalendarFragment() {

    companion object {
        const val SELECTED_DAY = "selected_day"

        fun newInstance(route: Route) = CalendarFragment().withArgs(
            route.arguments.apply {
                if (route.paramsHash.containsKey(SELECTED_DAY)) {
                    putString(SELECTED_DAY, route.paramsHash[SELECTED_DAY])
                }
            }
        )

        fun makeRoute() = Route(CalendarFragment::class.java, null)
    }
}