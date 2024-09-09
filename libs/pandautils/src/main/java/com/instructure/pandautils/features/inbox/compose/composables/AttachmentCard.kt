/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.compose.composables

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.inbox.compose.AttachmentCardItem
import com.instructure.pandautils.features.inbox.compose.AttachmentStatus
import com.instructure.pandautils.utils.iconRes

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AttachmentCard(
    attachmentCardItem: AttachmentCardItem,
    context: Context,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    val attachment = attachmentCardItem.attachment
    val status = attachmentCardItem.status

    Card(
        border = BorderStroke(1.dp, colorResource(id = R.color.backgroundMedium)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp)
            .clickable { onSelect() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
            ){
                if (attachment.thumbnailUrl != null) {
                    GlideImage(
                        model = attachment.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(id = attachment.iconRes),
                        contentDescription = null,
                        tint = colorResource(id = R.color.textDark),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
            ){
                Text(
                    attachment.filename ?: "",
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    Formatter.formatFileSize(context, attachment.size),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            when (status) {
                AttachmentStatus.UPLOADING -> {
                    Loading()
                }
                AttachmentStatus.UPLOADED -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_complete),
                        contentDescription = null,
                        tint = colorResource(id = R.color.textDark)
                    )
                }
                AttachmentStatus.FAILED -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_no),
                        contentDescription = null,
                        tint = colorResource(id = R.color.textDark)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { onRemove() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(id = R.string.a11y_removeAttachment),
                    tint = colorResource(id = R.color.textDark),
                )
            }
        }
    }
}

@Composable
@Preview
fun AttachmentCardPreview() {
    val context = LocalContext.current
    AttachmentCard(
        AttachmentCardItem(
            Attachment(
                id = 1,
                contentType = "image/png",
                filename = "image.png",
                displayName = "image.png",
                url = "https://www.example.com/image.png",
                thumbnailUrl = null,
                previewUrl = null,
                size = 1024
            ),
            AttachmentStatus.UPLOADED
        ),
        context,
        {},
        {}
    )
}