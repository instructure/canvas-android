/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import android.content.ComponentName
import android.content.Intent

/**
 * Creates a chooser from this intent that will exclude Instructure components (i.e. student, parent, and teacher apps).
 * Only works on Android N and above. On pre-N devices this intent is returned unmodified.
 */
fun Intent.asChooserExcludingInstructure(title: String? = null): Intent {
    val excludeComponents = arrayOf(
        // Student
        ComponentName("com.instructure.candroid", "com.instructure.student.activity.InterwebsToApplication"),

        // Parent (native)
        ComponentName("com.instructure.parentapp", "com.instructure.parentapp.features.login.routevalidator.RouteValidatorActivity"),

        // Parent (flutter)
        ComponentName("com.instructure.parentapp", "com.instructure.parentapp.MainActivity"),

        // Teacher
        ComponentName("com.instructure.teacher", "com.instructure.teacher.activities.RouteValidatorActivity")
    )
    return Intent.createChooser(this, title).apply { putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludeComponents) }
}
