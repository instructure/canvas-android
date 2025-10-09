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
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import com.instructure.canvasapi2.utils.weave.apiAsync
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class PlannerManager(private val plannerApi: PlannerAPI) {

    fun getPlannerItemsAsync(
        forceNetwork: Boolean,
        startDate: String? = null,
        endDate: String? = null,
        contextCodes: List<String> = emptyList(),
        filter: String? = null
    ): Deferred<DataResult<List<PlannerItem>>> {
        return if (contextCodes.isEmpty() && filter == null) {
            apiAsync<List<PlannerItem>> { getPlannerItems(forceNetwork, it, startDate, endDate) }
        } else {
            GlobalScope.async {
                val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
                val api = RestBuilder().build(PlannerAPI.PlannerInterface::class.java, restParams)
                api.getPlannerItems(startDate, endDate, contextCodes, filter, restParams)
                    .depaginate { api.nextPagePlannerItems(it, restParams) }
            }
        }
    }

    companion object {
        fun getPlannerItemsExhaustiveAsync(
            startDate: String?,
            endDate: String?,
            contextCodes: List<String>,
            filter: String?,
            forceNetwork: Boolean
        ): Deferred<DataResult<List<PlannerItem>>> {
            return GlobalScope.async {
                val restParams = RestParams(usePerPageQueryParam = true, isForceReadFromNetwork = forceNetwork)
                val api = RestBuilder().build(PlannerAPI.PlannerInterface::class.java, restParams)
                api.getPlannerItems(startDate, endDate, contextCodes, filter, restParams)
                    .depaginate { api.nextPagePlannerItems(it, restParams) }
            }
        }
    }

    fun getPlannerItems(
            forceNetwork: Boolean,
            callback: StatusCallback<List<PlannerItem>>,
            startDate: String? = null,
            endDate: String? = null
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)

        plannerApi.getPlannerItems(adapter, callback, params, startDate, endDate)
    }

    fun createPlannerOverride(
            forceNetwork: Boolean,
            callback: StatusCallback<PlannerOverride>,
            plannerOverride: PlannerOverride
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        plannerApi.createPlannerOverride(adapter, callback, params, plannerOverride)
    }

    fun createPlannerOverrideAsync(forceNetwork: Boolean, plannerOverride: PlannerOverride) = apiAsync<PlannerOverride> { createPlannerOverride(forceNetwork, it, plannerOverride) }

    fun updatePlannerOverride(
            forceNetwork: Boolean,
            callback: StatusCallback<PlannerOverride>,
            completed: Boolean,
            overrideId: Long
    ) {
        val adapter = RestBuilder(callback)
        val params = RestParams(isForceReadFromNetwork = forceNetwork)

        plannerApi.updatePlannerOverride(adapter, callback, params, overrideId, completed)
    }

    fun updatePlannerOverrideAsync(forceNetwork: Boolean, completed: Boolean, overrideId: Long) = apiAsync<PlannerOverride> { updatePlannerOverride(forceNetwork, it, completed, overrideId) }
}