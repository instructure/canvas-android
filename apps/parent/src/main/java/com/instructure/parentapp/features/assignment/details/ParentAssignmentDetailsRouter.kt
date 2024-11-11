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
package com.instructure.parentapp.features.assignment.details

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.parentapp.util.navigation.Navigation

class ParentAssignmentDetailsRouter(
    private val navigation: Navigation
): AssignmentDetailsRouter() {
    override fun navigateToSendMessage(activity: FragmentActivity, options: InboxComposeOptions) {
        val route = navigation.inboxComposeRoute(options)
        navigation.navigate(activity, route)
    }
}