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
package com.instructure.ngc.features.comments

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.instui.compose.container.Tray
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.list.Separator
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.file
import com.instructure.instui.token.icon.mic
import com.instructure.instui.token.icon.video
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.pandautils.features.file.upload.FileUploadDialogFragment
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.FloatingRecordingView
import com.instructure.pandautils.views.RecordingMediaType
import java.io.File
import kotlin.math.roundToInt

/**
 * Stateful, reusable submission-comments component. Owns its [SubmissionCommentsViewModel] and loads its
 * own data for the given student/assignment/attempt, so it can be dropped into the assignment-details
 * bottom sheet or the submission screen's Comments pane with just ids and an attachment-open callback.
 */
@Composable
fun SubmissionCommentsComponent(
    courseId: Long,
    assignmentId: Long,
    studentId: Long,
    attemptId: Long?,
    modifier: Modifier = Modifier,
    // File attachments are opened by content type (image/pdf/doc/etc.), audio/video use the media player.
    onOpenFile: (url: String, contentType: String, fileName: String) -> Unit = { _, _, _ -> },
    onOpenMedia: (url: String, contentType: String, fileName: String, thumbnailUrl: String?) -> Unit = { _, _, _, _ -> },
    viewModel: SubmissionCommentsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(courseId, assignmentId, studentId, attemptId) {
        viewModel.load(courseId, assignmentId, studentId, attemptId)
    }

    Box(modifier = modifier.fillMaxSize()) {
        SubmissionCommentsContent(
            state = state,
            modifier = Modifier.fillMaxSize(),
            onOpenAttachment = { onOpenFile(it.url, it.contentType, it.displayName) },
            onOpenMedia = {
                onOpenMedia(
                    it.mediaDownloadUrl.orEmpty(),
                    it.contentType.orEmpty(),
                    it.title.orEmpty(),
                    it.thumbnailUrl
                )
            },
        )

        if (state.showAttachmentTypeDialog) {
            AttachmentTypeTray(actions = state.actions)
        }

        if (state.showRecordFloatingView != null) {
            RecordingOverlay(
                mediaType = state.showRecordFloatingView!!,
                onDismiss = state.actions.onRecordDialogDismiss,
                onMediaRecorded = state.actions.onMediaRecorded,
            )
        }

        state.fileSelectorDialogData?.let { data ->
            val fragmentManager = LocalContext.current.getFragmentActivity().supportFragmentManager
            if (fragmentManager.findFragmentByTag(FileUploadDialogFragment.TAG) == null) {
                val bundle = FileUploadDialogFragment.createTeacherSubmissionCommentBundle(
                    data.courseId,
                    data.assignmentId,
                    data.userId,
                    data.attempt
                )
                FileUploadDialogFragment.newInstance(bundle).show(fragmentManager, FileUploadDialogFragment.TAG)
            }
        }
    }
}

@Composable
private fun RecordingOverlay(
    mediaType: RecordingMediaType,
    onDismiss: () -> Unit,
    onMediaRecorded: (File) -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                FloatingRecordingView(context).apply {
                    setContentType(mediaType)
                    setVisible()
                    if (mediaType == RecordingMediaType.Video) startVideoView()
                    stoppedCallback = { onDismiss() }
                    recordingCallback = { mediaFile ->
                        mediaFile?.let(onMediaRecorded)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentTypeTray(
    actions: SubmissionCommentsActions,
) {
    val context = LocalContext.current
    val audioPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) actions.onRecordAudio() }
    val videoPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results -> if (results.all { it.value }) actions.onRecordVideo() }

    Tray(onDismiss = { actions.onAttachmentTypeDialogDismiss() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        ) {
            Text(
                text = stringResource(R.string.ngc_selectAttachmentType),
                style = InstUIText.contentSmall,
                color = InstUISemanticColors.Text.muted(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = InstUILayoutSizes.Spacing.SpaceMd.spaceMd, vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
            )
            Separator()
            AttachmentTypeRow(
                icon = InstUIIcons.mic,
                label = stringResource(R.string.ngc_recordAudio),
                onClick = {
                    if (isPermissionGranted(context, PermissionUtils.RECORD_AUDIO)) {
                        actions.onRecordAudio()
                    } else {
                        audioPermissionRequest.launch(PermissionUtils.RECORD_AUDIO)
                    }
                },
            )
            AttachmentTypeRow(
                icon = InstUIIcons.video,
                label = stringResource(R.string.ngc_recordVideo),
                onClick = {
                    if (isPermissionGranted(context, PermissionUtils.CAMERA) &&
                        isPermissionGranted(context, PermissionUtils.RECORD_AUDIO)
                    ) {
                        actions.onRecordVideo()
                    } else {
                        videoPermissionRequest.launch(arrayOf(PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO))
                    }
                },
            )
            AttachmentTypeRow(
                icon = InstUIIcons.file,
                label = stringResource(R.string.ngc_chooseFiles),
                onClick = { actions.onChooseFiles() },
            )
        }
    }
}

@Composable
private fun AttachmentTypeRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(imageVector = icon, color = IconColor.Base, size = IconSize.Medium)
        Spacer(Modifier.width(InstUILayoutSizes.Spacing.SpaceMd.spaceMd))
        Text(text = label, style = InstUIText.contentImportant, color = InstUISemanticColors.Text.base())
    }
}

private fun isPermissionGranted(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
