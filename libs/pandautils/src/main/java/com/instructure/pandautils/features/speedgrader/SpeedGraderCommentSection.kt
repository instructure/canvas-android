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

package com.instructure.pandautils.features.speedgrader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.features.speedgrader.comments.SpeedGraderComment
import com.instructure.pandautils.features.speedgrader.comments.SpeedGraderCommentAttachment


@Composable
fun SpeedGraderCommentSection(
    comments: List<SpeedGraderComment>,
    modifier: Modifier = Modifier,
    gradingAnonymously: Boolean = false
) {
    LazyColumn(
        modifier = modifier
            .background(colorResource(id = R.color.backgroundLightest))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(comments) { comment ->
            if (comment.isOwnComment) {
                SpeedGraderOwnCommentItem(comment = comment)
            } else {
                SpeedGraderUserCommentItem(comment = comment)
            }

        }
    }
}

@Composable
fun SpeedGraderOwnCommentItem(
    comment: SpeedGraderComment,
    modifier: Modifier = Modifier,
    gradingAnonymously: Boolean = false
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = DateHelper.getDateTimeString(
                LocalContext.current,
                DateHelper.speedGraderDateStringToDate(comment.createdAt)
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
        Text(
            text = comment.content,
            modifier = Modifier
                .padding(start = 36.dp)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
                .background(
                    color = colorResource(id = R.color.messageBackground),
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .padding(8.dp),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = colorResource(id = R.color.textLightest),
            textAlign = TextAlign.Right
        )
        SpeedGraderAttachmentComponent(attachments = comment.attachments)
    }
}

@Composable
fun SpeedGraderAttachmentComponent(
    attachments: List<SpeedGraderCommentAttachment>,
    modifier: Modifier = Modifier,
    gradingAnonymously: Boolean = false
) {
    if (attachments.isNotEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.End
        ) {
            attachments.forEach { attachment ->
                Row {
                    // Circle image placeholder
                    GlideImage(
                        model = attachment.contentType,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .background(colorResource(id = R.color.backgroundLight))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = attachment.displayName,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = colorResource(id = R.color.textDarkest),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SpeedGraderUserCommentItem(
    comment: SpeedGraderComment,
    modifier: Modifier = Modifier,
    gradingAnonymously: Boolean = false
) {
    Row(modifier = modifier) {
        // Circle image placeholder
        GlideImage(
            model = comment.authorAvatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(start = 8.dp, top = 8.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.backgroundLight))
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = comment.authorName,
                fontSize = 16.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight(600),
                color = colorResource(id = R.color.textDarkest)
            )
            Text(
                text = DateHelper.getDateTimeString(
                    LocalContext.current,
                    DateHelper.speedGraderDateStringToDate(comment.createdAt)
                ) ?: "",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colorResource(id = R.color.textDark)
            )
            Text(
                text = comment.content,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = colorResource(id = R.color.textDarkest)
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
            createdAt = "Wed May 27 00:23:23 GMT+02:00 2025",
            attachments = listOf(
                SpeedGraderCommentAttachment(
                    displayName = "image_file_name.jpg",
                    contentType = "image/jpeg",
                    size = "1.2 MB"
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
        )
    )
    SpeedGraderCommentSection(comments = comments)
}

