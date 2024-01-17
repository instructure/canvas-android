package com.instructure.pandautils.features.calendar

import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.PlannerItem

class CalendarRepository(private val plannerApi: PlannerAPI.PlannerInterface) {

    // TODO error handling
    suspend fun getPlannerItems(startDate: String,
                                endDate: String,
                                contextCodes: List<String>,
                                forceNetwork: Boolean): List<PlannerItem> {
        return plannerApi.getPlannerItems(
            startDate,
            endDate,
            contextCodes,
            RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)).dataOrThrow // TODO Paging?

    }
}