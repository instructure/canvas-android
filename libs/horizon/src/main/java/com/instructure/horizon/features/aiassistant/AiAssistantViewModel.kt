/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.aiassistant

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.DomainServicesAuthenticationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val domainServicesAuthenticationManager: DomainServicesAuthenticationManager,
): ViewModel() {
    init {
        viewModelScope.launch {
            val pineToken = domainServicesAuthenticationManager.getDomainServicesAuthenticationToken(
                domainService = com.instructure.canvasapi2.models.DomainService.PINE
            )
            val cedarToken = domainServicesAuthenticationManager.getDomainServicesAuthenticationToken(
                domainService = com.instructure.canvasapi2.models.DomainService.CEDAR
            )
            val redwoodToken = domainServicesAuthenticationManager.getDomainServicesAuthenticationToken(
                domainService = com.instructure.canvasapi2.models.DomainService.REDWOOD
            )

            val
            Log.d("AiAssistantToken", "pineToken: $pineToken")
            Log.d("AiAssistantToken", "cedarToken: $cedarToken")
            Log.d("AiAssistantToken", "redwoodToken: $redwoodToken")
        }
    }
}