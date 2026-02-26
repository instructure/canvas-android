package com.instructure.pandautils.features.dashboard.widget.usecase

import com.google.gson.Gson
import com.instructure.pandautils.domain.usecase.BaseFlowUseCase
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.repository.WidgetConfigDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveGlobalConfigUseCase @Inject constructor(
    private val repository: WidgetConfigDataRepository,
    private val gson: Gson
) : BaseFlowUseCase<Unit, GlobalConfig>() {

    override fun execute(params: Unit): Flow<GlobalConfig> {
        return repository.observeConfigJson(WidgetMetadata.WIDGET_ID_GLOBAL).map { json ->
            json?.let {
                try {
                    gson.fromJson(it, GlobalConfig::class.java)
                } catch (e: Exception) {
                    GlobalConfig()
                }
            } ?: GlobalConfig()
        }
    }
}