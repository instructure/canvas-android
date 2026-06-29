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
package com.instructure.canvas.espresso

import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs

/**
 * Test helper for deterministically controlling Firebase Remote Config feature flags in
 * interaction/E2E tests.
 *
 * [TestAppManager] initializes `RemoteConfigUtils` without fetching from Firebase, which makes
 * [RemoteConfigPrefs] the sole source of truth for remote config values. Production code reads
 * those values through `RemoteConfigUtils.getString`/`getBoolean`, so anything seeded here is
 * picked up the next time a flag is read (typically after `tokenLogin`, before navigation).
 *
 * Values must be set before the screen that reads them is created. They are cleared between tests
 * by the app-specific activity test rule's `performReset()`.
 */
object RemoteConfigOverrides {

    fun set(param: RemoteConfigParam, value: Boolean) = set(param, value.toString())

    fun set(param: RemoteConfigParam, value: String) {
        RemoteConfigPrefs.putString(param.rc_name, value)
    }

    fun clear() {
        RemoteConfigPrefs.clearPrefs()
    }
}
