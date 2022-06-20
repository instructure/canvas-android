/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigUtils

class FeatureFlagProvider(
    private val userManager: UserManager,
    private val remoteConfigUtils: RemoteConfigUtils,
    private val apiPrefs: ApiPrefs
) {

    suspend fun getCanvasForElementaryFlag(): Boolean {
        try {
            val userResult = userManager.getSelfAsync(false).await()
            val canvasForElementary = userResult.dataOrThrow.k5User
            apiPrefs.canvasForElementary = canvasForElementary
            return canvasForElementary && apiPrefs.elementaryDashboardEnabledOverride
        } catch (e: Exception) {
            return apiPrefs.canvasForElementary
        }
    }

    fun getDiscussionRedesignFeatureFlag(): Boolean {
        return remoteConfigUtils.getBoolean(RemoteConfigParam.DISCUSSION_REDESIGN)
    }
}