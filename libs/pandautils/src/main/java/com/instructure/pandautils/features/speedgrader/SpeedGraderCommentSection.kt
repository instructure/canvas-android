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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.features.speedgrader.comments.SpeedGraderComment
import com.instructure.pandautils.features.speedgrader.comments.SpeedGraderCommentAttachment
import com.instructure.pandautils.utils.iconRes


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
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .padding(8.dp),
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = colorResource(id = R.color.textLightest),
            textAlign = TextAlign.Right
        )
        SpeedGraderAttachmentsComponent(
            attachments = comment.attachments,
            gradingAnonymously = gradingAnonymously,
            isOwn = true
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
            modifier = modifier
                .fillMaxWidth(),
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
        ) {
            var itemModifier = Modifier
                .padding(top = 4.dp)
                .height(64.dp)
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.backgroundMedium),
                    shape = RoundedCornerShape(size = 16.dp)
                )
            itemModifier = if (isOwn) {
                itemModifier
                    .fillMaxWidth(0.6f)
            } else {
                itemModifier
                    .fillMaxWidth(0.7f)
            }
            attachments.forEach { attachment ->
                SpeedGraderAttachmentComponent(
                    attachment = attachment,
                    modifier = itemModifier
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
            modifier = Modifier
                .weight(1f)
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SpeedGraderUserCommentItem(
    comment: SpeedGraderComment,
    modifier: Modifier = Modifier,
    gradingAnonymously: Boolean = false
) {
    Row(modifier = modifier) {
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
            SpeedGraderAttachmentsComponent(
                attachments = comment.attachments,
                gradingAnonymously = gradingAnonymously,
                isOwn = false
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

