/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.learninglibrary.list

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemChipState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionState
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toFormattedString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnLearningLibraryListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: LearnLearningLibraryListRepository
): ViewModel() {

    private var allCollections: List<LearnLearningLibraryCollectionState> = emptyList()
    private val pageSize: Int = 3

    private val _uiState = MutableStateFlow(LearnLearningLibraryListUiState(
        loadingState = LoadingState(
            onRefresh = ::refreshData,
            onSnackbarDismiss = ::onDismissSnackbar
        ),
        itemsToDisplays = pageSize,
        increaseItemsToDisplay = ::increaseItemsToDisplay,
        updateSearchQuery = ::updateSearchQuery,
        onBookmarkClicked = ::onBookmarkItem,
        onEnrollClicked = ::onEnrollItem
    ))
    val uiState = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            val result = fetchData()
            allCollections = result.toUiState()
            _uiState.update { it.copy(collections = allCollections.applyFilters()) }
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true)) }
        }
    }

    private fun refreshData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            val result = fetchData(true)
            allCollections = result.toUiState()
            _uiState.update { it.copy(collections = allCollections.applyFilters()) }
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true, isError = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, snackbarMessage = "Failed to load Learning Library")) }
        }
    }

    private suspend fun fetchData(forceNetwork: Boolean = false): List<EnrolledLearningLibraryCollection> {
        return repository.getEnrolledLearningLibraries(forceNetwork)
    }

    private fun updateSearchQuery(value: TextFieldValue) {
        _uiState.update { it.copy(searchQuery = value) }
        _uiState.update { it.copy(collections = allCollections.applyFilters()) }
    }

    private fun onBookmarkItem(itemId: String) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
                collectionState.copy(
                    items = collectionState.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(bookmarkLoading = true)
                        } else {
                            collectionItemState
                        }
                    }
                )
            })}

            val newIsBookmarked = repository.toggleLearningLibraryItemIsBookmarked(itemId)

            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
                collectionState.copy(
                    items = collectionState.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(
                                bookmarkLoading = false,
                                isBookmarked = newIsBookmarked
                            )
                        } else {
                            collectionItemState
                        }
                    }
                )
            })}
        } catch {
            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
                collectionState.copy(
                    items = collectionState.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(
                                bookmarkLoading = false,
                            )
                        } else {
                            collectionItemState
                        }
                    }
                )
            }, loadingState = it.loadingState.copy(errorMessage = context.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage)))}
        }
    }

    private fun onEnrollItem(itemId: String) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
                collectionState.copy(
                    items = collectionState.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(enrollLoading = true)
                        } else {
                            collectionItemState
                        }
                    }
                )
            })}

            val newItem = repository.enrollLearningLibraryItem(itemId)

            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
                collectionState.copy(
                    items = collectionState.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            newItem.toUiState()
                        } else {
                            collectionItemState
                        }
                    }
                )
            })}
        } catch {
            _uiState.update { it.copy(collections = it.collections.map { collectionState ->
                collectionState.copy(
                    items = collectionState.items.map { collectionItemState ->
                        if (collectionItemState.id == itemId) {
                            collectionItemState.copy(
                                enrollLoading = false,
                            )
                        } else {
                            collectionItemState
                        }
                    }
                )
            }, loadingState = it.loadingState.copy(errorMessage = context.getString(R.string.learnLearningLibraryFailedToEnrollMessage)))}
        }
    }

    private fun onDismissSnackbar() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }
    }

    private fun increaseItemsToDisplay() {
        _uiState.update { it.copy(itemsToDisplays = it.itemsToDisplays + pageSize) }
    }

    private fun List<LearnLearningLibraryCollectionState>.applyFilters(): List<LearnLearningLibraryCollectionState> {
        return this.filter {
            it.name.contains(_uiState.value.searchQuery.text, ignoreCase = true)
        }
    }
}

private fun List<EnrolledLearningLibraryCollection>.toUiState(): List<LearnLearningLibraryCollectionState> {
    return this.map {
        LearnLearningLibraryCollectionState(
            id = it.id,
            name = it.name,
            itemCount = it.items.size,
            items = it.items.map { item ->
                item.toUiState()
            }
        )
    }
}

private fun LearningLibraryCollectionItem.toUiState(): LearnLearningLibraryCollectionItemState {
    return LearnLearningLibraryCollectionItemState(
        id = this.id,
        courseId = this.canvasCourse?.courseId?.toLongOrNull() ?: -1L,
        imageUrl = this.canvasCourse?.courseImageUrl,
        name = this.canvasCourse?.courseName.orEmpty(),
        isBookmarked = this.isBookmarked,
        canEnroll = this.itemType == CollectionItemType.COURSE && !this.isEnrolledInCanvas.orDefault(true),
        bookmarkLoading = false,
        enrollLoading = false,
        isCompleted = this.completionPercentage == 100.0,
        type = this.itemType,
        chips = listOf(
            this.itemType.toUiChipState(),
            this.completionPercentage?.toProgressUiChipState(),
            this.toUnitsUiChipState(),
            this.toEstimatedDurationUiChipState()
        ).mapNotNull { it }
    )
}

fun CollectionItemType.toUiChipState(): LearnLearningLibraryCollectionItemChipState? {
    return when(this) {
        CollectionItemType.PAGE -> LearnLearningLibraryCollectionItemChipState(
            label = "Page",
            color = StatusChipColor.Sky,
            iconRes = R.drawable.text_snippet
        )
        CollectionItemType.FILE -> LearnLearningLibraryCollectionItemChipState(
            label = "File",
            color = StatusChipColor.Sky,
            iconRes = R.drawable.attach_file
        )
        CollectionItemType.EXTERNAL_URL -> LearnLearningLibraryCollectionItemChipState(
            label = "External link",
            color = StatusChipColor.Orange,
            iconRes = R.drawable.text_snippet
        )
        CollectionItemType.EXTERNAL_TOOL -> LearnLearningLibraryCollectionItemChipState(
            label = "External tool",
            color = StatusChipColor.Honey,
            iconRes = R.drawable.note_alt
        )
        CollectionItemType.COURSE -> LearnLearningLibraryCollectionItemChipState(
            label = "Course",
            color = StatusChipColor.Institution,
            iconRes = R.drawable.book_2
        )
        CollectionItemType.PROGRAM -> LearnLearningLibraryCollectionItemChipState(
            label = "Program",
            color = StatusChipColor.Violet,
            iconRes = R.drawable.book_5,
        )
        else -> null
    }
}

private fun Double.toProgressUiChipState(): LearnLearningLibraryCollectionItemChipState? {
    return if (this > 0 && this < 100) {
        LearnLearningLibraryCollectionItemChipState(
            label = "In progress",
            color = StatusChipColor.Grey,
            iconRes = R.drawable.trending_up
        )
    } else {
        null
    }
}

private fun LearningLibraryCollectionItem.toEstimatedDurationUiChipState(): LearnLearningLibraryCollectionItemChipState? {
    val estimatedMinutes = this.canvasCourse?.estimatedDurationMinutes
    return if (estimatedMinutes != null) {
        LearnLearningLibraryCollectionItemChipState(
            label = "$estimatedMinutes mins",
            color = StatusChipColor.Grey,
            iconRes = R.drawable.schedule
        )
    } else {
        null
    }
}

private fun LearningLibraryCollectionItem.toUnitsUiChipState(): LearnLearningLibraryCollectionItemChipState? {
    return if (this.itemType == CollectionItemType.COURSE && this.canvasCourse != null && this.canvasCourse?.moduleItemCount.orDefault() > 0) {
        LearnLearningLibraryCollectionItemChipState(
            label = "${this.canvasCourse?.moduleItemCount.orDefault().toFormattedString(0)} units",
            color = StatusChipColor.Grey,
            iconRes = R.drawable.courses_format_list_bulleted
        )
    } else {
        null
    }
}