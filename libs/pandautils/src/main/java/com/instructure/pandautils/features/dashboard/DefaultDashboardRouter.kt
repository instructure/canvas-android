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

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter

class DefaultDashboardRouter : DashboardRouter {
    override fun routeToGlobalAnnouncement(subject: String, message: String) = Unit

    override fun routeToSubmissionDetails(
        canvasContext: CanvasContext,
        assignmentId: Long,
        attemptId: Long
    ) = Unit

    override fun routeToMyFiles(canvasContext: CanvasContext, folderId: Long) = Unit

    override fun routeToSyncProgress() = Unit

    override fun routeToManageOfflineContent() = Unit

    override fun routeToCustomizeDashboard() = Unit

    override fun restartApp() = Unit
}