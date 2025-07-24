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

import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.features.file.upload.FileUploadDialogParent
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.pandautils.utils.iconRes
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.FloatingRecordingView
import com.instructure.pandautils.views.RecordingMediaType
import dagger.hilt.android.EarlyEntryPoints
import java.util.UUID
import kotlin.math.roundToInt


@Composable
fun SpeedGraderCommentsSection(
    state: SpeedGraderCommentsUiState,
    modifier: Modifier = Modifier,
    gradingAnonymously: Boolean = false,
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

    val fileDialogShown = remember { mutableStateOf(false) }
    val dialogParent = remember {
        object : FileUploadDialogParent {
            override fun attachmentCallback(event: Int, attachment: FileSubmitObject?) {
                if (event == FileUploadDialogFragment.EVENT_DIALOG_CANCELED) {
                    Log.d("FileUploadDialog", "Dialog canceled")
                    fileDialogShown.value = false
                    actionHandler(SpeedGraderCommentsAction.FileUploadDialogClosed)
                } else if (event == FileUploadDialogFragment.EVENT_ON_UPLOAD_BEGIN) {
                    Log.d("FileUploadDialog", "Upload started")
                }
            }

            override fun selectedUriStringsCallback(filePaths: List<String>) {
                actionHandler(SpeedGraderCommentsAction.FilesSelected(filePaths))
                Log.d("FileUploadDialog", "Selected file paths: $filePaths")
            }

            override fun workInfoLiveDataCallback(
                uuid: UUID?,
                workInfoLiveData: LiveData<WorkInfo>
            ) {
                actionHandler(SpeedGraderCommentsAction.FileUploadStarted(workInfoLiveData))
                Log.d("FileUploadDialog", "WorkInfoLiveData callback with UUID: $uuid")

            }
        }
    }

    Box(
        modifier = modifier.background(colorResource(id = R.color.backgroundLightest))
    ) {
        Column {
            SpeedGraderCommentItems(
                comments = state.comments,
                modifier = Modifier.fillMaxSize(),
                gradingAnonymously = gradingAnonymously,
                onAttachmentClick = { attachmentRouter.openAttachment(activity, it) },
                actionHandler = actionHandler
            )
            HorizontalDivider(color = colorResource(id = R.color.backgroundMedium))
            SpeedGraderCommentCreator(
                commentText = state.commentText, actionHandler = actionHandler
            )
            if (state.showAttachmentTypeDialog) {
                AttachmentTypeSelectorDialog(actionHandler = actionHandler)
            }
        }
        if (state.showRecordFloatingView != null) {
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            // TODO Handle permissions for recording audio/video
            // Permission request is sent if not granted, but we should handle the result
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
                            Log.d("ASpeedGraderCommentSection", "offset: $offsetX, $offsetY")
                        }
                    }
            )
        }
        if (state.fileSelectorDialogData != null && !fileDialogShown.value) {
            val fragmentManager = LocalContext.current.getFragmentActivity().supportFragmentManager

            val bundle = FileUploadDialogFragment.createTeacherSubmissionCommentBundle(
                state.fileSelectorDialogData.courseId,
                state.fileSelectorDialogData.assignmentId,
                state.fileSelectorDialogData.userId,
                state.fileSelectorDialogData.attempt
            )

            FileUploadDialogFragment.newInstance(bundle, dialogParent = dialogParent).show(
                fragmentManager, FileUploadDialogFragment.TAG
            )
            fileDialogShown.value = true

        }
    }
}

@Composable
private fun AttachmentTypeSelectorDialog(
    actionHandler: (SpeedGraderCommentsAction) -> Unit = {}
) {
    Dialog(
        onDismissRequest = {
            actionHandler(SpeedGraderCommentsAction.AttachmentTypeSelectorDialogClosed)
        }, properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .wrapContentHeight()
                    .background(colorResource(id = R.color.backgroundLightest)),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    text = "Select Attachment Type",
                    color = colorResource(id = R.color.textDark),
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight(400),
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { actionHandler(SpeedGraderCommentsAction.RecordAudioClicked) }
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
                        text = "Record Audio",
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight(600),
                        color = colorResource(id = R.color.textDarkest)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { actionHandler(SpeedGraderCommentsAction.RecordVideoClicked) }
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
                        text = "Record Video",
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight(600),
                        color = colorResource(id = R.color.textDarkest)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        text = "Choose Files",
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight(600),
                        color = colorResource(id = R.color.textDarkest)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedGraderCommentItems(
    comments: List<SpeedGraderComment>,
    onAttachmentClick: (SpeedGraderCommentAttachment) -> Unit,
    modifier: Modifier = Modifier,
    actionHandler: (SpeedGraderCommentsAction) -> Unit = {},
    gradingAnonymously: Boolean = false
) {
//    val listState = rememberLazyListState()
//    LaunchedEffect(comments.size) {
//        listState.scrollToItem(comments.size)
//    }
    Column(
        modifier = modifier.padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        comments.forEach { comment ->
            if (comment.isOwnComment) {
                SpeedGraderOwnCommentItem(
                    comment = comment,
                    gradingAnonymously = gradingAnonymously,
                    onAttachmentClick = onAttachmentClick,
                    actionHandler = actionHandler,
                )
            } else {
                SpeedGraderUserCommentItem(
                    comment = comment,
                    gradingAnonymously = gradingAnonymously,
                    onAttachmentClick = onAttachmentClick
                )
            }
        }
    }
}

@Composable
fun SpeedGraderOwnCommentItem(
    comment: SpeedGraderComment,
    onAttachmentClick: (SpeedGraderCommentAttachment) -> Unit,
    modifier: Modifier = Modifier,
    actionHandler: (SpeedGraderCommentsAction) -> Unit = {},
    gradingAnonymously: Boolean = false,
) {
    Column(
        modifier = modifier
            .padding(8.dp)
    ) {
        Column(
            modifier = modifier
                .alpha(if (comment.isPending) 0.5f else 1f)
        ) {
            Text(
                text = DateHelper.getDateTimeString(
                    LocalContext.current, DateHelper.speedGraderDateStringToDate(comment.createdAt)
                ) ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End),
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
                        .padding(8.dp),
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
                gradingAnonymously = gradingAnonymously,
                isOwn = true,
                onSelect = onAttachmentClick
            )
            SpeedGraderMediaAttachmentComponent(
                mediaObject = comment.mediaObject,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.End),
                gradingAnonymously = gradingAnonymously,
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
            text = "This message could not be sent. Tap to try again.",
            fontSize = 14.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight(400),
            color = colorResource(id = R.color.textDanger)
        )
    }
}

@Composable
fun SpeedGraderAttachmentsComponent(
    attachments: List<SpeedGraderCommentAttachment>,
    modifier: Modifier = Modifier,
    gradingAnonymously: Boolean = false,
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
                        .height(64.dp)
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.backgroundMedium),
                            shape = RoundedCornerShape(size = 16.dp)
                        )
                        .fillMaxWidth(if (isOwn) 0.7f else 1f)
                        .clickable { onSelect(attachment) },

                    gradingAnonymously = gradingAnonymously,
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
    gradingAnonymously: Boolean = false,
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
                fontWeight = FontWeight(600),
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = attachment.size,
                color = colorResource(id = R.color.textDark),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight(400)
            )
        }
    }
}

@Composable
fun SpeedGraderMediaAttachmentComponent(
    mediaObject: SpeedGraderMediaObject?,
    modifier: Modifier = Modifier,
    gradingAnonymously: Boolean = false,
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
            fontWeight = FontWeight(600),
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SpeedGraderUserCommentItem(
    comment: SpeedGraderComment,
    onAttachmentClick: (SpeedGraderCommentAttachment) -> Unit,
    modifier: Modifier = Modifier,
    gradingAnonymously: Boolean = false
) {
    Row(modifier = modifier.padding(horizontal = 8.dp)) {
        GlideImage(
            model = comment.authorAvatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.backgroundLight))
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
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight(600),
                    color = colorResource(id = R.color.textDarkest)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = DateHelper.getDateTimeString(
                        LocalContext.current, DateHelper.speedGraderDateStringToDate(comment.createdAt)
                    ) ?: "",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = colorResource(id = R.color.textDark)
                )
            }
            if (comment.content.isNotEmpty()) {
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = colorResource(id = R.color.textDarkest)
                )
            }
            SpeedGraderAttachmentsComponent(
                attachments = comment.attachments,
                gradingAnonymously = gradingAnonymously,
                isOwn = false,
                onSelect = onAttachmentClick
            )
            SpeedGraderMediaAttachmentComponent(
                mediaObject = comment.mediaObject,
                gradingAnonymously = gradingAnonymously,
                isOwn = false,
                onAttachmentClick = onAttachmentClick
            )
        }
    }
}

@Composable
fun SpeedGraderCommentCreator(
    modifier: Modifier = Modifier,
    commentText: TextFieldValue = TextFieldValue(""),
    actionHandler: (SpeedGraderCommentsAction) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.backgroundMedium),
                shape = RoundedCornerShape(size = 16.dp)
            )
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.Transparent),
            label = { Text("Comment") },
            value = commentText,
            maxLines = 5,
            onValueChange = {
                actionHandler(SpeedGraderCommentsAction.CommentFieldChanged(it))
            },
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight(400),
                color = colorResource(id = R.color.textDarkest),
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedLabelColor = colorResource(R.color.textDark),
                unfocusedLabelColor = colorResource(R.color.textDark),
                disabledLabelColor = colorResource(R.color.textDark),
                errorLabelColor = colorResource(R.color.textDark),
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                .wrapContentHeight()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_message),
                contentDescription = "Comment Library",
                modifier = Modifier
                    .height(24.dp)
                    .clickable { /* Handle open comment library action */ },
                tint = colorResource(id = R.color.textDark)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_attachment),
                contentDescription = "Add Attachment",
                modifier = Modifier
                    .height(24.dp)
                    .clickable { actionHandler(SpeedGraderCommentsAction.AddAttachmentClicked) },
                tint = colorResource(id = R.color.textDark)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(painter = painterResource(id = R.drawable.ic_send_outlined),
                contentDescription = "Send Comment",
                modifier = Modifier
                    .height(24.dp)
                    .clickable { actionHandler(SpeedGraderCommentsAction.SendCommentClicked) }
                    .alpha(if (commentText.text.isEmpty()) 0.5f else 1f),
                tint = colorResource(id = R.color.messageBackground))
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
            createdAt = "Wed May 27 00:23:23 GMT+02:00 2025",
            attachments = listOf(
                SpeedGraderCommentAttachment(
                    displayName = "image_file_name.jpg", contentType = "image/jpeg", size = "1.2 MB"
                )
            )
        ),
        SpeedGraderComment(
            content = "Please review the feedback provided.",
            isOwnComment = true,
            createdAt = "Wed May 28 00:12:38 GMT+02:00 2025"
        ),
        SpeedGraderComment(
            content = "Good job overall. She has met the basic requirements. There are a few areas where she could improve clarity and depth of analysis.",
            isOwnComment = true,
            createdAt = "Wed May 29 00:43:38 GMT+02:00 2025",
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
            createdAt = "Wed May 29 00:43:38 GMT+02:00 2025",
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
            createdAt = "Wed May 31 00:43:38 GMT+02:00 2025",
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
    SpeedGraderCommentsSection(state = SpeedGraderCommentsUiState(
        comments = comments,
        commentText = TextFieldValue(""),
        isLoading = false,
        errorMessage = null,
        isEmpty = false,
    ), gradingAnonymously = false, actionHandler = {})
}

@Preview
@Composable
fun AttachmentTypeSelectorDialogPreview() {
    AttachmentTypeSelectorDialog(actionHandler = {})
}

