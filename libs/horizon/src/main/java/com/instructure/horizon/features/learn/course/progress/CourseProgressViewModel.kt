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
package com.instructure.horizon.features.learn.course.progress

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.organisms.cards.ModuleHeaderStateMapper
import com.instructure.horizon.horizonui.organisms.cards.ModuleItemCardStateMapper
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CourseProgressViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CourseProgressRepository,
    private val moduleHeaderStateMapper: ModuleHeaderStateMapper,
    private val moduleItemCardStateMapper: ModuleItemCardStateMapper,
): ViewModel() {
    private val _uiState = MutableStateFlow(
        CourseProgressUiState(
            screenState = LoadingState(
                onRefresh = ::refresh,
                onSnackbarDismiss = ::dismissSnackbar,
            ),
        )
    )
    val uiState = _uiState.asStateFlow()

    fun loadState(courseId: Long) {
        _uiState.update {
            it.copy(
                screenState = it.screenState.copy(isLoading = true),
                courseId = courseId,
            )
        }
        viewModelScope.tryLaunch {
            getData(courseId)
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = false))
            }
        } catch { exception ->
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(isLoading = false, isError = true, errorMessage = context.getString(
                        R.string.failedToLoadPrgress
                    )),
                )
            }
        }
    }

    private suspend fun getData(courseId: Long, forceRefresh: Boolean = false) {
        val modules = repository.getModuleItems(courseId, forceRefresh)

        val moduleItemStates = modules.associateWith { module ->
            moduleHeaderStateMapper.mapModuleObjectToHeaderState(
                module,
                modules,
                ::moduleHeaderSelected
            ).copy(expanded = uiState.value.moduleItemStates[module.id]?.first?.expanded.orDefault()) to module.items.mapNotNull { moduleItem ->
                if (moduleItem.type == ModuleItem.Type.SubHeader.name) {
                    ModuleItemState.SubHeader(moduleItem.title ?: "")
                } else {
                    val cardState = moduleItemCardStateMapper.mapModuleItemToCardState(
                        moduleItem
                    ) {}

                    if (cardState != null) {
                        ModuleItemState.ModuleItemCard(
                            moduleItem.id,
                            cardState
                        )
                    } else {
                        null
                    }
                }
            }
        }.mapKeys { it.key.id }

        _uiState.update { it.copy(moduleItemStates = moduleItemStates) }
    }

    fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isRefreshing = true)) }
            getData(uiState.value.courseId, forceRefresh = true)
            _uiState.update { it.copy(screenState = it.screenState.copy(isRefreshing = false)) }
        } catch {
            _uiState.update { it.copy(screenState = it.screenState.copy(snackbarMessage = context.getString(
                R.string.errorOccurred), isRefreshing = false)) }
        }
    }

    private fun moduleHeaderSelected(moduleId: Long) {
        _uiState.update {
            it.copy(
                moduleItemStates = it.moduleItemStates.toMutableMap().apply {
                    it.moduleItemStates[moduleId]?.let { moduleItemState ->
                        this[moduleId] = moduleItemState.copy(
                            first = moduleItemState.first.copy(
                                expanded = !moduleItemState.first.expanded,
                            )
                        )
                    }
                },
            )
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(snackbarMessage = null))
        }
    }
}