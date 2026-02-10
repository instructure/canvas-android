package com.instructure.pandautils.domain.usecase.planner

import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.pandautils.data.repository.planner.PlannerRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import javax.inject.Inject

class LoadPlannerItemsUseCase @Inject constructor(
    private val repository: PlannerRepository
) : BaseUseCase<LoadPlannerItemsUseCase.Params, List<PlannerItem>>() {

    override suspend fun execute(params: Params): List<PlannerItem> {
        return repository.getPlannerItems(
            startDate = params.startDate,
            endDate = params.endDate,
            contextCodes = emptyList(),
            forceRefresh = params.forceNetwork
        ).dataOrThrow
            .filter { it.plannableType != PlannableType.ANNOUNCEMENT && it.plannableType != PlannableType.ASSESSMENT_REQUEST }
    }

    data class Params(
        val startDate: String,
        val endDate: String,
        val forceNetwork: Boolean = false
    )
}