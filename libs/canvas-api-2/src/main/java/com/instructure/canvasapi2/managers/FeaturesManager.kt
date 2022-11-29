/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 */
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.weave.apiAsync

object FeaturesManager {

    const val SEND_USAGE_METRICS = "send_usage_metrics"

    fun getEnabledFeaturesForCourse(courseId: Long, forceNetwork: Boolean, callback: StatusCallback<List<String>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        FeaturesAPI.getEnabledFeaturesForCourse(adapter, courseId, callback, params)
    }

    fun getEnabledFeaturesForCourseAsync(
        courseId: Long,
        forceNetwork: Boolean
    ) = apiAsync<List<String>> { getEnabledFeaturesForCourse(courseId, forceNetwork, it) }

    fun getEnvironmentFeatureFlags(forceNetwork: Boolean, callback: StatusCallback<Map<String, Boolean>>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        FeaturesAPI.getEnvironmentFeatureFlags(adapter, callback, params)
    }

    fun getEnvironmentFeatureFlagsAsync(forceNetwork: Boolean) = apiAsync<Map<String, Boolean>> { getEnvironmentFeatureFlags(forceNetwork, it) }
}
