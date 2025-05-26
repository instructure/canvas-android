/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.student.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import com.instructure.canvasapi2.utils.Analytics

class LoggingStartActivityAction : ActionCallback {

    companion object {
        private val keyIntent = ActionParameters.Key<Intent>("key_widget_action_intent")
        private val keyAnalyticsEvent = ActionParameters.Key<String>("key_widget_action_analytics_event")

        fun createActionParams(intent: Intent, analyticsEvent: String? = null): ActionParameters {
            return actionParametersOf(
                keyIntent to intent,
                keyAnalyticsEvent to (analyticsEvent ?: "")
            )
        }
    }
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        parameters[keyAnalyticsEvent]?.let { analyticsEvent ->
            Analytics.logEvent(analyticsEvent)
        }
        parameters[keyIntent]?.let {
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}