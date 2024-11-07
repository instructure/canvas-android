/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.lti

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LtiLaunchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LtiLaunchRepository
) : ViewModel() {

    private val ltiUrl: String? = savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL)

    private val _events = Channel<LtiLaunchAction>()
    val events = _events.receiveAsFlow()

    init {
        loadLtiAuthenticatedUrl()
    }

    private fun loadLtiAuthenticatedUrl() {
        viewModelScope.tryLaunch {
            ltiUrl?.let {
                val ltiTool = repository.getLtiFromAuthenticationUrl(it)
                ltiTool.url?.let { url ->
                    _events.send(LtiLaunchAction.LaunchCustomTab(url))
                } ?: _events.send(LtiLaunchAction.ShowError)
            }
        } catch {
            viewModelScope.launch {
                _events.send(LtiLaunchAction.ShowError)
            }
        }
    }
}