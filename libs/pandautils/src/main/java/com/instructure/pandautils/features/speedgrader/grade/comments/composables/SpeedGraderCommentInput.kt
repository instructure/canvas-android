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

package com.instructure.pandautils.features.speedgrader.grade.comments.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.LocalCourseColor


@Composable
fun SpeedGraderCommentInput(
    commentText: String,
    onCommentFieldChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    onCommentLibraryClicked: () -> Unit = {},
    onAttachmentClicked: () -> Unit = {},
    sendCommentClicked: () -> Unit = {},
    isOnCommentLibrary: Boolean = false,
    testTag: String = "speedGraderCommentInputField"
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
                .background(Color.Transparent)
                .testTag(testTag),
            label = { Text(text = stringResource(R.string.speedGraderCommentHint)) },
            value = commentText,
            maxLines = 5,
            onValueChange = onCommentFieldChanged,
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 17.sp,
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
                .wrapContentHeight()
        ) {
            IconButton(
                modifier =  if (!isOnCommentLibrary) Modifier.testTag("commentLibraryButton") else Modifier.testTag("closeCommentLibraryButton"),
                onClick = onCommentLibraryClicked,
                colors = IconButtonDefaults.iconButtonColors(contentColor = colorResource(id = R.color.textDark))
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isOnCommentLibrary) R.drawable.ic_arrow_down else R.drawable.ic_message
                    ),
                    contentDescription = stringResource(
                        if (isOnCommentLibrary) R.string.close else R.string.toolbarCommentLibrary
                    )
                )
            }
            IconButton(
                modifier = Modifier.testTag("commentAttachmentButton"),
                onClick = onAttachmentClicked,
                enabled = !isOnCommentLibrary,
                colors = IconButtonDefaults.iconButtonColors(contentColor = colorResource(id = R.color.textDark))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_attachment),
                    contentDescription = stringResource(R.string.a11y_addAttachment)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                modifier = Modifier.testTag( if(isOnCommentLibrary) "commentLibrarySendCommentButton" else  "sendCommentButton"),
                onClick = sendCommentClicked,
                enabled = commentText.isNotEmpty(),
                colors = IconButtonDefaults.iconButtonColors(contentColor = LocalCourseColor.current)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send_outlined),
                    contentDescription = stringResource(R.string.send)
                )
            }
        }
    }
}

