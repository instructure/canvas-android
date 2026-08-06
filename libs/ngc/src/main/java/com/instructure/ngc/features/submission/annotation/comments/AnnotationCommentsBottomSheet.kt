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
package com.instructure.ngc.features.submission.annotation.comments

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.container.Dialog
import com.instructure.instui.compose.container.DialogAction
import com.instructure.instui.compose.container.Tray
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.indicator.Spinner
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.input.TextInput
import com.instructure.instui.compose.list.ListItem
import com.instructure.instui.compose.list.ListItemTrailing
import com.instructure.instui.compose.list.Separator
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.circleArrowUp
import com.instructure.instui.token.icon.pencil
import com.instructure.instui.token.icon.trash2
import com.instructure.instui.token.icon.x
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.annotation.CommentThreadRequest
import com.instructure.ngc.features.submission.annotation.CommentThreadResult

// Minimum touch target for the per-comment edit/delete affordances. The icons themselves render at
// IconSize.Small, but the tappable area is padded out to the platform minimum — QA flagged the raw
// icon-sized hit boxes as too small to hit reliably.
private val CommentActionTouchTarget = 48.dp

/**
 * Comment-thread Tray opened from
 * [com.instructure.ngc.features.submission.annotation.PdfAnnotationSubmissionView.onShowAnnotationComments]
 * once an annotation already has at least one reply (see MBL-19937 follow-on plan, Phase B). Mirrors the
 * classic `AnnotationCommentListFragment` UX (reply list; add/edit/delete own replies) with InstUI
 * components instead of the legacy XML/RecyclerView + `RouteMatcher`/`EventBus` stack.
 *
 * Presented as a [Tray] layered OVER the still-composed submission pane rather than as its own
 * Navigation-Compose destination. Navigating would have disposed the submission destination — tearing
 * down `PdfAnnotationHostFragment` and with it the whole PSPDFKit surface — so every visit re-downloaded
 * the document and reset scroll/zoom to page 1, the `CommentThreadResult` patch landed on an instance
 * that was about to be replaced, and `flushAnnotations()` could report success while the previous
 * instance's writes were still in flight (its `pendingAnnotationWrites` set lives on the view). Keeping
 * the pane resident fixes all of those at once.
 *
 * [onDismiss] is handed the ViewModel's current, authoritative [CommentThreadResult] on every exit path —
 * scrim tap, swipe-down, system/predictive back (all routed through [Tray]'s own dismiss, so no separate
 * `BackHandler` is needed), and the ViewModel's one-shot `exitEvent` after the last reply is deleted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationCommentsBottomSheet(
    request: CommentThreadRequest,
    onDismiss: (CommentThreadResult) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnnotationCommentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Rebinds and reloads when the user opens a different annotation's thread without the Tray leaving
    // composition (mirrors SubmissionCommentsComponent's load-on-params-change idiom).
    LaunchedEffect(request) {
        viewModel.load(request)
    }

    LaunchedEffect(uiState.exitEvent) {
        if (uiState.exitEvent) {
            uiState.onExitEventConsumed()
            onDismiss(viewModel.buildResult())
        }
    }

    // Capped below the status bar so the sheet can never animate under it, and opened fully expanded so
    // the whole thread is laid out immediately — matching SubmissionCommentsBottomSheet.
    Tray(
        onDismiss = { onDismiss(viewModel.buildResult()) },
        modifier = modifier.windowInsetsPadding(WindowInsets.statusBars),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Heading(
                text = stringResource(R.string.comments),
                level = HeadingLevel.H3,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
            )

            when {
                uiState.loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Spinner()
                    }
                }
                uiState.loadError != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = uiState.loadError.orEmpty(), color = InstUISemanticColors.Text.error())
                            Spacer(Modifier.height(InstUILayoutSizes.Spacing.SpaceMd.spaceMd))
                            TextButton(
                                text = stringResource(R.string.ngc_submission_retry),
                                onClick = uiState.onRetry,
                            )
                        }
                    }
                }
                else -> {
                    AnnotationCommentsContent(uiState = uiState, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun AnnotationCommentsContent(
    uiState: AnnotationCommentsUiState,
    modifier: Modifier = Modifier,
) {
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    pendingDeleteId?.let { id ->
        DeleteCommentConfirmDialog(
            onConfirm = {
                pendingDeleteId = null
                uiState.onDeleteClick(id)
            },
            onDismiss = { pendingDeleteId = null },
        )
    }

    Column(modifier = modifier) {
        if (uiState.replies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.ngc_annotationComments_empty),
                    style = InstUIText.content,
                    color = InstUISemanticColors.Text.muted(),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(uiState.replies, key = { it.id }) { comment ->
                    ListItem(
                        title = comment.authorName,
                        subtext1 = comment.timestampText,
                        text = comment.body,
                        trailing = if (comment.canEdit || comment.canDelete) {
                            ListItemTrailing.Custom {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (comment.canEdit) {
                                        CommentActionIcon(
                                            imageVector = InstUIIcons.pencil,
                                            contentDescription = stringResource(R.string.a11y_annotationComments_editComment),
                                            onClick = { uiState.onEditClick(comment.id) },
                                        )
                                    }
                                    if (comment.canDelete) {
                                        CommentActionIcon(
                                            imageVector = InstUIIcons.trash2,
                                            contentDescription = stringResource(R.string.a11y_annotationComments_deleteComment),
                                            onClick = { pendingDeleteId = comment.id },
                                        )
                                    }
                                }
                            }
                        } else {
                            null
                        },
                    )
                    Separator()
                }
            }
        }

        if (uiState.errorMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = uiState.errorMessage,
                    style = InstUIText.contentSmall,
                    color = InstUISemanticColors.Text.error(),
                    modifier = Modifier.weight(1f),
                )
                CommentActionIcon(
                    imageVector = InstUIIcons.x,
                    contentDescription = stringResource(R.string.cancel),
                    onClick = uiState.onErrorMessageConsumed,
                )
            }
        }

        if (uiState.canComment) {
            val isEditing = uiState.editingReplyId != null
            Column(modifier = Modifier.fillMaxWidth().imePadding().padding(InstUILayoutSizes.Spacing.SpaceMd.spaceMd)) {
                if (isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.editComment),
                            style = InstUIText.contentSmall,
                            color = InstUISemanticColors.Text.muted(),
                        )
                        TextButton(
                            text = stringResource(R.string.cancel),
                            onClick = uiState.onCancelEditClick,
                        )
                    }
                    Spacer(Modifier.height(InstUILayoutSizes.Spacing.SpaceXs.spaceXs))
                }
                // Deliberately the same construction as the submission-comments composer
                // (SubmissionCommentsContent.CommentInput): a TextInput whose actionsContent holds a
                // large course-coloured circleArrowUp, muted while there is nothing to send. Using
                // InstUI's ChatInput here instead gave the annotation thread a visibly different send
                // button from the rest of NGC's commenting UI. No paperclip: annotation replies are
                // plain text, with no attachment endpoint behind them.
                val sendEnabled = uiState.inputText.isNotBlank() && !uiState.sending
                TextInput(
                    value = uiState.inputText,
                    onValueChange = uiState.onInputTextChanged,
                    enabled = !uiState.sending,
                    placeholder = stringResource(R.string.addComment),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    actionsContent = {
                        Spacer(Modifier.weight(1f))
                        Icon(
                            imageVector = InstUIIcons.circleArrowUp,
                            color = if (sendEnabled) IconColor.CourseColor else IconColor.Muted,
                            size = IconSize.Large,
                            contentDescription = stringResource(R.string.ngc_sendComment),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(enabled = sendEnabled, onClick = uiState.onSendClick)
                                .padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun DeleteCommentConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        title = stringResource(R.string.deleteComment),
        description = stringResource(R.string.ngc_annotationComments_deleteConfirmMessage),
        onDismissRequest = onDismiss,
        actions = listOf(
            DialogAction(label = stringResource(R.string.cancel), onClick = onDismiss),
            DialogAction(
                label = stringResource(R.string.delete),
                onClick = onConfirm,
                contentColor = InstUISemanticColors.Text.error(),
            ),
        ),
    )
}

/** Small muted icon in a full 48dp circular touch target, so the hit box isn't the icon's own size. */
@Composable
private fun CommentActionIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(CommentActionTouchTarget)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            size = IconSize.Small,
            color = IconColor.Muted,
            contentDescription = contentDescription,
        )
    }
}

// region Previews

@Preview(name = "AnnotationComments — Light", showBackground = true)
@Preview(name = "AnnotationComments — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AnnotationCommentsContentPreview() {
    InstUITheme {
        AnnotationCommentsContent(
            uiState = AnnotationCommentsUiState(
                loading = false,
                replies = listOf(
                    AnnotationCommentUiState(
                        id = "1",
                        authorName = "Jane Doe",
                        timestampText = "Jun 19 at 10:30 AM",
                        body = "Can you clarify this part?",
                        canEdit = false,
                        canDelete = false,
                    ),
                    AnnotationCommentUiState(
                        id = "2",
                        authorName = "You",
                        timestampText = "Jun 19 at 10:32 AM",
                        body = "Sure, I meant the second paragraph.",
                        canEdit = true,
                        canDelete = true,
                    ),
                ),
            ),
        )
    }
}

@Preview(name = "AnnotationComments — Empty", showBackground = true)
@Composable
private fun AnnotationCommentsEmptyPreview() {
    InstUITheme {
        AnnotationCommentsContent(uiState = AnnotationCommentsUiState(loading = false))
    }
}

// endregion
