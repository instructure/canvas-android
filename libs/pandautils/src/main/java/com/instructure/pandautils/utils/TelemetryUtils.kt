/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
 *
 */package com.instructure.pandautils.utils

import android.content.Context
import com.newrelic.agent.android.FeatureFlag
import com.newrelic.agent.android.NewRelic

/**
 * This class is designed with two purposes in mind:
 *   (1) Consolidate all low-level telemetry logic into one place, which will allow us to
 *       more easily turn enable/disable telemetry in our build.
 *   (2) It could potentially also allow us to more easily switch to other telemetry providers
 *       (e.g., Splunk Mobile, Firebase).  Although some things (e.g., setInteractionName) are
 *       admittedly pretty specific to NewRelic.
 */
object TelemetryUtils {
    fun setInteractionName(interactionName: String) {
        if(initialized) {
            NewRelic.setInteractionName(interactionName)
        }
    }

    private var initialized = false

    fun initialize(context: Context, appToken: String) {
        NewRelic.enableFeature(FeatureFlag.NetworkRequests)
        NewRelic.enableFeature(FeatureFlag.NetworkErrorRequests)
        NewRelic.withApplicationToken(appToken)
                .withCrashReportingEnabled(false) // might interfere with Crashlytics
                .withInteractionTracing(true)
                .withLoggingEnabled(false)
                .start(context)
        initialized = true
    }
}