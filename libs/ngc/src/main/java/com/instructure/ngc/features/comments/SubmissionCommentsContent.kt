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
@file:OptIn(ExperimentalGlideComposeApi::class)

package com.instructure.ngc.features.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.indicator.Spinner
import com.instructure.instui.compose.input.TextInput
import com.instructure.instui.compose.list.Separator
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.audioLines
import com.instructure.instui.token.icon.circleArrowUp
import com.instructure.instui.token.icon.file
import com.instructure.instui.token.icon.lock
import com.instructure.instui.token.icon.paperclip
import com.instructure.instui.token.icon.triangleAlert
import com.instructure.instui.token.icon.video
import com.instructure.instui.token.primitives.InstUISizes
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.instui.token.component.InstUIText
import com.instructure.ngc.R
import com.instructure.ngc.features.comments.domain.SubmissionCommentAttachment
import com.instructure.ngc.features.comments.domain.SubmissionCommentItem
import com.instructure.ngc.features.comments.domain.SubmissionCommentMedia
import com.instructure.ngc.features.comments.domain.SubmissionCommentMediaType
import com.instructure.pandautils.compose.composables.UserAvatar

/**
 * Reusable, stateless comments UI: a scrollable list of comments with the comment input pinned at the
 * bottom. Own comments are shown as course-colored bubbles on the right; other users on the left with an
 * avatar (or a lock for anonymous authors). Consumed by both the assignment-details bottom sheet and the
 * submission screen's Comments pane.
 */
@Composable
fun SubmissionCommentsContent(
    state: SubmissionCommentsUiState,
    modifier: Modifier = Modifier,
    onOpenAttachment: (SubmissionCommentAttachment) -> Unit = {},
    onOpenMedia: (SubmissionCommentMedia) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Spinner(modifier = Modifier.testTag("submissionComments_loading"))
            }

            state.isEmpty -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.ngc_commentsEmpty),
                    style = InstUIText.content,
                    color = InstUISemanticColors.Text.muted(),
                    textAlign = TextAlign.Center,
                )
            }

            else -> {
                // De-duplicate by id: an optimistic pending comment and its just-completed copy can
                // momentarily share an id, and a LazyColumn requires unique keys (a duplicate crashes).
                val comments = remember(state.comments) { state.comments.distinctBy { it.id } }
                val listState = rememberLazyListState()
                // Keep the latest comment in view: jump (no animation) to the bottom whenever the
                // number of comments changes (initial load, a new/sent comment, etc.).
                LaunchedEffect(comments.size) {
                    if (comments.isNotEmpty()) {
                        listState.scrollToItem(comments.lastIndex)
                    }
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = InstUILayoutSizes.Spacing.SpaceMd.spaceMd,
                        vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd,
                    ),
                    verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                ) {
                    items(comments, key = { it.id }) { comment ->
                        if (comment.isOwnComment) {
                            OwnCommentItem(comment, state.actions.onRetryComment, onOpenAttachment, onOpenMedia)
                        } else {
                            OtherCommentItem(comment, onOpenAttachment, onOpenMedia)
                        }
                    }
                }
            }
        }

        Separator()

        CommentInputRow(
            commentText = state.commentText,
            onCommentChanged = state.actions.onCommentChanged,
            onAttachmentClicked = state.actions.onAddAttachment,
            onSendClicked = state.actions.onSendComment,
        )
    }
}

@Composable
private fun OwnCommentItem(
    comment: SubmissionCommentItem,
    onRetry: (SubmissionCommentItem) -> Unit,
    onOpenAttachment: (SubmissionCommentAttachment) -> Unit,
    onOpenMedia: (SubmissionCommentMedia) -> Unit,
) {
    val courseColor = LocalCourseColor.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.alpha(if (comment.isPending || comment.isFailed) 0.5f else 1f)) {
            Text(
                text = DateHelper.getDateTimeString(LocalContext.current, comment.createdAt).orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .testTag("ownCommentDate"),
                style = InstUIText.contentSmall,
                color = InstUISemanticColors.Text.muted(),
                textAlign = TextAlign.End,
            )
            Spacer(Modifier.height(InstUILayoutSizes.Spacing.Space2xs.space2xs))
            if (comment.content.isNotEmpty()) {
                Text(
                    text = comment.content,
                    modifier = Modifier
                        .padding(start = InstUISizes.size36)
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.End)
                        .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.md))
                        .background(courseColor)
                        .padding(InstUILayoutSizes.Spacing.SpaceSm.spaceSm)
                        .testTag("ownCommentText"),
                    style = InstUIText.content,
                    color = InstUISemanticColors.Text.onColor(),
                    textAlign = TextAlign.End,
                )
            }
            if (comment.isPending && comment.content.isEmpty()) {
                Spinner(
                    modifier = Modifier
                        .size(InstUISizes.size24)
                        .align(Alignment.End)
                )
            }
            AttachmentList(comment.attachments, isOwn = true, onOpenAttachment)
            MediaAttachment(comment.mediaObject, isOwn = true, onOpenMedia)
        }
        if (comment.isFailed) {
            Spacer(Modifier.height(InstUILayoutSizes.Spacing.Space2xs.space2xs))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .clickable { onRetry(comment) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = InstUIIcons.triangleAlert, color = IconColor.Error, size = IconSize.Small)
                Spacer(Modifier.width(InstUILayoutSizes.Spacing.Space2xs.space2xs))
                Text(
                    text = stringResource(R.string.ngc_commentSendError),
                    style = InstUIText.contentSmall,
                    color = InstUISemanticColors.Text.error(),
                )
            }
        }
    }
}

@Composable
private fun OtherCommentItem(
    comment: SubmissionCommentItem,
    onOpenAttachment: (SubmissionCommentAttachment) -> Unit,
    onOpenMedia: (SubmissionCommentMedia) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Unread indicator dot shown next to the avatar for comments the student hasn't read yet.
        Box(
            modifier = Modifier
                .padding(top = InstUILayoutSizes.Spacing.SpaceLg.spaceLg, end = InstUILayoutSizes.Spacing.Space2xs.space2xs)
                .size(7.dp)
        ) {
            if (comment.isUnread) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(LocalCourseColor.current)
                        .testTag("unreadCommentIndicator")
                )
            }
        }
        CommentAuthorAvatar(comment)
        Spacer(Modifier.width(InstUILayoutSizes.Spacing.SpaceMd.spaceMd))
        Column(modifier = Modifier.padding(top = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorName,
                    modifier = Modifier.testTag("commentAuthorName"),
                    style = InstUIText.contentImportant,
                    color = InstUISemanticColors.Text.base(),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = DateHelper.getDateTimeString(LocalContext.current, comment.createdAt).orEmpty(),
                    style = InstUIText.contentSmall,
                    color = InstUISemanticColors.Text.muted(),
                )
            }
            if (comment.content.isNotEmpty()) {
                Text(
                    text = comment.content,
                    modifier = Modifier.testTag("commentText"),
                    style = InstUIText.content,
                    color = InstUISemanticColors.Text.base(),
                )
            }
            AttachmentList(comment.attachments, isOwn = false, onOpenAttachment)
            MediaAttachment(comment.mediaObject, isOwn = false, onOpenMedia)
        }
    }
}

@Composable
private fun CommentAuthorAvatar(comment: SubmissionCommentItem) {
    if (comment.isAnonymous) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)
                .size(InstUISizes.size36)
                .clip(CircleShape)
                .border(InstUILayoutSizes.BorderWidth.Sm.sm, InstUISemanticColors.Stroke.muted(), CircleShape)
                .testTag("anonymousCommentAvatar")
        ) {
            Icon(imageVector = InstUIIcons.lock, color = IconColor.Muted, size = IconSize.Small)
        }
    } else {
        UserAvatar(
            imageUrl = comment.authorAvatarUrl,
            name = comment.authorName,
            modifier = Modifier
                .padding(top = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)
                .size(InstUISizes.size36)
        )
    }
}

@Composable
private fun AttachmentList(
    attachments: List<SubmissionCommentAttachment>,
    isOwn: Boolean,
    onOpenAttachment: (SubmissionCommentAttachment) -> Unit,
) {
    if (attachments.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start,
    ) {
        attachments.forEach { attachment ->
            AttachmentRow(
                attachment = attachment,
                modifier = Modifier
                    .padding(top = InstUILayoutSizes.Spacing.Space2xs.space2xs)
                    .attachmentWidth(isOwn)
                    .clickable { onOpenAttachment(attachment) }
            )
        }
    }
}

/**
 * Width for an attachment/media card: own (student) cards are narrower and end-aligned by the
 * parent; other (e.g. teacher) cards stay full width but get trailing end padding so the card is
 * visually distinguishable from the student's own, narrower cards.
 */
private fun Modifier.attachmentWidth(isOwn: Boolean): Modifier = if (isOwn) {
    fillMaxWidth(0.7f)
} else {
    fillMaxWidth().padding(end = InstUILayoutSizes.Spacing.SpaceXl.spaceXl)
}

@Composable
private fun AttachmentRow(
    attachment: SubmissionCommentAttachment,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(InstUISizes.size64)
            .border(InstUILayoutSizes.BorderWidth.Sm.sm, InstUISemanticColors.Stroke.muted(), RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.md))
            .padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(InstUISizes.size48)
                .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Interactive.base))
                .background(InstUISemanticColors.Background.muted())
        ) {
            val thumbnailUrl = attachment.thumbnailUrl
            if (!thumbnailUrl.isNullOrEmpty()) {
                GlideImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(InstUISizes.size32),
                )
            } else {
                Icon(imageVector = InstUIIcons.file, color = IconColor.Muted, size = IconSize.Medium)
            }
        }
        Spacer(Modifier.width(InstUILayoutSizes.Spacing.SpaceSm.spaceSm))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = attachment.displayName,
                style = InstUIText.contentSmall,
                color = InstUISemanticColors.Text.base(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (attachment.size.isNotEmpty()) {
                Text(
                    text = attachment.size,
                    style = InstUIText.legend,
                    color = InstUISemanticColors.Text.muted(),
                )
            }
        }
    }
}

@Composable
private fun MediaAttachment(
    media: SubmissionCommentMedia?,
    isOwn: Boolean,
    onOpenMedia: (SubmissionCommentMedia) -> Unit,
) {
    if (media == null) return
    val isVideo = media.mediaType == SubmissionCommentMediaType.VIDEO
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = InstUILayoutSizes.Spacing.Space2xs.space2xs),
        contentAlignment = if (isOwn) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .attachmentWidth(isOwn)
                .height(InstUISizes.size64)
                .border(InstUILayoutSizes.BorderWidth.Sm.sm, InstUISemanticColors.Stroke.muted(), RoundedCornerShape(InstUILayoutSizes.BorderRadius.Container.md))
                .clickable { onOpenMedia(media) }
                .padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(InstUISizes.size48)
                    .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Interactive.base))
                    .background(InstUISemanticColors.Background.muted())
                    .testTag("mediaAttachment")
            ) {
                Icon(
                    imageVector = if (isVideo) InstUIIcons.video else InstUIIcons.audioLines,
                    color = IconColor.Muted,
                    size = IconSize.Medium,
                )
            }
            Spacer(Modifier.width(InstUILayoutSizes.Spacing.SpaceSm.spaceSm))
            Text(
                text = stringResource(if (isVideo) R.string.ngc_videoComment else R.string.ngc_audioComment),
                style = InstUIText.contentImportant,
                color = InstUISemanticColors.Text.base(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CommentInputRow(
    commentText: String,
    onCommentChanged: (String) -> Unit,
    onAttachmentClicked: () -> Unit,
    onSendClicked: () -> Unit,
) {
    TextInput(
        value = commentText,
        onValueChange = onCommentChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(InstUILayoutSizes.Spacing.SpaceMd.spaceMd)
            .testTag("commentInput"),
        placeholder = stringResource(R.string.ngc_commentHint),
        singleLine = false,
        actionsContent = {
            Icon(
                imageVector = InstUIIcons.paperclip,
                color = IconColor.Base,
                size = IconSize.Medium,
                contentDescription = stringResource(R.string.ngc_addAttachment),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAttachmentClicked)
                    .padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
                    .testTag("addAttachmentButton"),
            )
            Spacer(Modifier.weight(1f))
            val sendEnabled = commentText.isNotBlank()
            Icon(
                imageVector = InstUIIcons.circleArrowUp,
                color = if (sendEnabled) IconColor.CourseColor else IconColor.Muted,
                size = IconSize.Large,
                contentDescription = stringResource(R.string.ngc_sendComment),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = sendEnabled, onClick = onSendClicked)
                    .padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
                    .testTag("sendCommentButton"),
            )
        },
    )
}
