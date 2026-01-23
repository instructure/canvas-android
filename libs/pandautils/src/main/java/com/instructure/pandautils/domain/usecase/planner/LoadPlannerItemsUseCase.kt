package com.instructure.pandautils.domain.usecase.planner

import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class LoadPlannerItemsUseCase @Inject constructor(
    private val plannerApi: PlannerAPI.PlannerInterface
) {

    suspend operator fun invoke(
        startDate: String,
        endDate: String,
        forceNetwork: Boolean = false
    ): List<PlannerItem> {
        val restParams = RestParams(
            isForceReadFromNetwork = forceNetwork,
            usePerPageQueryParam = true
        )

        return plannerApi.getPlannerItems(
            startDate,
            endDate,
            emptyList(),
            null,
            restParams
        ).depaginate {
            plannerApi.nextPagePlannerItems(it, restParams)
        }.dataOrThrow
            .filter { it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST }
    }
}