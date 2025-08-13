/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.parentapp.features.inbox.details

import com.instructure.pandautils.features.inbox.details.InboxDetailsBehavior
import com.instructure.pandautils.utils.FeatureFlagProvider
import javax.inject.Inject

class ParentInboxDetailsBehavior @Inject constructor(
    private val featureFlagProvider: FeatureFlagProvider
) : InboxDetailsBehavior() {

    override suspend fun shouldRestrictDeleteConversation(): Boolean {
        return try {
            featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access")
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun shouldRestrictReplyAll(): Boolean {
        return try {
            featureFlagProvider.checkEnvironmentFeatureFlag("restrict_student_access")
        } catch (e: Exception) {
            false
        }
    }
}
