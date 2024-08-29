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

import com.instructure.canvasapi2.utils.BooleanPref
import com.instructure.canvasapi2.utils.PrefManager

object CalendarPrefs : PrefManager("calendar") {

    var calendarExpanded by BooleanPref(false)

    // When we first start the new calendar we won't have saved filters for groups so we will add all groups
    var firstStart by BooleanPref(true)
}