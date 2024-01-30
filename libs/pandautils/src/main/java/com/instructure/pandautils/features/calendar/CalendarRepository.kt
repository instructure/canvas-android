package com.instructure.pandautils.features.calendar

import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.depaginate

class CalendarRepository(private val plannerApi: PlannerAPI.PlannerInterface) {

    suspend fun getPlannerItems(startDate: String,
                                endDate: String,
                                contextCodes: List<String>,
                                forceNetwork: Boolean): List<PlannerItem> {
        val restParams =
            RestParams(isForceReadFromNetwork = forceNetwork, usePerPageQueryParam = true)

        // The calendar on web does not include announcements, so we filter them out here to match that behavior
        return plannerApi.getPlannerItems(
            startDate,
            endDate,
            contextCodes,
            restParams
        ).depaginate {
            plannerApi.nextPagePlannerItems(it, restParams)
        }.dataOrThrow
            .filter { it.plannableType != PlannableType.ANNOUNCEMENT }
    }
}