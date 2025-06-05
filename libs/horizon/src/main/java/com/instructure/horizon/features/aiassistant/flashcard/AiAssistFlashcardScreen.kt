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
package com.instructure.horizon.features.aiassistant.flashcard

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistPagination
import com.instructure.horizon.features.aiassistant.common.composable.AiAssistScaffold
import com.instructure.horizon.features.aiassistant.flashcard.composable.AiAssistFlashcard
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.molecules.Spinner

@Composable
fun AiAssistFlashcardScreen(
    navController: NavHostController,
    state: AiAssistFlashcardUiState,
    onDismiss: () -> Unit
) {
    AiAssistScaffold(
        navController,
        onDismiss
    ) { modifier ->
        if (state.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {
                Spinner(color = HorizonColors.Surface.cardPrimary())
            }
        } else {
            val pagerState = rememberPagerState { state.flashcardList.size }

            LaunchedEffect(state.currentCardIndex) {
                pagerState.animateScrollToPage(state.currentCardIndex)
            }

            LaunchedEffect(pagerState.currentPage) {
                snapshotFlow { pagerState.currentPage }.collect {
                    state.updateCurrentCardIndex(it)
                }
            }

            Column {
                HorizontalPager(
                    pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    val flashcardState = state.flashcardList[pageIndex]
                    val paddingAnimation by animateDpAsState(
                        if (pageIndex == state.currentCardIndex) 32.dp else 64.dp,
                        label = "FlashcardPagerAnimation",
                    )

                    AiAssistFlashcard(
                        question = flashcardState.question,
                        answer = flashcardState.answer,
                        isFlippedToAnswer = flashcardState.isFlippedToAnswer,
                        onClick = {
                            state.onFlashcardClicked(flashcardState)
                        },
                        modifier
                            .padding(vertical = paddingAnimation)
                    )
                }

                AiAssistPagination(
                    currentPage = state.currentCardIndex + 1,
                    onNextPage = { state.updateCurrentCardIndex(state.currentCardIndex + 1) },
                    onPreviousPage = { state.updateCurrentCardIndex(state.currentCardIndex - 1) },
                    totalPages = state.flashcardList.size,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
@Preview
private fun AiAssistFlashCardScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current

    val state = AiAssistFlashcardUiState(
        isLoading = true,
        flashcardList = emptyList(),
        onFlashcardClicked = {},
        updateCurrentCardIndex = {}
    )

    AiAssistFlashcardScreen(
        navController = NavHostController(ContextKeeper.appContext),
        state = state,
        onDismiss = {}
    )
}

@Composable
@Preview
private fun AiAssistFlashCardScreenPreview() {
    ContextKeeper.appContext = LocalContext.current

    val state = AiAssistFlashcardUiState(
        isLoading = false,
        flashcardList = listOf(
            FlashcardState("What is the capital of France?", "Paris", false),
            FlashcardState("What is the capital of Germany?", "Berlin", false)
        ),
        onFlashcardClicked = {},
        updateCurrentCardIndex = {}
    )

    AiAssistFlashcardScreen(
        navController = NavHostController(ContextKeeper.appContext),
        state = state,
        onDismiss = {}
    )
}