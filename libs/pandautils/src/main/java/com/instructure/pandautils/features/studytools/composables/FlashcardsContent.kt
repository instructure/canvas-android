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
package com.instructure.pandautils.features.studytools.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag as testTagProperty
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.instructure.instui.compose.container.Card
import com.instructure.instui.compose.container.Elevation
import com.instructure.instui.token.component.InstUISharedTokens
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.PagerIndicator
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.domain.models.studytools.Flashcard
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import com.instructure.instui.compose.text.Text as InstUIText
import kotlin.math.absoluteValue

/**
 * A swipeable deck of [flashcards]. Flip state is kept per card and, matching the reference
 * implementation (Horizon's `AiAssistFlashcardViewModel.onFlashcardClicked`), persists across
 * paging - swiping away from a flipped card and back still shows its answer side.
 *
 * Every reselect of the tool passes through [com.instructure.pandautils.features.studytools.StudyToolState.Generating]
 * first, which unmounts this composable, so the deck naturally starts unflipped at card one on
 * regenerate with no extra keying needed.
 */
@Composable
fun FlashcardsContent(
    flashcards: StudyToolResult.Flashcards,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { flashcards.cards.size })
    // Keyed on the deck's own content, not just gated by it: rememberSaveable's `inputs` control
    // when the init block re-runs, but the SaveableStateRegistry lookup itself is keyed by
    // position, not by `flashcards`. Without an explicit content-derived key, a flip list restored
    // after a process death can belong to a completely different deck (e.g. the panel was killed
    // mid-deck, reopened, and regenerated into a different-sized deck before this composable ever
    // mounted to consume the restored value) - which throws once flipped[page] indexes past its end.
    val flipped = rememberSaveable(
        flashcards,
        key = "studyToolsFlashcardsFlipped_${flashcards.hashCode()}",
        saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
    ) { List(flashcards.cards.size) { false }.toMutableStateList() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("studyToolsFlashcards")
    ) {
        HorizontalPager(
            state = pagerState,
            // A gap that survives mid-drag, not just at rest - without it, adjacent cards render
            // flush against each other for the entire swipe gesture and only look "spaced" once a
            // page settles and the shrink-peek padding below kicks back in.
            pageSpacing = InstUILayoutSizes.Spacing.SpaceMd.spaceMd,
            modifier = Modifier
                .fillMaxWidth()
                .height(FLASHCARD_HEIGHT)
                .padding(horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg)
                .testTag("studyToolsFlashcardsPager")
        ) { page ->
            FlashcardCard(
                card = flashcards.cards[page],
                // Defensive, not expected to trigger: flipped is content-keyed above, but this
                // keeps a hash collision or future refactor from turning into an index crash instead
                // of just showing an unflipped card.
                isFlipped = flipped.getOrElse(page) { false },
                onClick = { if (page < flipped.size) flipped[page] = !flipped[page] },
                modifier = Modifier
                    .fillMaxSize()
                    // Mirrors Horizon's actual "peek" mechanism: the active page sits flush,
                    // inactive pages shrink via vertical inset - there is no true horizontal
                    // side-peek of neighboring cards, in Horizon's reference implementation or
                    // here. Read inside layout, not composition, so a drag re-lays-out the visible
                    // pages instead of recomposing every one of them (and re-running their
                    // stringResource lookups) on every scroll frame.
                    .layout { measurable, constraints ->
                        val settleDistance = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                            .absoluteValue
                            .coerceIn(0f, 1f)
                        val inset = (INACTIVE_CARD_PADDING * settleDistance).roundToPx()
                        val placeable = measurable.measure(constraints.offset(vertical = -2 * inset))
                        layout(placeable.width, placeable.height + 2 * inset) {
                            placeable.place(0, inset)
                        }
                    }
            )
        }

        PagerIndicator(
            pagerState = pagerState,
            activeColor = InstUISemanticColors.Text.base(),
            inactiveColor = InstUISemanticColors.Stroke.muted(),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = InstUILayoutSizes.Spacing.SpaceMd.spaceMd)
                .testTag("studyToolsFlashcardsIndicator")
        )
    }
}

/**
 * A single flashcard, question side up until flipped. The flip mechanics (rotationY on a
 * [graphicsLayer], content swap at the halfway point, mirroring the back face so its text doesn't
 * read backwards) are ported directly from Horizon's `AiAssistFlashcard.kt` - the technique itself
 * has no dependency on Horizon's tokens.
 */
@Composable
private fun FlashcardCard(
    card: Flashcard,
    isFlipped: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flipAnimation by animateFloatAsState(
        targetValue = if (isFlipped) FLIPPED_ROTATION_DEGREES else 0f,
        animationSpec = tween(durationMillis = FLIP_ANIMATION_DURATION_MS),
        label = "FlashcardFlip"
    )
    val showingAnswer = flipAnimation >= FLIPPED_ROTATION_DEGREES / 2

    val questionLabel = stringResource(id = R.string.studyTools_flashcardQuestionLabel)
    val answerLabel = stringResource(id = R.string.studyTools_flashcardAnswerLabel)
    val tapToFlip = stringResource(id = R.string.studyTools_flashcardTapToFlipContentDescription)

    val label = if (showingAnswer) answerLabel else questionLabel
    val face = if (showingAnswer) card.answer else card.question

    Card(
        borderColor = InstUISemanticColors.Stroke.muted(),
        elevation = Elevation.Level1,
        modifier = modifier
            .graphicsLayer {
                rotationY = flipAnimation
                cameraDistance = CAMERA_DISTANCE
            }
            // Clipped (and made clickable) after the rotation, not before, so the clip rotates
            // rigidly together with the card's own border/shadow instead of staying a static
            // rectangle that slices off the anti-aliased edge of the perspective-transformed
            // border mid-flip.
            .clip(RoundedCornerShape(InstUISharedTokens.BorderRadius.Card.md))
            .clickable(onClick = onClick)
            // The back face is rendered mirrored by rotationY, so it is flipped back the other way
            // here or its text would read backwards.
            .conditional(showingAnswer) { scale(scaleX = -1f, scaleY = 1f) }
            // clearAndSetSemantics, not semantics: this card is one tappable surface with one
            // announcement, so the label/face/hint child texts must not remain separately
            // traversable nodes underneath it. clearAndSetSemantics replaces this node's entire
            // semantics configuration, so testTag must be set as a property inside this same block -
            // a separate Modifier.testTag() anywhere else on this chain gets wiped along with
            // everything else it clears.
            .clearAndSetSemantics {
                contentDescription = face
                stateDescription = label
                onClick(label = tapToFlip) { onClick(); true }
                testTagProperty = "studyToolsFlashcard"
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            InstUIText(
                text = label,
                style = InstUITextTokens.contentSmall,
                color = InstUISemanticColors.Text.muted()
            )

            Spacer(modifier = Modifier.weight(1f))

            InstUIText(
                text = face,
                style = InstUITextTokens.contentImportant,
                color = InstUISemanticColors.Text.base(),
                modifier = Modifier.verticalScroll(rememberScrollState())
            )

            Spacer(modifier = Modifier.weight(1f))

            InstUIText(
                text = tapToFlip,
                style = InstUITextTokens.contentSmall,
                color = InstUISemanticColors.Text.muted()
            )
        }
    }
}

private val FLASHCARD_HEIGHT = 280.dp
private val INACTIVE_CARD_PADDING = 32.dp
private const val FLIP_ANIMATION_DURATION_MS = 600
private const val FLIPPED_ROTATION_DEGREES = 180f

// Copied verbatim from Horizon's AiAssistFlashcard.kt; chosen there to match the reference's visual
// depth, not derived from a documented formula.
private const val CAMERA_DISTANCE = 20f

@Preview(showBackground = true)
@Composable
private fun FlashcardsContentPreview() {
    FlashcardsContent(
        flashcards = StudyToolResult.Flashcards(
            listOf(
                Flashcard(question = "What is the capital of France?", answer = "Paris"),
                Flashcard(question = "What is 2 + 2?", answer = "4")
            )
        )
    )
}
