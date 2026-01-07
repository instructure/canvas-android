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
@file:OptIn(ExperimentalGlideComposeApi::class)

package com.instructure.pandautils.features.speedgrader.grade.comments

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.speedgrader.grade.comments.commentlibrary.SpeedGraderCommentLibraryScreen
import com.instructure.pandautils.features.speedgrader.grade.comments.composables.SpeedGraderCommentInput
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.pandautils.utils.iconRes
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.FloatingRecordingView
import com.instructure.pandautils.views.RecordingMediaType
import dagger.hilt.android.EarlyEntryPoints
import java.util.Date
import kotlin.math.roundToInt


@Composable
fun SpeedGraderCommentsSection(
    state: SpeedGraderCommentsUiState,
    modifier: Modifier = Modifier,
    actionHandler: (SpeedGraderCommentsAction) -> Unit = {},
) {
    val activity = LocalContext.current.getFragmentActivity()
    val context = LocalContext.current.applicationContext
    val attachmentRouter: SpeedGraderCommentsAttachmentRouter by lazy {
        EarlyEntryPoints.get(
            context,
            SpeedGraderCommentsAttachmentRouterEntryPoint::class.java
        ).speedGraderCommentsAttachmentRouter()
    }

    var showCommentLibrary by rememberSaveable { mutableStateOf(false) }
    if (showCommentLibrary) {
        SpeedGraderCommentLibraryScreen(
            onDismissRequest = {
                showCommentLibrary = false
                actionHandler(SpeedGraderCommentsAction.CommentFieldChanged(it))
            },
            initialCommentValue = state.commentText,
            onSendCommentClicked = {
                showCommentLibrary = false
                actionHandler(SpeedGraderCommentsAction.CommentFieldChanged(it, false))
                actionHandler(SpeedGraderCommentsAction.SendCommentClicked)
            }
        )
    }

    Box(
        modifier = modifier.background(colorResource(id = R.color.backgroundLightest))
    ) {
        Column {
            SpeedGraderCommentItems(
                comments = state.comments,
                modifier = Modifier.fillMaxSize(),
                onAttachmentClick = { attachmentRouter.openAttachment(activity, it) },
                actionHandler = actionHandler
            )
            CanvasDivider()
            SpeedGraderCommentInput(
                commentText = state.commentText,
                onCommentFieldChanged = {
                    actionHandler(
                        SpeedGraderCommentsAction.CommentFieldChanged(
                            it
                        )
                    )
                },
                onCommentLibraryClicked = { showCommentLibrary = true },
                onAttachmentClicked = { actionHandler(SpeedGraderCommentsAction.AddAttachmentClicked) },
                sendCommentClicked = { actionHandler(SpeedGraderCommentsAction.SendCommentClicked) },
            )
            if (state.showAttachmentTypeDialog) {
                AttachmentTypeSelectorDialog(actionHandler = actionHandler)
            }
        }
        if (state.showRecordFloatingView != null) {
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            AndroidView(
                factory = { context ->
                    FloatingRecordingView(context).apply {
                        setContentType(state.showRecordFloatingView)
                        setVisible()
                        if (state.showRecordFloatingView == RecordingMediaType.Video) {
                            startVideoView()
                        }
                        stoppedCallback = {
                            actionHandler(SpeedGraderCommentsAction.AttachmentRecordDialogClosed)
                        }
                        recordingCallback = { mediaFile ->
                            mediaFile?.let {
                                actionHandler(
                                    SpeedGraderCommentsAction.MediaRecorded(
                                        mediaFile
                                    )
                                )
                            }
                        }
                        replayCallback = { mediaFile ->
                            mediaFile?.let {
                                attachmentRouter.openAttachment(
                                    activity, SpeedGraderCommentAttachment(
                                        contentType = "video",
                                        url = mediaFile.absolutePath
                                    )
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()

                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
            )
        }
        if (state.fileSelectorDialogData != null) {
            val fragmentManager = LocalContext.current.getFragmentActivity().supportFragmentManager

            // Check if dialog is already showing to prevent duplicates
            val existingDialog = fragmentManager.findFragmentByTag(FileUploadDialogFragment.TAG)
            if (existingDialog == null) {
                val bundle = FileUploadDialogFragment.createTeacherSubmissionCommentBundle(
                    state.fileSelectorDialogData.courseId,
                    state.fileSelectorDialogData.assignmentId,
                    state.fileSelectorDialogData.userId,
                    state.fileSelectorDialogData.attempt
                )

                FileUploadDialogFragment.newInstance(bundle).show(
                    fragmentManager, FileUploadDialogFragment.TAG
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentTypeSelectorDialog(
    actionHandler: (SpeedGraderCommentsAction) -> Unit = {}
) {
    ModalBottomSheet(
        dragHandle = null,
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colorResource(R.color.backgroundLightestElevated),
        onDismissRequest = {
            actionHandler(SpeedGraderCommentsAction.AttachmentTypeSelectorDialogClosed)
        }
    ) {

        val audioPermissionRequest =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { result ->
                // Not working, needs to fixed in MBL-19070
                if (result) {
                    actionHandler(SpeedGraderCommentsAction.RecordAudioClicked)
                }
            }
        val videoPermissionRequest =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions()) { results ->
                // Not working, needs to fixed in MBL-19070
                if (results.all { it.value }) {
                    actionHandler(SpeedGraderCommentsAction.RecordVideoClicked)
                }
            }
        val context = LocalContext.current
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            text = stringResource(R.string.select_attachment_type),
            color = colorResource(id = R.color.textDark),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    this.role = Role.Button
                }
                .clickable {
                    if (isPermissionGranted(context, PermissionUtils.RECORD_AUDIO)) {
                        actionHandler(SpeedGraderCommentsAction.RecordAudioClicked)
                    } else {
                        audioPermissionRequest.launch(PermissionUtils.RECORD_AUDIO)
                    }
                }
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mic),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colorResource(id = R.color.textDark)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = stringResource(R.string.recordAudio),
                fontSize = 16.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.textDarkest)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    this.role = Role.Button
                }
                .clickable {
                    if (isPermissionGranted(context, PermissionUtils.CAMERA) &&
                        isPermissionGranted(context, PermissionUtils.RECORD_AUDIO)
                    ) {
                        actionHandler(SpeedGraderCommentsAction.RecordVideoClicked)
                    } else {
                        videoPermissionRequest.launch(
                            arrayOf(
                                PermissionUtils.CAMERA,
                                PermissionUtils.RECORD_AUDIO
                            )
                        )
                    }
                }
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_video),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colorResource(id = R.color.textDark)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = stringResource(R.string.recordVideo),
                fontSize = 16.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.textDarkest)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    this.role = Role.Button
                }
                .clickable { actionHandler(SpeedGraderCommentsAction.ChooseFilesClicked) }
                .padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_blank_doc),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colorResource(id = R.color.textDark)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = stringResource(R.string.choose_files),
                fontSize = 16.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = R.color.textDarkest)
            )
        }
    }
}

private fun isPermissionGranted(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun SpeedGraderCommentItems(
    comments: List<SpeedGraderComment>,
    onAttachmentClick: (SpeedGraderCommentAttachment) -> Unit,
    modifier: Modifier = Modifier,
    actionHandler: (SpeedGraderCommentsAction) -> Unit = {}
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        comments.forEach { comment ->
            if (comment.isOwnComment) {
                SpeedGraderOwnCommentItem(
                    comment = comment,
                    onAttachmentClick = onAttachmentClick,
                    actionHandler = actionHandler,
                )
            } else {
                SpeedGraderUserCommentItem(
                    comment = comment,
                    onAttachmentClick = onAttachmentClick
                )
            }
        }
    }
}

val CommentIdKey = SemanticsPropertyKey<String>("commentId")
private var SemanticsPropertyReceiver.commentId by CommentIdKey
@Composable
fun SpeedGraderOwnCommentItem(
    comment: SpeedGraderComment,
    onAttachmentClick: (SpeedGraderCommentAttachment) -> Unit,
    modifier: Modifier = Modifier,
    actionHandler: (SpeedGraderCommentsAction) -> Unit = {},
) {
    Column(
        modifier = modifier
            .padding(8.dp)
    ) {
        Column(
            modifier = modifier
                .alpha(if (comment.isPending || comment.isFailed) 0.5f else 1f)
        ) {
            Text(
                text = DateHelper.getDateTimeString(
                    LocalContext.current, comment.createdAt
                ).orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .testTag("ownCommentCreatedAtDate"),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colorResource(id = R.color.textDark),
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (comment.content.isNotEmpty()) {
                Text(
                    text = comment.content,
                    modifier = Modifier
                        .padding(start = 36.dp)
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.End)
                        .background(
                            color = LocalCourseColor.current,
                            shape = RoundedCornerShape(size = 16.dp)
                        )
                        .padding(8.dp)
                        .testTag("ownCommentText")
                        .semantics { commentId = comment.id },
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = colorResource(id = R.color.textLightest),
                    textAlign = TextAlign.Right
                )
            }
            if (comment.isPending && comment.content.isEmpty()) {
                CircularProgressIndicator(
                    color = LocalCourseColor.current,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.End)
                )
            }
            SpeedGraderAttachmentsComponent(
                attachments = comment.attachments,
                isOwn = true,
                onSelect = onAttachmentClick
            )
            SpeedGraderMediaAttachmentComponent(
                mediaObject = comment.mediaObject,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.End),
                isOwn = true,
                onAttachmentClick = onAttachmentClick
            )
        }
        if (comment.isFailed) {
            Spacer(modifier = Modifier.height(4.dp))
            SpeedGraderCommentErrorComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .clickable { actionHandler(SpeedGraderCommentsAction.RetryCommentUpload(comment)) }
            )
        }
    }
}

@Composable
fun SpeedGraderCommentErrorComponent(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_warning_red),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = colorResource(id = R.color.textDanger)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = stringResource(R.string.errorSendingMessage),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = colorResource(id = R.color.textDanger)
        )
    }
}

@Composable
fun SpeedGraderAttachmentsComponent(
    attachments: List<SpeedGraderCommentAttachment>,
    modifier: Modifier = Modifier,
    onSelect: (SpeedGraderCommentAttachment) -> Unit = {},
    isOwn: Boolean = false
) {
    if (attachments.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
        ) {
            attachments.forEach { attachment ->
                SpeedGraderAttachmentComponent(
                    attachment = attachment,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .requiredHeight(64.dp)
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.backgroundMedium),
                            shape = RoundedCornerShape(size = 16.dp)
                        )
                        .fillMaxWidth(if (isOwn) 0.7f else 1f)
                        .clickable { onSelect(attachment) }
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SpeedGraderAttachmentComponent(
    attachment: SpeedGraderCommentAttachment,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth(0.6f)
            .height(64.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.backgroundMedium),
                shape = RoundedCornerShape(size = 16.dp)
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .padding(start = 4.dp)
                .background(
                    colorResource(id = R.color.backgroundLight),
                    shape = RoundedCornerShape(size = 12.dp)
                )
        ) {
            if (attachment.thumbnailUrl != null) {
                GlideImage(
                    model = attachment.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .background(colorResource(id = R.color.backgroundLight))
                )
            } else {
                Icon(
                    painter = painterResource(id = attachment.iconRes),
                    contentDescription = null,
                    tint = colorResource(id = R.color.textDark),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                attachment.displayName,
                color = colorResource(id = R.color.textDarkest),
                fontSize = 14.sp,
                lineHeight = 19.sp,
                maxLines = 2,
                fontWeight = FontWeight.SemiBold,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = attachment.size,
                color = colorResource(id = R.color.textDark),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun SpeedGraderMediaAttachmentComponent(
    mediaObject: SpeedGraderMediaObject?,
    modifier: Modifier = Modifier,
    isOwn: Boolean,
    onAttachmentClick: (SpeedGraderCommentAttachment) -> Unit,
) {
    if (mediaObject == null) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
        modifier = modifier
            .height(64.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.backgroundMedium),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .clickable {
                onAttachmentClick(
                    SpeedGraderCommentAttachment().copy(
                        contentType = mediaObject.contentType.orEmpty(),
                        title = mediaObject.title.orEmpty(),
                        url = mediaObject.mediaDownloadUrl.orEmpty()
                    )
                )
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .padding(start = 4.dp)
                .background(
                    colorResource(id = R.color.backgroundLight),
                    shape = RoundedCornerShape(size = 12.dp)
                )
        ) {
            Icon(
                painter = painterResource(if (mediaObject.mediaType == MediaType.VIDEO) R.drawable.ic_video else R.drawable.ic_audio),
                contentDescription = null,
                tint = colorResource(id = R.color.textDark),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(
                if (mediaObject.mediaType == MediaType.VIDEO) R.string.mediaUploadVideo else R.string.mediaUploadAudio
            ),
            color = colorResource(id = R.color.textDarkest),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            maxLines = 2,
            fontWeight = FontWeight.SemiBold,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SpeedGraderUserCommentItem(
    comment: SpeedGraderComment,
    onAttachmentClick: (SpeedGraderCommentAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(horizontal = 8.dp)) {
        UserAvatar(
            imageUrl = comment.authorAvatarUrl,
            name = comment.authorName,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(36.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.authorName,
                    modifier = Modifier.testTag("commentAuthorName"),
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = R.color.textDarkest)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = DateHelper.getDateTimeString(
                        LocalContext.current, comment.createdAt
                    ).orEmpty(),
                    modifier = Modifier.testTag("commentCreatedAtDate"),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = colorResource(id = R.color.textDark)
                )
            }
            if (comment.content.isNotEmpty()) {
                Text(
                    text = comment.content,
                    modifier = Modifier
                        .testTag("commentText")
                        .semantics { commentId = comment.id },
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = colorResource(id = R.color.textDarkest)
                )
            }
            SpeedGraderAttachmentsComponent(
                attachments = comment.attachments,
                isOwn = false,
                onSelect = onAttachmentClick
            )
            SpeedGraderMediaAttachmentComponent(
                mediaObject = comment.mediaObject,
                isOwn = false,
                onAttachmentClick = onAttachmentClick
            )
        }
    }
}

@Preview
@Composable
fun SpeedGraderCommentSectionPreview() {
    val comments = listOf(
        SpeedGraderComment(
            content = "This is a great submission!",
            authorName = "Student A",
            createdAt = Date(),
            attachments = listOf(
                SpeedGraderCommentAttachment(
                    displayName = "image_file_name.jpg", contentType = "image/jpeg", size = "1.2 MB"
                )
            )
        ),
        SpeedGraderComment(
            content = "Please review the feedback provided.",
            isOwnComment = true,
            createdAt = Date()
        ),
        SpeedGraderComment(
            content = "Good job overall. She has met the basic requirements. There are a few areas where she could improve clarity and depth of analysis.",
            isOwnComment = true,
            createdAt = Date(),
            attachments = listOf(
                SpeedGraderCommentAttachment(
                    displayName = "important_document.pdf",
                    contentType = "application/pdf",
                    size = "500 KB"
                )
            )
        ),
        SpeedGraderComment(
            content = "",
            isOwnComment = true,
            createdAt = Date(),
            mediaObject = SpeedGraderMediaObject(
                id = "media123",
                mediaDownloadUrl = "https://example.com/media.mp4",
                title = "Recorded Video",
                mediaType = MediaType.VIDEO,
                thumbnailUrl = "https://example.com/thumbnail.jpg",
                contentType = "audio/amr"
            )
        ),
        SpeedGraderComment(
            content = "",
            authorName = "Student A",
            isOwnComment = false,
            createdAt = Date(),
            mediaObject = SpeedGraderMediaObject(
                id = "media2",
                mediaDownloadUrl = "https://example.com/media.amr",
                title = "Recorded Video",
                mediaType = MediaType.AUDIO,
                thumbnailUrl = "https://example.com/thumbnail.jpg",
                contentType = "audio/amr"
            )
        )
    )
    SpeedGraderCommentsSection(
        state = SpeedGraderCommentsUiState(
            comments = comments,
            commentText = "",
            isLoading = false,
            errorMessage = null,
            isEmpty = false,
        ),
        actionHandler = {}
    )
}

@Preview
@Composable
fun AttachmentTypeSelectorDialogPreview() {
    AttachmentTypeSelectorDialog(actionHandler = {})
}
