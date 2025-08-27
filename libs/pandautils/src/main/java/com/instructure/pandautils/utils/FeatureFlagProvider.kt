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

import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.appdatabase.daos.EnvironmentFeatureFlagsDao
import com.instructure.pandautils.room.appdatabase.entities.EnvironmentFeatureFlags

const val FEATURE_FLAG_OFFLINE = "mobile_offline_mode"
class FeatureFlagProvider(
    private val userManager: UserManager,
    private val apiPrefs: ApiPrefs,
    private val featuresApi: FeaturesAPI.FeaturesInterface,
    private val environmentFeatureFlags: EnvironmentFeatureFlagsDao
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

    suspend fun fetchEnvironmentFeatureFlags() {
        val restParams = RestParams(isForceReadFromNetwork = true, shouldIgnoreToken = true)
        val featureFlags = featuresApi.getEnvironmentFeatureFlags(restParams).dataOrNull ?: return
        apiPrefs.user?.id?.let {
            environmentFeatureFlags.insert(EnvironmentFeatureFlags(it, featureFlags))
        }
    }

    suspend fun offlineEnabled(): Boolean {
        return checkEnvironmentFeatureFlag(FEATURE_FLAG_OFFLINE) && !apiPrefs.canvasForElementary
    }

    suspend fun checkEnvironmentFeatureFlag(featureFlag: String): Boolean {
        return apiPrefs.user?.id?.let { environmentFeatureFlags.findByUserId(it)?.featureFlags?.get(featureFlag) == true } ?: false
    }

    suspend fun checkAccountSurveyNotificationsFlag(): Boolean {
        return checkEnvironmentFeatureFlag("account_survey_notifications")
    }

    suspend fun checkRestrictStudentAccessFlag(): Boolean {
        return checkEnvironmentFeatureFlag("restrict_student_access")
    }
}