/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.pandautils.features.pages.details

import com.instructure.canvasapi2.models.Page

data class PageDetailsUiState(
    val screenState: PageDetailsScreenState = PageDetailsScreenState.Loading,
    val toolbarTitle: String = "",
    val htmlContent: String = "",
    val baseUrl: String? = null,
    val editMenuVisible: Boolean = false,
    // Flips to true the moment a real Page has been fetched/resolved (mirrors the legacy
    // fragment's `setPageObject()` call), used to gate the `@PageView` "pageSet" prerequisite.
    // A total fetch failure never sets this, matching existing (unchanged) analytics behavior.
    val pageSet: Boolean = false,
    // The page's resolved slug, once known - null for locked pages and while the fetch is in
    // flight. This is the only Study-Tools-adjacent field left here: it is a page fact, not a
    // Study Tools fact. PageDetailsScreen forwards it to StudyToolsViewModel.onPageResolved once set.
    val pageSlug: String? = null,
    val actions: PageDetailsActions = PageDetailsActions(),
    // Navigation — one destination at a time.
    val navigation: PageDetailsNavigation? = null,
    val onNavigationHandled: () -> Unit = {},
    // Independent one-off effects (may co-occur), each consumed via its own callback.
    val showNoInternetDialog: Boolean = false,
    val onNoInternetDialogShown: () -> Unit = {},
    // One-shot signal telling the WebView to clear its navigation history after an edit-triggered
    // reload, so the back button can't return to stale (pre-edit) content.
    val clearWebViewHistory: Boolean = false,
    val onWebViewHistoryCleared: () -> Unit = {}
)

sealed class PageDetailsScreenState {
    data object Loading : PageDetailsScreenState()
    data object Content : PageDetailsScreenState()
}

/**
 * UI → ViewModel callbacks, wired to private ViewModel functions in the [PageDetailsUiState] the
 * ViewModel emits.
 */
data class PageDetailsActions(
    val onEditClick: () -> Unit = {},
    val onLtiButtonPressed: (String) -> Unit = {}
)

/**
 * One-off navigation destinations. The ViewModel exposes the current pending destination as the
 * [PageDetailsUiState.navigation] state field; the host (Fragment) consumes it and then calls
 * [PageDetailsUiState.onNavigationHandled] to clear it.
 */
sealed class PageDetailsNavigation {
    data class LtiScreen(val url: String) : PageDetailsNavigation()
    data class EditPageScreen(val page: Page) : PageDetailsNavigation()
}
