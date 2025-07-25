/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.horizonui.molecules.filedrop

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.SpinnerSize
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.utils.toPx

@Composable
fun FileDrop(
    acceptedFileTypes: List<String>,
    modifier: Modifier = Modifier,
    uploadButtonEnabled: Boolean = true,
    fileItems: @Composable ColumnScope.() -> Unit = {},
    onUploadClick: () -> Unit = {}
) {
    Column {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .background(color = HorizonColors.Surface.cardPrimary(), shape = HorizonCornerRadius.level3)
                .drawBehind {
                    val stroke = Stroke(
                        width = 1.toPx.toFloat(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                    drawRoundRect(
                        color = HorizonColors.LineAndBorder.lineStroke(),
                        style = stroke,
                        cornerRadius = CornerRadius(16.toPx.toFloat(), 16.toPx.toFloat())
                    )
                }
                .padding(top = 56.dp, start = 48.dp, end = 48.dp, bottom = 48.dp)
        ) {
            Button(
                label = stringResource(R.string.fileDrop_uploadFile),
                height = ButtonHeight.NORMAL,
                width = ButtonWidth.RELATIVE,
                color = ButtonColor.Institution,
                onClick = onUploadClick,
                enabled = uploadButtonEnabled,
            )
            HorizonSpace(SpaceSize.SPACE_16)
            if (acceptedFileTypes.isNotEmpty()) {
                val acceptedFileTypesText = acceptedFileTypes.joinToString(", ")
                Text(
                    text = stringResource(R.string.fileDrop_acceptFileTypes, acceptedFileTypesText),
                    style = HorizonTypography.p2,
                    textAlign = TextAlign.Center
                )
            }
        }
        fileItems()
    }
}

sealed class FileDropItemState(
    open val fileName: String,
    open val actionIconRes: Int,
    open val onActionClick: (() -> Unit)? = null,
    open val onClick: (() -> Unit)? = null
) {
    data class Success(
        override val fileName: String,
        override val onActionClick: (() -> Unit)? = null,
        override val onClick: (() -> Unit)? = null,
        override val actionIconRes: Int = R.drawable.delete
    ) :
        FileDropItemState(fileName, actionIconRes)

    data class InProgress(
        override val fileName: String,
        val progress: Float? = null,
        override val onActionClick: (() -> Unit)? = null,
        override val onClick: (() -> Unit)? = null,
        override val actionIconRes: Int = R.drawable.close
    ) :
        FileDropItemState(fileName, actionIconRes)

    data class NoLongerEditable(
        override val fileName: String,
        override val onActionClick: (() -> Unit)? = null,
        override val onClick: (() -> Unit)? = null,
        override val actionIconRes: Int = R.drawable.download
    ) :
        FileDropItemState(fileName, actionIconRes)

    data class Error(
        override val fileName: String,
        override val onActionClick: (() -> Unit)? = null,
        override val onClick: (() -> Unit)? = null,
        override val actionIconRes: Int = R.drawable.refresh
    ) :
        FileDropItemState(fileName, actionIconRes)

}

@Composable
fun FileDropItem(
    state: FileDropItemState,
    modifier: Modifier = Modifier,
    hasBorder: Boolean = true,
    borderColor: Color = HorizonColors.LineAndBorder.lineStroke()
) {
    Column(modifier = modifier) {
        HorizonSpace(SpaceSize.SPACE_8)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 64.dp)
                .background(color = HorizonColors.Surface.pageSecondary(), shape = HorizonCornerRadius.level3)
                .conditional(hasBorder) { border(HorizonBorder.level1(borderColor), shape = HorizonCornerRadius.level3) }
                .clip(HorizonCornerRadius.level3)
                .conditional(state.onClick != null) {
                    clickable { state.onClick?.invoke() }
                }
                .padding(16.dp)
        ) {
            AnimatedContent(state) { targetState ->
                when (targetState) {
                    is FileDropItemState.InProgress -> {
                        Spinner(
                            size = SpinnerSize.EXTRA_SMALL,
                            hasStrokeBackground = true,
                            progress = targetState.progress,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    is FileDropItemState.Success -> {
                        Badge(
                            type = BadgeType.Success,
                            content = BadgeContent.Icon(R.drawable.check, null),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    is FileDropItemState.Error -> {
                        Icon(
                            painter = painterResource(R.drawable.error),
                            tint = HorizonColors.Text.error(),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(24.dp)
                        )
                        HorizonSpace(SpaceSize.SPACE_8)
                    }

                    else -> {}
                }
            }
            Text(text = state.fileName, style = HorizonTypography.p1, modifier = Modifier.weight(1f))
            HorizonSpace(SpaceSize.SPACE_8)
            state.onActionClick?.let {
                IconButton(
                    iconRes = state.actionIconRes,
                    size = IconButtonSize.SMALL,
                    color = IconButtonColor.Inverse,
                    onClick = it
                )
            }
        }
    }
}

@Composable
@Preview
fun FileDropPreview() {
    ContextKeeper.appContext = LocalContext.current
    FileDrop(listOf("pdf", "jpg"), fileItems = {
        FileDropItem(state = FileDropItemState.InProgress("In progress file", onActionClick = {}, onClick = {}))
        FileDropItem(state = FileDropItemState.Success("Success file", onActionClick = {}, onClick = {}))
        FileDropItem(state = FileDropItemState.NoLongerEditable("No longer editable file", onActionClick = {}, onClick = {}))
        FileDropItem(state = FileDropItemState.Error("Error text", onActionClick = {}, onClick = {}))
    })
}