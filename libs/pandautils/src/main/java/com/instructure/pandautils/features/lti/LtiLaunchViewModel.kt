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
package com.instructure.pandautils.features.lti

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.replaceWithURLQueryParameter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LtiLaunchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LtiLaunchRepository,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val ltiUrl: String? = savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL)
    private val ltiTab: Tab? = savedStateHandle.get<Tab>(LtiLaunchFragment.LTI_TAB)
    private val ltiTool: LTITool? = savedStateHandle.get<LTITool>(LtiLaunchFragment.LTI_TOOL)
    private val sessionLessLaunch: Boolean = savedStateHandle.get<Boolean>(LtiLaunchFragment.SESSION_LESS_LAUNCH) ?: false
    private val assignmentLti: Boolean = savedStateHandle.get<Boolean>(LtiLaunchFragment.IS_ASSIGNMENT_LTI) ?: false
    private val canvasContext: CanvasContext? = savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT)
    private val openInternally: Boolean = savedStateHandle.get<Boolean>(LtiLaunchFragment.OPEN_INTERNALLY) ?: false

    private val _events = Channel<LtiLaunchAction>()
    val events = _events.receiveAsFlow()

    private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
    val state = _state.asStateFlow()

    init {
        handleLtiInput()
    }

    private fun handleLtiInput() {
        try {
            when {
                ltiTab != null -> loadLtiAuthenticatedUrl(ltiTab.ltiUrl)
                ltiUrl.isNullOrBlank().not() -> {
                    var url = ltiUrl.orEmpty() // Replace deep link scheme
                        .replaceFirst("canvas-courses://", "${apiPrefs.protocol}://")
                        .replaceFirst("canvas-student://", "${apiPrefs.protocol}://")
                        .replaceWithURLQueryParameter(HtmlContentFormatter.hasKalturaUrl(ltiUrl))

                    when {
                        sessionLessLaunch -> {
                            // This is specific for Studio and Gauge
                            val id = url.substringAfterLast("/external_tools/").substringBefore("?")
                            url = when {
                                (id.toIntOrNull() != null) -> when (canvasContext) {
                                    is Course -> "${apiPrefs.fullDomain}/api/v1/courses/${canvasContext.id}/external_tools/sessionless_launch?id=$id"
                                    is Group -> "${apiPrefs.fullDomain}/api/v1/groups/${canvasContext.id}/external_tools/sessionless_launch?id=$id"
                                    else -> "${apiPrefs.fullDomain}/api/v1/accounts/self/external_tools/sessionless_launch?id=$id"
                                }

                                else -> when (canvasContext) {
                                    is Course -> "${apiPrefs.fullDomain}/api/v1/courses/${canvasContext.id}/external_tools/sessionless_launch?url=$url"
                                    is Group -> "${apiPrefs.fullDomain}/api/v1/groups/${canvasContext.id}/external_tools/sessionless_launch?url=$url"
                                    else -> "${apiPrefs.fullDomain}/api/v1/accounts/self/external_tools/sessionless_launch?url=$url"
                                }
                            }
                            loadLtiAuthenticatedUrl(url)
                        }

                        assignmentLti -> loadLtiAuthenticatedUrl(url)
                        else -> launchLti(url)
                    }
                }

                else -> displayError()
            }
        } catch (e: Exception) {
            displayError()
        }
    }

    private fun loadLtiAuthenticatedUrl(url: String? = null) {
        viewModelScope.tryLaunch {
            url?.let {
                val authenticatedLtiTool = repository.getLtiFromAuthenticationUrl(it, ltiTool)
                authenticatedLtiTool.url?.let { url ->
                    launchLti(url)
                } ?: displayError()
            }
        } catch {
            displayError()
        }
    }

    private fun launchLti(url: String) {
        viewModelScope.launch {
            if (openInternally || Assignment.internalLtiTools.any { url.contains(it) }) {
                _events.send(LtiLaunchAction.LoadLtiWebView(url))
            } else {
                _events.send(LtiLaunchAction.LaunchCustomTab(url))
            }
            _state.value = ViewState.Success
        }
    }

    private fun displayError() {
        viewModelScope.launch {
            _events.send(LtiLaunchAction.ShowError)
        }
    }
}