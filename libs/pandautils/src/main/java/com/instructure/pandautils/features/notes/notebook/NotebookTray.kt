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
package com.instructure.pandautils.features.notes.notebook

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.features.notes.notebook.composables.NoteDetailsContent
import com.instructure.pandautils.features.notes.notebook.composables.NotesListContent
import kotlinx.coroutines.launch
import com.instructure.instui.compose.container.Tray

/**
 * The Notebook, hosted as a fully-expanded [Tray] (modal bottom sheet) composed directly from the
 * screen that owns [args] (today, [com.instructure.pandautils.features.pages.details.composables.PageDetailsScreen]) -
 * see the Notebook Tray plan §2.1 for why this replaced the old standalone Fragment.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookTray(
    args: NotebookTrayArgs,
    courseColor: Color,
    onDismiss: () -> Unit,
    onScrollToNoteRequested: (String) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // "Done" dismisses programmatically, so animate the sheet out first (canonical M3 idiom);
    // swipe-down / scrim tap / system-back already animate before calling onDismiss themselves.
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) onDismiss() }
    }

    // InstUITheme is required here: the host screen (e.g. PageDetailsScreen) runs under the legacy
    // CanvasTheme, while every composable inside the tray is InstUI. courseColor keeps LocalCourseColor
    // resolving for the few course-accented InstUI internals used by the list/detail content.
    InstUITheme(courseColor = courseColor) {
        Tray(
            onDismiss = onDismiss,
            sheetState = sheetState,
            // Figma (25808-32140 et al.): the tray's own background - including the drag-handle strip at
            // the top and, on the detail level, everything below the content too - is the muted grey
            // sharedTokens/background/mutedColor, not InstUITray's own white default. The list level
            // overrides its own body back to Background.base() in NotesListContent - see the comment
            // there for why that layering, rather than the reverse, is correct.
            containerColor = InstUISemanticColors.Background.muted(),
            // Cap below the status bar and fill the rest, exactly like SubmissionCommentsBottomSheet.
            // windowInsetsPadding(WindowInsets.statusBars) can't be called directly here: this whole
            // Tray(...) call's arguments are evaluated in the *caller's* composition (e.g. PageDetailsScreen,
            // or CourseBrowserFragment's small nested `searchBar` ComposeView), not inside the sheet's own
            // Dialog - so WindowInsets.statusBars would resolve against whichever View hosts the caller,
            // not the Dialog's own window. That's harmless from a fullscreen ComposeView host (same real
            // status bar height either way), but from a small/nested host like `searchBar` it reads a
            // stale/zero value, and the sheet ends up drawn on top of the status bar. composed{} defers the
            // WindowInsets.statusBars lookup to where this modifier actually attaches (the Surface inside
            // the Dialog), which is always the correct, dialog-scoped insets regardless of the caller.
            modifier = Modifier.testTag("notebookTray").composed { windowInsetsPadding(WindowInsets.statusBars) },
            // Also union in the display cutout (e.g. a camera cutout on the long edge in landscape) -
            // navigationBars alone doesn't cover it, so rotated content could render under the cutout.
            contentWindowInsets = {
                WindowInsets.navigationBars.union(WindowInsets.displayCutout)
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            },
            // Otherwise M3's default 640.dp cap centers the sheet with visible scrim padding on
            // both sides on any window wider than that (e.g. a phone in landscape) - this tray is
            // meant to be full-bleed at every width/orientation.
            sheetMaxWidth = Dp.Unspecified
        ) {
            // One destination only - this NavHost exists purely to scope the Hilt ViewModel to a single
            // tray opening and to feed it its arguments via SavedStateHandle; the Notebook's own level
            // navigation lives in NotebookViewModel's level stack, not in this NavHost. Same trick as
            // ToDoListScreen's filter-screen NavHost: without it, hiltViewModel() below would resolve to
            // the host Fragment's ViewModelStore and the tray would reuse a stale VM (and ignore
            // initialNoteId) on its next opening.
            //
            // Deliberately no BackHandler here: system/software back must always close the whole tray,
            // regardless of level - only the header's own back control (wired to onBackClick on the
            // detail level only) pops a level. The sheet's own default back handling already dismisses
            // it for us via onDismiss.
            NavHost(
                navController = rememberNavController(),
                startDestination = NotebookRoute.route(args),
                modifier = Modifier.fillMaxWidth().fillMaxHeight()
            ) {
                composable(route = NotebookRoute.PATTERN, arguments = NotebookRoute.arguments) {
                    val viewModel = hiltViewModel<NotebookViewModel>()
                    val uiState by viewModel.uiState.collectAsState()

                    // A note row's arrow was tapped. Either the ViewModel already routed to the note's
                    // underlying page/assignment/quiz (app-specific, via NotebookRouter) and all that's
                    // left here is closing the tray - exactly like tapping "Done" - or the note belongs
                    // to the page the tray is already sitting on top of, in which case there's nowhere
                    // to route to and scrollToNoteOnDismiss asks the host to scroll to it instead.
                    LaunchedEffect(uiState.closeTrayRequested) {
                        if (uiState.closeTrayRequested) {
                            uiState.scrollToNoteOnDismiss?.let { onScrollToNoteRequested(it) }
                            close()
                        }
                    }

                    NotebookContent(uiState = uiState, onDoneClick = close)
                }
            }
        }
    }
}

@Composable
private fun NotebookContent(uiState: NotebookUiState, onDoneClick: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = uiState.currentLevel,
        label = "notebookLevel",
        modifier = modifier,
        transitionSpec = {
            // Global -> Course -> Page -> NoteDetails is a fixed hierarchy (see NotebookLevel.depth()) -
            // drilling in (target deeper than where we came from) always pushes from the right, zooming
            // out always pushes from the left, regardless of which two levels are actually involved.
            val drillingIn = targetState.depth() > initialState.depth()
            if (drillingIn) {
                slideInHorizontally(tween()) { width -> width } togetherWith slideOutHorizontally(tween()) { width -> -width }
            } else {
                slideInHorizontally(tween()) { width -> -width } togetherWith slideOutHorizontally(tween()) { width -> width }
            }
        }
    ) { level ->
        when (level) {
            is NotebookLevel.ListLevel -> NotesListContent(uiState = uiState, onDoneClick = onDoneClick)
            is NotebookLevel.NoteDetails -> NoteDetailsContent(
                uiState = uiState,
                onBackClick = uiState.actions.onBackClick,
                onDoneClick = onDoneClick
            )
        }
    }
}

/** Position in the fixed Global > Course > Page > NoteDetails hierarchy - see [NotebookLevel]. */
private fun NotebookLevel.depth(): Int = when (this) {
    is NotebookLevel.GlobalNotes -> 0
    is NotebookLevel.CourseNotes -> 1
    is NotebookLevel.PageNotes -> 2
    is NotebookLevel.NoteDetails -> 3
}
