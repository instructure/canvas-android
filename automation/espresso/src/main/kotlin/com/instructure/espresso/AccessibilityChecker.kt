//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.instructure.espresso

import androidx.test.espresso.accessibility.AccessibilityChecks
import com.google.android.apps.common.testing.accessibility.framework.integrations.espresso.AccessibilityValidator

// https://instructure.atlassian.net/wiki/spaces/MOBILE/pages/30867503/Android+Accessibility+Automation
//
// AccessibilityChecks.enable() should only be called *once* (or else it will cause an exception
// to be thrown).
object AccessibilityChecker {

    // Allow public access so as to be able to make configuration calls on the validator
    var accessibilityValidator: AccessibilityValidator? = null
        private set

    /**
     * The first time that this is called, it will enable accessibility checks permanently.
     */
    fun runChecks() {
        if (accessibilityValidator == null) {
            accessibilityValidator = AccessibilityChecks.enable()
        }
    }
}

