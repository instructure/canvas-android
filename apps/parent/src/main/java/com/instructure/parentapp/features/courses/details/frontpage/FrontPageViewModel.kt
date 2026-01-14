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

package com.instructure.parentapp.features.courses.details.frontpage

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.toBaseUrl
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.features.grades.COURSE_ID_KEY
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.R
import com.instructure.parentapp.util.ParentPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FrontPageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: FrontPageRepository,
    private val parentPrefs: ParentPrefs
) : ViewModel() {

    private val courseId = savedStateHandle.get<Long>(COURSE_ID_KEY).orDefault()

    private val _uiState = MutableStateFlow(FrontPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<FrontPageViewModelAction>()
    val events = _events.receiveAsFlow()

    init {
        loadFrontPage(false)
    }

    private fun loadFrontPage(forceRefresh: Boolean) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    studentColor = parentPrefs.currentStudent.studentColor,
                    isLoading = it.htmlContent.isEmpty(),
                    isRefreshing = it.htmlContent.isNotEmpty(),
                    baseUrl = CanvasContext.emptyCourseContext(courseId).toBaseUrl()
                )
            }

            val frontPage = repository.loadFrontPage(courseId, forceRefresh)

            _uiState.update {
                it.copy(
                    htmlContent = frontPage.body.orEmpty(),
                    isLoading = false,
                    isRefreshing = false,
                    isError = false
                )
            }
        } catch {
            val showSnack = forceRefresh && _uiState.value.htmlContent.isNotEmpty()

            if (showSnack) {
                viewModelScope.launch {
                    _events.send(FrontPageViewModelAction.ShowSnackbar(context.getString(R.string.frontPageRefreshFailed)))
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isError = !showSnack,
                    isRefreshing = false
                )
            }
        }
    }

    fun handleAction(action: FrontPageAction) {
        when (action) {
            is FrontPageAction.Refresh -> {
                loadFrontPage(true)
            }
        }
    }
}
